package com.example.allenare_mobile.model

data class Routine(
    val routineID: String = "", // ID del documento
    val nombre: String = "",
    val tipo: String = "", // "predefinida" o "personal"
    val userID: String? = null // Null si es predefinida (Admin)
)