package com.example.allenare_mobile.screens

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalInspectionMode
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.allenare_mobile.model.CompletedWorkouts
import com.example.allenare_mobile.model.RunningSummary
import com.example.allenare_mobile.model.ExerciseLogs
import com.example.allenare_mobile.model.ExerciseSummary
import com.example.allenare_mobile.model.RoutineCompletions
import com.example.allenare_mobile.model.RoutineSummary
import com.example.allenare_mobile.screens.leaderboards_components.LeaderBoardDominadaCard
import com.example.allenare_mobile.screens.leaderboards_components.LeaderBoardExerciseCard
import com.example.allenare_mobile.screens.leaderboards_components.LeaderBoardRoutineCard
import com.example.allenare_mobile.screens.leaderboards_components.LeaderBoardRunningCard
import com.example.allenare_mobile.ui.theme.AllenaremobileTheme
import com.google.firebase.Firebase
import com.google.firebase.firestore.firestore

@Composable
fun LeaderBoardsScreen(modifier: Modifier = Modifier) {
    val isInPreview = LocalInspectionMode.current
    var runnings by remember { mutableStateOf<List<CompletedWorkouts>>(emptyList()) }
    var routines by remember { mutableStateOf<List<RoutineCompletions>>(emptyList()) }
    var exercises by remember { mutableStateOf<List<ExerciseLogs>>(emptyList()) }
    var selectedTab by remember { mutableStateOf("running") }

    if (!isInPreview) {
        LaunchedEffect(Unit) {
            val db = Firebase.firestore
            db.collection("completed_workouts")
                .get()
                .addOnSuccessListener { snapshot ->
                    runnings = snapshot.toObjects(CompletedWorkouts::class.java)
                }
            db.collection("routine_completions")
                .get()
                .addOnSuccessListener { snapshot ->
                    routines = snapshot.toObjects(RoutineCompletions::class.java)
                }
            db.collection("exercise_logs")
                .get()
                .addOnSuccessListener { snapshot ->
                    exercises = snapshot.toObjects(ExerciseLogs::class.java)
                }
        }
    }

    val RunningSummary = remember(runnings) {
        runnings
            .groupBy { it.userId }
            .map { (userId, items) ->
                RunningSummary(
                    userId = userId,
                    name = items.firstOrNull()?.name ?: "",
                    cantidadTotal = items.sumOf { it.cantidad },
                    tiempoTotal = items.sumOf { it.tiempoSegundos }
                )
            }
            .sortedByDescending { it.cantidadTotal }
            .mapIndexed { index, item -> index to item }
    }

    val RoutineSummary = remember(routines) {
        routines
            .groupBy { it.userId }
            .map { (userId, items) ->
                RoutineSummary(
                    userId = userId,
                    nombre = items.firstOrNull()?.routineName ?: "",
                    cantidad = items.size
                )
            }
            .sortedByDescending { it.cantidad }
            .mapIndexed { index, item -> index to item }
    }

    val ExerciseSummary = remember(exercises) {
        exercises
            .groupBy { it.userId }
            .map { (userId, items) ->
                ExerciseSummary(
                    userId = userId,
                    nombre = items.firstOrNull()?.exerciseName ?: "",
                    cantidad = items.sumOf { it.sets }
                )
            }
            .sortedByDescending { it.cantidad }
            .mapIndexed { index, item -> index to item }
    }

    val DominadasSummary = remember(exercises) {
        exercises
            .filter { it.exerciseName == "Dominadas" }
            .groupBy { it.userId }
            .map { (userId, items) ->
                ExerciseSummary(
                    userId = userId,
                    nombre = "Dominadas",
                    cantidad = items.sumOf { it.sets }
                )
            }
            .sortedByDescending { it.cantidad }
            .mapIndexed { index, item -> index to item }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {

        Text("LeaderBoards", style = MaterialTheme.typography.headlineMedium)
        Spacer(modifier = Modifier.height(16.dp))

        Row(
            modifier = Modifier.fillMaxWidth()
        ) {
            listOf(
                "running" to "Running",
                "routines" to "Rutinas",
                "exercises" to "Ejercicios",
                "dominadas" to "Dominadas"
            ).forEach { (key, label) ->
                androidx.compose.material3.Button(
                    onClick = { selectedTab = key },
                    modifier = Modifier
                        .padding(end = 8.dp)
                        .weight(1f),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text(label)
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        when (selectedTab) {

            "running" -> {
                LazyColumn(modifier = Modifier.fillMaxSize()) {
                    item{
                        Text("Running", style = MaterialTheme.typography.headlineSmall)
                    }
                    items(RunningSummary.size) { i ->
                        val (index, item) = RunningSummary[i]
                        Card(modifier = Modifier.fillMaxWidth().padding(8.dp), shape = RoundedCornerShape(16.dp), elevation = CardDefaults.cardElevation(defaultElevation = 6.dp)) {
                            LeaderBoardRunningCard(
                                index = i + 1,
                                name = item.name,
                                cantidad = item.cantidadTotal,
                                timepoSegundos = item.tiempoTotal
                            )
                        }
                    }
                }
            }

            "routines" -> {
                LazyColumn(modifier = Modifier.fillMaxSize()) {
                    item{
                        Text("Rutinas", style = MaterialTheme.typography.headlineSmall)
                    }
                    items(RoutineSummary.size) { i ->
                        val (index, item) = RoutineSummary[i]
                        Card(modifier = Modifier.fillMaxWidth().padding(8.dp), shape = RoundedCornerShape(16.dp), elevation = CardDefaults.cardElevation(defaultElevation = 6.dp)) {
                            LeaderBoardRoutineCard(
                                index = i + 1,
                                nombre = item.nombre,
                                cantidad = item.cantidad
                            )
                        }
                    }
                }
            }

            "exercises" -> {
                LazyColumn(modifier = Modifier.fillMaxSize()) {
                    item{
                        Text("Ejercicios (Sets)", style = MaterialTheme.typography.headlineSmall)
                    }
                    items(ExerciseSummary.size) { i ->
                        val (index, item) = ExerciseSummary[i]
                        Card(modifier = Modifier.fillMaxWidth().padding(8.dp), shape = RoundedCornerShape(16.dp), elevation = CardDefaults.cardElevation(defaultElevation = 6.dp)) {
                            LeaderBoardExerciseCard(
                                index = i + 1,
                                nombre = item.nombre,
                                cantidad = item.cantidad
                            )
                        }
                    }
                }
            }

            "dominadas" -> {
                LazyColumn(modifier = Modifier.fillMaxSize()) {
                    item {
                        Text("Ejercicios (Dominadas)", style = MaterialTheme.typography.headlineSmall)
                    }
                    items(DominadasSummary.size) { i ->
                        val (index, item) = DominadasSummary[i]
                        Card(modifier = Modifier.fillMaxWidth().padding(8.dp), shape = RoundedCornerShape(16.dp), elevation = CardDefaults.cardElevation(defaultElevation = 6.dp)) {
                            LeaderBoardDominadaCard(
                                index = i + 1,
                                nombre = item.nombre,
                                cantidad = item.cantidad
                            )
                        }
                    }
                }
            }
        }
    }
}
