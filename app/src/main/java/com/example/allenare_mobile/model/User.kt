package com.example.allenare_mobile.model

import com.google.firebase.firestore.PropertyName

// Este "molde" representa la estructura de un documento en tu colección "users"
data class User(
    val userId: String = "",
    val nombre: String = "",
    val email: String = "",
    @get:PropertyName("fotoURL") @set:PropertyName("fotoURL") var photoUrl: String = "",
    val edad: Int = 0,
    val estatura: Int = 0,
    val peso: Int = 0,
    val sexo: String = "",
    val tipo: String = ""
)