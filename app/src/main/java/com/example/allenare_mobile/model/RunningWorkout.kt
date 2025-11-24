package com.example.allenare_mobile.model

import com.google.firebase.firestore.ServerTimestamp
import java.util.Date

data class RunningWorkout(
    val userId: String = "",
    val name: String = "",
    val distance: Double = 0.0,
    val duration: Long = 0, 
    val estatus: Int = 0,
    val route: List<Map<String, Double>> = emptyList(),
    @ServerTimestamp
    val date: Date? = null
)