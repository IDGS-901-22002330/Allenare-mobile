package com.example.allenare_mobile.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.allenare_mobile.screens.DashboardScreen
import com.example.allenare_mobile.screens.LogGymWorkoutScreen
import com.example.allenare_mobile.screens.LogRunningWorkoutScreen
import com.example.allenare_mobile.screens.LoginScreen
import com.example.allenare_mobile.screens.RegisterScreen

@Composable
fun AppNavigation(startDestination: String) {
    val navController = rememberNavController()

    NavHost(
        navController = navController,
        startDestination = startDestination
    ) {

        composable("login") {
            LoginScreen(
                onLoginSuccess = {
                    navController.navigate("dashboard") {
                        popUpTo("login") { inclusive = true }
                    }
                },
                onNavigateToRegister = {
                    navController.navigate("register")
                }
            )
        }

        composable("register") {
            RegisterScreen(
                onRegisterSuccess = {
                    navController.navigate("dashboard") {
                        popUpTo("login") { inclusive = true }
                    }
                },
                onNavigateToLogin = {
                    navController.popBackStack()
                }
            )
        }

        composable("dashboard") {
            DashboardScreen(
                onLogout = {
                    navController.navigate("login") {
                        popUpTo("dashboard") { inclusive = true }
                    }
                },
                onNavigateToLogGymWorkout = {
                    navController.navigate("log_gym_workout")
                },
                onNavigateToLogRunningWorkout = {
                    navController.navigate("log_running_workout")
                }
            )
        }

        composable("log_gym_workout") {
            LogGymWorkoutScreen(
                onWorkoutLogged = {
                    navController.popBackStack()
                }
            )
        }

        composable("log_running_workout") {
            LogRunningWorkoutScreen(
                onWorkoutLogged = {
                    navController.popBackStack()
                }
            )
        }
    }
}