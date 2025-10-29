package com.example.allenare_mobile.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalInspectionMode
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.allenare_mobile.model.GymWorkout
import com.example.allenare_mobile.model.RunningWorkout
import com.example.allenare_mobile.screens.dashboard_components.MeasureRoute
import com.example.allenare_mobile.screens.dashboard_components.RecentWorkouts
import com.example.allenare_mobile.screens.dashboard_components.TrainingStats
import com.example.allenare_mobile.screens.dashboard_components.UserInfo
import com.example.allenare_mobile.ui.theme.AllenaremobileTheme
import com.google.firebase.Firebase
import com.google.firebase.auth.auth
import com.google.firebase.firestore.firestore

@Composable
fun DashboardScreen(modifier: Modifier = Modifier) {
    val isInPreview = LocalInspectionMode.current
    val user = if (isInPreview) null else Firebase.auth.currentUser
    var gymWorkouts by remember { mutableStateOf<List<GymWorkout>>(emptyList()) }
    var runningWorkouts by remember { mutableStateOf<List<RunningWorkout>>(emptyList()) }

    if (!isInPreview) {
        LaunchedEffect(user) {
            if (user != null) {
                val db = Firebase.firestore
                db.collection("gym_workouts")
                    .whereEqualTo("userId", user.uid)
                    .addSnapshotListener { snapshots, e ->
                        if (e != null) { return@addSnapshotListener }
                        gymWorkouts = snapshots?.toObjects(GymWorkout::class.java) ?: emptyList()
                    }
                db.collection("running_workouts")
                    .whereEqualTo("userId", user.uid)
                    .addSnapshotListener { snapshots, e ->
                        if (e != null) { return@addSnapshotListener }
                        runningWorkouts = snapshots?.toObjects(RunningWorkout::class.java) ?: emptyList()
                    }
            }
        }
    }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .background(Color(0xFFF0F2F5))
            .padding(16.dp)
    ) {
        item {
            UserInfo(username = user?.displayName, email = user?.email, photoUrl = user?.photoUrl?.toString())
            Spacer(modifier = Modifier.height(24.dp))
        }

        item {
            TrainingStats(gymWorkouts, runningWorkouts)
            Spacer(modifier = Modifier.height(24.dp))
        }

        item {
            RecentWorkouts(gymWorkouts, runningWorkouts)
            Spacer(modifier = Modifier.height(24.dp))
        }

        item {
            MeasureRoute()
            Spacer(modifier = Modifier.height(24.dp))
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