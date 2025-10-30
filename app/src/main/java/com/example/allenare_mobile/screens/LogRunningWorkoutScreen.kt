package com.example.allenare_mobile.screens

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.allenare_mobile.model.RunningWorkout
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase

@Composable
fun LogRunningWorkoutScreen(onWorkoutLogged: () -> Unit) {
    var distance by remember { mutableStateOf("") }
    var duration by remember { mutableStateOf("") }
    val context = LocalContext.current
    val db = Firebase.firestore
    val user = Firebase.auth.currentUser

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFF0F2F5))
            .padding(32.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text("Registrar Carrera", fontSize = 28.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
        Spacer(modifier = Modifier.height(32.dp))

        OutlinedTextField(
            value = distance,
            onValueChange = { distance = it },
            label = { Text("Distancia (km)") },
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp),
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number) // <-- AQUÍ ESTÁ EL CAMBIO
        )
        Spacer(modifier = Modifier.height(16.dp))

        OutlinedTextField(
            value = duration,
            onValueChange = { duration = it },
            label = { Text("Duración (minutos)") },
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp),
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number) // <-- Y AQUÍ
        )

        Spacer(modifier = Modifier.height(24.dp))

        Button(
            onClick = {
                if (distance.isNotBlank() && duration.isNotBlank()) {
                    val runningWorkout = RunningWorkout(
                        userId = user?.uid ?: "",
                        distance = distance.toDoubleOrNull() ?: 0.0,
                        duration = duration.toLongOrNull() ?: 0
                    )

                    db.collection("running_workouts")
                        .add(runningWorkout)
                        .addOnSuccessListener { 
                            Toast.makeText(context, "Carrera registrada", Toast.LENGTH_SHORT).show()
                            onWorkoutLogged()
                         }
                        .addOnFailureListener { e ->
                            Toast.makeText(context, "Error: ${e.message}", Toast.LENGTH_SHORT).show()
                        }
                } else {
                    Toast.makeText(context, "Por favor, completa todos los campos.", Toast.LENGTH_SHORT).show()
                }
            },
            modifier = Modifier.fillMaxWidth().height(50.dp),
            shape = RoundedCornerShape(12.dp),
            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
        ) {
            Text("Registrar", fontSize = 18.sp)
        }
    }
}