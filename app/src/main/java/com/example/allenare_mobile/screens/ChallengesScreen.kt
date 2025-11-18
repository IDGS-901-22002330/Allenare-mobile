package com.example.allenare_mobile.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.EmojiEvents
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import kotlinx.coroutines.tasks.await
import java.text.SimpleDateFormat
import java.util.Locale

// Modelo local simple
data class Challenge(
    val id: String,
    val nombre: String,
    val descripcion: String,
    val fechaFin: java.util.Date?
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChallengesScreen(onBack: () -> Unit) {
    var communityChallenges by remember { mutableStateOf<List<Challenge>>(emptyList()) }
    var assignedChallenges by remember { mutableStateOf<List<Challenge>>(emptyList()) }
    var selectedTabIndex by remember { mutableStateOf(0) }
    var isLoading by remember { mutableStateOf(true) }

    val db = Firebase.firestore
    val user = Firebase.auth.currentUser

    LaunchedEffect(Unit) {
        if (user != null) {
            try {
                // 1. Retos Comunitarios
                val commSnap = db.collection("challenges")
                    .whereEqualTo("tipo", "comunitario") // Asumiendo que el admin pone este campo
                    .get().await()

                communityChallenges = commSnap.documents.map { doc ->
                    Challenge(
                        id = doc.id,
                        nombre = doc.getString("nombre") ?: "",
                        descripcion = doc.getString("descripcion") ?: "",
                        fechaFin = doc.getTimestamp("fechaFin")?.toDate()
                    )
                }

                // 2. Retos Asignados (Directos al usuario)
                val assignSnap = db.collection("challenges")
                    .whereEqualTo("tipo", "asignado")
                    .whereEqualTo("assignedUserID", user.uid)
                    .get().await()

                assignedChallenges = assignSnap.documents.map { doc ->
                    Challenge(
                        id = doc.id,
                        nombre = doc.getString("nombre") ?: "",
                        descripcion = doc.getString("descripcion") ?: "",
                        fechaFin = doc.getTimestamp("fechaFin")?.toDate()
                    )
                }
            } catch (e: Exception) {
                // Manejar error
            } finally {
                isLoading = false
            }
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Retos y Desafíos") },
                navigationIcon = { IconButton(onClick = onBack) { Icon(Icons.AutoMirrored.Filled.ArrowBack, "Volver") } }
            )
        }
    ) { padding ->
        Column(modifier = Modifier.padding(padding)) {
            TabRow(selectedTabIndex = selectedTabIndex) {
                Tab(selected = selectedTabIndex == 0, onClick = { selectedTabIndex = 0 }, text = { Text("Comunidad") })
                Tab(selected = selectedTabIndex == 1, onClick = { selectedTabIndex = 1 }, text = { Text("Asignados a Mí") })
            }

            if (isLoading) {
                LinearProgressIndicator(modifier = Modifier.fillMaxWidth())
            } else {
                val list = if (selectedTabIndex == 0) communityChallenges else assignedChallenges

                if (list.isEmpty()) {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = androidx.compose.ui.Alignment.Center) {
                        Text("No hay retos activos", color = Color.Gray)
                    }
                } else {
                    LazyColumn {
                        items(list) { challenge ->
                            val dateFormat = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
                            val fechaStr = if(challenge.fechaFin != null) "Termina: ${dateFormat.format(challenge.fechaFin)}" else "Sin fecha fin"

                            ListItem(
                                headlineContent = { Text(challenge.nombre) },
                                supportingContent = {
                                    Column {
                                        Text(challenge.descripcion)
                                        Text(fechaStr, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.primary)
                                    }
                                },
                                leadingContent = { Icon(Icons.Default.EmojiEvents, null, tint = Color(0xFFFFC107)) } // Icono dorado
                            )
                            Divider()
                        }
                    }
                }
            }
        }
    }
}