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
import com.example.allenare_mobile.screens.DashboardScreen
import com.example.allenare_mobile.screens.LogGymWorkoutScreen
import com.example.allenare_mobile.screens.LogRunningWorkoutScreen
import com.example.allenare_mobile.screens.ProfileScreen
import com.example.allenare_mobile.ui.theme.AllenaremobileTheme

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
        composable(BottomNavItem.LogGym.route) { LogGymWorkoutScreen { navController.navigate(BottomNavItem.Dashboard.route) } }
        composable(BottomNavItem.LogRun.route) { LogRunningWorkoutScreen { navController.navigate(BottomNavItem.Dashboard.route) } }
        composable(BottomNavItem.Profile.route) { ProfileScreen(onLogout = onLogout) }
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
            DashboardScreen(modifier = Modifier.padding(innerPadding))
        }
    }
}