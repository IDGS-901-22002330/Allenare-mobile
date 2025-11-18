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
import com.example.allenare_mobile.ui.theme.AllenaremobileTheme
import com.example.allenare_mobile.viewmodel.RecentWorkoutItem

@Composable
fun RecentWorkouts(recentWorkouts: List<RecentWorkoutItem>) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        val gradientColors = listOf(Color(0xFFFFEB3B), Color(0xFFFAEBC9))

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(
                    brush = Brush.verticalGradient(
                        colors = gradientColors
                    )
                )
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text("Entrenamientos recientes", fontWeight = FontWeight.Bold, fontSize = 18.sp)
                Spacer(modifier = Modifier.height(16.dp))
                if (recentWorkouts.isEmpty()) {
                    Text("No hay entrenamientos registrados esta semana.")
                } else {
                    recentWorkouts.forEach { workout ->
                        Text(workout.description)
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
            recentWorkouts = listOf(
                RecentWorkoutItem("Gimnasio: Día de Pecho", null),
                RecentWorkoutItem("Carrera: 5.0 km", null),
                RecentWorkoutItem("Gimnasio: Día de Pierna", null)
            )
        )
    }
}