package com.example.allenare_mobile.screens

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.Alignment
import androidx.navigation.NavController
import com.google.firebase.firestore.FirebaseFirestore
import com.example.allenare_mobile.model.ExercisePerformed
import com.example.allenare_mobile.model.GoalStats
import kotlinx.coroutines.tasks.await
import java.text.SimpleDateFormat
import java.util.Locale


@Composable
fun Records(navController: NavController, userId: String) {

    val firestore = FirebaseFirestore.getInstance()
    var stats by remember { mutableStateOf(GoalStats()) }
    var loading by remember { mutableStateOf(true) }
    var hasData by remember { mutableStateOf(false) }


    LaunchedEffect(userId) {
        try {
            val snapshot = firestore.collection("exercise_performed")
                .whereEqualTo("userId", userId)
                .get()
                .await()

            val records = snapshot.documents.mapNotNull {
                it.toObject(ExercisePerformed::class.java)
            }

            hasData = records.isNotEmpty() // si hay carreras

            if (records.isNotEmpty()) {

                // Distancia más larga
                val longest = records.maxOfOrNull { it.cantidad } ?: 0.0

                // Mejor tiempo por ruta → menor tiempo
                val bestTimes = records
                    .groupBy { it.name }
                    .mapValues { (_, runs) ->
                        runs.minOf { it.tiempoSegundos }.toInt()
                    }

                // Rutas recientes (5 últimas)
                val recent = records
                    .sortedByDescending { it.fechaRealizacion }
                    .take(5)

                stats = GoalStats(
                    longestDistance = longest,
                    bestTimesByRoute = bestTimes,
                    recentRuns = recent
                )
            }

        } catch (e: Exception) {
            e.printStackTrace()
        } finally {
            loading = false
        }
    }

    if (loading) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            CircularProgressIndicator()
        }
        return
    }

    // ---- UI ----
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {

        // Regresar
        Button(
            onClick = { navController.popBackStack() },
            modifier = Modifier.padding(bottom = 12.dp)
        ) {
            Text("Regresar")
        }

        Text(
            "Tus Metas",
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold
        )

        Spacer(modifier = Modifier.height(20.dp))

        if (!hasData) {
            Box(
                modifier = Modifier.fillMaxWidth(),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    "Aún no has completado ninguna ruta.\n" +
                            "Corre tu primera carrera para ver tus mejores marcas.",
                    style = MaterialTheme.typography.titleMedium
                )
            }
            return
        }

        // Verificacion de datos

        // Distancia mas larga
        Card(
            modifier = Modifier.fillMaxWidth(),
            elevation = CardDefaults.cardElevation(8.dp)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text("🏆 Distancia más larga", fontWeight = FontWeight.Bold)
                Text("${String.format("%.2f", stats.longestDistance)} km")
            }
        }

        Spacer(modifier = Modifier.height(20.dp))


        if (stats.bestTimesByRoute.isNotEmpty()) {
            Card(
                modifier = Modifier.fillMaxWidth(),
                elevation = CardDefaults.cardElevation(8.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {

                    Text("Mejores tiempos por ruta", fontWeight = FontWeight.Bold)

                    stats.bestTimesByRoute.forEach { (route, time) ->
                        val min = time / 60
                        val sec = time % 60
                        Text("• $route → ${min}m ${sec}s")
                    }
                }
            }

            Spacer(modifier = Modifier.height(20.dp))
        }


        // Rutas recientes
        if (stats.recentRuns.isNotEmpty()) {
            Text("Rutas recientes", fontWeight = FontWeight.Bold)
            Spacer(modifier = Modifier.height(8.dp))

            stats.recentRuns.forEach { run ->

                val fecha = run.fechaRealizacion?.let {
                    SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault()).format(it)
                } ?: "Fecha desconocida"

                Text(
                    "• ${run.name} — ${
                        String.format("%.2f", run.cantidad)
                    } km — ${run.tiempoSegundos / 60} min — $fecha"
                )

                Spacer(modifier = Modifier.height(6.dp))
            }
        }
    }
}