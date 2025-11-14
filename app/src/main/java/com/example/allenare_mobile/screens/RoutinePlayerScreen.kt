package com.example.allenare_mobile.screens

import android.util.Log
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.allenare_mobile.model.RoutineExercise
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.firestore.ktx.toObjects
import com.google.firebase.ktx.Firebase
import kotlinx.coroutines.delay
import kotlinx.coroutines.tasks.await

// Estado del reproductor
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

    // --- Estados para el cronómetro ---
    var remainingTime by remember { mutableStateOf(0) }
    var isRestButtonEnabled by remember { mutableStateOf(false) }

    // --- Carga los "pasos" de la rutina ---
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
            Log.e("RoutinePlayer", "Error al cargar pasos", e)
        }
    }

    // --- LaunchedEffect para el cronómetro ---
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

    // --- Obtenemos la BD y el usuario para el historial ---
    val db = Firebase.firestore
    val user = Firebase.auth.currentUser

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
                // Muestra el indicador de carga si los pasos aún no están listos
                CircularProgressIndicator()
            } else {
                // UI principal cuando los pasos están cargados
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

                // Muestra el botón solo si la rutina no ha terminado
                if (playerState != PlayerState.Finished) {

                    // Lógica para habilitar/deshabilitar el botón
                    val isButtonEnabled = when (playerState) {
                        is PlayerState.Exercising -> true // Botón de "Hecho" siempre activo
                        is PlayerState.Resting -> isRestButtonEnabled // Botón de "Siguiente" depende del timer
                        else -> false
                    }

                    Button(
                        onClick = {
                            // Lógica para avanzar en la rutina
                            if (playerState is PlayerState.Exercising) {
                                // Si estaba ejercitando, pasa a descanso
                                playerState = PlayerState.Resting(currentStep.tiempoDescansoSegundos)
                            } else {
                                // Si estaba descansando, pasa al siguiente ejercicio o finaliza
                                if (currentStepIndex < steps.size - 1) {
                                    currentStepIndex++
                                    playerState = PlayerState.Exercising
                                } else {
                                    // --- ¡MOMENTO DE FINALIZAR Y GUARDAR! ---
                                    playerState = PlayerState.Finished

                                    if (user != null) {
                                        // Creamos el objeto para el historial
                                        val historyEntry = hashMapOf(
                                            "userId" to user.uid,
                                            "routineId" to routineId,
                                            // Asumimos que todos los pasos tienen el mismo nombre de rutina,
                                            // o deberías pasar el nombre de la rutina desde la pantalla anterior.
                                            "routineName" to (steps.firstOrNull()?.exerciseNombre?.substringBefore(" ") ?: "Rutina"), // Un placeholder
                                            "timestamp" to com.google.firebase.firestore.FieldValue.serverTimestamp()
                                        )

                                        // Lo guardamos en la nueva colección
                                        db.collection("routine_completions")
                                            .add(historyEntry)
                                            .addOnSuccessListener { Log.d("RoutinePlayer", "Historial guardado") }
                                            .addOnFailureListener { e -> Log.w("RoutinePlayer", "Error al guardar historial", e) }
                                    }
                                    // --- FIN DE GUARDADO ---
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