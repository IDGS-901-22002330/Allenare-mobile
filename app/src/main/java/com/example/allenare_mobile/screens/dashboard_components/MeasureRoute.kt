package com.example.allenare_mobile.screens.dashboard_components

import android.Manifest
import android.annotation.SuppressLint
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.rememberPermissionState
import com.google.accompanist.permissions.isGranted
import com.google.android.gms.maps.model.*
import com.google.maps.android.compose.*
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.launch

@OptIn(ExperimentalPermissionsApi::class)
@SuppressLint("MissingPermission")
@Composable
fun MeasureRoute() {
    val context = LocalContext.current
    val locationPermission = rememberPermissionState(Manifest.permission.ACCESS_FINE_LOCATION)

    var routePoints by remember { mutableStateOf<List<LatLng>>(emptyList()) }
    var distance by remember { mutableStateOf("") }
    var duration by remember { mutableStateOf("") }

    val firestore = FirebaseFirestore.getInstance()
    val coroutineScope = rememberCoroutineScope()

    // Obtener la última ruta guardada
    LaunchedEffect(Unit) {
        if (locationPermission.status.isGranted) {
            firestore.collection("routes")
                .orderBy("timestamp", com.google.firebase.firestore.Query.Direction.DESCENDING)
                .limit(1)
                .get()
                .addOnSuccessListener { result ->
                    if (!result.isEmpty) {
                        val doc = result.documents[0]
                        val points = doc["points"] as? List<Map<String, Double>>
                        val latLngList = points?.mapNotNull { p ->
                            val lat = p["lat"]
                            val lng = p["lng"]
                            if (lat != null && lng != null) LatLng(lat, lng) else null
                        } ?: emptyList()
                        routePoints = latLngList
                        distance = doc["distance"]?.toString() ?: ""
                        duration = doc["duration"]?.toString() ?: ""
                    }
                }
        } else {
            locationPermission.launchPermissionRequest()
        }
    }

    val cameraPositionState = rememberCameraPositionState {
        position = CameraPosition.fromLatLngZoom(
            routePoints.firstOrNull() ?: LatLng(0.0, 0.0),
            14f
        )
    }

    Column(Modifier.fillMaxWidth().padding(16.dp)) {
        Text(
            "Última ruta registrada",
            style = MaterialTheme.typography.titleLarge
        )
        Spacer(Modifier.height(8.dp))

        if (routePoints.isNotEmpty()) {
            GoogleMap(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(400.dp),
                cameraPositionState = cameraPositionState,
                properties = MapProperties(isMyLocationEnabled = locationPermission.status.isGranted)
            ) {
                // Marcadores de inicio y fin
                Marker(
                    state = MarkerState(routePoints.first()),
                    title = "Inicio",
                    icon = BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_GREEN)
                )
                Marker(
                    state = MarkerState(routePoints.last()),
                    title = "Fin",
                    icon = BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_RED)
                )

                // Línea de la ruta
                Polyline(
                    points = routePoints,
                    color = MaterialTheme.colorScheme.primary,
                    width = 8f
                )
            }

            Spacer(Modifier.height(12.dp))

            Text("Distancia: $distance", style = MaterialTheme.typography.bodyLarge)
            Text("Duración: $duration", style = MaterialTheme.typography.bodyLarge)
        } else {
            Text("Cargando la última ruta guardada...", Modifier.padding(8.dp))
        }
    }
}