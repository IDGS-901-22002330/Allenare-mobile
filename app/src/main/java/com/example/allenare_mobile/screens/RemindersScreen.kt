package com.example.allenare_mobile.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Alarm
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import kotlinx.coroutines.tasks.await

data class Reminder(val id: String, val routineName: String, val hour: Int, val minute: Int)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RemindersScreen(onBack: () -> Unit) {
    var reminders by remember { mutableStateOf<List<Reminder>>(emptyList()) }
    val db = Firebase.firestore
    val user = Firebase.auth.currentUser

    // Función para borrar recordatorio
    fun deleteReminder(id: String) {
        if (user != null) {
            db.collection("users").document(user.uid).collection("reminders").document(id).delete()
                .addOnSuccessListener {
                    // Actualizar lista localmente
                    reminders = reminders.filter { it.id != id }
                }
        }
    }

    LaunchedEffect(Unit) {
        if (user != null) {
            val snap = db.collection("users").document(user.uid).collection("reminders").get().await()
            reminders = snap.documents.map { doc ->
                Reminder(
                    id = doc.id,
                    routineName = doc.getString("routineName") ?: "Rutina",
                    hour = doc.getLong("hour")?.toInt() ?: 0,
                    minute = doc.getLong("minute")?.toInt() ?: 0
                )
            }
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Mis Recordatorios Activos") },
                navigationIcon = { IconButton(onClick = onBack) { Icon(Icons.AutoMirrored.Filled.ArrowBack, "Volver") } }
            )
        }
    ) { padding ->
        if (reminders.isEmpty()) {
            Box(modifier = Modifier.padding(padding).fillMaxSize(), contentAlignment = Alignment.Center) {
                Text("No tienes recordatorios activos.")
            }
        } else {
            LazyColumn(modifier = Modifier.padding(padding)) {
                items(reminders) { reminder ->
                    ListItem(
                        headlineContent = { Text(reminder.routineName) },
                        supportingContent = { Text(String.format("%02d:%02d", reminder.hour, reminder.minute)) },
                        leadingContent = { Icon(Icons.Default.Alarm, null) },
                        trailingContent = {
                            IconButton(onClick = { deleteReminder(reminder.id) }) {
                                Icon(Icons.Default.Delete, "Borrar", tint = MaterialTheme.colorScheme.error)
                            }
                        }
                    )
                    Divider()
                }
            }
        }
    }
}