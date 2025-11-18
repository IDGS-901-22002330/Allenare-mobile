package com.example.allenare_mobile

import android.Manifest
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Modifier
import com.example.allenare_mobile.navigation.AppNavigation
import com.example.allenare_mobile.ui.theme.AllenaremobileTheme
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            AllenaremobileTheme {
                // --- LÓGICA DE PERMISOS ---
                val permissionsToRequest = mutableListOf<String>()

                // Permiso de Notificaciones (Android 13+)
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                    permissionsToRequest.add(Manifest.permission.POST_NOTIFICATIONS)
                }
                // Permiso de Ubicación (Para los mapas)
                permissionsToRequest.add(Manifest.permission.ACCESS_FINE_LOCATION)
                permissionsToRequest.add(Manifest.permission.ACCESS_COARSE_LOCATION)

                val launcher = rememberLauncherForActivityResult(
                    contract = ActivityResultContracts.RequestMultiplePermissions(),
                    onResult = { /* No necesitamos hacer nada específico al recibir resultado por ahora */ }
                )

                LaunchedEffect(Unit) {
                    launcher.launch(permissionsToRequest.toTypedArray())
                }
                // --------------------------

                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    val startDestination = if (Firebase.auth.currentUser != null) "main" else "login"
                    AppNavigation(startDestination = startDestination)
                }
            }
        }
    }
}