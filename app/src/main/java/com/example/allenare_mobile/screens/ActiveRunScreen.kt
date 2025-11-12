package com.example.allenare_mobile.screens


import android.Manifest
import android.annotation.SuppressLint
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.isGranted
import com.google.accompanist.permissions.rememberPermissionState
import com.google.android.gms.location.LocationServices
import com.google.android.gms.maps.model.BitmapDescriptorFactory
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.LatLng
import com.google.firebase.firestore.FirebaseFirestore
import com.google.maps.android.SphericalUtil
import com.google.maps.android.compose.*
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

@OptIn(ExperimentalPermissionsApi::class)
@SuppressLint("MissingPermission")
@Composable
fun ActiveRunScreen(navController: NavController, runId: String) {
    val locationPermission = rememberPermissionState(Manifest.permission.ACCESS_FINE_LOCATION)
    val context = LocalContext.current
    val firestore = FirebaseFirestore.getInstance()
    val fusedLocationClient = LocationServices.getFusedLocationProviderClient(context)
    val coroutineScope = rememberCoroutineScope()

    var runData by remember { mutableStateOf<RunData?>(null) }
    var message by remember { mutableStateOf("Iniciando ruta...") }

    // Datos de la ruta elegida
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
        position = CameraPosition.fromLatLngZoom(runData!!.routePoints.first(), 15f)
    }

    // Checar progreso
    LaunchedEffect(runData) {
        val totalTime = runData!!.duration + 600 // +10 minutos
        val endTime = System.currentTimeMillis() + (totalTime * 1000)

        while (System.currentTimeMillis() < endTime) {
            val location = fusedLocationClient.lastLocation.await()
            location?.let {
                val userLatLng = LatLng(it.latitude, it.longitude)
                val distanceToEnd = SphericalUtil.computeDistanceBetween(
                    userLatLng,
                    runData!!.routePoints.last()
                )
                if (distanceToEnd <= 500) {
                    firestore.collection("running_workouts")
                        .document(runId)
                        .update("status", 1)
                    message = "¡Felicidades ruta completada con éxito!"
                    delay(3000)
                    navController.navigate("all_runs")
                    return@LaunchedEffect
                }
            }
            delay(15000) // Revisar cada 15 segundos
        }

        message = "⚠️ Ruta incompleta, buena suerte la próxima."
        delay(3000)
        navController.navigate("all_runs")
    }

    // Diseño
    Column(modifier = Modifier.fillMaxSize()) {
        GoogleMap(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f),
            cameraPositionState = cameraPositionState
        ) {
            Marker(
                state = MarkerState(runData!!.routePoints.first()),
                title = "Inicio",
                icon = BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_GREEN)
            )
            Marker(
                state = MarkerState(runData!!.routePoints.last()),
                title = "Fin",
                icon = BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_RED)
            )
            Polyline(points = runData!!.routePoints, width = 8f)
        }

        Text(
            text = message,
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            style = MaterialTheme.typography.titleMedium
        )

        Button(
            onClick = { navController.navigate("all_runs") },
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Text("Cancelar ruta")
        }
    }
}
