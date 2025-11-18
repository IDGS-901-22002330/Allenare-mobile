package com.example.allenare_mobile.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.allenare_mobile.screens.dashboard_components.MeasureRoute
import com.example.allenare_mobile.screens.dashboard_components.RecentWorkouts
import com.example.allenare_mobile.screens.dashboard_components.TrainingStats
import com.example.allenare_mobile.screens.dashboard_components.UserInfo
import com.example.allenare_mobile.ui.theme.AllenaremobileTheme
import com.example.allenare_mobile.viewmodel.DashboardViewModel

@Composable
fun DashboardScreen(
    modifier: Modifier = Modifier,
    viewModel: DashboardViewModel = androidx.lifecycle.viewmodel.compose.viewModel()
) {
    val uiState by viewModel.uiState.collectAsState()

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(Color(0xFFF0F2F5)),
        contentAlignment = Alignment.Center
    ) {
        if (uiState.isLoading) {
            CircularProgressIndicator()
        } else if (uiState.userInfo == null) {
            Text("No se ha podido cargar la información del usuario.")
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp)
            ) {
                item {
                    uiState.userInfo?.let {
                        UserInfo(username = it.name, email = it.email, photoUrl = it.photoUrl)
                    }
                    Spacer(modifier = Modifier.height(24.dp))
                }

                item {
                    TrainingStats(uiState.weeklyStats.gymDays, uiState.weeklyStats.totalKm)
                    Spacer(modifier = Modifier.height(24.dp))
                }

                item {
                    RecentWorkouts(uiState.recentWorkouts)
                    Spacer(modifier = Modifier.height(24.dp))
                }

                item {
                    MeasureRoute()
                    Spacer(modifier = Modifier.height(24.dp))
                }
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun DashboardScreenPreview() {
    AllenaremobileTheme {
        DashboardScreen()
    }
}