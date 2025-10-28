package com.example.allenare_mobile

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.allenare_mobile.navigation.AppNavigation
import com.example.allenare_mobile.screens.DashboardScreen
import com.example.allenare_mobile.screens.LoginScreen
import com.example.allenare_mobile.screens.RegisterScreen
import com.example.allenare_mobile.ui.theme.AllenaremobileTheme
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            AllenaremobileTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    // Inicia el flujo de navegación de la app
                    val startDestination = if (Firebase.auth.currentUser != null) "dashboard" else "login"
                    AppNavigation(startDestination = startDestination)
                }
            }
        }
    }
}