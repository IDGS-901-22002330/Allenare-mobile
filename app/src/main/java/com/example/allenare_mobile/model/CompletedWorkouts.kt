package com.example.allenare_mobile.model

import com.google.firebase.Timestamp
import java.util.Date

data class CompletedWorkouts (
    val userId: String = "",
    val cantidad: Double = 0.00,
    val fechaRealizacion: Timestamp = Timestamp.now(),
    val name: String = "",
    val tiempoSegundos: Double = 0.00
)