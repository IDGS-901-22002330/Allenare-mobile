package com.example.allenare_mobile.model

import com.google.android.gms.maps.model.LatLng

data class RunData(
    val id: String,
    val name: String,
    val distance: Double,
    val duration: Long,
    val routePoints: List<LatLng>
)
