package com.example.allenare_mobile.model

import com.google.firebase.Timestamp

data class GymWorkout(
    val userId: String = "",
    val type: String = "", // e.g., 'Pecho', 'Pierna'
    val duration: Long = 0, // in seconds
    val date: Timestamp = Timestamp.now()
)