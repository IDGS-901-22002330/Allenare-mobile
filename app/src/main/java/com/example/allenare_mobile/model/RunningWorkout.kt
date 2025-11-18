package com.example.allenare_mobile.model

import com.google.firebase.firestore.PropertyName
import com.google.firebase.firestore.ServerTimestamp
import java.util.Date

data class RunningWorkout(
    @get:PropertyName("userId") @set:PropertyName("userId") var userId: String = "",
    @get:PropertyName("name") @set:PropertyName("name") var name: String = "",
    @get:PropertyName("cantidad") @set:PropertyName("cantidad") var distance: Double = 0.0, // Mapeado desde cantidad
    @get:PropertyName("tiempoSegundos") @set:PropertyName("tiempoSegundos") var duration: Long = 0, // Mapeado desde tiempoSegundos
    val route: List<Map<String, Double>> = emptyList(),
    val estatus: Int = 0,
    @ServerTimestamp
    @get:PropertyName("fechaRealizacion") @set:PropertyName("fechaRealizacion") var date: Date? = null // Mapeado desde fechaRealizacion
)