package com.example.allenare_mobile.model

import com.google.firebase.firestore.ServerTimestamp
import java.util.Date

// Modelo actualizado para que coincida con la estructura de Firestore
data class RunningWorkout(
    val userId: String = "",
    val name: String = "",
    val distance: Double = 0.0, // Corregido
    val duration: Long = 0,     // Corregido
    val route: List<Map<String, Double>> = emptyList(),
    val estatus: Int = 0,
    @ServerTimestamp
    val date: Date? = null      // Corregido
)