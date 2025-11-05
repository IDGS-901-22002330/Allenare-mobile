package com.example.allenare_mobile.model

data class Exercise(
    val exerciseID: String = "", // ID del documento
    val nombre: String = "",
    val descripcion: String = "",
    val mediaURL: String = "",      // Link a Firebase Storage (video o img)
    val grupoMuscular: String = ""
)