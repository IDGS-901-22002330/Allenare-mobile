package com.example.allenare_mobile.screens

import android.net.Uri
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.example.allenare_mobile.model.User
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.ktx.storage

@Composable
fun RegisterScreen(
    onRegisterSuccess: () -> Unit,
    onNavigateToLogin: () -> Unit
) {
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var nombre by remember { mutableStateOf("") }
    var edad by remember { mutableStateOf("") }
    var sexo by remember { mutableStateOf("") }
    var peso by remember { mutableStateOf("") }
    var estatura by remember { mutableStateOf("") }
    var selectedImageUri by remember { mutableStateOf<Uri?>(null) }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    var isLoading by remember { mutableStateOf(false) }

    val auth: FirebaseAuth = Firebase.auth
    val db = Firebase.firestore
    val storage = Firebase.storage
    val context = LocalContext.current

    val photoPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia(),
        onResult = { uri ->
            val type = uri?.let { context.contentResolver.getType(it) }
            if (type == "image/jpeg" || type == "image/png") {
                selectedImageUri = uri
            } else if (uri != null) {
                Toast.makeText(context, "Formato de imagen no válido.", Toast.LENGTH_SHORT).show()
            }
        }
    )

    Column(
        modifier = Modifier.fillMaxSize().padding(32.dp).verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // ... (UI de la foto de perfil y campos de texto - sin cambios)

        Button(
            onClick = {
                if (email.isBlank() || password.isBlank() || nombre.isBlank()) {
                    errorMessage = "Email, contraseña y nombre son obligatorios."
                    return@Button
                }
                isLoading = true

                auth.createUserWithEmailAndPassword(email, password).addOnCompleteListener { task ->
                    if (task.isSuccessful) {
                        val firebaseUser = task.result?.user
                        if (firebaseUser == null) {
                            errorMessage = "No se pudo obtener el usuario de Firebase."
                            isLoading = false
                            return@addOnCompleteListener
                        }

                        val userId = firebaseUser.uid

                        // Función para guardar el usuario en Firestore
                        fun saveUserToFirestore(photoUrl: String) {
                            val newUser = User(
                                userId = userId,
                                nombre = nombre,
                                email = email,
                                fotoURL = photoUrl,
                                edad = edad.toIntOrNull() ?: 0,
                                estatura = estatura.toIntOrNull() ?: 0,
                                peso = peso.toIntOrNull() ?: 0,
                                sexo = sexo,
                                tipo = "usuario"
                            )

                            db.collection("users").document(userId).set(newUser)
                                .addOnSuccessListener { onRegisterSuccess() } // Navega SOLO si se guarda bien
                                .addOnFailureListener { e ->
                                    errorMessage = "Error al guardar datos: ${e.message}"
                                    isLoading = false
                                }
                        }

                        // Subir foto si existe
                        if (selectedImageUri != null) {
                            val photoRef = storage.reference.child("profile_images/$userId.jpg")
                            photoRef.putFile(selectedImageUri!!).continueWithTask { 
                                photoRef.downloadUrl
                            }.addOnCompleteListener { urlTask ->
                                if (urlTask.isSuccessful) {
                                    saveUserToFirestore(urlTask.result.toString())
                                } else {
                                    Toast.makeText(context, "Error al subir foto, se usará la por defecto.", Toast.LENGTH_SHORT).show()
                                    saveUserToFirestore("")
                                }
                            }
                        } else {
                            saveUserToFirestore("")
                        }

                    } else {
                        errorMessage = task.exception?.message ?: "Error al registrar."
                        isLoading = false
                    }
                }
            },
            modifier = Modifier.fillMaxWidth(),
            enabled = !isLoading
        ) {
             if (isLoading) {
                CircularProgressIndicator(modifier = Modifier.size(24.dp), color = MaterialTheme.colorScheme.onPrimary)
            } else {
                Text("Registrarse")
            }
        }

    }
}