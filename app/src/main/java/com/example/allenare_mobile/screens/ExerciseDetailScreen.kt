package com.example.allenare_mobile.screens

import android.content.Intent
import android.net.Uri
import android.util.Log
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.AsyncImage
import com.example.allenare_mobile.model.Exercise
import com.example.gemini_ai.GeminiViewModel
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.firestore.ktx.toObject
import com.google.firebase.ktx.Firebase
import kotlinx.coroutines.tasks.await

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ExerciseDetailScreen(
    exerciseId: String,
    onBack: () -> Unit,
    geminiViewModel: GeminiViewModel = viewModel()
) {
    var exercise by remember { mutableStateOf<Exercise?>(null) }
    var isLoading by remember { mutableStateOf(true) }
    var currentVideoId by remember { mutableStateOf<String?>(null) }

    // State for Gemini AI feature
    var userQuestion by remember { mutableStateOf("") }
    val aiResponse by geminiViewModel.aiResponse.collectAsState()
    val isAiLoading by geminiViewModel.isLoading.collectAsState()

    // Esta función extrae el ID del video (sin cambios)
    fun extractVideoId(url: String): String? {
        return try {
            val uri = java.net.URI(url)
            if (uri.host.contains("youtube.com")) {
                if (uri.path.contains("shorts")) {
                    uri.path.split("/").lastOrNull { it.isNotBlank() }
                } else {
                    uri.query.split("&").firstOrNull { it.startsWith("v=") }?.substring(2)
                }
            } else if (uri.host.contains("youtu.be")) {
                uri.path.substring(1)
            } else {
                null
            }
        } catch (_: Exception) {
            null
        }
    }

    LaunchedEffect(exerciseId) {
        if (exerciseId.isBlank()) {
            isLoading = false
            return@LaunchedEffect
        }
        try {
            val db = Firebase.firestore
            val snapshot = db.collection("exercises").document(exerciseId).get().await()
            val loadedExercise = snapshot.toObject<Exercise>()
            exercise = loadedExercise

            if (loadedExercise != null) {
                currentVideoId = extractVideoId(loadedExercise.mediaURL)
            }

        } catch (e: Exception) {
            Log.e("ExerciseDetail", "Error al cargar ejercicio", e)
        } finally {
            isLoading = false
        }
    }


    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(exercise?.nombre ?: "Cargando...") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, "Volver")
                    }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .padding(padding)
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
        ) {
            if (isLoading) {
                LinearProgressIndicator(modifier = Modifier.fillMaxWidth())
            } else if (exercise == null) {
                Text("Ejercicio no encontrado.", modifier = Modifier.padding(16.dp))
            } else {

                val url = exercise!!.mediaURL
                if (currentVideoId != null) {
                    val context = LocalContext.current
                    val openYouTubeIntent = remember(url) {
                        Intent(Intent.ACTION_VIEW, Uri.parse(url))
                    }
                    Column(
                        modifier = Modifier
                            .fillMaxWidth(),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Button(onClick = {
                            try {
                                context.startActivity(openYouTubeIntent)
                            } catch (e: Exception) {
                                Log.e("OpenLink", "No se pudo abrir el enlace: $url", e)
                            }
                        }) {
                            Text("Ver video en YouTube")
                        }
                    }
                } else if (url.isNotBlank()) {
                    AsyncImage(
                        model = url,
                        contentDescription = "Imagen del ejercicio",
                        modifier = Modifier.fillMaxWidth().aspectRatio(16 / 9f),
                        contentScale = ContentScale.Crop
                    )
                }

                Column(Modifier.padding(16.dp)) {
                    Text(
                        text = exercise!!.nombre,
                        style = MaterialTheme.typography.headlineMedium
                    )
                    Spacer(Modifier.height(8.dp))
                    Text(
                        text = exercise!!.grupoMuscular,
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.primary
                    )
                    Spacer(Modifier.height(16.dp))
                    Text(
                        text = exercise!!.descripcion,
                        style = MaterialTheme.typography.bodyLarge
                    )

                    // --- Start of Gemini AI Section ---
                    Spacer(Modifier.height(24.dp))
                    Divider()
                    Spacer(Modifier.height(16.dp))

                    Text(
                        "Asistente IA de Gemini",
                        style = MaterialTheme.typography.headlineSmall
                    )
                    Spacer(Modifier.height(8.dp))

                    OutlinedTextField(
                        value = userQuestion,
                        onValueChange = { userQuestion = it },
                        label = { Text("Pregúntale a Gemini sobre este ejercicio...") },
                        modifier = Modifier.fillMaxWidth()
                    )
                    Spacer(Modifier.height(8.dp))

                    Button(
                        onClick = {
                            if (userQuestion.isNotBlank()) {
                                geminiViewModel.askQuestion(exercise!!.nombre, userQuestion)
                            }
                        },
                        modifier = Modifier.align(Alignment.End),
                        enabled = !isAiLoading && userQuestion.isNotBlank()
                    ) {
                        Text("Enviar")
                    }

                    Spacer(Modifier.height(16.dp))

                    if (isAiLoading) {
                        Box(modifier = Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
                            CircularProgressIndicator()
                        }
                    }

                    aiResponse?.let {
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            elevation = CardDefaults.cardElevation(2.dp),
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
                        ) {
                            Text(
                                text = it,
                                modifier = Modifier.padding(16.dp),
                                style = MaterialTheme.typography.bodyMedium
                            )
                        }
                    }
                    // --- End of Gemini AI Section ---
                }
            }
        }
    }
}
