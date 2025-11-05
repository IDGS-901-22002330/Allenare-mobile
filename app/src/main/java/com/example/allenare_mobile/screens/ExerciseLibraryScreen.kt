package com.example.allenare_mobile.screens

import android.util.Log
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.SportsGymnastics
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.allenare_mobile.model.Exercise
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.firestore.ktx.toObjects
import com.google.firebase.ktx.Firebase
import kotlinx.coroutines.tasks.await

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ExerciseLibraryScreen(
    onNavigateToDetail: (String) -> Unit,
    onBack: () -> Unit
) {
    // 1. Estados para guardar la lista de ejercicios y el texto de búsqueda
    var allExercises by remember { mutableStateOf<List<Exercise>>(emptyList()) }
    var searchText by remember { mutableStateOf("") }
    var isLoading by remember { mutableStateOf(true) }

    // 2. Carga los datos de Firestore UNA SOLA VEZ cuando la pantalla se inicia
    LaunchedEffect(Unit) {
        try {
            val db = Firebase.firestore
            val snapshot = db.collection("exercises")
                .get()
                .await()
            allExercises = snapshot.toObjects<Exercise>()
        } catch (e: Exception) {
            Log.e("ExerciseLibrary", "Error al cargar ejercicios", e)
        } finally {
            isLoading = false
        }
    }

    // 3. Filtra la lista dinámicamente basado en el texto de búsqueda
    val filteredExercises by remember(allExercises, searchText) {
        derivedStateOf {
            if (searchText.isBlank()) {
                allExercises
            } else {
                // Filtra por nombre O por grupo muscular
                allExercises.filter {
                    it.nombre.contains(searchText, ignoreCase = true) ||
                            it.grupoMuscular.contains(searchText, ignoreCase = true)
                }
            }
        }
    }

    // 4. UI de la pantalla
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Biblioteca de Ejercicios") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.ArrowBack, "Volver")
                    }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .padding(padding)
                .fillMaxSize()
        ) {
            // Buscador (RFM-10)
            TextField(
                value = searchText,
                onValueChange = { searchText = it },
                label = { Text("Buscar por nombre o músculo...") },
                leadingIcon = { Icon(Icons.Default.Search, null) },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp)
            )

            if (isLoading) {
                LinearProgressIndicator(modifier = Modifier.fillMaxWidth())
            }

            // Lista de Ejercicios (RFM-10)
            LazyColumn(modifier = Modifier.fillMaxWidth()) {
                items(filteredExercises) { exercise ->
                    ListItem(
                        headlineContent = { Text(exercise.nombre) },
                        supportingContent = { Text(exercise.grupoMuscular) },
                        leadingContent = { Icon(Icons.Default.SportsGymnastics, null) },
                        modifier = Modifier.clickable {
                            // Navegación (RFM-10)
                            onNavigateToDetail(exercise.exerciseID)
                        }
                    )
                    Divider()
                }
            }
        }
    }
}