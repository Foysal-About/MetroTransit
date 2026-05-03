package com.example.metrotransit.navigation

import androidx.compose.runtime.Composable
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import com.example.metrotransit.ui.screens.*
import com.example.metrotransit.viewmodel.MRTPassViewModel

sealed class Screen(val route: String) {
    object Splash : Screen("splash")
    object Home : Screen("home")
    object Result : Screen("result/{fromId}/{toId}") {
        fun createRoute(fromId: Int, toId: Int) = "result/$fromId/$toId"
    }
    object Stations : Screen("stations")
    object MRTPassLogin : Screen("mrtpass_login")
    object MRTPassDashboard : Screen("mrtpass_dashboard")
    object MRTPassRecharge : Screen("mrtpass_recharge")
    object MRTPassWebView : Screen("mrtpass_webview")
    object PaymentGateway : Screen("payment_gateway/{amount}") {
        fun createRoute(amount: String) = "payment_gateway/$amount"
    }
}

@Composable
fun NavGraph(navController: NavHostController) {
    val mrtPassViewModel: MRTPassViewModel = viewModel()
    
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
                onNavigateToMRTPass = {
                    navController.navigate(Screen.MRTPassLogin.route)
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
        
        // MRT Pass Flow
        composable(Screen.MRTPassLogin.route) {
            MRTPassLoginScreen(
                onLoginSuccess = {
                    navController.navigate(Screen.MRTPassDashboard.route) {
                        popUpTo(Screen.MRTPassLogin.route) { inclusive = true }
                    }
                },
                onOpenWebView = {
                    navController.navigate(Screen.MRTPassWebView.route)
                },
                onBack = { navController.popBackStack() },
                viewModel = mrtPassViewModel
            )
        }
        composable(Screen.MRTPassDashboard.route) {
            MRTPassDashboardScreen(
                onRecharge = { card ->
                    mrtPassViewModel.selectedCard = card
                    navController.navigate(Screen.MRTPassRecharge.route)
                },
                onLogout = {
                    mrtPassViewModel.logout()
                    navController.navigate(Screen.Home.route) {
                        popUpTo(Screen.MRTPassDashboard.route) { inclusive = true }
                    }
                },
                viewModel = mrtPassViewModel
            )
        }
        composable(Screen.MRTPassRecharge.route) {
            MRTPassRechargeScreen(
                onBack = { navController.popBackStack() },
                onProceedToPayment = { amount ->
                    navController.navigate(Screen.PaymentGateway.createRoute(amount))
                },
                viewModel = mrtPassViewModel
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
                        mrtPassViewModel.recharge()
                        navController.navigate(Screen.MRTPassDashboard.route) {
                            popUpTo(Screen.MRTPassDashboard.route) { inclusive = true }
                        }
                    } else {
                        navController.popBackStack()
                    }
                }
            )
        }
        composable(Screen.MRTPassWebView.route) {
            MRTPassWebViewScreen(onBack = { navController.popBackStack() })
        }
    }
}
