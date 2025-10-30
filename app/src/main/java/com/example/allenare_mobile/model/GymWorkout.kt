package com.example.allenare_mobile.model

import com.google.firebase.firestore.ServerTimestamp
import java.util.Date

data class GymWorkout(
    val userId: String = "",
    val title: String = "",
    val routineType: String = "", // Campo que antes era 'type'
    val exercises: String = "",
    val duration: Long = 0,      // en minutos
    val description: String = "",
    @ServerTimestamp
    val date: Date? = null
)