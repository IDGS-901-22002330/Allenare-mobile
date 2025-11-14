package com.example.allenare_mobile.screens

import android.content.Intent
import android.net.Uri
import android.util.Log
import android.widget.Toast
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.example.allenare_mobile.model.Exercise
import com.google.firebase.auth.ktx.auth
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

    // --- NUEVO: ESTADO PARA EL CONTADOR DE SETS ---
    var setCount by remember { mutableStateOf(0) }
    val context = LocalContext.current
    val db = Firebase.firestore
    val user = Firebase.auth.currentUser
    // ---------------------------------------------

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

    // Carga los datos del ejercicio (sin cambios)
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

                // Lógica para mostrar video/imagen (sin cambios)
                val url = exercise!!.mediaURL
                if (currentVideoId != null) {
                    // ... (El código de tu botón de YouTube se queda aquí)
                    val openYouTubeIntent = remember(url) {
                        Intent(Intent.ACTION_VIEW, Uri.parse(url))
                    }
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 24.dp),
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

                // Detalles del ejercicio (sin cambios)
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

                // --- NUEVO: EL TRACKER DE SETS ---
                Divider(modifier = Modifier.padding(horizontal = 16.dp, vertical = 24.dp))

                Column(
                    modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        "¿Cuántos sets completaste?",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(Modifier.height(16.dp))

                    // Contador
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        // Botón de menos
                        IconButton(
                            onClick = { if (setCount > 0) setCount-- },
                            enabled = setCount > 0
                        ) {
                            Icon(Icons.Default.Remove, "Quitar set")
                        }

                        // Número
                        Text(
                            text = "$setCount",
                            style = MaterialTheme.typography.headlineLarge,
                            fontWeight = FontWeight.Bold
                        )

                        // Botón de más
                        IconButton(onClick = { setCount++ }) {
                            Icon(Icons.Default.Add, "Añadir set")
                        }
                    }

                    Spacer(Modifier.height(24.dp))

                    // Botón de guardar
                    Button(
                        onClick = {
                            if (user != null && exercise != null) {
                                // 1. Creamos el objeto para el historial
                                val logEntry = hashMapOf(
                                    "userId" to user.uid,
                                    "exerciseId" to exercise!!.exerciseID,
                                    "exerciseName" to exercise!!.nombre,
                                    "sets" to setCount,
                                    "timestamp" to com.google.firebase.firestore.FieldValue.serverTimestamp()
                                )

                                // 2. Lo guardamos en la nueva colección
                                db.collection("exercise_logs")
                                    .add(logEntry)
                                    .addOnSuccessListener {
                                        Toast.makeText(context, "¡Ejercicio guardado!", Toast.LENGTH_SHORT).show()
                                        setCount = 0 // Resetea el contador
                                        onBack() // Vuelve a la lista
                                    }
                                    .addOnFailureListener { e ->
                                        Toast.makeText(context, "Error: ${e.message}", Toast.LENGTH_SHORT).show()
                                    }
                            }
                        },
                        modifier = Modifier.fillMaxWidth().height(50.dp),
                        enabled = setCount > 0 // Solo se puede guardar si es más de 0
                    ) {
                        Text("Guardar Entrenamiento")
                    }
                    Spacer(Modifier.height(32.dp))
                }
                // --- FIN DEL TRACKER ---
            }
        }
    }
}