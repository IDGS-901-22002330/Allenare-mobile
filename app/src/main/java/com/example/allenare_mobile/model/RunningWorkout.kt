package com.example.allenare_mobile.model

import com.google.firebase.firestore.ServerTimestamp
import java.util.Date

data class RunningWorkout(
    val userId: String = "",
    val distance: Double = 0.0, // in kilometers
    val duration: Long = 0, // in seconds
    @ServerTimestamp
    val date: Date? = null
)