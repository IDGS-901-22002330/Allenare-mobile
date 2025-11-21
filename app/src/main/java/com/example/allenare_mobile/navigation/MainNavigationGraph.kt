package com.example.allenare_mobile.navigation

import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.allenare_mobile.screens.ActiveRunScreen
import com.example.allenare_mobile.screens.ChatScreen
import com.example.allenare_mobile.screens.DashboardScreen
import com.example.allenare_mobile.screens.LogGymWorkoutScreen
import com.example.allenare_mobile.screens.LogRunningWorkoutScreen
import com.example.allenare_mobile.screens.Records
import com.example.allenare_mobile.screens.AllRunsScreen
import com.example.allenare_mobile.screens.LeaderBoardsScreen
import com.example.allenare_mobile.screens.ProfileScreen
import com.example.allenare_mobile.ui.theme.AllenaremobileTheme
import com.google.firebase.auth.FirebaseAuth

@Composable
fun MainNavigationGraph(
    navController: NavHostController,
    modifier: Modifier = Modifier,
    onLogout: () -> Unit
) {
    NavHost(
        navController,
        startDestination = BottomNavItem.Dashboard.route,
        modifier = modifier
    ) {
        composable(BottomNavItem.Dashboard.route) { DashboardScreen(modifier = modifier) }
        composable(BottomNavItem.Chat.route) { ChatScreen() }
        composable(BottomNavItem.LogRun.route) {
            LogRunningWorkoutScreen(
                navController = navController,
                onWorkoutLogged = {
                    navController.navigate(BottomNavItem.Dashboard.route)
                }
            )
        }
        composable(BottomNavItem.AllRuns.route) { AllRunsScreen( navController = navController,  onWorkoutLogged = { navController.navigate(BottomNavItem.Dashboard.route) }) }
        composable("active_run/{runId}") { backStackEntry -> val runId = backStackEntry.arguments?.getString("runId") ?: ""
            ActiveRunScreen(navController = navController, runId = runId)
        }
        composable("records/{userId}") { backStackEntry ->
            val userId = backStackEntry.arguments?.getString("userId") ?: ""
            Records(userId = userId)
        }
        composable(BottomNavItem.Profile.route) { ProfileScreen(onLogout = onLogout) }
        composable(BottomNavItem.LeaderBoards.route) { LeaderBoardsScreen() }
        composable(BottomNavItem.Library.route) { ContentNavigationGraph() }
    }
}

@Preview(showBackground = true)
@Composable
fun MainScreenWithDashboardPreview() {
    AllenaremobileTheme {
        val navController = rememberNavController()
        Scaffold(
            bottomBar = { BottomNavBar(navController = navController) }
        ) { innerPadding ->
            MainNavigationGraph(navController = navController, modifier = Modifier.padding(innerPadding), onLogout = {})
        }
    }
}