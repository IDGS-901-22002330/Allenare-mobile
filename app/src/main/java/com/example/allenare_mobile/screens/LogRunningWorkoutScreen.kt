package com.example.allenare_mobile.screens

import android.widget.Toast
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.example.allenare_mobile.model.RunningWorkout
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import android.Manifest
import android.annotation.SuppressLint
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.material3.*
import androidx.compose.runtime.*
import com.google.android.gms.maps.model.LatLng
import com.google.maps.android.compose.*
import kotlin.math.*

@SuppressLint("MissingPermission")
@Composable
fun LogRunningWorkoutScreen(onWorkoutLogged: () -> Unit) {
    val context = LocalContext.current
    val db = Firebase.firestore
    val user = Firebase.auth.currentUser

    var routePoints by remember { mutableStateOf(listOf<LatLng>()) }
    var distanceKm by remember { mutableStateOf(0.0) }
    var durationSeconds by remember { mutableStateOf(0L) }

    var locationPermissionGranted by remember { mutableStateOf(false) }
    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { granted -> locationPermissionGranted = granted }

    LaunchedEffect(Unit) {
        permissionLauncher.launch(Manifest.permission.ACCESS_FINE_LOCATION)
    }

    val cameraPositionState = rememberCameraPositionState {
        position = CameraPositionState().position
    }

    Scaffold(
        floatingActionButton = {
            Button(
                onClick = {
                    if (routePoints.isNotEmpty()) {
                        val routeMap = routePoints.map { mapOf("lat" to it.latitude, "lng" to it.longitude) }
                        val workout = RunningWorkout(
                            userId = user?.uid ?: "",
                            distance = distanceKm,
                            duration = durationSeconds,
                            route = routeMap
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
                    } else {
                        Toast.makeText(context, "No hay ruta para guardar", Toast.LENGTH_SHORT).show()
                    }
                },
                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
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
            GoogleMap(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
                cameraPositionState = cameraPositionState,
                properties = MapProperties(isMyLocationEnabled = locationPermissionGranted),
                uiSettings = MapUiSettings(zoomControlsEnabled = true, myLocationButtonEnabled = true),
                onMapClick = { latLng ->
                    routePoints = routePoints + latLng
                    if (routePoints.size > 1) {
                        distanceKm = calculateTotalDistance(routePoints)
                        durationSeconds = ((distanceKm / 8.0) * 3600).toLong() // aprox. a 8 km/h
                    }
                }
            ) {
                Polyline(
                    points = routePoints,
                    color = Color.Blue,
                    width = 8f
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

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

// Calcula la distancia total entre los puntos
private fun calculateTotalDistance(points: List<LatLng>): Double {
    if (points.size < 2) return 0.0
    var total = 0.0
    for (i in 0 until points.size - 1) {
        total += haversine(points[i], points[i + 1])
    }
    return total
}

// Fórmula para distancia entre coordenadas
private fun haversine(p1: LatLng, p2: LatLng): Double {
    val R = 6371.0 // radio Tierra en km
    val lat1 = Math.toRadians(p1.latitude)
    val lat2 = Math.toRadians(p2.latitude)
    val dLat = Math.toRadians(p2.latitude - p1.latitude)
    val dLon = Math.toRadians(p2.longitude - p1.longitude)
    val a = sin(dLat / 2).pow(2.0) + cos(lat1) * cos(lat2) * sin(dLon / 2).pow(2.0)
    return 2 * R * atan2(sqrt(a), sqrt(1 - a))
}