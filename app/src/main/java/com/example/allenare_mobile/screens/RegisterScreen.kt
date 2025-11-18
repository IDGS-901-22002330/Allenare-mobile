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
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.ktx.storage // Importante para subir fotos

@Composable
fun RegisterScreen(
    onRegisterSuccess: () -> Unit,
    onNavigateToLogin: () -> Unit
) {
    // --- ESTADOS DE FORMULARIO ---
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var nombre by remember { mutableStateOf("") }
    var edad by remember { mutableStateOf("") }
    var sexo by remember { mutableStateOf("") }
    var peso by remember { mutableStateOf("") }
    var estatura by remember { mutableStateOf("") }

    // --- ESTADO DE LA FOTO ---
    var selectedImageUri by remember { mutableStateOf<Uri?>(null) }

    var errorMessage by remember { mutableStateOf<String?>(null) }
    var isLoading by remember { mutableStateOf(false) }

    val auth: FirebaseAuth = Firebase.auth
    val db = Firebase.firestore
    val storage = Firebase.storage // Referencia al Storage
    val context = LocalContext.current

    // --- SELECTOR DE IMÁGENES ---
    val photoPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia(),
        onResult = { uri ->
            if (uri != null) {
                // VALIDACIÓN DE FORMATO (JPG, JPEG, PNG)
                val type = context.contentResolver.getType(uri)
                if (type == "image/jpeg" || type == "image/png" || type == "image/jpg") {
                    selectedImageUri = uri
                } else {
                    Toast.makeText(context, "Solo se permiten imágenes JPG o PNG.", Toast.LENGTH_LONG).show()
                    selectedImageUri = null
                }
            }
        }
    )

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(32.dp)
            .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text("Crear Cuenta", style = MaterialTheme.typography.headlineLarge)
        Spacer(modifier = Modifier.height(24.dp))

        // --- UI DE FOTO DE PERFIL ---
        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier
                .size(100.dp)
                .clip(CircleShape)
                .background(Color.LightGray)
                .clickable {
                    // Abrir galería solo para imágenes
                    photoPickerLauncher.launch(
                        PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                    )
                }
        ) {
            if (selectedImageUri != null) {
                AsyncImage(
                    model = selectedImageUri,
                    contentDescription = "Foto seleccionada",
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.Crop
                )
            } else {
                Icon(
                    imageVector = Icons.Default.Person,
                    contentDescription = "Agregar foto",
                    tint = Color.White,
                    modifier = Modifier.size(50.dp)
                )
            }
        }
        Text("Toca para agregar foto", style = MaterialTheme.typography.bodySmall, color = Color.Gray)

        Spacer(modifier = Modifier.height(24.dp))

        // --- CAMPOS DE TEXTO (Igual que antes) ---
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
        Spacer(modifier = Modifier.height(16.dp))

        OutlinedTextField(
            value = nombre,
            onValueChange = { nombre = it },
            label = { Text("Nombre Completo") },
            modifier = Modifier.fillMaxWidth(),
            enabled = !isLoading
        )
        Spacer(modifier = Modifier.height(16.dp))

        OutlinedTextField(
            value = edad,
            onValueChange = { edad = it },
            label = { Text("Edad") },
            modifier = Modifier.fillMaxWidth(),
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
            enabled = !isLoading
        )
        Spacer(modifier = Modifier.height(16.dp))

        OutlinedTextField(
            value = sexo,
            onValueChange = { sexo = it },
            label = { Text("Sexo (ej. Hombre, Mujer)") },
            modifier = Modifier.fillMaxWidth(),
            enabled = !isLoading
        )
        Spacer(modifier = Modifier.height(16.dp))

        Row(Modifier.fillMaxWidth()) {
            OutlinedTextField(
                value = peso,
                onValueChange = { peso = it },
                label = { Text("Peso (kg)") },
                modifier = Modifier.weight(1f),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                enabled = !isLoading
            )
            Spacer(Modifier.width(16.dp))
            OutlinedTextField(
                value = estatura,
                onValueChange = { estatura = it },
                label = { Text("Estatura (cm)") },
                modifier = Modifier.weight(1f),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                enabled = !isLoading
            )
        }

        Spacer(modifier = Modifier.height(24.dp))

        Button(
            onClick = {
                if (email.isNotBlank() && password.isNotBlank() && nombre.isNotBlank()) {
                    isLoading = true

                    // 1. Crear Usuario en Auth
                    auth.createUserWithEmailAndPassword(email, password)
                        .addOnCompleteListener { task ->
                            if (task.isSuccessful) {
                                val user = task.result?.user
                                if (user != null) {

                                    // Función auxiliar para guardar en Firestore
                                    fun saveToFirestore(downloadUrl: String) {
                                        val userProfile = hashMapOf<String, Any>(
                                            "userId" to user.uid,
                                            "email" to (user.email ?: ""),
                                            "tipo" to "usuario",
                                            "nombre" to nombre,
                                            "edad" to (edad.toIntOrNull() ?: 0),
                                            "sexo" to sexo,
                                            "peso" to (peso.toDoubleOrNull() ?: 0.0),
                                            "estatura" to (estatura.toIntOrNull() ?: 0),
                                            "fotoURL" to downloadUrl // <-- AQUÍ VA LA URL
                                        )

                                        db.collection("users").document(user.uid)
                                            .set(userProfile)
                                            .addOnSuccessListener {
                                                onRegisterSuccess()
                                            }
                                            .addOnFailureListener { e ->
                                                errorMessage = "Error al guardar datos: ${e.message}"
                                                isLoading = false
                                            }
                                    }

                                    // 2. ¿Hay imagen para subir?
                                    if (selectedImageUri != null) {
                                        // Referencia: profile_images/USER_ID.jpg
                                        val storageRef = storage.reference.child("profile_images/${user.uid}.jpg")

                                        storageRef.putFile(selectedImageUri!!)
                                            .addOnSuccessListener {
                                                // 3a. Obtener URL y Guardar
                                                storageRef.downloadUrl.addOnSuccessListener { uri ->
                                                    saveToFirestore(uri.toString())
                                                }
                                            }
                                            .addOnFailureListener { e ->
                                                // Si falla la foto, guardamos sin foto
                                                Toast.makeText(context, "Error al subir foto: ${e.message}", Toast.LENGTH_SHORT).show()
                                                saveToFirestore("")
                                            }
                                    } else {
                                        // 3b. Guardar sin foto
                                        saveToFirestore("")
                                    }

                                } else {
                                    errorMessage = "Error al obtener usuario."
                                    isLoading = false
                                }
                            } else {
                                errorMessage = task.exception?.message ?: "Error al registrarse."
                                isLoading = false
                            }
                        }
                } else {
                    errorMessage = "Por favor, completa los campos obligatorios."
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