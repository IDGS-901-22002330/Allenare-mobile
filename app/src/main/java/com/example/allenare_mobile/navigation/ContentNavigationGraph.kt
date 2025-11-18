package com.example.allenare_mobile.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.example.allenare_mobile.screens.ChallengesScreen
import com.example.allenare_mobile.screens.ExerciseDetailScreen
import com.example.allenare_mobile.screens.ExerciseLibraryScreen
import com.example.allenare_mobile.screens.RemindersScreen
import com.example.allenare_mobile.screens.RoutineLibraryScreen
import com.example.allenare_mobile.screens.RoutinePlayerScreen

object ContentRoutes {
    const val ROUTINE_LIBRARY = "routine_library"
    const val EXERCISE_LIBRARY = "exercise_library"
    const val ROUTINE_PLAYER = "routine_player/{routineId}"
    const val EXERCISE_DETAIL = "exercise_detail/{exerciseId}"
    const val REMINDERS = "reminders"
    const val CHALLENGES = "challenges"
}

@Composable
fun ContentNavigationGraph() {
    val navController = rememberNavController()

    NavHost(
        navController = navController,
        startDestination = ContentRoutes.ROUTINE_LIBRARY
    ) {

        // 1. Biblioteca de Rutinas (Conectamos el nuevo botón)
        composable(ContentRoutes.ROUTINE_LIBRARY) {
            RoutineLibraryScreen(
                onNavigateToPlayer = { routineId -> navController.navigate("routine_player/$routineId") },
                onNavigateToExercises = { navController.navigate(ContentRoutes.EXERCISE_LIBRARY) },
                onNavigateToReminders = { navController.navigate(ContentRoutes.REMINDERS) },
                onNavigateToChallenges = { navController.navigate(ContentRoutes.CHALLENGES) } // <--- CONEXIÓN
            )
        }

        // 2. Reproductor de Rutina
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

        // 3. Biblioteca de Ejercicios
        composable(ContentRoutes.EXERCISE_LIBRARY) {
            ExerciseLibraryScreen(
                onNavigateToDetail = { exerciseId -> navController.navigate("exercise_detail/$exerciseId") },
                onBack = { navController.popBackStack() }
            )
        }

        // 4. Detalle de Ejercicio
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

        // 5. Recordatorios
        composable(ContentRoutes.REMINDERS) {
            RemindersScreen(onBack = { navController.popBackStack() })
        }

        // 6. Retos (Challenges)
        composable(ContentRoutes.CHALLENGES) {
            ChallengesScreen(onBack = { navController.popBackStack() })
        }
    }
}