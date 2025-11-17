package com.example.allenare_mobile.model

data class GoalStats(
    var longestDistance: Double = 0.0,
    var bestTimesByRoute: Map<String, Int> = emptyMap(),
    var recentRuns: List<ExercisePerformed> = emptyList(),
    var userId: String = ""
)