package com.example.allenare_mobile.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.allenare_mobile.model.ActivitySummary
import com.example.allenare_mobile.model.CompletedWorkouts
import com.example.allenare_mobile.model.ExerciseLogs
import com.google.firebase.Timestamp
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.Calendar
import androidx.compose.material3.OutlinedTextField
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.ui.text.input.KeyboardType
import com.example.allenare_mobile.model.User
import android.widget.Toast
import androidx.compose.ui.platform.LocalContext

// Imports de Vico
import com.patrykandpatrick.vico.compose.axis.horizontal.rememberBottomAxis
import com.patrykandpatrick.vico.core.*
import com.patrykandpatrick.vico.compose.*
import com.patrykandpatrick.vico.compose.axis.vertical.rememberStartAxis
import com.patrykandpatrick.vico.compose.chart.Chart
import com.patrykandpatrick.vico.compose.chart.column.columnChart
import com.patrykandpatrick.vico.compose.chart.line.lineChart
import com.patrykandpatrick.vico.compose.style.ProvideChartStyle
import com.patrykandpatrick.vico.core.axis.AxisItemPlacer
import com.patrykandpatrick.vico.core.entry.ChartEntryModelProducer
import com.patrykandpatrick.vico.core.entry.FloatEntry
import com.patrykandpatrick.vico.core.component.text.textComponent
import com.patrykandpatrick.vico.core.entry.entriesOf
import kotlin.math.roundToInt

@Composable
fun ProfileScreen(onLogout: () -> Unit) {

    val weekly = remember { mutableStateOf<List<ActivitySummary>>(emptyList()) }
    val monthly = remember { mutableStateOf<List<ActivitySummary>>(emptyList()) }
    val monthlyExercises = remember { mutableStateOf<Map<String, Int>>(emptyMap()) }
    
    val context = LocalContext.current
    var userProfile by remember { mutableStateOf<User?>(null) }

    LaunchedEffect(Unit) {
        fetchUserProfile { user ->
            userProfile = user
        }
        fetchWeeklyActivity { weekly.value = it }
        fetchMonthlyActivity { monthly.value = it }
        fetchMonthlyExercise { monthlyExercises.value = it }
    }

    //ALTURA DEL HEADER FIJO
    val headerHeight = 70.dp

    Box(
        modifier = Modifier
            .fillMaxSize()
    ) {

        //CONTENIDO SCROLEABLE
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(top = headerHeight, start = 16.dp, end = 16.dp, bottom = 16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {

            item {
                Button(onClick = {
                    Firebase.auth.signOut()
                    onLogout()
                }) {
                    Text("Log Out")
                }
            }

            item {
                userProfile?.let { user ->
                    UserProfileEditor(
                        user = user,
                        onSave = { updatedUser ->
                            updateUserProfile(updatedUser) { success ->
                                if (success) {
                                    Toast.makeText(context, "Perfil actualizado", Toast.LENGTH_SHORT).show()
                                    userProfile = updatedUser
                                } else {
                                    Toast.makeText(context, "Error al actualizar", Toast.LENGTH_SHORT).show()
                                }
                            }
                        }
                    )
                }
            }

            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp)
                ) {
                    ActivityTable()
                }
            }

            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Column(Modifier.padding(16.dp)) {
                        Text("Actividad Semanal")
                        Spacer(Modifier.height(12.dp))
                        if (weekly.value.isNotEmpty()) WeeklyBarChart(weekly.value)
                    }
                }
            }

            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Column(Modifier.padding(16.dp)) {
                        Text("Actividad Mensual")
                        Spacer(Modifier.height(12.dp))
                        if (monthly.value.isNotEmpty()) MonthlyLineChart(monthly.value)
                    }
                }
            }

            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Column(Modifier.padding(16.dp)) {
                        Text("Ejercicios del Mes")
                        Spacer(Modifier.height(12.dp))
                        MonthlyExerciseBarChart(monthlyExercises.value)
                    }
                }
            }

        }



        //HEADER FIJO ARRIBA
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .height(headerHeight)
                .padding(16.dp)
                .align(Alignment.TopCenter)
        ) {
            Text(
                "Estadísticas",
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold
            )
        }
    }
}


@Composable
fun WeeklyBarChart(data: List<ActivitySummary>) {
    val chartEntryModelProducer = remember { ChartEntryModelProducer() }
    val hasData = remember { mutableStateOf(false) }

    LaunchedEffect(data) {
        if (data.isNotEmpty()) {
            val entries = data.mapIndexed { index, item ->
                FloatEntry(index.toFloat(), item.totalSets.toFloat())
            }
            chartEntryModelProducer.setEntries(entries)
            hasData.value = true
        }
    }

    if (hasData.value) {
        ProvideChartStyle {
            Chart(
                chart = columnChart(spacing = 16.dp),
                chartModelProducer = chartEntryModelProducer,
                startAxis = rememberStartAxis(
                    label = textComponent {
                        color = MaterialTheme.colorScheme.onSurface.hashCode()
                        textSizeSp = 12.0F
                    },
                    guideline = null,
                    itemPlacer = remember {
                        AxisItemPlacer.Vertical.default(maxItemCount = 5)
                    },
                    valueFormatter = { value, _ -> value.roundToInt().toString() }
                ),
                bottomAxis = rememberBottomAxis(
                    label = textComponent {
                        color = MaterialTheme.colorScheme.onSurface.hashCode()
                        textSizeSp = 11.0F
                    },
                    valueFormatter = { value, _ ->
                        val index = value.toInt()
                        if (index in data.indices) {
                            data[index].label
                        } else {
                            ""
                        }
                    }
                ),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(250.dp)
            )
        }
    }
}

@Composable
fun MonthlyLineChart(data: List<ActivitySummary>) {
    val chartEntryModelProducer = remember { ChartEntryModelProducer() }
    val hasData = remember { mutableStateOf(false) }

    LaunchedEffect(data) {
        if (data.isNotEmpty()) {
            // Si solo hay un dato, agregar un punto en 0 para poder dibujar línea
            val entries = if (data.size == 1) {
                listOf(
                    FloatEntry(0f, 0f), // Punto inicial en 0
                    FloatEntry(1f, data[0].totalSets.toFloat()) // Tu dato real
                )
            } else {
                data.mapIndexed { index, item ->
                    FloatEntry(index.toFloat(), item.totalSets.toFloat())
                }
            }
            chartEntryModelProducer.setEntries(entries)
            hasData.value = true
        }
    }

    if (hasData.value) {
        ProvideChartStyle {
            Chart(
                chart = lineChart(),
                chartModelProducer = chartEntryModelProducer,
                startAxis = rememberStartAxis(
                    label = textComponent {
                        color = MaterialTheme.colorScheme.onSurface.hashCode()
                        textSizeSp = 12.0F
                    },
                    guideline = null,
                    itemPlacer = remember {
                        AxisItemPlacer.Vertical.default(maxItemCount = 5)
                    },
                    valueFormatter = { value, _ -> value.roundToInt().toString() }
                ),
                bottomAxis = rememberBottomAxis(
                    label = textComponent {
                        color = MaterialTheme.colorScheme.onSurface.hashCode()
                        textSizeSp = 11.0F
                    },
                    valueFormatter = { value, _ ->
                        if (data.size == 1) {
                            // Si solo hay un dato, muestra el label solo para el índice 1
                            if (value.toInt() == 1) data[0].label else ""
                        } else {
                            val index = value.toInt()
                            if (index in data.indices) data[index].label else ""
                        }
                    }
                ),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(250.dp)
            )
        }
    }
}

@Composable
fun MonthlyExerciseBarChart(data: Map<String, Int>) {
    if (data.isEmpty()) {
        Text("No hay ejercicios este mes.")
        return
    }

    val labels = data.keys.toList()
    val values = data.values.map { it.toFloat() }

    val entries = entriesOf(
        *values.mapIndexed { index, value ->
            index.toFloat() to value
        }.toTypedArray()
    )

    val chartEntryModelProducer = remember { ChartEntryModelProducer(entries) }

    ProvideChartStyle {
        Chart(
            chart = columnChart(spacing = 16.dp),
            chartModelProducer = chartEntryModelProducer,
            startAxis = rememberStartAxis(
                label = textComponent { textSizeSp = 12f },
                guideline = null,
                itemPlacer = AxisItemPlacer.Vertical.default(
                    maxItemCount = values.distinct().size.coerceAtMost(4)
                ),
                valueFormatter = { value, _ -> value.toInt().toString() }
            ),
            bottomAxis = rememberBottomAxis(
                label = textComponent { textSizeSp = 12f },
                valueFormatter = { value, _ ->
                    val index = value.roundToInt()
                    if (index in labels.indices) labels[index] else ""
                }
            ),
            modifier = Modifier
                .fillMaxWidth()
                .height((values.size * 60).dp)
        )
    }

    Spacer(Modifier.height(12.dp))

    Column {
        labels.forEachIndexed { i, label ->
            Text(
                "$label: ${values[i].toInt()} veces",
                style = MaterialTheme.typography.bodyLarge
            )
        }
    }
}
@Composable
fun ActivityTable() {
    var stats by remember { mutableStateOf<UserStats?>(null) }

    LaunchedEffect(Unit) {
        fetchUserStats { stats = it }
    }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp)
    ) {
        Text(
            "Estadísticas del usuario",
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSurface
        )

        Spacer(Modifier.height(12.dp))

        stats?.let { s ->
            Text("Nombre: ${s.userName}", style = MaterialTheme.typography.bodyLarge)
            Text("Total de sets: ${s.totalSets}", style = MaterialTheme.typography.bodyLarge)
            Text("Total de km: ${"%.2f".format(s.totalKm)} km", style = MaterialTheme.typography.bodyLarge)
        } ?: Text("Cargando...", style = MaterialTheme.typography.bodyLarge)
    }
}


fun fetchMonthlyActivity(onResult: (List<ActivitySummary>) -> Unit) {
    val db = Firebase.firestore
    val currentUserId = Firebase.auth.currentUser?.uid ?: return

    db.collection("exercise_logs")
        .whereEqualTo("userId", currentUserId)
        .get()
        .addOnSuccessListener { snapshot ->
            // Mapeo de nombres de meses para ordenar correctamente
            val monthOrder = mapOf(
                "ene" to 1, "feb" to 2, "mar" to 3, "abr" to 4,
                "may" to 5, "jun" to 6, "jul" to 7, "ago" to 8,
                "sep" to 9, "oct" to 10, "nov" to 11, "dic" to 12,
                "jan" to 1, "feb" to 2, "mar" to 3, "apr" to 4,
                "may" to 5, "jun" to 6, "jul" to 7, "aug" to 8,
                "sep" to 9, "oct" to 10, "nov" to 11, "dec" to 12
            )

            val formatter = SimpleDateFormat("MMM", Locale.getDefault())

            val monthlyData = snapshot.documents
                .mapNotNull { it.toObject(ExerciseLogs::class.java) }
                .groupBy { formatter.format(toDate(it.timestamp)) }
                .map { (month, items) ->
                    ActivitySummary(month, items.sumOf { it.sets })
                }
                .sortedBy { monthOrder[it.label.lowercase()] ?: 13 }

            onResult(monthlyData)
        }
        .addOnFailureListener { exception ->
            println("Error fetching monthly activity: ${exception.message}")
            onResult(emptyList())
        }
}


fun fetchWeeklyActivity(onResult: (List<ActivitySummary>) -> Unit) {
    val db = Firebase.firestore
    val currentUserId = Firebase.auth.currentUser?.uid ?: return

    db.collection("exercise_logs")
        .whereEqualTo("userId", currentUserId)
        .get()
        .addOnSuccessListener { snapshot ->
            // Orden de días de la semana
            val dayOrder = mapOf(
                "lun" to 1, "mar" to 2, "mié" to 3, "jue" to 4,
                "vie" to 5, "sáb" to 6, "dom" to 7,
                "mon" to 1, "tue" to 2, "wed" to 3, "thu" to 4,
                "fri" to 5, "sat" to 6, "sun" to 7
            )

            val formatter = SimpleDateFormat("EEE", Locale.getDefault())

            val weeklyData = snapshot.documents
                .mapNotNull { it.toObject(ExerciseLogs::class.java) }
                .groupBy { formatter.format(toDate(it.timestamp)) }
                .map { (day, items) ->
                    ActivitySummary(day, items.sumOf { it.sets })
                }
                .sortedBy { dayOrder[it.label.lowercase()] ?: 8 }

            onResult(weeklyData)
        }
        .addOnFailureListener { exception ->
            println("Error fetching weekly activity: ${exception.message}")
            onResult(emptyList())
        }
}

fun fetchMonthlyExercise(onResult: (Map<String, Int>) -> Unit) {
    val db = Firebase.firestore
    val calendar = Calendar.getInstance()
    val currentMonth = calendar.get(Calendar.MONTH)
    val currentYear = calendar.get(Calendar.YEAR)
    val currentUserId = Firebase.auth.currentUser?.uid ?: return

    db.collection("exercise_logs")
        .whereEqualTo("userId", currentUserId)
        .get()
        .addOnSuccessListener { snapshot ->

            val result = snapshot.documents
                .mapNotNull { it.toObject(ExerciseLogs::class.java) }
                .filter { log ->
                    val date = toDate(log.timestamp)
                    val cal = Calendar.getInstance().apply { time = date }
                    cal.get(Calendar.MONTH) == currentMonth &&
                            cal.get(Calendar.YEAR) == currentYear
                }
                .groupBy { it.exerciseName }
                .mapValues { (_, items) -> items.size }

            onResult(result)
        }
        .addOnFailureListener { onResult(emptyMap()) }
}

data class UserStats(
    val userName: String,
    val totalSets: Int,
    val totalKm: Double
)

fun fetchUserStats(onResult: (UserStats) -> Unit) {
    val user = Firebase.auth.currentUser
    if (user == null) {
        onResult(UserStats("Desconocido", 0, 0.0))
        return
    }

    val db = Firebase.firestore

    // Primero obtenemos total de sets de exercise_logs
    db.collection("exercise_logs")
        .whereEqualTo("userId", user.uid)
        .get()
        .addOnSuccessListener { exerciseSnapshot ->
            val totalSets = exerciseSnapshot.documents
                .mapNotNull { it.toObject(ExerciseLogs::class.java) }
                .sumOf { it.sets }

            // Ahora obtenemos total de km de completed_workouts
            db.collection("completed_workouts")
                .whereEqualTo("userId", user.uid)
                .get()
                .addOnSuccessListener { workoutSnapshot ->
                    val totalKm = workoutSnapshot.documents
                        .mapNotNull { it.toObject(CompletedWorkouts::class.java) }
                        .sumOf { it.cantidad }

                    onResult(UserStats(user.displayName ?: "Usuario", totalSets, totalKm))
                }
                .addOnFailureListener {
                    // En caso de fallo en completed_workouts
                    onResult(UserStats(user.displayName ?: "Usuario", totalSets, 0.0))
                }
        }
        .addOnFailureListener {
            // En caso de fallo en exercise_logs
            onResult(UserStats(user.displayName ?: "Usuario", 0, 0.0))
        }
}

fun toDate(value: Any?): Date {
    return when (value) {
        is Timestamp -> value.toDate()
        is Long -> Date(value)
        is String -> {
            if (value.isBlank()) {
                // Evita ParseException
                return Date()
            }

            val formats = listOf(
                "yyyy-MM-dd HH:mm:ss",
                "yyyy-MM-dd",
                "dd/MM/yyyy",
                "MM/dd/yyyy"
            )

            for (format in formats) {
                try {
                    return SimpleDateFormat(format, Locale.getDefault()).parse(value)!!
                } catch (_: Exception) {}
            }

            Date() // Último recurso
        }
        else -> Date()
    }
}

@Composable
fun UserProfileEditor(user: User, onSave: (User) -> Unit) {
    var draftUser by remember(user) { mutableStateOf(user) }

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text(
                "Perfil de Usuario",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold
            )

            // Campos NO editables
            OutlinedTextField(
                value = draftUser.email,
                onValueChange = {},
                label = { Text("Email") },
                enabled = false,
                modifier = Modifier.fillMaxWidth()
            )

            // Campos Editables
            OutlinedTextField(
                value = draftUser.nombre,
                onValueChange = { draftUser = draftUser.copy(nombre = it) },
                label = { Text("Nombre") },
                modifier = Modifier.fillMaxWidth()
            )

            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedTextField(
                    value = if (draftUser.edad == 0) "" else draftUser.edad.toString(),
                    onValueChange = { 
                        if (it.all { char -> char.isDigit() }) {
                            draftUser = draftUser.copy(edad = it.toIntOrNull() ?: 0) 
                        }
                    },
                    label = { Text("Edad") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier.weight(1f)
                )

                OutlinedTextField(
                    value = if (draftUser.peso == 0) "" else draftUser.peso.toString(),
                    onValueChange = { 
                         if (it.all { char -> char.isDigit() }) {
                            draftUser = draftUser.copy(peso = it.toIntOrNull() ?: 0)
                         }
                    },
                    label = { Text("Peso (kg)") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier.weight(1f)
                )
            }

            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedTextField(
                    value = if (draftUser.estatura == 0) "" else draftUser.estatura.toString(),
                    onValueChange = { 
                        if (it.all { char -> char.isDigit() }) {
                            draftUser = draftUser.copy(estatura = it.toIntOrNull() ?: 0)
                        }
                    },
                    label = { Text("Estatura (cm)") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier.weight(1f)
                )

                OutlinedTextField(
                    value = draftUser.sexo,
                    onValueChange = { draftUser = draftUser.copy(sexo = it) },
                    label = { Text("Sexo") },
                    modifier = Modifier.weight(1f)
                )
            }
            
            // Foto URL (No editable pero visible)
             OutlinedTextField(
                value = draftUser.fotoURL,
                onValueChange = {},
                label = { Text("Foto URL") },
                enabled = false,
                modifier = Modifier.fillMaxWidth(),
                maxLines = 1
            )

            Button(
                onClick = { onSave(draftUser) },
                modifier = Modifier.align(Alignment.End)
            ) {
                Text("Guardar Cambios")
            }
        }
    }
}

fun fetchUserProfile(onResult: (User?) -> Unit) {
    val db = Firebase.firestore
    val uid = Firebase.auth.currentUser?.uid
    if (uid == null) {
        onResult(null)
        return
    }
    db.collection("users").document(uid).get()
        .addOnSuccessListener { document ->
            if (document.exists()) {
                onResult(document.toObject(User::class.java))
            } else {
                // Si no existe, retornamos un objeto base con los datos de Auth
                val user = Firebase.auth.currentUser
                onResult(User(
                    userId = uid,
                    email = user?.email ?: "",
                    nombre = user?.displayName ?: "",
                    fotoURL = user?.photoUrl?.toString() ?: ""
                ))
            }
        }
        .addOnFailureListener { onResult(null) }
}

fun updateUserProfile(user: User, onResult: (Boolean) -> Unit) {
    val db = Firebase.firestore
    db.collection("users").document(user.userId).set(user)
        .addOnSuccessListener { onResult(true) }
        .addOnFailureListener { onResult(false) }
}
