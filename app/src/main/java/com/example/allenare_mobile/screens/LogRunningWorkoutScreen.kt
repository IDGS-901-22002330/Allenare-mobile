package com.example.allenare_mobile.screens

import android.Manifest
import android.annotation.SuppressLint
import android.location.Location
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.example.allenare_mobile.model.RunningWorkout
import com.google.android.gms.location.LocationServices
import com.google.android.gms.maps.model.*
import com.google.maps.android.compose.*
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import kotlin.math.*

@OptIn(ExperimentalMaterial3Api::class)
@SuppressLint("MissingPermission")
@Composable
fun LogRunningWorkoutScreen(navController: NavController, onWorkoutLogged: () -> Unit) {
    val context = LocalContext.current
    val db = Firebase.firestore
    val user = Firebase.auth.currentUser

    var routePoints by remember { mutableStateOf(listOf<LatLng>()) }
    var distanceKm by remember { mutableStateOf(0.0) }
    var durationSeconds by remember { mutableStateOf(0L) }
    var name by remember { mutableStateOf("") }

    var locationPermissionGranted by remember { mutableStateOf(false) }

    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { granted -> locationPermissionGranted = granted }

    LaunchedEffect(Unit) {
        permissionLauncher.launch(Manifest.permission.ACCESS_FINE_LOCATION)
    }

    val cameraPositionState = rememberCameraPositionState()

    // Centrar cámara en ubicación actual si hay permiso
    LaunchedEffect(locationPermissionGranted) {
        if (locationPermissionGranted) {
            LocationServices.getFusedLocationProviderClient(context)
                .lastLocation
                .addOnSuccessListener { location: Location? ->
                    location?.let {
                        val userLocation = LatLng(it.latitude, it.longitude)
                        cameraPositionState.position =
                            CameraPosition.fromLatLngZoom(userLocation, 15f)
                    }
                }
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Crear nueva ruta") },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(
                            imageVector = Icons.Default.ArrowBack,
                            contentDescription = "Volver",
                            tint = MaterialTheme.colorScheme.primary
                        )
                    }
                }
            )
        },
        floatingActionButton = {
            Button(
                onClick = {
                    if (user == null) {
                        Toast.makeText(context, "Debes iniciar sesión.", Toast.LENGTH_SHORT).show()
                        return@Button
                    }

                    if (!isValidName(name)) {
                        Toast.makeText(
                            context,
                            "Nombre inválido (3-40 caracteres sin caracteres especiales).",
                            Toast.LENGTH_SHORT
                        ).show()
                        return@Button
                    }

                    if (routePoints.size < 2) {
                        Toast.makeText(
                            context,
                            "Agrega al menos dos puntos para crear una ruta.",
                            Toast.LENGTH_SHORT
                        ).show()
                        return@Button
                    }

                    if (routePoints.size > 200) {
                        Toast.makeText(
                            context,
                            "Demasiados puntos en la ruta (máx 200).",
                            Toast.LENGTH_SHORT
                        ).show()
                        return@Button
                    }

                    val cleanedRoute = routePoints.map {
                        mapOf(
                            "lat" to String.format("%.6f", it.latitude).toDouble(),
                            "lng" to String.format("%.6f", it.longitude).toDouble()
                        )
                    }

                    val safeDistance = if (distanceKm.isNaN() || distanceKm < 0) 0.0 else distanceKm
                    val safeDuration = durationSeconds.coerceIn(60L, 5 * 3600L)

                    val workout = RunningWorkout(
                        userId = user.uid,
                        name = name.trim(),
                        distance = safeDistance,
                        duration = safeDuration,
                        route = cleanedRoute,
                        estatus = 0
                    )

                    db.collection("running_workouts")
                        .add(workout)
                        .addOnSuccessListener {
                            Toast.makeText(context, "Ruta guardada correctamente", Toast.LENGTH_SHORT).show()
                            onWorkoutLogged()
                        }
                        .addOnFailureListener {
                            Toast.makeText(context, "Error al guardar la ruta", Toast.LENGTH_SHORT).show()
                        }
                },
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.primary
                )
            ) {
                Text("Guardar ruta", color = Color.White)
            }
        }
    ) { paddingValues ->

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {

            Text(
                text = "Dale un nombre a tu ruta",
                style = MaterialTheme.typography.titleMedium,
                modifier = Modifier.padding(start = 16.dp, top = 8.dp)
            )

            OutlinedTextField(
                value = name,
                onValueChange = { if (it.length <= 40) name = it },
                label = { Text("Nombre de la ruta") },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp)
            )

            if (!locationPermissionGranted) {
                Text(
                    "Activa el permiso de ubicación para usar el mapa.",
                    color = Color.Red,
                    modifier = Modifier.padding(16.dp)
                )
            }

            GoogleMap(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(300.dp),
                cameraPositionState = cameraPositionState,
                properties = MapProperties(isMyLocationEnabled = locationPermissionGranted),
                uiSettings = MapUiSettings(zoomControlsEnabled = true, myLocationButtonEnabled = true),
                onMapClick = { latLng ->
                    if (routePoints.size < 200) {
                        routePoints = routePoints + latLng
                        if (routePoints.size > 1) {
                            distanceKm = calculateTotalDistance(routePoints)
                            durationSeconds = ((distanceKm / 8.0) * 3600).toLong()
                        }
                    } else {
                        Toast.makeText(context, "Límite máximo de 200 puntos alcanzado.", Toast.LENGTH_SHORT).show()
                    }
                }
            ) {
                Polyline(
                    points = routePoints,
                    color = Color.Blue,
                    width = 8f
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                IconButton(
                    onClick = {
                        if (routePoints.isNotEmpty()) {
                            routePoints = routePoints.dropLast(1)
                            distanceKm = calculateTotalDistance(routePoints)
                            durationSeconds = ((distanceKm / 8.0) * 3600).toLong()
                        }
                    },
                    enabled = routePoints.isNotEmpty()
                ) {
                    Icon(Icons.Default.ArrowBack, contentDescription = "Eliminar último punto", tint = Color(0xFF1976D2))
                }

                Button(
                    onClick = {
                        routePoints = emptyList()
                        distanceKm = 0.0
                        durationSeconds = 0L
                        Toast.makeText(context, "Ruta reiniciada", Toast.LENGTH_SHORT).show()
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Color.Red)
                ) {
                    Text("Reiniciar", color = Color.White)
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text("Distancia: ${"%.2f".format(distanceKm)} km", style = MaterialTheme.typography.bodyLarge)
                Text("Duración estimada: ${durationSeconds / 60} min", style = MaterialTheme.typography.bodyMedium)
                Text(
                    text = "Toca el mapa para marcar tu ruta.",
                    color = Color.Gray,
                    style = MaterialTheme.typography.bodySmall
                )
            }
        }
    }
}

// Validaciones

fun isValidName(name: String): Boolean {
    val regex = Regex("^[A-Za-zÁÉÍÓÚáéíóúñÑ0-9 ]{3,40}$")
    return regex.matches(name.trim())
}

private fun calculateTotalDistance(points: List<LatLng>): Double {
    if (points.size < 2) return 0.0
    var total = 0.0
    for (i in 0 until points.size - 1) {
        total += haversine(points[i], points[i + 1])
    }
    return total
}

private fun haversine(p1: LatLng, p2: LatLng): Double {
    val R = 6371.0
    val dLat = Math.toRadians(p2.latitude - p1.latitude)
    val dLon = Math.toRadians(p2.longitude - p1.longitude)
    val lat1 = Math.toRadians(p1.latitude)
    val lat2 = Math.toRadians(p2.latitude)

    val a = sin(dLat / 2).pow(2.0) + cos(lat1) * cos(lat2) * sin(dLon / 2).pow(2.0)
    return 2 * R * atan2(sqrt(a), sqrt(1 - a))
}
