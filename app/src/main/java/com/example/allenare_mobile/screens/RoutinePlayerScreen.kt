package com.example.allenare_mobile.screens

import android.util.Log // --- AÑADIDO ---
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack // --- CORREGIDO (era AutoMirrored) ---
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.allenare_mobile.model.RoutineExercise
import com.google.firebase.auth.ktx.auth // --- AÑADIDO ---
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.firestore.ktx.toObjects
import com.google.firebase.ktx.Firebase
import kotlinx.coroutines.delay
import kotlinx.coroutines.tasks.await

private sealed class PlayerState {
    data object Exercising : PlayerState()
    data class Resting(val duration: Int) : PlayerState()
    data object Finished : PlayerState()
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RoutinePlayerScreen(
    routineId: String,
    onBack: () -> Unit
) {
    var steps by remember { mutableStateOf<List<RoutineExercise>>(emptyList()) }
    var currentStepIndex by remember { mutableStateOf(0) }
    var playerState by remember { mutableStateOf<PlayerState>(PlayerState.Exercising) }

    var remainingTime by remember { mutableStateOf(0) }
    var isRestButtonEnabled by remember { mutableStateOf(false) }

    // --- LÓGICA DE HISTORIAL RE-AÑADIDA ---
    val db = Firebase.firestore
    val user = Firebase.auth.currentUser
    // -------------------------------------

    LaunchedEffect(routineId) {
        if (routineId.isBlank()) return@LaunchedEffect
        try {
            val db = Firebase.firestore
            val snapshot = db.collection("routine_exercises")
                .whereEqualTo("routineID", routineId)
                .orderBy("orden") // ¡Muy importante!
                .get()
                .await()
            steps = snapshot.toObjects<RoutineExercise>()
        } catch (e: Exception) {
            // --- AÑADIDO (para ver errores) ---
            Log.e("RoutinePlayer", "Error al cargar los pasos: ${e.message}", e)
        }
    }

    // ... (El LaunchedEffect de playerState no cambia) ...
    LaunchedEffect(playerState) {
        if (playerState is PlayerState.Resting) {
            val duration = (playerState as PlayerState.Resting).duration
            remainingTime = duration
            isRestButtonEnabled = false // Deshabilita el botón

            // Inicia la cuenta regresiva
            while (remainingTime > 0) {
                delay(1000L) // Espera 1 segundo
                remainingTime--
            }

            // Cuando el timer llega a 0, habilita el botón
            isRestButtonEnabled = true
        }
    }

    val currentStep = steps.getOrNull(currentStepIndex)

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Rutina en Curso") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.ArrowBack, "Volver")
                    }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier.padding(padding).fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            if (currentStep == null) {
                CircularProgressIndicator()
            } else {
                // ... (El when(state) no cambia) ...
                when (val state = playerState) {
                    is PlayerState.Exercising -> {
                        Text(currentStep.exerciseNombre, style = MaterialTheme.typography.headlineLarge)
                        Text("${currentStep.series} Series x ${currentStep.repeticiones} Reps", style = MaterialTheme.typography.headlineMedium)
                    }
                    is PlayerState.Resting -> {
                        Text("DESCANSO", style = MaterialTheme.typography.headlineLarge)
                        Text("$remainingTime segundos", style = MaterialTheme.typography.headlineMedium)
                    }
                    is PlayerState.Finished -> {
                        Text("¡RUTINA COMPLETA!", style = MaterialTheme.typography.headlineLarge)
                        Button(onClick = onBack) { Text("Finalizar") }
                    }
                }

                Spacer(Modifier.height(32.dp))

                if (playerState != PlayerState.Finished) {

                    // ... (El isButtonEnabled no cambia) ...
                    val isButtonEnabled = when (playerState) {
                        is PlayerState.Exercising -> true
                        is PlayerState.Resting -> isRestButtonEnabled
                        else -> false
                    }

                    Button(
                        onClick = {
                            if (playerState is PlayerState.Exercising) {
                                playerState = PlayerState.Resting(currentStep.tiempoDescansoSegundos)
                            } else {
                                if (currentStepIndex < steps.size - 1) {
                                    currentStepIndex++
                                    playerState = PlayerState.Exercising
                                } else {
                                    // --- LÓGICA DE HISTORIAL RE-AÑADIDA ---
                                    playerState = PlayerState.Finished

                                    if (user != null) {
                                        val historyEntry = hashMapOf(
                                            "userId" to user.uid,
                                            "routineId" to routineId,
                                            // Usamos el nombre del primer ejercicio como nombre de rutina (o puedes mejorarlo)
                                            "routineName" to (steps.firstOrNull()?.exerciseNombre ?: "Rutina"),
                                            "timestamp" to com.google.firebase.firestore.FieldValue.serverTimestamp()
                                        )

                                        db.collection("routine_completions")
                                            .add(historyEntry)
                                            .addOnSuccessListener { Log.d("RoutinePlayer", "Historial guardado") }
                                            .addOnFailureListener { e -> Log.w("RoutinePlayer", "Error al guardar historial", e) }
                                    }
                                    // --- FIN DE LA LÓGICA ---
                                }
                            }
                        },
                        enabled = isButtonEnabled
                    ) {
                        val text = if (playerState is PlayerState.Exercising) "Hecho (Descansar)" else "Siguiente Ejercicio"
                        Text(text)
                    }
                }
            }
        }
    }
}