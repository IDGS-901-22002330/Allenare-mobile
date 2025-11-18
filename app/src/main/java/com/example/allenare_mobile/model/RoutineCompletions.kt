package com.example.allenare_mobile.model

import com.google.firebase.Timestamp

data class RoutineCompletions(
    val userId: String = "",
    val routineId: String = "",
    val routineName: String = "",
    val timestamp: Any? = null
)
