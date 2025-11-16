package com.example.allenare_mobile.screens

import android.widget.Toast // --- AÑADIDO ---
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext // --- AÑADIDO ---
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.ktx.firestore // --- AÑADIDO ---
import com.google.firebase.ktx.Firebase

@Composable
fun RegisterScreen(
    onRegisterSuccess: () -> Unit,
    onNavigateToLogin: () -> Unit // Para volver al login
) {
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    var isLoading by remember { mutableStateOf(false) } // --- AÑADIDO ---

    val auth: FirebaseAuth = Firebase.auth
    val db = Firebase.firestore // --- AÑADIDO ---
    val context = LocalContext.current // --- AÑADIDO ---

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(32.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text("Crear Cuenta", style = MaterialTheme.typography.headlineLarge)
        Spacer(modifier = Modifier.height(32.dp))

        OutlinedTextField(
            value = email,
            onValueChange = { email = it },
            label = { Text("Correo Electrónico") },
            modifier = Modifier.fillMaxWidth(),
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
            enabled = !isLoading
        )
        Spacer(modifier = Modifier.height(16.dp))

        OutlinedTextField(
            value = password,
            onValueChange = { password = it },
            label = { Text("Contraseña (mínimo 6 caracteres)") },
            modifier = Modifier.fillMaxWidth(),
            visualTransformation = PasswordVisualTransformation(),
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
            enabled = !isLoading
        )
        Spacer(modifier = Modifier.height(24.dp))

        Button(
            onClick = {
                if (email.isNotBlank() && password.isNotBlank()) {
                    isLoading = true // --- AÑADIDO ---
                    // Usamos el método de Firebase para crear un nuevo usuario
                    auth.createUserWithEmailAndPassword(email, password)
                        .addOnCompleteListener { task ->
                            if (task.isSuccessful) {
                                // --- LÓGICA DE PERFIL AÑADIDA ---
                                val user = task.result?.user
                                if (user != null) {
                                    // 1. Crear el objeto de perfil
                                    val userProfile = hashMapOf(
                                        "userId" to user.uid,
                                        "email" to user.email,
                                        "tipo" to "usuario", // <-- ¡REQUERIMIENTO CUMPLIDO!
                                        "nombre" to "",
                                        "peso" to 0,
                                        "altura" to 0,
                                        "fotoURL" to ""
                                    )

                                    // 2. Guardarlo en Firestore en la colección "users"
                                    db.collection("users").document(user.uid)
                                        .set(userProfile)
                                        .addOnSuccessListener {
                                            // 3. Solo navegar si ambos (Auth y Firestore) funcionan
                                            onRegisterSuccess()
                                        }
                                        .addOnFailureListener { e ->
                                            errorMessage = "Error al crear perfil: ${e.message}"
                                            isLoading = false
                                        }
                                } else {
                                    errorMessage = "Error al obtener el usuario."
                                    isLoading = false
                                }
                                // --- FIN DE LA LÓGICA ---
                            } else {
                                // Si hay un error (ej. contraseña corta, email inválido), lo mostramos
                                errorMessage = task.exception?.message ?: "Error al registrarse."
                                isLoading = false
                            }
                        }
                } else {
                    errorMessage = "Por favor, completa todos los campos."
                }
            },
            modifier = Modifier.fillMaxWidth(),
            enabled = !isLoading // --- AÑADIDO ---
        ) {
            if (isLoading) {
                CircularProgressIndicator(modifier = Modifier.size(24.dp), color = MaterialTheme.colorScheme.onPrimary)
            } else {
                Text("Registrarse")
            }
        }

        errorMessage?.let {
            Spacer(modifier = Modifier.height(16.dp))
            Text(text = it, color = MaterialTheme.colorScheme.error)
        }

        Spacer(modifier = Modifier.height(16.dp))

        TextButton(onClick = onNavigateToLogin, enabled = !isLoading) {
            Text("¿Ya tienes cuenta? Inicia sesión")
        }
    }
}