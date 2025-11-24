package com.example.allenare_mobile.model

data class User(
    val userId: String = "",
    val nombre: String = "",
    val email: String = "",
    val fotoURL: String = "", // Corregido de photoUrl a fotoURL
    val edad: Int = 0,
    val estatura: Int = 0,
    val peso: Int = 0,
    val sexo: String = "",
    val tipo: String = ""
)