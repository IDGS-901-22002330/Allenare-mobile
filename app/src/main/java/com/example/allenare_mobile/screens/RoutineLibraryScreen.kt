package com.example.allenare_mobile.screens

import android.app.AlarmManager
import android.app.PendingIntent
import android.app.TimePickerDialog
import android.content.Context
import android.content.Intent
import android.os.Build
import android.provider.Settings
import android.util.Log
import android.widget.Toast
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccessTime
import androidx.compose.material.icons.filled.EmojiEvents // Icono de Copa para Retos
import androidx.compose.material.icons.filled.FitnessCenter
import androidx.compose.material.icons.filled.ListAlt // Icono de Lista para Recordatorios
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import com.example.allenare_mobile.model.Routine
import com.example.allenare_mobile.utils.ReminderReceiver
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.firestore.ktx.toObjects
import com.google.firebase.ktx.Firebase
import kotlinx.coroutines.tasks.await
import java.util.Calendar

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RoutineLibraryScreen(
    onNavigateToPlayer: (String) -> Unit,
    onNavigateToExercises: () -> Unit,
    onNavigateToReminders: () -> Unit,
    onNavigateToChallenges: () -> Unit
) {
    // Estados para las listas
    var genericRoutines by remember { mutableStateOf<List<Routine>>(emptyList()) }
    var assignedRoutines by remember { mutableStateOf<List<Routine>>(emptyList()) }

    var isLoading by remember { mutableStateOf(true) }
    var selectedTabIndex by remember { mutableStateOf(0) } // 0: Generales, 1: Asignadas

    val user = Firebase.auth.currentUser
    val context = LocalContext.current
    val db = Firebase.firestore

    // --- FUNCIÓN PARA PROGRAMAR ALARMA ---
    fun scheduleNotification(routineName: String, hour: Int, minute: Int) {
        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager

        // Verificar permiso en Android 12+
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            if (!alarmManager.canScheduleExactAlarms()) {
                context.startActivity(Intent(Settings.ACTION_REQUEST_SCHEDULE_EXACT_ALARM))
                return
            }
        }

        // Preparar la intención
        val intent = Intent(context, ReminderReceiver::class.java).apply {
            putExtra("routineName", routineName)
        }

        val pendingIntent = PendingIntent.getBroadcast(
            context,
            routineName.hashCode(),
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        // Configurar la hora
        val calendar = Calendar.getInstance().apply {
            set(Calendar.HOUR_OF_DAY, hour)
            set(Calendar.MINUTE, minute)
            set(Calendar.SECOND, 0)
        }

        // Si la hora ya pasó hoy, programar para mañana
        if (calendar.timeInMillis <= System.currentTimeMillis()) {
            calendar.add(Calendar.DAY_OF_MONTH, 1)
        }

        try {
            // 1. Programar alarma exacta
            alarmManager.setExactAndAllowWhileIdle(
                AlarmManager.RTC_WAKEUP,
                calendar.timeInMillis,
                pendingIntent
            )

            // 2. Guardar en Firestore (para consultar en pantalla de Recordatorios)
            if (user != null) {
                val reminderData = hashMapOf(
                    "routineName" to routineName,
                    "hour" to hour,
                    "minute" to minute,
                    "timestamp" to com.google.firebase.firestore.FieldValue.serverTimestamp()
                )
                db.collection("users")
                    .document(user.uid)
                    .collection("reminders")
                    .add(reminderData)
            }

            Toast.makeText(context, "Recordatorio: ${hour}:${String.format("%02d", minute)}", Toast.LENGTH_SHORT).show()

        } catch (e: SecurityException) {
            Toast.makeText(context, "Falta permiso de alarma", Toast.LENGTH_SHORT).show()
        }
    }

    // Diálogo del reloj
    val showTimePicker = { routineName: String ->
        val cal = Calendar.getInstance()
        TimePickerDialog(
            context,
            { _, h, m -> scheduleNotification(routineName, h, m) },
            cal.get(Calendar.HOUR_OF_DAY),
            cal.get(Calendar.MINUTE),
            true
        ).show()
    }

    // --- CARGA DE DATOS ---
    LaunchedEffect(user) {
        if (user == null) {
            isLoading = false
            return@LaunchedEffect
        }
        try {
            // 1. Rutinas Generales (Predefinidas)
            val genSnap = db.collection("routines")
                .whereEqualTo("tipo", "predefinida")
                .get()
                .await()
            genericRoutines = genSnap.toObjects()

            // 2. Rutinas Asignadas (Donde userID es el mío)
            val assignSnap = db.collection("routines")
                .whereEqualTo("userID", user.uid)
                .get()
                .await()
            assignedRoutines = assignSnap.toObjects()

        } catch (e: Exception) {
            // Manejo de error silencioso o log
        } finally {
            isLoading = false
        }
    }

    // --- UI PRINCIPAL ---
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Rutinas") },
                actions = {
                    // Botón Retos
                    IconButton(onClick = onNavigateToChallenges) {
                        Icon(Icons.Default.EmojiEvents, "Retos", tint = Color(0xFFFFC107))
                    }
                    // Botón Recordatorios
                    IconButton(onClick = onNavigateToReminders) {
                        Icon(Icons.Default.ListAlt, "Mis Recordatorios")
                    }
                    // Botón Ejercicios
                    TextButton(onClick = onNavigateToExercises) {
                        Text("Ejercicios")
                    }
                }
            )
        }
    ) { padding ->
        Column(modifier = Modifier.padding(padding)) {

            // Tabs para cambiar entre listas
            TabRow(selectedTabIndex = selectedTabIndex) {
                Tab(
                    selected = selectedTabIndex == 0,
                    onClick = { selectedTabIndex = 0 },
                    text = { Text("Generales") }
                )
                Tab(
                    selected = selectedTabIndex == 1,
                    onClick = { selectedTabIndex = 1 },
                    text = { Text("Mis Rutinas") }
                )
            }

            if (isLoading) {
                LinearProgressIndicator(modifier = Modifier.fillMaxWidth())
            } else {
                // Seleccionar qué lista mostrar
                val listToShow = if (selectedTabIndex == 0) genericRoutines else assignedRoutines

                if (listToShow.isEmpty()) {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = androidx.compose.ui.Alignment.Center
                    ) {
                        Text(
                            if (selectedTabIndex == 0) "No hay rutinas generales" else "No tienes rutinas asignadas",
                            color = androidx.compose.ui.graphics.Color.Gray
                        )
                    }
                } else {
                    LazyColumn(modifier = Modifier.fillMaxWidth()) {
                        items(listToShow) { routine ->
                            ListItem(
                                headlineContent = { Text(routine.nombre) },
                                leadingContent = { Icon(Icons.Default.FitnessCenter, null) },
                                trailingContent = {
                                    // Botón para programar alarma
                                    IconButton(onClick = { showTimePicker(routine.nombre) }) {
                                        Icon(
                                            Icons.Default.AccessTime,
                                            "Programar",
                                            tint = MaterialTheme.colorScheme.primary
                                        )
                                    }
                                },
                                modifier = Modifier.clickable {
                                    // Validación para evitar crash si no hay ID
                                    if (routine.routineID.isNotBlank()) {
                                        onNavigateToPlayer(routine.routineID)
                                    } else {
                                        // Si no pasa nada, es porque entra aquí.
                                        // Probablemente tus rutinas asignadas en Firestore NO tienen el campo 'routineID' relleno.
                                        Log.e("RoutineLibrary", "Error: Rutina sin ID")
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