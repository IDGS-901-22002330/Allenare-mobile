package com.example.allenare_mobile.screens

import android.app.Activity
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.IntentSenderRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.allenare_mobile.R
import com.example.allenare_mobile.auth.GoogleAuthUiClient
import com.example.allenare_mobile.model.User
import com.google.android.gms.auth.api.identity.Identity
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import kotlinx.coroutines.launch

@Composable
fun LoginScreen(onLoginSuccess: () -> Unit, onNavigateToRegister: () -> Unit) {
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var isLoading by remember { mutableStateOf(false) }

    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    val db = Firebase.firestore

    val googleAuthUiClient by lazy {
        GoogleAuthUiClient(
            context = context,
            oneTapClient = Identity.getSignInClient(context)
        )
    }

    val launcher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartIntentSenderForResult(),
        onResult = { result ->
            if (result.resultCode == Activity.RESULT_OK) {
                isLoading = true
                coroutineScope.launch {
                    val userData = googleAuthUiClient.signInWithIntent(result.data ?: return@launch)
                    if (userData != null) {
                        val userDocRef = db.collection("users").document(userData.userId)
                        userDocRef.get().addOnSuccessListener { document ->
                            if (document.exists()) {
                                // Si el usuario ya existe en la BD, navegamos.
                                onLoginSuccess()
                            } else {
                                // Si el usuario es nuevo, lo creamos en la BD.
                                val newUser = User(
                                    userId = userData.userId,
                                    nombre = userData.username ?: "",
                                    email = userData.email ?: "",
                                    photoUrl = userData.profilePictureUrl ?: ""
                                )
                                // SOLO navegamos DESPUÉS de que se haya guardado con éxito.
                                userDocRef.set(newUser).addOnSuccessListener {
                                    onLoginSuccess()
                                }.addOnFailureListener {
                                    isLoading = false
                                    Toast.makeText(context, "Error al guardar el nuevo usuario.", Toast.LENGTH_SHORT).show()
                                }
                            }
                        }.addOnFailureListener {
                             isLoading = false
                             Toast.makeText(context, "Error al verificar el usuario.", Toast.LENGTH_SHORT).show()
                        }
                    } else {
                        isLoading = false
                        Toast.makeText(context, "Error al iniciar sesión con Google.", Toast.LENGTH_SHORT).show()
                    }
                }
            }
        }
    )

    Box(modifier = Modifier.fillMaxSize().background(Color(0xFFF0F2F5))) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(32.dp),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text("Bienvenido de Nuevo", fontSize = 28.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
            Text("Inicia sesión para continuar", fontSize = 16.sp, color = Color.Gray)

            Spacer(modifier = Modifier.height(48.dp))

            OutlinedTextField(value = email, onValueChange = { email = it }, label = { Text("Correo Electrónico") }, modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(12.dp))
            Spacer(modifier = Modifier.height(16.dp))

            OutlinedTextField(value = password, onValueChange = { password = it }, label = { Text("Contraseña") }, visualTransformation = PasswordVisualTransformation(), modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(12.dp))
            Spacer(modifier = Modifier.height(24.dp))

            Button(
                onClick = {
                    if (email.isNotBlank() && password.isNotBlank()) {
                        isLoading = true
                        Firebase.auth.signInWithEmailAndPassword(email, password)
                            .addOnCompleteListener { task ->
                                if (task.isSuccessful) {
                                    onLoginSuccess()
                                } else {
                                    Toast.makeText(context, "Error: ${task.exception?.message}", Toast.LENGTH_SHORT).show()
                                }
                                isLoading = false
                            }
                    } else {
                        Toast.makeText(context, "Por favor, completa todos los campos.", Toast.LENGTH_SHORT).show()
                    }
                },
                modifier = Modifier.fillMaxWidth().height(50.dp),
                shape = RoundedCornerShape(12.dp),
                enabled = !isLoading
            ) {
                Text("Iniciar Sesión", fontSize = 18.sp)
            }

            TextButton(onClick = onNavigateToRegister) {
                Text("¿No tienes cuenta? Regístrate aquí")
            }

            Spacer(modifier = Modifier.height(16.dp))

            Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth()) {
                Divider(modifier = Modifier.weight(1f))
                Text(" O ", color = Color.Gray, modifier = Modifier.padding(horizontal = 8.dp))
                Divider(modifier = Modifier.weight(1f))
            }

            Spacer(modifier = Modifier.height(16.dp))

            OutlinedButton(
                onClick = {
                    coroutineScope.launch {
                        val signInIntentSender = googleAuthUiClient.signIn()
                        launcher.launch(
                            IntentSenderRequest.Builder(signInIntentSender ?: return@launch).build()
                        )
                    }
                },
                modifier = Modifier.fillMaxWidth().height(50.dp),
                shape = RoundedCornerShape(12.dp),
                enabled = !isLoading
            ) {
                Image(painter = painterResource(id = R.drawable.googleicon), contentDescription = "Google Logo", modifier = Modifier.size(24.dp))
                Spacer(modifier = Modifier.width(8.dp))
                Text("Iniciar sesión con Google", fontSize = 16.sp)
            }
        }

        if (isLoading) {
            CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
        }
    }
}