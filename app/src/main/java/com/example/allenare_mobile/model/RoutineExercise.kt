package com.example.allenare_mobile.model

// Nota: Asumimos que tu BD tendrá estos campos
// (exerciseNombre y exerciseMediaURL) para evitar
// tener que hacer N+1 consultas.
data class RoutineExercise(
    val routineID: String = "", // Para saber a qué rutina pertenece
    val exerciseID: String = "", // ID del ejercicio original
    val orden: Int = 0,
    val series: String = "",
    val repeticiones: String = "",
    val tiempoDescansoSegundos: Int = 0,

    // Datos "desnormalizados" (copiados) del ejercicio para fácil acceso
    val exerciseNombre: String = "",
    val exerciseMediaURL: String = ""
)