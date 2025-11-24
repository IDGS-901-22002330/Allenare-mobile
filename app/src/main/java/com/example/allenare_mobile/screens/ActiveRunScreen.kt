package com.example.allenare_mobile.screens

import android.Manifest
import android.annotation.SuppressLint
import android.os.Looper
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.example.allenare_mobile.model.ExercisePerformed
import com.example.allenare_mobile.model.RunData
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.isGranted
import com.google.accompanist.permissions.rememberPermissionState
import com.google.android.gms.location.*
import com.google.android.gms.maps.model.BitmapDescriptorFactory
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.LatLng
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.maps.android.SphericalUtil
import com.google.maps.android.compose.*
import kotlinx.coroutines.delay
import kotlinx.coroutines.tasks.await

@OptIn(ExperimentalPermissionsApi::class)
@SuppressLint("MissingPermission")
@Composable
fun ActiveRunScreen(navController: NavController, runId: String) {
    val locationPermission = rememberPermissionState(Manifest.permission.ACCESS_FINE_LOCATION)
    val context = LocalContext.current
    val firestore = FirebaseFirestore.getInstance()
    val fusedLocationClient = LocationServices.getFusedLocationProviderClient(context)
    val currentUser = FirebaseAuth.getInstance().currentUser

    var runData by remember { mutableStateOf<RunData?>(null) }
    var message by remember { mutableStateOf("Iniciando ruta...") }

    var distanceCovered by remember { mutableStateOf(0.0) }
    var elapsedTime by remember { mutableStateOf(0L) }
    var isPaused by remember { mutableStateOf(false) }
    var userLocation by remember { mutableStateOf<LatLng?>(null) }

    LaunchedEffect(runId) {
        val doc = firestore.collection("running_workouts").document(runId).get().await()
        val name = doc.getString("name") ?: "Sin nombre"
        val dist = (doc.get("distance") as? Number)?.toDouble() ?: 0.0
        val dur = (doc.get("duration") as? Number)?.toLong() ?: 0L
        val points = doc.get("route") as? List<Map<String, Double>>
        val latLngList = points?.mapNotNull { p ->
            val lat = p["lat"]
            val lng = p["lng"]
            if (lat != null && lng != null) LatLng(lat, lng) else null
        } ?: emptyList()
        runData = RunData(runId, name, dist, dur, latLngList)
    }

    if (runData == null) {
        Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            CircularProgressIndicator()
        }
        return
    }

    val cameraPositionState = rememberCameraPositionState {
        position = CameraPosition.fromLatLngZoom(runData!!.routePoints.first(), 17f)
    }

    DisposableEffect(runData, isPaused) {
        val locationRequest = LocationRequest.Builder(Priority.PRIORITY_HIGH_ACCURACY, 1000).build()
        var lastLocation: LatLng? = null

        val locationCallback = object : LocationCallback() {
            override fun onLocationResult(locationResult: LocationResult) {
                if (!isPaused) {
                    locationResult.lastLocation?.let { location ->
                        val currentLatLng = LatLng(location.latitude, location.longitude)
                        userLocation = currentLatLng

                        if (lastLocation != null) {
                            distanceCovered += SphericalUtil.computeDistanceBetween(lastLocation, currentLatLng)
                        }
                        lastLocation = currentLatLng
                    }
                }
            }
        }

        fusedLocationClient.requestLocationUpdates(locationRequest, locationCallback, Looper.getMainLooper())

        onDispose {
            fusedLocationClient.removeLocationUpdates(locationCallback)
        }
    }


    LaunchedEffect(elapsedTime, isPaused) {
        while (true) {
            delay(1000)
            if (!isPaused) {
                elapsedTime++
            }
        }
    }

    LaunchedEffect(userLocation) {
        userLocation?.let {
            val distanceToEnd = SphericalUtil.computeDistanceBetween(
                it,
                runData!!.routePoints.last()
            )
            if (distanceToEnd <= 50) {
                message = "¡Felicidades, ruta completada con éxito!"

                if (currentUser != null) {
                    val record = ExercisePerformed(
                        userId = currentUser.uid,
                        name = runData!!.name,
                        cantidad = distanceCovered,
                        tiempoSegundos = elapsedTime
                    )
                    firestore.collection("completed_workouts").add(record)
                }

                firestore.collection("running_workouts")
                    .document(runId)
                    .update("estatus", 1)

                delay(3000)
                navController.navigate("all_runs")
            }
        }
    }


    Box(modifier = Modifier.fillMaxSize()) {
        GoogleMap(
            modifier = Modifier.fillMaxSize(),
            cameraPositionState = cameraPositionState,
            properties = MapProperties(isMyLocationEnabled = locationPermission.status.isGranted)
        ) {
            runData?.let {
                if (it.routePoints.isNotEmpty()) {
                    Marker(
                        state = MarkerState(it.routePoints.first()),
                        title = "Inicio",
                        icon = BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_GREEN)
                    )
                    Marker(
                        state = MarkerState(it.routePoints.last()),
                        title = "Fin",
                        icon = BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_RED)
                    )
                    Polyline(points = it.routePoints, width = 10f, color = MaterialTheme.colorScheme.primary)
                }
            }
            userLocation?.let {
                Marker(
                    state = MarkerState(position = it),
                    title = "Mi Ubicación",
                    icon = BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_AZURE)
                )
            }
        }

        // HUD Overlay
        Card(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .fillMaxWidth()
                .padding(16.dp),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.95f)
            ),
            elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
        ) {
            Column(
                modifier = Modifier.padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                if (message != "Iniciando ruta...") {
                    Text(
                        text = message,
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.padding(bottom = 8.dp)
                    )
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceEvenly
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            text = "TIEMPO",
                            style = MaterialTheme.typography.labelMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Text(
                            text = String.format("%02d:%02d", elapsedTime / 60, elapsedTime % 60),
                            style = MaterialTheme.typography.displayMedium,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }

                    Spacer(modifier = Modifier.width(16.dp))

                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            text = "DISTANCIA",
                            style = MaterialTheme.typography.labelMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Text(
                            text = "${(distanceCovered / 1000).format(2)}",
                            style = MaterialTheme.typography.displayMedium,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = "km",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceEvenly
                ) {
                    Button(
                        onClick = { isPaused = !isPaused },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = if (isPaused) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.secondary
                        ),
                        modifier = Modifier.weight(1f).padding(end = 8.dp)
                    ) {
                        Text(if (isPaused) "REANUDAR" else "PAUSAR")
                    }

                    OutlinedButton(
                        onClick = { navController.navigate("all_runs") },
                        modifier = Modifier.weight(1f).padding(start = 8.dp)
                    ) {
                        Text("CANCELAR")
                    }
                }
            }
        }
    }
}

fun Double.format(decimals: Int): String = "%.${decimals}f".format(this)