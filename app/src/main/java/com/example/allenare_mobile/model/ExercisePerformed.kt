package com.example.allenare_mobile.model

import com.google.firebase.firestore.ServerTimestamp
import java.util.Date


data class ExercisePerformed(
    val userId: String = "", // ID del usuario
    val name: String = "", // Nombre del ejercicio o ruta
    val cantidad: Double = 0.0, // Repeticiones hechas o distancia recorrida (km)
    val tiempoSegundos: Long = 0, // Tiempo total en segundos
    @ServerTimestamp
    val fechaRealizacion: Date? = null // Fecha en que se completó
)