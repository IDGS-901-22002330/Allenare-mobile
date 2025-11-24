package com.example.allenare_mobile.screens.dashboard_components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DirectionsRun
import androidx.compose.material.icons.filled.FitnessCenter
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.allenare_mobile.ui.theme.AllenaremobileTheme
import java.text.DecimalFormat

@Composable
fun TrainingStats(gymDays: Int, totalKm: Double) {
    val kmFormatted = DecimalFormat("#.##").format(totalKm)

    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
        StatCard(
            title = "Días de entrenamiento",
            value = "$gymDays",
            icon = Icons.Default.FitnessCenter,
            gradientColors = listOf(Color(0xFF0066CC), Color(0xFFA2C9F6)),
            modifier = Modifier.weight(1f).height(120.dp)
        )
        Spacer(modifier = Modifier.width(16.dp))
        StatCard(
            title = "Total de km recorridos",
            value = "$kmFormatted km",
            icon = Icons.Default.DirectionsRun,
            gradientColors = listOf(Color(0xFF0066CC), Color(0xFFA2C9F6)),
            modifier = Modifier.weight(1f).height(120.dp)
        )
    }
}

@Preview(showBackground = true)
@Composable
fun TrainingStatsPreview() {
    AllenaremobileTheme {
        Row(modifier = Modifier.padding(16.dp)) {
            TrainingStats(gymDays = 3, totalKm = 24.5)
        }
    }
}