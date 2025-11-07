package com.example.allenare_mobile.screens.dashboard_components

import android.Manifest
import android.annotation.SuppressLint
import android.location.Location
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.rememberPermissionState
import com.google.accompanist.permissions.isGranted
import com.google.android.gms.location.LocationServices
import com.google.android.gms.maps.model.*
import com.google.maps.android.compose.*
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import okhttp3.OkHttpClient
import okhttp3.Request
import org.json.JSONObject

@OptIn(ExperimentalPermissionsApi::class)
@SuppressLint("MissingPermission")
@Composable
fun MeasureRoute() {
    val context = LocalContext.current
    val locationPermission = rememberPermissionState(Manifest.permission.ACCESS_FINE_LOCATION)
    val fusedLocationClient = LocationServices.getFusedLocationProviderClient(context)

    var currentLocation by remember { mutableStateOf<LatLng?>(null) }
    var startPoint by remember { mutableStateOf<LatLng?>(null) }
    var endPoint by remember { mutableStateOf<LatLng?>(null) }
    var routePoints by remember { mutableStateOf<List<LatLng>>(emptyList()) }
    var distance by remember { mutableStateOf("") }
    var duration by remember { mutableStateOf("") }

    // Pedir permiso y obtener ubicación actual
    LaunchedEffect(Unit) {
        locationPermission.launchPermissionRequest()
        if (locationPermission.status.isGranted) {
            val loc: Location? = fusedLocationClient.lastLocation.await()
            loc?.let {
                currentLocation = LatLng(it.latitude, it.longitude)
            }
        }
    }

    val cameraPositionState = rememberCameraPositionState {
        position = CameraPosition.fromLatLngZoom(currentLocation ?: LatLng(0.0, 0.0), 14f)
    }

    Column(Modifier.fillMaxWidth()) {
        if (currentLocation != null) {
            GoogleMap(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(400.dp),
                cameraPositionState = cameraPositionState,
                onMapClick = { point ->
                    if (startPoint == null) {
                        startPoint = point
                    } else if (endPoint == null) {
                        endPoint = point
                    }
                },
                properties = MapProperties(isMyLocationEnabled = locationPermission.status.isGranted)
            ) {
                startPoint?.let {
                    Marker(
                        state = MarkerState(it),
                        title = "Inicio",
                        icon = BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_GREEN)
                    )
                }
                endPoint?.let {
                    Marker(
                        state = MarkerState(it),
                        title = "Destino",
                        icon = BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_RED)
                    )
                }
                if (routePoints.isNotEmpty()) {
                    Polyline(points = routePoints, color = MaterialTheme.colorScheme.primary)
                }
            }

            Spacer(Modifier.height(8.dp))
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Button(onClick = {
                    startPoint = null
                    endPoint = null
                    routePoints = emptyList()
                    distance = ""
                    duration = ""
                }) { Text("Reiniciar") }

                val coroutineScope = rememberCoroutineScope()

                if (startPoint != null && endPoint != null) {
                    Button(onClick = {
                        coroutineScope.launch {
                            calculateRoute(startPoint!!, endPoint!!) { poly, dist, dur ->
                                routePoints = poly
                                distance = dist
                                duration = dur
                            }
                        }
                    }) { Text("Calcular Ruta") }
                }
            }

            if (distance.isNotEmpty() && duration.isNotEmpty()) {
                Text("Distancia: $distance", style = MaterialTheme.typography.bodyLarge)
                Text("Duración: $duration", style = MaterialTheme.typography.bodyLarge)
            }
        } else {
            Text("Obteniendo ubicación...", Modifier.padding(16.dp))
        }
    }
}

/**
 * Llama a la API Directions para obtener la ruta entre dos puntos
 */
suspend fun calculateRoute(
    start: LatLng,
    end: LatLng,
    onResult: (List<LatLng>, String, String) -> Unit
) {
    val client = OkHttpClient()
    val url =
        "https://maps.googleapis.com/maps/api/directions/json?origin=${start.latitude},${start.longitude}&destination=${end.latitude},${end.longitude}&key=TU_API_KEY"
    val request = Request.Builder().url(url).build()
    val response = client.newCall(request).execute()
    val body = response.body?.string() ?: return

    val json = JSONObject(body)
    val routes = json.getJSONArray("routes")
    if (routes.length() == 0) return

    val points = routes.getJSONObject(0).getJSONObject("overview_polyline").getString("points")
    val distance = routes.getJSONObject(0).getJSONArray("legs").getJSONObject(0)
        .getJSONObject("distance").getString("text")
    val duration = routes.getJSONObject(0).getJSONArray("legs").getJSONObject(0)
        .getJSONObject("duration").getString("text")

    val decoded = decodePolyline(points)
    onResult(decoded, distance, duration)
}

/**
 * Decodifica la polyline de Google Directions
 */
fun decodePolyline(encoded: String): List<LatLng> {
    val poly = mutableListOf<LatLng>()
    var index = 0
    val len = encoded.length
    var lat = 0
    var lng = 0

    while (index < len) {
        var b: Int
        var shift = 0
        var result = 0
        do {
            b = encoded[index++].code - 63
            result = result or (b and 0x1f shl shift)
            shift += 5
        } while (b >= 0x20)
        val dlat = if ((result and 1) != 0) (result shr 1).inv() else result shr 1
        lat += dlat

        shift = 0
        result = 0
        do {
            b = encoded[index++].code - 63
            result = result or (b and 0x1f shl shift)
            shift += 5
        } while (b >= 0x20)
        val dlng = if ((result and 1) != 0) (result shr 1).inv() else result shr 1
        lng += dlng

        poly.add(LatLng(lat / 1E5, lng / 1E5))
    }

    return poly
}