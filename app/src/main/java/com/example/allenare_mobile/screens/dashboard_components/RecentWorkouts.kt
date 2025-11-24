package com.example.allenare_mobile.screens.dashboard_components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DirectionsRun
import androidx.compose.material.icons.filled.FitnessCenter
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.allenare_mobile.model.GymWorkout
import com.example.allenare_mobile.model.RunningWorkout
import com.example.allenare_mobile.ui.theme.AllenaremobileTheme
import java.util.Date

sealed class DisplayableWorkout(val date: Date?) {
    data class Gym(val workout: GymWorkout) : DisplayableWorkout(workout.timestamp)
    data class Running(val workout: RunningWorkout) : DisplayableWorkout(workout.date)
}

@Composable
fun RecentWorkouts(
    gymWorkouts: List<GymWorkout>,
    runningWorkouts: List<RunningWorkout>
) {
    val combinedList = (gymWorkouts.map { DisplayableWorkout.Gym(it) } + runningWorkouts.map { DisplayableWorkout.Running(it) })
        .sortedByDescending { it.date }
        .take(5)

    Card(
        modifier = Modifier
            .fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = "Entrenamientos recientes",
                style = MaterialTheme.typography.titleLarge,
                color = MaterialTheme.colorScheme.onSurface
            )

            Spacer(modifier = Modifier.height(12.dp))

            if (combinedList.isEmpty()) {
                Text(
                    text = "No hay entrenamientos registrados.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            } else {
                combinedList.forEach {
                    WorkoutRow(workout = it)
                }
            }
        }
    }
}

@Composable
private fun WorkoutRow(workout: DisplayableWorkout) {
    val icon: ImageVector
    val title: String
    val durationText: String

    when (workout) {
        is DisplayableWorkout.Gym -> {
            icon = Icons.Default.FitnessCenter
            title = workout.workout.routineName
            durationText = "${workout.workout.duration} min"
        }
        is DisplayableWorkout.Running -> {
            icon = Icons.Default.DirectionsRun
            val durationMinutes = (workout.workout.duration / 60).toInt()
            title = workout.workout.name
            durationText = "$durationMinutes min"
        }
    }

    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp)
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null, // Decorative
            modifier = Modifier.size(24.dp),
            tint = MaterialTheme.colorScheme.primary
        )

        Spacer(modifier = Modifier.width(16.dp))

        Text(
            text = title,
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurface,
            modifier = Modifier.weight(1f),
            fontWeight = FontWeight.Medium
        )

        Text(
            text = durationText,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@Preview(showBackground = true)
@Composable
fun RecentWorkoutsPreview() {
    AllenaremobileTheme {
        RecentWorkouts(
            gymWorkouts = listOf(
                GymWorkout(routineName = "Pecho y Tríceps", duration = 75, timestamp = Date()),
                GymWorkout(routineName = "Espalda y Bíceps", duration = 70, timestamp = Date(System.currentTimeMillis() - 86400000))
            ),
            runningWorkouts = listOf(
                RunningWorkout(name = "Carrera mañanera", distance = 5.2, duration = 1800, date = Date(System.currentTimeMillis() - 172800000)),
                RunningWorkout(name = "10K Lomas", distance = 10.0, duration = 3900, date = Date(System.currentTimeMillis() - 259200000))
            )
        )
    }
}
