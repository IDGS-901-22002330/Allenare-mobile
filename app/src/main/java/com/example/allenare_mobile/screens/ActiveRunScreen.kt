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
import com.example.allenare_mobile.model.ExercisePerformed
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.isGranted
import com.google.accompanist.permissions.rememberPermissionState
import com.google.android.gms.location.LocationServices
import com.google.android.gms.maps.model.BitmapDescriptorFactory
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.LatLng
import com.google.firebase.auth.FirebaseAuth
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
    val currentUser = FirebaseAuth.getInstance().currentUser

    var runData by remember { mutableStateOf<RunData?>(null) }
    var message by remember { mutableStateOf("Iniciando ruta...") }

    var distanceCovered by remember { mutableStateOf(0.0) } // Distancia recorrida (m)
    var elapsedTime by remember { mutableStateOf(0L) } // Tiempo (s)
    var isPaused by remember { mutableStateOf(false) }

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

    // Contadores
    LaunchedEffect(runData, isPaused) {
        var lastLocation: LatLng? = null
        val totalTime = runData!!.duration + 600 // +10 minutos extra
        val endTime = System.currentTimeMillis() + (totalTime * 1000)

        while (System.currentTimeMillis() < endTime) {
            if (!isPaused) {
                val location = fusedLocationClient.lastLocation.await()
                location?.let {
                    val userLatLng = LatLng(it.latitude, it.longitude)
                    if (lastLocation != null) {
                        val segment = SphericalUtil.computeDistanceBetween(lastLocation, userLatLng)
                        distanceCovered += segment
                    }
                    lastLocation = userLatLng

                    // Verificar si llegó al final
                    val distanceToEnd = SphericalUtil.computeDistanceBetween(
                        userLatLng,
                        runData!!.routePoints.last()
                    )
                    if (distanceToEnd <= 50) {
                        message = "¡Felicidades, ruta completada con éxito!"

                        // Guardar registro
                        if (currentUser != null) {
                            val record = ExercisePerformed(
                                userId = currentUser.uid,
                                name = runData!!.name,
                                cantidad = runData!!.distance,
                                tiempoSegundos = elapsedTime
                            )
                            firestore.collection("completed_workouts").add(record)
                        }

                        // Actualizar estado de la ruta
                        firestore.collection("running_workouts")
                            .document(runId)
                            .update("estatus", 1)

                        delay(3000)
                        navController.navigate("all_runs")
                        return@LaunchedEffect
                    }
                }

                elapsedTime += 1
            }

            delay(1000) // Cada segundo
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

        // Estadísticas en tiempo real
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text("Tiempo: ${elapsedTime / 60} min ${elapsedTime % 60} s", style = MaterialTheme.typography.titleMedium)
            Text("Distancia: ${(distanceCovered / 1000).format(2)} km", style = MaterialTheme.typography.titleMedium)
            Text(message, style = MaterialTheme.typography.bodyMedium)

            Spacer(Modifier.height(12.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                Button(onClick = {
                    isPaused = !isPaused
                }) {
                    Text(if (isPaused) "Continuar" else "Pausar")
                }

                Button(onClick = {
                    navController.navigate("all_runs")
                }) {
                    Text("Cancelar ruta")
                }
            }
        }
    }
}

// Función formatear decimales
fun Double.format(decimals: Int): String = "%.${decimals}f".format(this)
