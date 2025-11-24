package com.example.allenare_mobile.model

import com.google.firebase.firestore.ServerTimestamp
import java.util.Date

data class GymWorkout(
    val userId: String = "",
    val routineName: String = "",
    val routineType: String = "",
    val exercises: String = "",
    val duration: Long = 0,      // en minutos
    val description: String = "",
    @ServerTimestamp
    val timestamp: Date? = null
)