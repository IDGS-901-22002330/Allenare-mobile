package com.example.allenare_mobile.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.example.allenare_mobile.screens.ExerciseDetailScreen
import com.example.allenare_mobile.screens.ExerciseLibraryScreen
import com.example.allenare_mobile.screens.RoutineLibraryScreen
import com.example.allenare_mobile.screens.RoutinePlayerScreen

// Rutas internas del Módulo 3
private object ContentRoutes {
    const val ROUTINE_LIBRARY = "routine_library" // RFM-09
    const val EXERCISE_LIBRARY = "exercise_library" // RFM-10
    const val ROUTINE_PLAYER = "routine_player/{routineId}"
    const val EXERCISE_DETAIL = "exercise_detail/{exerciseId}"
}

@Composable
fun ContentNavigationGraph() {
    val navController = rememberNavController()

    NavHost(
        navController = navController,
        startDestination = ContentRoutes.ROUTINE_LIBRARY
    ) {

        // Pantalla Principal del Módulo (RFM-09)
        composable(ContentRoutes.ROUTINE_LIBRARY) {
            RoutineLibraryScreen(
                onNavigateToPlayer = { routineId ->
                    navController.navigate("routine_player/$routineId")
                },
                onNavigateToExercises = {
                    navController.navigate(ContentRoutes.EXERCISE_LIBRARY)
                }
            )
        }

        // Reproductor de Rutina (RFM-09)
        composable(
            route = ContentRoutes.ROUTINE_PLAYER,
            arguments = listOf(navArgument("routineId") { type = NavType.StringType })
        ) { backStackEntry ->
            val routineId = backStackEntry.arguments?.getString("routineId")
            RoutinePlayerScreen(
                routineId = routineId ?: "",
                onBack = { navController.popBackStack() }
            )
        }

        // Biblioteca de Ejercicios (RFM-10)
        composable(ContentRoutes.EXERCISE_LIBRARY) {
            ExerciseLibraryScreen(
                onNavigateToDetail = { exerciseId ->
                    navController.navigate("exercise_detail/$exerciseId")
                },
                onBack = { navController.popBackStack() }
            )
        }

        // Detalle de Ejercicio (RFM-10)
        composable(
            route = ContentRoutes.EXERCISE_DETAIL,
            arguments = listOf(navArgument("exerciseId") { type = NavType.StringType })
        ) { backStackEntry ->
            val exerciseId = backStackEntry.arguments?.getString("exerciseId")
            ExerciseDetailScreen(
                exerciseId = exerciseId ?: "",
                onBack = { navController.popBackStack() }
            )
        }
    }
}