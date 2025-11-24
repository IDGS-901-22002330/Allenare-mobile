package com.example.allenare_mobile.viewmodel

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.allenare_mobile.model.GymWorkout
import com.example.allenare_mobile.model.RunningWorkout
import com.example.allenare_mobile.model.User
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.callbackFlow
import java.util.Calendar
import java.util.Date

// --- Data classes for UI State ---
data class UserInfo(val name: String?, val email: String?, val photoUrl: String?)
data class WeeklyStats(val gymDays: Int = 0, val totalKm: Double = 0.0)

data class DashboardUiState(
    val userInfo: UserInfo? = null,
    val weeklyStats: WeeklyStats = WeeklyStats(),
    val recentGymWorkouts: List<GymWorkout> = emptyList(),
    val recentRunningWorkouts: List<RunningWorkout> = emptyList(),
    val isLoading: Boolean = true
)

class DashboardViewModel : ViewModel() {

    private val user = Firebase.auth.currentUser
    private val db = Firebase.firestore

    // --- Flujo para obtener los datos del usuario ---
    private val userFlow: StateFlow<User?> = callbackFlow {
        val listener = if (user == null) {
            trySend(null)
            null
        } else {
            db.collection("users").document(user.uid)
                .addSnapshotListener { snapshot, e ->
                    if (e != null) {
                        Log.e("DashboardViewModel", "Error al obtener el usuario: ", e)
                        trySend(null)
                        return@addSnapshotListener
                    }
                    val userDoc = snapshot?.toObject(User::class.java)
                    Log.d("DashboardViewModel", "Usuario encontrado: ${userDoc?.nombre}")
                    trySend(userDoc).isSuccess
                }
        }
        awaitClose { listener?.remove() }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    private fun getWeekDateRange(): Pair<Date, Date> {
        val calendar = Calendar.getInstance()
        calendar.firstDayOfWeek = Calendar.MONDAY
        calendar.set(Calendar.DAY_OF_WEEK, Calendar.MONDAY)
        calendar.set(Calendar.HOUR_OF_DAY, 0)
        calendar.set(Calendar.MINUTE, 0)
        val startOfWeek = calendar.time

        calendar.add(Calendar.DAY_OF_WEEK, 6)
        calendar.set(Calendar.HOUR_OF_DAY, 23)
        calendar.set(Calendar.MINUTE, 59)
        val endOfWeek = calendar.time
        return Pair(startOfWeek, endOfWeek)
    }

    private val gymWorkoutsFlow: StateFlow<List<GymWorkout>> = callbackFlow {
        val listener = if (user == null) {
            trySend(emptyList())
            null
        } else {
            val (start, end) = getWeekDateRange()
            db.collection("routine_completions")
                .whereEqualTo("userId", user.uid)
                .whereGreaterThanOrEqualTo("timestamp", start)
                .whereLessThanOrEqualTo("timestamp", end)
                .addSnapshotListener { snapshots, e ->
                    if (e != null) {
                        Log.e("DashboardViewModel", "Error en GymWorkouts: ", e)
                        return@addSnapshotListener
                    }
                    val workouts = snapshots?.toObjects(GymWorkout::class.java) ?: emptyList()
                    Log.d("DashboardViewModel", "GymWorkouts encontrados: ${workouts.size}")
                    trySend(workouts).isSuccess
                }
        }
        awaitClose { listener?.remove() }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    private val runningWorkoutsFlow: StateFlow<List<RunningWorkout>> = callbackFlow {
        val listener = if (user == null) {
            trySend(emptyList())
            null
        } else {
            val (start, end) = getWeekDateRange()
            db.collection("completed_workouts")
                .whereEqualTo("userId", user.uid)
                .whereGreaterThanOrEqualTo("date", start)
                .whereLessThanOrEqualTo("date", end)
                .addSnapshotListener { snapshots, e ->
                    if (e != null) {
                        Log.e("DashboardViewModel", "Error en RunningWorkouts: ", e)
                        return@addSnapshotListener
                    }
                    val workouts = snapshots?.toObjects(RunningWorkout::class.java) ?: emptyList()
                    Log.d("DashboardViewModel", "RunningWorkouts encontrados: ${workouts.size}")
                    trySend(workouts).isSuccess
                }
        }
        awaitClose { listener?.remove() }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val uiState: StateFlow<DashboardUiState> = combine(
        userFlow, // <-- Combinamos el nuevo flujo
        gymWorkoutsFlow,
        runningWorkoutsFlow
    ) { userDoc, gymWorkouts, runningWorkouts ->
        
        val userInfo = UserInfo(
            name = userDoc?.nombre,
            email = userDoc?.email,
            photoUrl = userDoc?.fotoURL?.takeIf { it.isNotBlank() } // <-- Corregido
        )

        val weeklyStats = WeeklyStats(
            gymDays = gymWorkouts.size,
            totalKm = runningWorkouts.sumOf { it.distance }
        )

        val recentGym = gymWorkouts.sortedByDescending { it.timestamp }.take(5)
        val recentRunning = runningWorkouts.sortedByDescending { it.date }.take(5)

        DashboardUiState(userInfo, weeklyStats, recentGym, recentRunning, false)
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), DashboardUiState(isLoading = true))
}