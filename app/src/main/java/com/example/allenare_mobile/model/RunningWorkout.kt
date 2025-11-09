package com.example.allenare_mobile.model

import com.google.firebase.firestore.ServerTimestamp
import java.util.Date
import java.util.*

data class RunningWorkout(
    val userId: String = "",
    val distance: Double = 0.0, // en kilómetros
    val duration: Long = 0, // en segundos
    val route: List<Map<String, Double>> = emptyList(), // lista de coordenadas
    @ServerTimestamp
    val date: Date? = null
)