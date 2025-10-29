package com.example.allenare_mobile.screens.dashboard_components

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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.allenare_mobile.model.GymWorkout
import com.example.allenare_mobile.model.RunningWorkout
import com.example.allenare_mobile.ui.theme.AllenaremobileTheme

@Composable
fun RecentWorkouts(gymWorkouts: List<GymWorkout>, runningWorkouts: List<RunningWorkout>) {
    val combinedList = (gymWorkouts.map { "Gimnasio: '${it.type}' - ${it.duration} min" } + runningWorkouts.map { "Running: ${it.distance} km- ${it.duration} min"  }).take(5)

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFFFFF9C4))
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text("Entrenamientos recientes", fontWeight = FontWeight.Bold, fontSize = 18.sp)
            Spacer(modifier = Modifier.height(16.dp))
            if (combinedList.isEmpty()){
                Text("No hay entrenamientos registrados.")
            } else {
                combinedList.forEach {
                    Text(it)
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
            gymWorkouts = listOf(GymWorkout(type = "Pecho"), GymWorkout(type = "Pierna")),
            runningWorkouts = listOf(RunningWorkout(distance = 5.0))
        )
    }
}