    package com.example.allenare_mobile.model

    import com.google.firebase.Timestamp
    data class ExerciseLogs(
        val userId: String = "",
        val exerciseId: String = "",
        val exercise: String = "",
        val sets: Int = 1,
        val timestamp: Any? = null
    )
