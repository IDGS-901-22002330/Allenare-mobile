package com.example.allenare_mobile.model

import com.google.firebase.firestore.PropertyName
import com.google.firebase.firestore.ServerTimestamp
import java.util.Date

data class GymWorkout(
    @get:PropertyName("userId") @set:PropertyName("userId") var userId: String = "",
    @get:PropertyName("routineName") @set:PropertyName("routineName") var title: String = "", // Mapeado desde routineName
    var routineType: String = "",
    var exercises: String = "",
    var duration: Long = 0,
    var description: String = "",
    @ServerTimestamp
    @get:PropertyName("timestamp") @set:PropertyName("timestamp") var date: Date? = null // Mapeado desde timestamp
)