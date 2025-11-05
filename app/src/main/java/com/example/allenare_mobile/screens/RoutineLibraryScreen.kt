package com.example.allenare_mobile.screens

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.FitnessCenter
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import com.example.allenare_mobile.model.Routine
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.firestore.ktx.toObjects
import com.google.firebase.ktx.Firebase
import kotlinx.coroutines.tasks.await

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RoutineLibraryScreen(
    onNavigateToPlayer: (String) -> Unit,
    onNavigateToExercises: () -> Unit
) {
    var routines by remember { mutableStateOf<List<Routine>>(emptyList()) }
    var isLoading by remember { mutableStateOf(true) }
    val user = Firebase.auth.currentUser

    // Carga las rutinas (predefinidas Y personales)
    LaunchedEffect(user) {
        if (user == null) {
            isLoading = false
            return@LaunchedEffect
        }
        try {
            val db = Firebase.firestore

            // 1. Rutinas predefinidas
            val predefinedSnap = db.collection("routines")
                .whereEqualTo("tipo", "predefinida")
                .get()
                .await()
            val predefined = predefinedSnap.toObjects<Routine>()

            // 2. Rutinas personales
            val personalSnap = db.collection("routines")
                .whereEqualTo("userID", user.uid)
                .get()
                .await()
            val personal = personalSnap.toObjects<Routine>()

            routines = predefined + personal
        } catch (e: Exception) {
            // Manejar error
        } finally {
            isLoading = false
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Biblioteca de Rutinas") },
                actions = {
                    TextButton(onClick = onNavigateToExercises) {
                        Text("Ejercicios") // Link a RFM-10
                    }
                }
            )
        }
    ) { padding ->
        Column(modifier = Modifier.padding(padding)) {
            if (isLoading) {
                LinearProgressIndicator(modifier = Modifier.fillMaxWidth())
            }
            LazyColumn(modifier = Modifier.fillMaxWidth()) {
                items(routines) { routine ->
                    ListItem(
                        headlineContent = { Text(routine.nombre) },
                        supportingContent = { Text(if (routine.tipo == "predefinida") "Recomendada" else "Personal") },
                        leadingContent = { Icon(Icons.Default.FitnessCenter, null) },
                        modifier = Modifier.clickable {
                            onNavigateToPlayer(routine.routineID) // Navega al reproductor
                        }
                    )
                }
            }
        }
    }
}