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
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.*
import java.util.Calendar
import java.util.Date

// --- Data classes for UI State ---
data class UserInfo(val name: String?, val email: String?, val photoUrl: String?)
data class WeeklyStats(val gymDays: Int = 0, val totalKm: Double = 0.0)
data class RecentWorkoutItem(val description: String, val date: Date?)

data class DashboardUiState(
    val userInfo: UserInfo? = null,
    val weeklyStats: WeeklyStats = WeeklyStats(),
    val recentWorkouts: List<RecentWorkoutItem> = emptyList(),
    val isLoading: Boolean = true
)

@OptIn(ExperimentalCoroutinesApi::class)
class DashboardViewModel : ViewModel() {

    private val db = Firebase.firestore
    private val _userTrigger = MutableStateFlow(Firebase.auth.currentUser)

    fun reloadUserData() {
        _userTrigger.value = Firebase.auth.currentUser
    }

    // Flujo 1: Información del Usuario (Corregido)
    private val userFlow: StateFlow<User?> = _userTrigger.flatMapLatest { user ->
        if (user == null) {
            flowOf(null)
        } else {
            // CORRECCIÓN: Se debe especificar <User?> explícitamente
            callbackFlow<User?> {
                val listener = db.collection("users").document(user.uid)
                    .addSnapshotListener { snapshot, e ->
                        if (e != null) {
                            Log.e("ViewModel", "Error user: ", e)
                            trySend(null)
                        } else {
                            trySend(snapshot?.toObject(User::class.java))
                        }
                    }
                awaitClose { listener.remove() }
            }
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    // Auxiliar: Rango de fechas Lunes-Domingo exacto
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

    // Flujo 2: Gimnasio
    private val gymWorkoutsFlow: StateFlow<List<GymWorkout>> = _userTrigger.flatMapLatest { user ->
        if (user == null) flowOf(emptyList()) else callbackFlow {
            val (start, end) = getWeekDateRange()
            val listener = db.collection("routine_completions")
                .whereEqualTo("userId", user.uid)
                .whereGreaterThanOrEqualTo("timestamp", start)
                .whereLessThanOrEqualTo("timestamp", end)
                .addSnapshotListener { snapshots, e ->
                    if (e != null) { Log.e("ViewModel", "Error gym: ", e); trySend(emptyList()) }
                    else { trySend(snapshots?.toObjects(GymWorkout::class.java) ?: emptyList()) }
                }
            awaitClose { listener.remove() }
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Flujo 3: Running
    private val runningWorkoutsFlow: StateFlow<List<RunningWorkout>> = _userTrigger.flatMapLatest { user ->
        if (user == null) flowOf(emptyList()) else callbackFlow {
            val (start, end) = getWeekDateRange()
            val listener = db.collection("completed_workouts")
                .whereEqualTo("userId", user.uid)
                .whereGreaterThanOrEqualTo("date", start)
                .whereLessThanOrEqualTo("date", end)
                .addSnapshotListener { snapshots, e ->
                    if (e != null) { Log.e("ViewModel", "Error running: ", e); trySend(emptyList()) }
                    else { trySend(snapshots?.toObjects(RunningWorkout::class.java) ?: emptyList()) }
                }
            awaitClose { listener.remove() }
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // UI State Combinado
    val uiState: StateFlow<DashboardUiState> = combine(
        userFlow, gymWorkoutsFlow, runningWorkoutsFlow
    ) { userDoc, gymWorkouts, runningWorkouts ->

        val userInfo = userDoc?.let { UserInfo(it.nombre, it.email, it.photoUrl.takeIf(String::isNotBlank)) }

        val totalKmRaw = runningWorkouts.sumOf { it.distance }
        val totalKmRounded = Math.round(totalKmRaw * 100.0) / 100.0

        val weeklyStats = WeeklyStats(
            gymDays = gymWorkouts.size,
            totalKm = totalKmRounded
        )

        val recentGym = gymWorkouts.map {
            RecentWorkoutItem("Gimnasio: ${it.title}", it.date)
        }
        val recentRunning = runningWorkouts.map {
            val kmDisplay = String.format("%.2f", it.distance)
            RecentWorkoutItem("Carrera: $kmDisplay km", it.date)
        }

        val recentWorkouts = (recentGym + recentRunning)
            .sortedByDescending { it.date }
            .take(5)

        DashboardUiState(userInfo, weeklyStats, recentWorkouts, false)

    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), DashboardUiState(isLoading = true))
}