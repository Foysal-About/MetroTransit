package com.example.metrotransit.navigation

import androidx.compose.runtime.Composable
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import com.example.metrotransit.ui.screens.*
import com.example.metrotransit.viewmodel.RapidPassViewModel

sealed class Screen(val route: String) {
    object Splash : Screen("splash")
    object Home : Screen("home")
    object Result : Screen("result/{fromId}/{toId}") {
        fun createRoute(fromId: Int, toId: Int) = "result/$fromId/$toId"
    }
    object Stations : Screen("stations")
    object RapidPassLogin : Screen("rapidpass_login")
    object RapidPassDashboard : Screen("rapidpass_dashboard")
    object RapidPassRecharge : Screen("rapidpass_recharge")
    object RapidPassWebView : Screen("rapidpass_webview")
    object PaymentGateway : Screen("payment_gateway/{amount}") {
        fun createRoute(amount: String) = "payment_gateway/$amount"
    }
}

@Composable
fun NavGraph(navController: NavHostController) {
    val rapidPassViewModel: RapidPassViewModel = viewModel()
    
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
                },
                onNavigateToRapidPass = {
                    navController.navigate(Screen.RapidPassLogin.route)
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
        
        // RapidPass Flow
        composable(Screen.RapidPassLogin.route) {
            RapidPassLoginScreen(
                onLoginSuccess = {
                    navController.navigate(Screen.RapidPassDashboard.route) {
                        popUpTo(Screen.RapidPassLogin.route) { inclusive = true }
                    }
                },
                onOpenWebView = {
                    navController.navigate(Screen.RapidPassWebView.route)
                },
                onBack = { navController.popBackStack() },
                viewModel = rapidPassViewModel
            )
        }
        composable(Screen.RapidPassDashboard.route) {
            RapidPassDashboardScreen(
                onRecharge = { card ->
                    rapidPassViewModel.selectedCard = card
                    navController.navigate(Screen.RapidPassRecharge.route)
                },
                onLogout = {
                    rapidPassViewModel.logout()
                    navController.navigate(Screen.Home.route) {
                        popUpTo(Screen.RapidPassDashboard.route) { inclusive = true }
                    }
                },
                viewModel = rapidPassViewModel
            )
        }
        composable(Screen.RapidPassRecharge.route) {
            RapidPassRechargeScreen(
                onBack = { navController.popBackStack() },
                onProceedToPayment = { amount ->
                    navController.navigate(Screen.PaymentGateway.createRoute(amount))
                },
                viewModel = rapidPassViewModel
            )
        }
        composable(
            route = Screen.PaymentGateway.route,
            arguments = listOf(navArgument("amount") { type = NavType.StringType })
        ) { backStackEntry ->
            val amount = backStackEntry.arguments?.getString("amount") ?: "0.0"
            DummyPaymentGatewayScreen(
                amount = amount,
                onPaymentComplete = { success ->
                    if (success) {
                        rapidPassViewModel.recharge()
                        navController.navigate(Screen.RapidPassDashboard.route) {
                            popUpTo(Screen.RapidPassDashboard.route) { inclusive = true }
                        }
                    } else {
                        navController.popBackStack()
                    }
                }
            )
        }
        composable(Screen.RapidPassWebView.route) {
            RapidPassWebViewScreen(onBack = { navController.popBackStack() })
        }
    }
}
