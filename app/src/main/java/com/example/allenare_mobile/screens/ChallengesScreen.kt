package com.example.allenare_mobile.screens

import android.widget.Toast
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.EmojiEvents
import androidx.compose.material.icons.filled.PlayCircle
import androidx.compose.material.icons.filled.Done // Nuevo icono
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import kotlinx.coroutines.tasks.await
import java.text.SimpleDateFormat
import java.util.Locale

// Modelo local para la UI
data class ChallengeUI(
    val id: String, // ID del reto original o del user_challenge
    val userChallengeId: String?, // ID específico de la participación (para completar)
    val originalChallengeId: String,
    val nombre: String,
    val descripcion: String,
    val fechaFin: java.util.Date?,
    val status: String, // "disponible", "activo", "completado"
    val tipoOrigen: String // "comunitario" o "asignado"
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChallengesScreen(onBack: () -> Unit) {
    // Listas para las 3 pestañas
    var availableChallenges by remember { mutableStateOf<List<ChallengeUI>>(emptyList()) }
    var activeChallenges by remember { mutableStateOf<List<ChallengeUI>>(emptyList()) }
    var completedChallenges by remember { mutableStateOf<List<ChallengeUI>>(emptyList()) }

    var selectedTabIndex by remember { mutableStateOf(0) }
    var isLoading by remember { mutableStateOf(true) }

    val db = Firebase.firestore
    val user = Firebase.auth.currentUser
    val context = LocalContext.current

    // --- FUNCIÓN PARA UNIRSE ---
    fun joinChallenge(challenge: ChallengeUI) {
        if (user == null) return

        val participationData = hashMapOf(
            "userId" to user.uid,
            "challengeId" to challenge.originalChallengeId,
            "challengeName" to challenge.nombre,
            "challengeDescription" to challenge.descripcion,
            "status" to "activo",
            "progreso" to 0,
            "fechaUnio" to com.google.firebase.firestore.FieldValue.serverTimestamp()
        )

        db.collection("user_challenges")
            .add(participationData)
            .addOnSuccessListener { docRef ->
                Toast.makeText(context, "¡Te uniste a ${challenge.nombre}!", Toast.LENGTH_SHORT).show()
                // Mover localmente a "En Curso"
                availableChallenges = availableChallenges.filter { it.id != challenge.id }
                // Creamos una copia con el nuevo ID de participación y estado activo
                val newActive = challenge.copy(status = "activo", userChallengeId = docRef.id)
                activeChallenges = activeChallenges + newActive
                selectedTabIndex = 1
            }
    }

    // --- NUEVA FUNCIÓN: COMPLETAR RETO ---
    fun completeChallenge(challenge: ChallengeUI) {
        if (challenge.userChallengeId == null) return

        db.collection("user_challenges").document(challenge.userChallengeId)
            .update("status", "completado", "fechaCompletado", com.google.firebase.firestore.FieldValue.serverTimestamp())
            .addOnSuccessListener {
                Toast.makeText(context, "¡Felicidades! Reto completado.", Toast.LENGTH_LONG).show()
                // Mover localmente a "Logrados"
                activeChallenges = activeChallenges.filter { it.id != challenge.id }
                val newCompleted = challenge.copy(status = "completado")
                completedChallenges = completedChallenges + newCompleted
                selectedTabIndex = 2 // Llevar a la pestaña de Logrados
            }
    }
    // -------------------------------------

    LaunchedEffect(Unit) {
        if (user != null) {
            try {
                // 1. Obtener mis retos (Activos y Completados)
                val myChallengesSnap = db.collection("user_challenges")
                    .whereEqualTo("userId", user.uid)
                    .get().await()

                val myParticipationIds = mutableSetOf<String>()
                val myActiveList = mutableListOf<ChallengeUI>()
                val myCompletedList = mutableListOf<ChallengeUI>()

                for (doc in myChallengesSnap) {
                    val originalId = doc.getString("challengeId") ?: ""
                    val status = doc.getString("status") ?: "activo"
                    myParticipationIds.add(originalId)

                    val uiModel = ChallengeUI(
                        id = doc.id, // Este es el ID del documento de participación
                        userChallengeId = doc.id, // Guardamos el ID para poder actualizarlo luego
                        originalChallengeId = originalId,
                        nombre = doc.getString("challengeName") ?: "",
                        descripcion = doc.getString("challengeDescription") ?: "",
                        fechaFin = null,
                        status = status,
                        tipoOrigen = "n/a"
                    )

                    if (status == "completado") {
                        myCompletedList.add(uiModel)
                    } else {
                        myActiveList.add(uiModel)
                    }
                }
                activeChallenges = myActiveList
                completedChallenges = myCompletedList

                // 2. Obtener retos disponibles
                val availableList = mutableListOf<ChallengeUI>()

                // A) Comunitarios
                val commSnap = db.collection("challenges")
                    .whereEqualTo("tipo", "comunitario")
                    .get().await()

                for (doc in commSnap) {
                    if (!myParticipationIds.contains(doc.id)) {
                        availableList.add(ChallengeUI(
                            id = doc.id,
                            userChallengeId = null,
                            originalChallengeId = doc.id,
                            nombre = doc.getString("nombre") ?: "",
                            descripcion = doc.getString("descripcion") ?: "",
                            fechaFin = doc.getTimestamp("fechaFin")?.toDate(),
                            status = "disponible",
                            tipoOrigen = "Comunitario"
                        ))
                    }
                }

                // B) Asignados a Mí
                val assignSnap = db.collection("challenges")
                    .whereEqualTo("tipo", "asignado")
                    .whereEqualTo("assignedUserID", user.uid)
                    .get().await()

                for (doc in assignSnap) {
                    if (!myParticipationIds.contains(doc.id)) {
                        availableList.add(ChallengeUI(
                            id = doc.id,
                            userChallengeId = null,
                            originalChallengeId = doc.id,
                            nombre = doc.getString("nombre") ?: "",
                            descripcion = doc.getString("descripcion") ?: "",
                            fechaFin = doc.getTimestamp("fechaFin")?.toDate(),
                            status = "disponible",
                            tipoOrigen = "Asignado Personalmente"
                        ))
                    }
                }

                availableChallenges = availableList

            } catch (e: Exception) {
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

            ScrollableTabRow(selectedTabIndex = selectedTabIndex, edgePadding = 0.dp) {
                Tab(selected = selectedTabIndex == 0, onClick = { selectedTabIndex = 0 }, text = { Text("Por Unirme") })
                Tab(selected = selectedTabIndex == 1, onClick = { selectedTabIndex = 1 }, text = { Text("En Curso") })
                Tab(selected = selectedTabIndex == 2, onClick = { selectedTabIndex = 2 }, text = { Text("Logrados") })
            }

            if (isLoading) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator()
                }
            } else {
                val listToShow = when(selectedTabIndex) {
                    0 -> availableChallenges
                    1 -> activeChallenges
                    2 -> completedChallenges
                    else -> emptyList()
                }

                if (listToShow.isEmpty()) {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        val msg = when(selectedTabIndex) {
                            0 -> "No hay retos nuevos disponibles."
                            1 -> "No tienes retos activos. ¡Únete a uno!"
                            2 -> "Aún no has completado ningún reto."
                            else -> ""
                        }
                        Text(msg, color = Color.Gray)
                    }
                } else {
                    LazyColumn {
                        items(listToShow) { challenge ->
                            val dateFormat = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
                            val fechaStr = if(challenge.fechaFin != null) "Termina: ${dateFormat.format(challenge.fechaFin)}" else ""

                            ListItem(
                                headlineContent = {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Text(challenge.nombre, fontWeight = FontWeight.Bold)
                                        if (challenge.tipoOrigen == "Asignado Personalmente") {
                                            Spacer(Modifier.width(8.dp))
                                            Badge(containerColor = MaterialTheme.colorScheme.tertiary) { Text("Para Ti") }
                                        }
                                    }
                                },
                                supportingContent = {
                                    Column {
                                        Text(challenge.descripcion, maxLines = 2)
                                        if (fechaStr.isNotEmpty()) Text(fechaStr, style = MaterialTheme.typography.bodySmall, color = Color.Gray)
                                    }
                                },
                                leadingContent = {
                                    val icon = when(challenge.status) {
                                        "completado" -> Icons.Default.EmojiEvents
                                        "activo" -> Icons.Default.PlayCircle
                                        else -> Icons.Default.CheckCircle
                                    }
                                    val tint = if (challenge.status == "completado") Color(0xFFFFC107) else MaterialTheme.colorScheme.primary
                                    Icon(icon, null, tint = tint)
                                },
                                trailingContent = {
                                    // --- BOTONES SEGÚN ESTADO ---
                                    if (challenge.status == "disponible") {
                                        Button(
                                            onClick = { joinChallenge(challenge) },
                                            contentPadding = PaddingValues(horizontal = 12.dp, vertical = 4.dp)
                                        ) {
                                            Text("Unirme")
                                        }
                                    } else if (challenge.status == "activo") {
                                        // AHORA: Botón para completar
                                        Button(
                                            onClick = { completeChallenge(challenge) },
                                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF4CAF50)), // Verde
                                            contentPadding = PaddingValues(horizontal = 12.dp, vertical = 4.dp)
                                        ) {
                                            Icon(Icons.Default.Done, null, modifier = Modifier.size(16.dp))
                                            Spacer(Modifier.width(4.dp))
                                            Text("Completar")
                                        }
                                    } else {
                                        Text("¡Hecho!", color = Color(0xFFFFC107), style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.Bold)
                                    }
                                }
                            )
                            Divider()
                        }
                    }
                }
            }
        }
    }
}