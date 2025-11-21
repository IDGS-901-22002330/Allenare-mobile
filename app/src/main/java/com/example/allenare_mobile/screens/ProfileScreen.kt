package com.example.allenare_mobile.screens

import android.widget.Toast
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import coil.compose.rememberAsyncImagePainter
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.UserProfileChangeRequest

@Composable
fun ProfileScreen(onLogout: () -> Unit) {
    val context = LocalContext.current
    val user = FirebaseAuth.getInstance().currentUser

    // Datos actuales
    val originalName = user?.displayName ?: ""
    val originalPhoto = user?.photoUrl?.toString() ?: ""
    val email = user?.email ?: ""

    // Estados editables
    var displayName by remember { mutableStateOf(originalName) }
    var photoUrl by remember { mutableStateOf(originalPhoto) }

    // Control de modo edición
    var isEditing by remember { mutableStateOf(false) }

    // Para cambiar contraseña
    var newPassword by remember { mutableStateOf("") }
    var showPasswordField by remember { mutableStateOf(false) }
    val canChangePassword = user?.providerData?.any { it.providerId == "password" } ?: false

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(20.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {

        // Ejemplo Foto de Perfil
        Image(
            painter = rememberAsyncImagePainter(
                if (originalPhoto.isBlank()) "https://via.placeholder.com/150"
                else originalPhoto
            ),
            contentDescription = "Profile Photo",
            modifier = Modifier
                .size(120.dp)
                .clip(CircleShape)
        )

        Spacer(modifier = Modifier.height(15.dp))

        // Vista de datos
        if (!isEditing) {
            Text(text = originalName, style = MaterialTheme.typography.titleLarge)
            Spacer(modifier = Modifier.height(6.dp))
            Text(text = email, color = MaterialTheme.colorScheme.onSurfaceVariant)

            Spacer(modifier = Modifier.height(25.dp))

            Button(
                onClick = { isEditing = true },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Editar Perfil")
            }

            Spacer(modifier = Modifier.height(20.dp))

            Button(
                onClick = {
                    FirebaseAuth.getInstance().signOut()
                    onLogout()
                },
                colors = ButtonDefaults.buttonColors(MaterialTheme.colorScheme.errorContainer),
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Cerrar Sesión")
            }

            return@Column
        }

        // Editor
        OutlinedTextField(
            value = displayName,
            onValueChange = { displayName = it },
            label = { Text("Nombre") },
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(12.dp))

        OutlinedTextField(
            value = photoUrl,
            onValueChange = { photoUrl = it },
            label = { Text("URL de Foto") },
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(20.dp))

        // Cambiar contraseña
        if (canChangePassword) {
            TextButton(onClick = { showPasswordField = !showPasswordField }) {
                Text(if (showPasswordField) "Ocultar cambio de contraseña" else "Cambiar contraseña")
            }

            if (showPasswordField) {
                OutlinedTextField(
                    value = newPassword,
                    onValueChange = { newPassword = it },
                    label = { Text("Nueva contraseña") },
                    visualTransformation = PasswordVisualTransformation(),
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(modifier = Modifier.height(20.dp))
            }
        }

        // Botón para guardar cambios
        Button(
            onClick = {
                val profileUpdates = UserProfileChangeRequest.Builder()
                    .setDisplayName(displayName)
                    .setPhotoUri(photoUrl.takeIf { it.isNotBlank() }?.let { android.net.Uri.parse(it) })
                    .build()

                user?.updateProfile(profileUpdates)
                    ?.addOnSuccessListener {
                        if (newPassword.isNotBlank()) {
                            user.updatePassword(newPassword)
                        }
                        Toast.makeText(context, "Perfil actualizado", Toast.LENGTH_SHORT).show()
                        isEditing = false
                    }
                    ?.addOnFailureListener { e ->
                        Toast.makeText(context, "Error: ${e.message}", Toast.LENGTH_SHORT).show()
                    }
            },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Guardar Cambios")
        }

        Spacer(modifier = Modifier.height(10.dp))

        TextButton(
            onClick = {
                displayName = originalName
                photoUrl = originalPhoto
                newPassword = ""
                isEditing = false
            }
        ) {
            Text("Cancelar")
        }
    }
}
