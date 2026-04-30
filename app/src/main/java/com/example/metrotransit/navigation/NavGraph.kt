package com.example.metrotransit.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import com.example.metrotransit.ui.screens.HomeScreen
import com.example.metrotransit.ui.screens.ResultScreen
import com.example.metrotransit.ui.screens.SplashScreen
import com.example.metrotransit.ui.screens.StationListScreen

sealed class Screen(val route: String) {
    object Splash : Screen("splash")
    object Home : Screen("home")
    object Result : Screen("result/{fromId}/{toId}") {
        fun createRoute(fromId: Int, toId: Int) = "result/$fromId/$toId"
    }
    object Stations : Screen("stations")
}

@Composable
fun NavGraph(navController: NavHostController) {
    NavHost(
        navController = navController,
        startDestination = Screen.Splash.route
    ) {
        composable(Screen.Splash.route) {
            SplashScreen(onNavigateToHome = {
                navController.navigate(Screen.Home.route) {
                    popUpTo(Screen.Splash.route) { inclusive = true }
                }
            })
        }
        composable(Screen.Home.route) {
            HomeScreen(
                onShowTrains = { fromId, toId ->
                    navController.navigate(Screen.Result.createRoute(fromId, toId))
                },
                onViewStations = {
                    navController.navigate(Screen.Stations.route)
                }
            )
        }
        composable(
            route = Screen.Result.route,
            arguments = listOf(
                navArgument("fromId") { type = NavType.IntType },
                navArgument("toId") { type = NavType.IntType }
            )
        ) { backStackEntry ->
            val fromId = backStackEntry.arguments?.getInt("fromId") ?: 0
            val toId = backStackEntry.arguments?.getInt("toId") ?: 0
            ResultScreen(
                fromId = fromId,
                toId = toId,
                onBack = { navController.popBackStack() }
            )
        }
        composable(Screen.Stations.route) {
            StationListScreen(onBack = { navController.popBackStack() })
        }
    }
}
