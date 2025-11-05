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
import coil.compose.AsyncImage
import com.example.allenare_mobile.model.Exercise
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.firestore.ktx.toObject
import com.google.firebase.ktx.Firebase
import kotlinx.coroutines.tasks.await

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ExerciseDetailScreen(
    exerciseId: String,
    onBack: () -> Unit
) {
    var exercise by remember { mutableStateOf<Exercise?>(null) }
    var isLoading by remember { mutableStateOf(true) }
    var currentVideoId by remember { mutableStateOf<String?>(null) }

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
                // Sacamos el ID del video y lo guardamos
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

                // --- LÓGICA DE VIDEO ACTUALIZADA CON BOTÓN ---
                val url = exercise!!.mediaURL

                if (currentVideoId != null) {

                    // 1. Obtenemos el contexto para poder lanzar el Intent
                    val context = LocalContext.current

                    // 2. Creamos el "Intent" (la orden de abrir el link)
                    //    Usamos la URL original completa que viene de Firebase
                    val openYouTubeIntent = remember(url) {
                        Intent(Intent.ACTION_VIEW, Uri.parse(url))
                    }

                    // 3. Mostramos el texto y el botón
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 24.dp), // Un poco de espacio
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = "¿Quieres ver cómo hacerlo?",
                            style = MaterialTheme.typography.titleMedium
                        )
                        Spacer(Modifier.height(12.dp))
                        Button(onClick = {
                            try {
                                // 4. Al hacer clic, le decimos al contexto que abra el link
                                context.startActivity(openYouTubeIntent)
                            } catch (e: Exception) {
                                // Por si no tiene app de Youtube o navegador
                                Log.e("OpenLink", "No se pudo abrir el enlace: $url", e)
                            }
                        }) {
                            Text("Ver video en YouTube")
                        }
                    }

                } else if (url.isNotBlank()) {
                    // Si no es un enlace de YouTube, intentamos mostrar una imagen (esto se queda igual)
                    AsyncImage(
                        model = url,
                        contentDescription = "Imagen del ejercicio",
                        modifier = Modifier.fillMaxWidth().aspectRatio(16 / 9f),
                        contentScale = ContentScale.Crop
                    )
                }
                // ---------------------------------

                // El resto de la UI no cambia
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
                }
            }
        }
    }
}