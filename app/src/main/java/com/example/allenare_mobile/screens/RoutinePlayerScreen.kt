package com.example.allenare_mobile.screens

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.allenare_mobile.model.RoutineExercise
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.firestore.ktx.toObjects
import com.google.firebase.ktx.Firebase
import kotlinx.coroutines.delay
import kotlinx.coroutines.tasks.await

// Estado del reproductor (sin cambios)
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

    // --- NUEVO (1): Estados para el cronómetro ---
    // Almacena el tiempo restante que se muestra en pantalla
    var remainingTime by remember { mutableStateOf(0) }
    // Controla si el botón de "Siguiente Ejercicio" está habilitado
    var isRestButtonEnabled by remember { mutableStateOf(false) }
    // ---------------------------------------------

    // Carga los "pasos" de la rutina (sin cambios)
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
            // Manejar error
        }
    }

    // --- NUEVO (2): Un LaunchedEffect para el cronómetro ---
    // Este efecto se dispara CADA VEZ que 'playerState' cambia
    LaunchedEffect(playerState) {
        // Si el estado es "Descansando"
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
    // ----------------------------------------------------

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
                when (val state = playerState) {
                    is PlayerState.Exercising -> {
                        // (Sin cambios)
                        Text(currentStep.exerciseNombre, style = MaterialTheme.typography.headlineLarge)
                        Text("${currentStep.series} Series x ${currentStep.repeticiones} Reps", style = MaterialTheme.typography.headlineMedium)
                    }
                    is PlayerState.Resting -> {
                        Text("DESCANSO", style = MaterialTheme.typography.headlineLarge)

                        // --- MODIFICADO (3): Mostrar el tiempo restante ---
                        // En lugar de la duración total, muestra el estado 'remainingTime'
                        Text("$remainingTime segundos", style = MaterialTheme.typography.headlineMedium)
                        // -------------------------------------------------
                    }
                    is PlayerState.Finished -> {
                        // (Sin cambios)
                        Text("¡RUTINA COMPLETA!", style = MaterialTheme.typography.headlineLarge)
                        Button(onClick = onBack) { Text("Finalizar") }
                    }
                }

                Spacer(Modifier.height(32.dp))

                if (playerState != PlayerState.Finished) {

                    // --- MODIFICADO (4): Lógica de habilitación del botón ---
                    // El botón "Hecho (Descansar)" siempre está habilitado.
                    // El botón "Siguiente Ejercicio" depende de 'isRestButtonEnabled'.
                    val isButtonEnabled = when (playerState) {
                        is PlayerState.Exercising -> true
                        is PlayerState.Resting -> isRestButtonEnabled
                        else -> false
                    }

                    Button(
                        onClick = {
                            // La lógica interna del botón no cambia
                            if (playerState is PlayerState.Exercising) {
                                playerState = PlayerState.Resting(currentStep.tiempoDescansoSegundos)
                            } else {
                                if (currentStepIndex < steps.size - 1) {
                                    currentStepIndex++
                                    playerState = PlayerState.Exercising
                                } else {
                                    playerState = PlayerState.Finished
                                }
                            }
                        },
                        // Aplicamos la nueva lógica de habilitación
                        enabled = isButtonEnabled
                    ) {
                        val text = if (playerState is PlayerState.Exercising) "Hecho (Descansar)" else "Siguiente Ejercicio"
                        Text(text)
                    }
                    // -------------------------------------------------------
                }
            }
        }
    }
}