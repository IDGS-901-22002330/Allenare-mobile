package com.example.allenare_mobile.screens.dashboard_components

import android.Manifest
import android.annotation.SuppressLint
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.rememberPermissionState
import com.google.accompanist.permissions.isGranted
import com.google.android.gms.maps.model.*
import com.google.maps.android.compose.*
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.ktx.Firebase

@OptIn(ExperimentalPermissionsApi::class)
@SuppressLint("MissingPermission")
@Composable
fun MeasureRoute() {
    val context = LocalContext.current
    val locationPermission = rememberPermissionState(Manifest.permission.ACCESS_FINE_LOCATION)
    val firestore = FirebaseFirestore.getInstance()
    val user = Firebase.auth.currentUser

    var routePoints by remember { mutableStateOf<List<LatLng>>(emptyList()) }
    var distance by remember { mutableStateOf("") }
    var duration by remember { mutableStateOf("") }
    var routeName by remember { mutableStateOf("") }
    var isLoading by remember { mutableStateOf(true) }
    var hasRoutes by remember { mutableStateOf(true) }

    val cameraPositionState = rememberCameraPositionState {
        position = CameraPosition.fromLatLngZoom(LatLng(19.4326, -99.1332), 12f) // CDMX por defecto
    }

    LaunchedEffect(Unit) {
        if (locationPermission.status.isGranted) {
            // Ruta completada más reciente
            firestore.collection("running_workouts")
                .whereEqualTo("userId", user?.uid)
                .whereEqualTo("estatus", 1)
                .orderBy("date", com.google.firebase.firestore.Query.Direction.DESCENDING)
                .limit(1)
                .get()
                .addOnSuccessListener { completedResult ->
                    if (!completedResult.isEmpty) {
                        val doc = completedResult.documents[0]
                        loadRouteFromDocument(doc, cameraPositionState) { n, d, t, points ->
                            routeName = n
                            distance = d
                            duration = t
                            routePoints = points
                            isLoading = false
                        }
                    } else {
                        // Rutas completadas, buscar la más reciente creada
                        firestore.collection("running_workouts")
                            .whereEqualTo("userId", user?.uid)
                            .orderBy("date", com.google.firebase.firestore.Query.Direction.DESCENDING)
                            .limit(1)
                            .get()
                            .addOnSuccessListener { createdResult ->
                                if (!createdResult.isEmpty) {
                                    val doc = createdResult.documents[0]
                                    loadRouteFromDocument(doc, cameraPositionState) { n, d, t, points ->
                                        routeName = n
                                        distance = d
                                        duration = t
                                        routePoints = points
                                        isLoading = false
                                    }
                                } else {
                                    // No tiene rutas
                                    hasRoutes = false
                                    isLoading = false
                                }
                            }
                    }
                }
        } else {
            locationPermission.launchPermissionRequest()
        }
    }

    if (isLoading) {
        Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            CircularProgressIndicator()
        }
    } else if (!hasRoutes) {
        // No hay rutas creadas
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(300.dp)
                .padding(16.dp),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = "Aún no tienes rutas registradas.\nInicia tu entrenamiento y crea tu primera ruta 🏃‍♂️",
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.primary,
                textAlign = androidx.compose.ui.text.style.TextAlign.Center
            )
        }
    } else {
        // Ruta encontrada
        Column(
            Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Text(routeName, style = MaterialTheme.typography.titleLarge)
            Spacer(Modifier.height(8.dp))

            GoogleMap(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(300.dp),
                cameraPositionState = cameraPositionState,
                properties = MapProperties(isMyLocationEnabled = locationPermission.status.isGranted)
            ) {
                if (routePoints.isNotEmpty()) {
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
                    Polyline(points = routePoints, color = MaterialTheme.colorScheme.primary, width = 8f)
                }
            }

            Spacer(Modifier.height(12.dp))
            Text("Distancia: $distance", style = MaterialTheme.typography.bodyLarge)
            Text("Duración: $duration", style = MaterialTheme.typography.bodyLarge)
        }
    }
}

// cargar datos y convertirlos
private fun loadRouteFromDocument(
    doc: com.google.firebase.firestore.DocumentSnapshot,
    cameraPositionState: CameraPositionState,
    onResult: (String, String, String, List<LatLng>) -> Unit
) {
    val points = doc["route"] as? List<Map<String, Double>>
    val latLngList = points?.mapNotNull { p ->
        val lat = p["lat"]
        val lng = p["lng"]
        if (lat != null && lng != null) LatLng(lat, lng) else null
    } ?: emptyList()

    val name = doc["name"]?.toString() ?: "Ruta sin nombre"
    val rawDistance = (doc["distance"] as? Number)?.toDouble() ?: 0.0
    val formattedDistance = String.format("%.2f km", rawDistance)
    val durSeconds = (doc["duration"] as? Long) ?: 0L
    val formattedDuration = "${durSeconds / 60} min"

    if (latLngList.isNotEmpty()) {
        val firstPoint = latLngList.first()
        cameraPositionState.position = CameraPosition.fromLatLngZoom(firstPoint, 15f)
    }

    onResult(name, formattedDistance, formattedDuration, latLngList)
}
