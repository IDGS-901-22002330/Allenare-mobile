package com.example.allenare_mobile.screens

import android.Manifest
import android.annotation.SuppressLint
import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.EmojiEvents
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.isGranted
import com.google.accompanist.permissions.rememberPermissionState
import com.google.android.gms.location.LocationServices
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.BitmapDescriptorFactory
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.maps.android.SphericalUtil
import com.google.maps.android.compose.*
import kotlinx.coroutines.tasks.await

data class RunData(
    val id: String,
    val name: String,
    val distance: Double,
    val duration: Long,
    val routePoints: List<LatLng>
)

@OptIn(ExperimentalPermissionsApi::class, ExperimentalMaterial3Api::class)
@SuppressLint("MissingPermission")
@Composable
fun AllRunsScreen(navController: NavController, onWorkoutLogged: () -> Unit) {

    val locationPermission = rememberPermissionState(Manifest.permission.ACCESS_FINE_LOCATION)
    val firestore = FirebaseFirestore.getInstance()
    val currentUser = FirebaseAuth.getInstance().currentUser
    val userId = currentUser?.uid ?: ""
    var runs by remember { mutableStateOf<List<RunData>>(emptyList()) }

    val context = androidx.compose.ui.platform.LocalContext.current
    val fusedLocationClient = LocationServices.getFusedLocationProviderClient(context)

    var selectedTabIndex by remember { mutableStateOf(0) }

    // Cargar las rutas
    LaunchedEffect(Unit) {
        if (locationPermission.status.isGranted) {
            try {
                currentUser?.let { user ->
                    val result = firestore.collection("running_workouts")
                        .whereEqualTo("userId", user.uid)
                        .orderBy("date", com.google.firebase.firestore.Query.Direction.DESCENDING)
                        .get()
                        .await()

                    runs = result.documents.mapNotNull { doc ->
                        val name = doc.getString("name") ?: "Sin nombre"
                        val dist = (doc.get("distance") as? Number)?.toDouble() ?: 0.0
                        val dur = (doc.get("duration") as? Number)?.toLong() ?: 0L
                        val points = doc.get("route") as? List<Map<String, Double>>
                        val latLngList = points?.mapNotNull { p ->
                            val lat = p["lat"]
                            val lng = p["lng"]
                            if (lat != null && lng != null) LatLng(lat, lng) else null
                        } ?: emptyList()
                        RunData(doc.id, name, dist, dur, latLngList)
                    }
                }
            } catch (e: Exception) {}
        } else locationPermission.launchPermissionRequest()
    }


    // UI
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Mis Carreras") },
                actions = {

                    // Botón Metas
                    IconButton(onClick = { navController.navigate("records/$userId") }) {
                        Icon(Icons.Default.EmojiEvents, "Metas", tint = Color(0xFFFFC107))
                    }

                    // Botón Nueva Ruta
                    IconButton(onClick = {
                        navController.navigate("log_running_workout")
                    }) {
                        Icon(
                            Icons.Default.Add,
                            contentDescription = "Crear nueva ruta"
                        )
                    }
                }
            )
        }
    ) { padding ->

        Column(modifier = Modifier.padding(padding)) {

            // Tabs estilo Rutinas
            TabRow(selectedTabIndex = selectedTabIndex) {
                Tab(
                    selected = selectedTabIndex == 0,
                    onClick = { selectedTabIndex = 0 },
                    text = { Text("Todas") }
                )
                Tab(
                    selected = selectedTabIndex == 1,
                    onClick = { selectedTabIndex = 1 },
                    text = { Text("Recientes") }
                )
            }

            val listToShow =
                if (selectedTabIndex == 0) runs
                else runs.take(5) // últimos 5 (NO afecta lógica principal)

            if (runs.isEmpty()) {
                Box(
                    Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        "Aún no has registrado ninguna carrera",
                        style = MaterialTheme.typography.titleMedium
                    )
                }
            } else {
                LazyColumn(
                    verticalArrangement = Arrangement.spacedBy(14.dp),
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(16.dp)
                ) {

                    items(listToShow) { run ->

                        var expanded by remember { mutableStateOf(false) }
                        var canStart by remember { mutableStateOf(false) }

                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { expanded = !expanded }
                                .animateContentSize(),
                            colors = CardDefaults.cardColors(
                                containerColor = MaterialTheme.colorScheme.surfaceVariant
                            ),
                            shape = MaterialTheme.shapes.large,
                            elevation = CardDefaults.cardElevation(8.dp)
                        ) {
                            Column(modifier = Modifier.padding(18.dp)) {

                                Text(run.name, style = MaterialTheme.typography.titleLarge)
                                Spacer(Modifier.height(6.dp))

                                Text("Distancia: %.2f km".format(run.distance))
                                Text("Duración: ${run.duration / 60} min")

                                if (expanded && run.routePoints.isNotEmpty()) {

                                    Spacer(Modifier.height(14.dp))

                                    val cameraPositionState = rememberCameraPositionState {
                                        position = CameraPosition.fromLatLngZoom(
                                            run.routePoints.first(), 15f
                                        )
                                    }

                                    GoogleMap(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .height(220.dp),
                                        cameraPositionState = cameraPositionState
                                    ) {
                                        Marker(
                                            state = MarkerState(run.routePoints.first()),
                                            title = "Inicio",
                                            icon = BitmapDescriptorFactory.defaultMarker(
                                                BitmapDescriptorFactory.HUE_GREEN
                                            )
                                        )

                                        Marker(
                                            state = MarkerState(run.routePoints.last()),
                                            title = "Fin",
                                            icon = BitmapDescriptorFactory.defaultMarker(
                                                BitmapDescriptorFactory.HUE_RED
                                            )
                                        )

                                        Polyline(
                                            points = run.routePoints,
                                            width = 10f
                                        )
                                    }

                                    Spacer(Modifier.height(12.dp))

                                    LaunchedEffect(Unit) {
                                        val location = fusedLocationClient.lastLocation.await()
                                        location?.let {
                                            val userLatLng = LatLng(it.latitude, it.longitude)
                                            val distanceToStart = SphericalUtil.computeDistanceBetween(
                                                userLatLng,
                                                run.routePoints.first()
                                            )
                                            canStart = distanceToStart <= 100
                                        }
                                    }

                                    if (canStart) {
                                        Button(
                                            onClick = {
                                                navController.navigate("active_run/${run.id}")
                                            },
                                            modifier = Modifier.fillMaxWidth()
                                        ) {
                                            Text("Comenzar ruta")
                                        }
                                    } else {
                                        Text(
                                            "Estás lejos del punto de inicio. Acércate para comenzar.",
                                            color = MaterialTheme.colorScheme.error
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}