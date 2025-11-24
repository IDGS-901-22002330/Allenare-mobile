package com.example.allenare_mobile.screens

import android.util.Log
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.allenare_mobile.model.GymWorkout
import com.example.allenare_mobile.model.RunningWorkout
import com.example.allenare_mobile.model.User
import com.example.allenare_mobile.screens.dashboard_components.MeasureRoute
import com.example.allenare_mobile.screens.dashboard_components.RecentWorkouts
import com.example.allenare_mobile.screens.dashboard_components.TrainingStats
import com.example.allenare_mobile.screens.dashboard_components.UserInfo
import com.example.allenare_mobile.ui.theme.AllenaremobileTheme
import com.example.allenare_mobile.viewmodel.RecentWorkoutItem
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import java.util.Calendar
import java.util.Date

@Composable
fun DashboardScreen(modifier: Modifier = Modifier) {
    // 1. Estados locales para manejar toda la información de la UI.
    var userInfo by remember { mutableStateOf<User?>(null) }
    var gymWorkouts by remember { mutableStateOf<List<GymWorkout>>(emptyList()) }
    var runningWorkouts by remember { mutableStateOf<List<RunningWorkout>>(emptyList()) }
    var isLoading by remember { mutableStateOf(true) }

    // 2. El "corazón" de la solución: Un efecto que reacciona a los cambios de usuario.
    DisposableEffect(Firebase.auth.currentUser) {
        val currentUser = Firebase.auth.currentUser
        if (currentUser == null) {
            userInfo = null
            gymWorkouts = emptyList()
            runningWorkouts = emptyList()
            isLoading = false
            return@DisposableEffect onDispose {} // No hacemos nada más.
        }

        isLoading = true
        val db = Firebase.firestore
        val userId = currentUser.uid

        // --- Listener para los datos del usuario ---
        val userListener = db.collection("users").document(userId)
            .addSnapshotListener { snapshot, e ->
                if (e != null) {
                    Log.e("Dashboard", "Error al cargar datos del usuario: ", e)
                    isLoading = false
                    return@addSnapshotListener
                }
                userInfo = snapshot?.toObject(User::class.java)
            }

        // --- Listener para los entrenamientos de gimnasio ---
        val (start, end) = getWeekDateRange()
        val gymListener = db.collection("routine_completions")
            .whereEqualTo("userId", userId)
            .whereGreaterThanOrEqualTo("timestamp", start)
            .whereLessThanOrEqualTo("timestamp", end)
            .addSnapshotListener { snapshots, e ->
                if (e != null) {
                    Log.e("Dashboard", "Error al cargar entrenamientos de gym: ", e)
                    return@addSnapshotListener
                }
                gymWorkouts = snapshots?.toObjects(GymWorkout::class.java) ?: emptyList()
                isLoading = false // Desactivamos el loading cuando llegan los datos.
            }

        // --- Listener para las carreras ---
        val runningListener = db.collection("completed_workouts")
            .whereEqualTo("userId", userId)
            .whereGreaterThanOrEqualTo("date", start)
            .whereLessThanOrEqualTo("date", end)
            .addSnapshotListener { snapshots, e ->
                if (e != null) {
                    Log.e("Dashboard", "Error al cargar carreras: ", e)
                    return@addSnapshotListener
                }
                runningWorkouts = snapshots?.toObjects(RunningWorkout::class.java) ?: emptyList()
                isLoading = false
            }

        // 3. Bloque de limpieza: Se ejecuta cuando la pantalla desaparece.
        onDispose {
            userListener.remove()
            gymListener.remove()
            runningListener.remove()
        }
    }

    // --- Lógica de la UI (Cálculos y mapeos) ---
    val weeklyGymDays = gymWorkouts.size
    val weeklyTotalKm = runningWorkouts.sumOf { it.distance }

    val recentWorkouts = (gymWorkouts.map { RecentWorkoutItem("Gimnasio: ${it.title}", it.date) } +
        runningWorkouts.map { RecentWorkoutItem("Carrera: ${it.distance} km", it.date) })
        .sortedByDescending { it.date }
        .take(5)

    // --- Composable de la UI ---
    Box(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background),
        contentAlignment = Alignment.Center
    ) {
        if (isLoading) {
            CircularProgressIndicator()
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp)
            ) {
                item {
                    UserInfo(username = userInfo?.nombre, email = userInfo?.email, photoUrl = userInfo?.photoUrl)
                    Spacer(modifier = Modifier.height(24.dp))
                }

                item {
                    TrainingStats(gymDays = weeklyGymDays, totalKm = weeklyTotalKm)
                    Spacer(modifier = Modifier.height(24.dp))
                }

                item {
                    RecentWorkouts(recentWorkouts = recentWorkouts)
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

// Función auxiliar para obtener el rango de la semana
private fun getWeekDateRange(): Pair<Date, Date> {
    val cal = Calendar.getInstance().apply {
        firstDayOfWeek = Calendar.MONDAY
        set(Calendar.DAY_OF_WEEK, Calendar.MONDAY)
        set(Calendar.HOUR_OF_DAY, 0); set(Calendar.MINUTE, 0); set(Calendar.SECOND, 0); set(Calendar.MILLISECOND, 0)
    }
    val start = cal.time
    cal.add(Calendar.DAY_OF_WEEK, 6)
    cal.set(Calendar.HOUR_OF_DAY, 23); cal.set(Calendar.MINUTE, 59); cal.set(Calendar.SECOND, 59); cal.set(Calendar.MILLISECOND, 999)
    val end = cal.time
    return Pair(start, end)
}

@Preview(showBackground = true)
@Composable
fun DashboardScreenPreview() {
    AllenaremobileTheme {
        // La preview no cargará datos, mostrará el estado inicial.
        DashboardScreen()
    }
}
