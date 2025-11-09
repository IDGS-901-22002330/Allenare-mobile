package com.example.allenare_mobile.screens.dashboard_components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.allenare_mobile.model.GymWorkout
import com.example.allenare_mobile.model.RunningWorkout
import com.example.allenare_mobile.ui.theme.AllenaremobileTheme


@Composable
fun RecentWorkouts(
    gymWorkouts: List<GymWorkout>,
    runningWorkouts: List<RunningWorkout>
) {
    // Converción de segundos → minutos y formato de texto
    val combinedList = (
            gymWorkouts.map { workout ->
                "Gimnasio: '${workout.title}' — ${workout.duration} min"
            } +
                    runningWorkouts.map { run ->
                        val durationMinutes = (run.duration / 60.0).toInt()
                        val formattedDistance = String.format("%.2f", run.distance)
                        "Carrera: ${formattedDistance} km — ${durationMinutes} min"
                    }
            ).take(5)

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp),
        shape = RoundedCornerShape(16.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 6.dp)
    ) {
        // Fondo
        val gradientColors = listOf(
            Color(0xFFFFEB3B),
            Color(0xFFF9D976),
            Color(0xFFFBC2EB)
        )

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(
                    brush = Brush.verticalGradient(colors = gradientColors)
                )
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    text = "Entrenamientos recientes",
                    fontWeight = FontWeight.Bold,
                    fontSize = 18.sp,
                    color = Color(0xFF333333)
                )

                Spacer(modifier = Modifier.height(12.dp))

                if (combinedList.isEmpty()) {
                    Text(
                        text = "No hay entrenamientos registrados.",
                        color = Color.Gray,
                        fontSize = 14.sp
                    )
                } else {
                    combinedList.forEach { workout ->
                        Text(
                            text = workout,
                            fontSize = 15.sp,
                            color = Color(0xFF212121),
                            modifier = Modifier.padding(vertical = 4.dp)
                        )
                    }
                }
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun RecentWorkoutsPreview() {
    AllenaremobileTheme {
        RecentWorkouts(
            gymWorkouts = listOf(
                GymWorkout(title = "Pecho", duration = 60),
                GymWorkout(title = "Pierna", duration = 90)
            ),
            runningWorkouts = listOf(
                RunningWorkout(distance = 5.0, duration = 1800), // 1800 seg = 30 min
                RunningWorkout(distance = 10.5, duration = 4200) // 4200 seg = 70 min
            )
        )
    }
}