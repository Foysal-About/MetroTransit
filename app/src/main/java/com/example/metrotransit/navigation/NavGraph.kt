package com.example.metrotransit.navigation

import androidx.compose.runtime.Composable
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import com.example.metrotransit.ui.screens.*
import com.example.metrotransit.viewmodel.HomeViewModel
import com.example.metrotransit.viewmodel.MRTPassViewModel
import com.example.metrotransit.viewmodel.TicketViewModel
import com.example.metrotransit.data.StationData

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
    object MRTPassHistory : Screen("mrtpass_history")
    object MRTPassProfile : Screen("mrtpass_profile")
    object UpdateProfile : Screen("update_profile")
    object UpdatePassword : Screen("update_password")
    object MRTPassPaymentMethod : Screen("mrtpass_payment_method/{amount}") {
        fun createRoute(amount: String) = "mrtpass_payment_method/$amount"
    }
    object BKashPayment : Screen("bkash_payment/{amount}") {
        fun createRoute(amount: String) = "bkash_payment/$amount"
    }
    object CardPayment : Screen("card_payment/{amount}") {
        fun createRoute(amount: String) = "card_payment/$amount"
    }
    object NagadPayment : Screen("nagad_payment/{amount}") {
        fun createRoute(amount: String) = "nagad_payment/$amount"
    }
    object MRTPassWebView : Screen("mrtpass_webview")
    object FareCalculator : Screen("fare_calculator")
    object NFCResult : Screen("nfc_result")
    object PaymentGateway : Screen("payment_gateway/{amount}") {
        fun createRoute(amount: String) = "payment_gateway/$amount"
    }
    object QuickPay : Screen("quick_pay/{fromId}/{toId}") {
        fun createRoute(fromId: Int, toId: Int) = "quick_pay/$fromId/$toId"
    }
    object MyTickets : Screen("my_tickets")
    object TicketDetails : Screen("ticket_details/{ticketId}") {
        fun createRoute(ticketId: String) = "ticket_details/$ticketId"
    }
}

@Composable
fun NavGraph(
    navController: NavHostController,
    homeViewModel: HomeViewModel          // ← received from MainActivity, not created here
) {
    val mrtPassViewModel: MRTPassViewModel = viewModel()
    val ticketViewModel: TicketViewModel = viewModel()

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
                onQuickPay = { fromId, toId ->
                    navController.navigate(Screen.QuickPay.createRoute(fromId, toId))
                },
                onViewStations = {
                    navController.navigate(Screen.MyTickets.route)
                },
                onNavigateToMRTPass = {
                    navController.navigate(Screen.MRTPassLogin.route)
                },
                onNavigateToNFCResult = {
                    navController.navigate(Screen.NFCResult.route)
                },
                onNavigateToFareCalculator = {
                    navController.navigate(Screen.FareCalculator.route)
                },
                viewModel = homeViewModel
            )
        }

        composable(Screen.FareCalculator.route) {
            FareCalculatorScreen(onBack = { navController.popBackStack() })
        }

        composable(Screen.NFCResult.route) {
            NFCResultScreen(
                viewModel = homeViewModel,
                onBack = { navController.popBackStack() }
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
            val toId   = backStackEntry.arguments?.getInt("toId")   ?: 0
            ResultScreen(
                fromId = fromId,
                toId   = toId,
                onBack = { navController.popBackStack() },
                onQuickPay = { fId, tId ->
                    navController.navigate(Screen.QuickPay.createRoute(fId, tId))
                }
            )
        }

        composable(Screen.Stations.route) {
            StationListScreen(onBack = { navController.popBackStack() })
        }

        // MRT Pass flow
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
                onBack    = { navController.popBackStack() },
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
                onShowProfile = {
                    navController.navigate(Screen.MRTPassProfile.route)
                },
                onShowHistory = {
                    navController.navigate(Screen.MRTPassHistory.route)
                },
                viewModel = mrtPassViewModel
            )
        }

        composable(Screen.MRTPassRecharge.route) {
            MRTPassRechargeScreen(
                onBack             = { navController.popBackStack() },
                onProceedToPayment = { amount ->
                    navController.navigate(Screen.MRTPassPaymentMethod.createRoute(amount))
                },
                viewModel = mrtPassViewModel
            )
        }

        composable(Screen.MRTPassHistory.route) {
            RechargeHistoryScreen(
                onBack = { navController.popBackStack() },
                viewModel = mrtPassViewModel
            )
        }

        composable(Screen.MRTPassProfile.route) {
            ProfileScreen(
                onBack = { navController.popBackStack() },
                onLogout = {
                    mrtPassViewModel.logout()
                    navController.navigate(Screen.Home.route) {
                        popUpTo(Screen.Home.route) { inclusive = true }
                    }
                },
                onUpdateProfile = {
                    navController.navigate(Screen.UpdateProfile.route)
                },
                onUpdatePassword = {
                    navController.navigate(Screen.UpdatePassword.route)
                }
            )
        }

        composable(Screen.UpdateProfile.route) {
            UpdateProfileScreen(
                onBack = { navController.popBackStack() },
                onUpdateSuccess = {
                    navController.popBackStack()
                }
            )
        }

        composable(Screen.UpdatePassword.route) {
            UpdatePasswordScreen(
                onBack = { navController.popBackStack() },
                onUpdateSuccess = {
                    navController.popBackStack()
                }
            )
        }

        composable(
            route = Screen.MRTPassPaymentMethod.route,
            arguments = listOf(navArgument("amount") { type = NavType.StringType })
        ) { backStackEntry ->
            val amount = backStackEntry.arguments?.getString("amount") ?: "0.0"
            PaymentMethodSelectionScreen(
                amount = amount,
                viewModel = mrtPassViewModel,
                onBack = { navController.popBackStack() },
                onMethodSelected = { method ->
                    mrtPassViewModel.paymentMethod = method.name
                    val name = method.name.lowercase()
                    when {
                        name.contains("bkash") -> navController.navigate(Screen.BKashPayment.createRoute(amount))
                        name.contains("card") -> navController.navigate(Screen.CardPayment.createRoute(amount))
                        name.contains("nagad") -> navController.navigate(Screen.NagadPayment.createRoute(amount))
                        else -> navController.navigate(Screen.PaymentGateway.createRoute(amount))
                    }
                }
            )
        }

        composable(
            route = Screen.BKashPayment.route,
            arguments = listOf(navArgument("amount") { type = NavType.StringType })
        ) { backStackEntry ->
            val amount = backStackEntry.arguments?.getString("amount") ?: "0.0"
            BKashPaymentScreen(
                amount = amount,
                onPaymentSuccess = {
                    mrtPassViewModel.recharge()
                    navController.navigate(Screen.MRTPassDashboard.route) {
                        popUpTo(Screen.MRTPassDashboard.route) { inclusive = true }
                    }
                },
                onClose = { navController.popBackStack() }
            )
        }

        composable(
            route = Screen.CardPayment.route,
            arguments = listOf(navArgument("amount") { type = NavType.StringType })
        ) { backStackEntry ->
            val amount = backStackEntry.arguments?.getString("amount") ?: "0.0"
            CardPaymentScreen(
                amount = amount,
                onPaymentSuccess = {
                    mrtPassViewModel.recharge()
                    navController.navigate(Screen.MRTPassDashboard.route) {
                        popUpTo(Screen.MRTPassDashboard.route) { inclusive = true }
                    }
                },
                onBack = { navController.popBackStack() }
            )
        }

        composable(
            route = Screen.NagadPayment.route,
            arguments = listOf(navArgument("amount") { type = NavType.StringType })
        ) { backStackEntry ->
            val amount = backStackEntry.arguments?.getString("amount") ?: "0.0"
            NagadPaymentScreen(
                amount = amount,
                onPaymentSuccess = {
                    mrtPassViewModel.recharge()
                    navController.navigate(Screen.MRTPassDashboard.route) {
                        popUpTo(Screen.MRTPassDashboard.route) { inclusive = true }
                    }
                },
                onClose = { navController.popBackStack() }
            )
        }

        composable(
            route     = Screen.PaymentGateway.route,
            arguments = listOf(navArgument("amount") { type = NavType.StringType })
        ) { backStackEntry ->
            val amount = backStackEntry.arguments?.getString("amount") ?: "0.0"
            DummyPaymentGatewayScreen(
                amount = amount,
                paymentMethod = mrtPassViewModel.paymentMethod,
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

        composable(
            route = Screen.QuickPay.route,
            arguments = listOf(
                navArgument("fromId") { type = NavType.IntType },
                navArgument("toId") { type = NavType.IntType }
            )
        ) { backStackEntry ->
            val fromId = backStackEntry.arguments?.getInt("fromId") ?: 0
            val toId = backStackEntry.arguments?.getInt("toId") ?: 0
            QuickPayScreen(
                fromId = fromId,
                toId = toId,
                onBack = { navController.popBackStack() },
                ticketViewModel = ticketViewModel,
                onTicketClick = { ticketId ->
                    navController.navigate(Screen.TicketDetails.createRoute(ticketId))
                },
                onPaymentSuccess = {
                    val from = StationData.stations.find { it.id == fromId }?.name ?: "Unknown"
                    val to = StationData.stations.find { it.id == toId }?.name ?: "Unknown"
                    val diff = kotlin.math.abs(StationData.stations.indexOfFirst { it.id == fromId } - StationData.stations.indexOfFirst { it.id == toId })
                    val fare = "৳${20 + diff * 5}"
                    
                    ticketViewModel.addTicket(from, to, fare)
                    val newTicket = ticketViewModel.tickets.first()
                    
                    navController.navigate(Screen.TicketDetails.createRoute(newTicket.id)) {
                        popUpTo(Screen.Home.route)
                    }
                }
            )
        }

        composable(Screen.MyTickets.route) {
            MyTicketsScreen(
                viewModel = ticketViewModel,
                onBack = { navController.popBackStack() },
                onTicketClick = { ticket ->
                    navController.navigate(Screen.TicketDetails.createRoute(ticket.id))
                }
            )
        }

        composable(
            route = Screen.TicketDetails.route,
            arguments = listOf(navArgument("ticketId") { type = NavType.StringType })
        ) { backStackEntry ->
            val ticketId = backStackEntry.arguments?.getString("ticketId") ?: ""
            val ticket = ticketViewModel.tickets.find { it.id == ticketId }
            if (ticket != null) {
                TicketDetailsScreen(
                    ticket = ticket,
                    onBack = { navController.popBackStack() },
                    onClose = {
                        navController.navigate(Screen.Home.route) {
                            popUpTo(Screen.Home.route) { inclusive = true }
                        }
                    }
                )
            }
        }
    }
}