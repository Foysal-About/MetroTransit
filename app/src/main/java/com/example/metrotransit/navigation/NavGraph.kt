package com.example.metrotransit.navigation

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.window.DialogProperties
import androidx.compose.ui.unit.dp
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.dialog
import androidx.navigation.navArgument
import com.example.metrotransit.ui.screens.*
import com.example.metrotransit.ui.theme.MetroTransitTheme
import com.example.metrotransit.viewmodel.AuthViewModel
import com.example.metrotransit.viewmodel.HomeViewModel
import com.example.metrotransit.viewmodel.MRTPassViewModel
import com.example.metrotransit.viewmodel.TicketViewModel
import com.example.metrotransit.data.AppPreferences
import com.example.metrotransit.data.FareCalculator
import com.example.metrotransit.data.StationData

sealed class Screen(val route: String) {
    object Splash : Screen("splash")
    object Onboarding : Screen("onboarding")
    object Login : Screen("login")
    object Home : Screen("home")
    object Result : Screen("result/{fromId}/{toId}") {
        fun createRoute(fromId: Int, toId: Int) = "result/$fromId/$toId"
    }
    object Stations : Screen("stations")
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
    object PartnerOffers : Screen("partner_offers")
    object NFCResult : Screen("nfc_result")
    object PaymentGateway : Screen("payment_gateway/{amount}") {
        fun createRoute(amount: String) = "payment_gateway/$amount"
    }
    object QuickPay : Screen("quick_pay/{fromId}/{toId}") {
        fun createRoute(fromId: Int, toId: Int) = "quick_pay/$fromId/$toId"
    }
    /** The SSLCOMMERZ gateway, on its own page — the fare page hands over to it, not to a panel. */
    object SslCommerzCheckout : Screen("ssl_checkout/{fromId}/{toId}") {
        fun createRoute(fromId: Int, toId: Int) = "ssl_checkout/$fromId/$toId"
    }
    /**
     * SSLCOMMERZ's other front end — the hosted page a top-up is redirected to. A route rather
     * than a dialog, because that is the whole difference between the two: the rider is taken
     * to the gateway's own page instead of having it opened over the app's.
     */
    object SslCommerzTopUp : Screen("ssl_topup/{amount}") {
        fun createRoute(amount: String) = "ssl_topup/$amount"
    }
    object MyTickets : Screen("my_tickets")
    object TicketDetails : Screen("ticket_details/{ticketId}") {
        fun createRoute(ticketId: String) = "ticket_details/$ticketId"
    }
    object Journey : Screen("journey/{ticketId}?exit={exit}") {
        /** [openExitGate] makes the screen bring up the exit-gate QR straight away. */
        fun createRoute(ticketId: String, openExitGate: Boolean = false) =
            "journey/$ticketId?exit=$openExitGate"
    }
}

/** Where a returning gateway page leaves its unpaid status for the fare page to read. */
private const val GatewayNoticeKey = "ssl_gateway_notice"

@Composable
fun NavGraph(
    navController: NavHostController,
    homeViewModel: HomeViewModel          // ← received from MainActivity, not created here
) {
    val authViewModel: AuthViewModel = viewModel()
    val mrtPassViewModel: MRTPassViewModel = viewModel()
    val ticketViewModel: TicketViewModel = viewModel()

    // A validated ticket means the rider is inside the paid area and still owes a tap-out,
    // so the journey bar rides along on top of every screen until that happens.
    val activeJourney = ticketViewModel.tickets.firstOrNull { it.isInTransit }
    val currentRoute = navController.currentBackStackEntryAsState().value?.destination?.route
    val onJourneyScreen = currentRoute?.startsWith("journey/") == true
    // Nothing rides over the screens the rider is not signed in behind yet.
    val onEntryScreen = currentRoute in setOf(
        Screen.Splash.route,
        Screen.Onboarding.route,
        Screen.Login.route
    )
    val showJourneyBar = activeJourney != null && !onEntryScreen

    // Navigation restores its back stack when the process is rebuilt; a session that was not
    // kept does not come back with it. Without this, a rider whose app was killed in the
    // background returns straight into a signed-in screen. The front door is the only gate
    // the app has, so it has to hold after a restore too.
    LaunchedEffect(authViewModel.isSignedIn, currentRoute) {
        if (!authViewModel.isSignedIn && currentRoute != null && !onEntryScreen) {
            navController.navigate(Screen.Login.route) {
                popUpTo(navController.graph.id) { inclusive = true }
                launchSingleTop = true
            }
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Transparent)
    ) {
        // The bar floats over the screens instead of shrinking them, so rather than padding
        // the host, tell the screens how much room to leave at the end of their scrolling
        // content — their last card can then be scrolled out from under the bar.
        CompositionLocalProvider(
            LocalJourneyBarInset provides if (showJourneyBar) {
                ActiveJourneyBarHeight + ActiveJourneyBarGap
            } else {
                0.dp
            }
        ) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.Transparent)
            ) {
                TicketNavHost(
                    navController = navController,
                    homeViewModel = homeViewModel,
                    authViewModel = authViewModel,
                    mrtPassViewModel = mrtPassViewModel,
                    ticketViewModel = ticketViewModel
                )
            }
        }

        if (activeJourney != null && showJourneyBar) {
            ActiveJourneyBar(
                ticket = activeJourney,
                onTapOut = {
                    navController.navigate(
                        Screen.Journey.createRoute(activeJourney.id, openExitGate = true)
                    ) {
                        // Tapping out from the journey screen itself would otherwise stack a
                        // second copy of it behind the first — same screen, different flag.
                        popUpTo(Screen.Journey.route) { inclusive = true }
                        launchSingleTop = true
                    }
                },
                // On the journey screen itself there is nothing to open.
                onOpenJourney = if (onJourneyScreen) null else {
                    { navController.navigate(Screen.Journey.createRoute(activeJourney.id)) { launchSingleTop = true } }
                },
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .background(Color.Transparent)
                    .padding(bottom = ActiveJourneyBarGap)
            )
        }
    }
}

/**
 * Back, at most once per screen.
 *
 * `popBackStack()` acts on the stack, not on the screen that asked — so a second tap landing
 * while the first pop is still animating takes a second screen with it. One level deep that
 * means the app closes instead of returning home, which is what a double-tapped back button
 * looked like. A destination on its way out is no longer RESUMED, and that is the cheapest
 * way to tell the two taps apart.
 */
private fun NavHostController.popOnce() {
    val entry = currentBackStackEntry ?: return
    if (entry.lifecycle.currentState.isAtLeast(Lifecycle.State.RESUMED)) {
        popBackStack()
    }
}

@Composable
private fun TicketNavHost(
    navController: NavHostController,
    homeViewModel: HomeViewModel,
    authViewModel: AuthViewModel,
    mrtPassViewModel: MRTPassViewModel,
    ticketViewModel: TicketViewModel
) {
    // Signing out drops the session and takes the whole stack back to the front door, so
    // pressing back cannot walk into a signed-in screen afterwards.
    val signOut: () -> Unit = {
        authViewModel.signOut()
        navController.navigate(Screen.Login.route) {
            popUpTo(Screen.Home.route) { inclusive = true }
            launchSingleTop = true
        }
    }

    NavHost(
        navController = navController,
        startDestination = Screen.Splash.route
    ) {
        composable(Screen.Splash.route) {
            val context = LocalContext.current
            val preferences = remember { AppPreferences(context) }
            SplashScreen(onNavigateToHome = {
                // Welcome page on the first launch only, then the front door — unless the
                // rider asked to be kept signed in, in which case straight to the dashboard.
                val next = when {
                    !preferences.hasSeenOnboarding -> Screen.Onboarding.route
                    authViewModel.isSignedIn -> Screen.Home.route
                    else -> Screen.Login.route
                }
                navController.navigate(next) {
                    popUpTo(Screen.Splash.route) { inclusive = true }
                    launchSingleTop = true
                }
            })
        }

        composable(Screen.Onboarding.route) {
            val context = LocalContext.current
            val preferences = remember { AppPreferences(context) }
            OnboardingScreen(onContinue = {
                preferences.hasSeenOnboarding = true
                navController.navigate(Screen.Login.route) {
                    popUpTo(Screen.Onboarding.route) { inclusive = true }
                    launchSingleTop = true
                }
            })
        }

        composable(Screen.Login.route) {
            LoginScreen(
                onSignedIn = {
                    navController.navigate(Screen.Home.route) {
                        popUpTo(Screen.Login.route) { inclusive = true }
                        launchSingleTop = true
                    }
                },
                onOpenWebsite = {
                    navController.navigate(Screen.MRTPassWebView.route) { launchSingleTop = true }
                },
                viewModel = authViewModel
            )
        }

        composable(Screen.Home.route) {
            HomeScreen(
                onShowTrains = { fromId, toId ->
                    navController.navigate(Screen.Result.createRoute(fromId, toId)) { launchSingleTop = true }
                },
                onQuickPay = { fromId, toId ->
                    navController.navigate(Screen.QuickPay.createRoute(fromId, toId)) { launchSingleTop = true }
                },
                onViewTickets = {
                    navController.navigate(Screen.MyTickets.route) { launchSingleTop = true }
                },
                onViewLine = {
                    navController.navigate(Screen.Stations.route) { launchSingleTop = true }
                },
                onNavigateToMRTPass = {
                    navController.navigate(Screen.MRTPassDashboard.route) { launchSingleTop = true }
                },
                onNavigateToNFCResult = {
                    navController.navigate(Screen.NFCResult.route) { launchSingleTop = true }
                },
                onNavigateToFareCalculator = {
                    navController.navigate(Screen.FareCalculator.route) { launchSingleTop = true }
                },
                onNavigateToPartnerOffers = {
                    navController.navigate(Screen.PartnerOffers.route) { launchSingleTop = true }
                },
                viewModel = homeViewModel
            )
        }

        composable(Screen.PartnerOffers.route) {
            PartnerOffersScreen(onBack = { navController.popOnce() })
        }

        composable(Screen.FareCalculator.route) {
            FareCalculatorScreen(onBack = { navController.popOnce() })
        }

        composable(Screen.NFCResult.route) {
            NFCResultScreen(
                viewModel = homeViewModel,
                onBack = { navController.popOnce() }
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
                onBack = { navController.popOnce() },
                onQuickPay = { fId, tId ->
                    navController.navigate(Screen.QuickPay.createRoute(fId, tId)) { launchSingleTop = true }
                }
            )
        }

        composable(Screen.Stations.route) {
            StationListScreen(onBack = { navController.popOnce() })
        }

        // MRT Pass flow. The portal has no sign-in of its own — the rider is already signed
        // in to the app by the time they can reach it — so it opens on their cards.
        composable(Screen.MRTPassDashboard.route) {
            MRTPassDashboardScreen(
                onRecharge = { card ->
                    mrtPassViewModel.selectedCard = card
                    navController.navigate(Screen.MRTPassRecharge.route) { launchSingleTop = true }
                },
                onLogout = signOut,
                onShowProfile = {
                    navController.navigate(Screen.MRTPassProfile.route) { launchSingleTop = true }
                },
                onShowHistory = {
                    navController.navigate(Screen.MRTPassHistory.route) { launchSingleTop = true }
                },
                onOpenWebsite = {
                    navController.navigate(Screen.MRTPassWebView.route) { launchSingleTop = true }
                },
                viewModel = mrtPassViewModel
            )
        }

        composable(Screen.MRTPassRecharge.route) {
            MRTPassRechargeScreen(
                onBack             = { navController.popOnce() },
                onProceedToPayment = { amount ->
                    navController.navigate(Screen.MRTPassPaymentMethod.createRoute(amount)) { launchSingleTop = true }
                },
                viewModel = mrtPassViewModel
            )
        }

        composable(Screen.MRTPassHistory.route) {
            RechargeHistoryScreen(
                onBack = { navController.popOnce() },
                viewModel = mrtPassViewModel
            )
        }

        composable(Screen.MRTPassProfile.route) {
            ProfileScreen(
                onBack = { navController.popOnce() },
                onLogout = signOut,
                onUpdateProfile = {
                    navController.navigate(Screen.UpdateProfile.route) { launchSingleTop = true }
                },
                onUpdatePassword = {
                    navController.navigate(Screen.UpdatePassword.route) { launchSingleTop = true }
                }
            )
        }

        composable(Screen.UpdateProfile.route) {
            UpdateProfileScreen(
                onBack = { navController.popOnce() },
                onUpdateSuccess = {
                    navController.popOnce()
                }
            )
        }

        composable(Screen.UpdatePassword.route) {
            UpdatePasswordScreen(
                onBack = { navController.popOnce() },
                onUpdateSuccess = {
                    navController.popOnce()
                }
            )
        }

        composable(
            route = Screen.MRTPassPaymentMethod.route,
            arguments = listOf(navArgument("amount") { type = NavType.StringType })
        ) { backStackEntry ->
            val amount = backStackEntry.arguments?.getString("amount") ?: "0.0"

            // A hosted session that came back without the money says so here, on the page the
            // rider picks a method from — not on the gateway page they have already left.
            val gatewayNotice by backStackEntry.savedStateHandle
                .getStateFlow<String?>(GatewayNoticeKey, null)
                .collectAsState()

            PaymentMethodSelectionScreen(
                amount = amount,
                viewModel = mrtPassViewModel,
                gatewayNotice = gatewayNotice,
                onBack = { navController.popOnce() },
                onMethodSelected = { method ->
                    mrtPassViewModel.paymentMethod = method.name
                    // Last attempt's notice belongs to last attempt.
                    backStackEntry.savedStateHandle[GatewayNoticeKey] = null
                    // One way to pay for a top-up, so there is nothing to branch on. The
                    // channel is picked on the gateway's own page, which is where the rider
                    // is about to be.
                    navController.navigate(Screen.SslCommerzTopUp.createRoute(amount)) {
                        launchSingleTop = true
                    }
                }
            )
        }

        // The hosted checkout, as a page of its own. Unlike the ticket gateway this is not a
        // dialog: a redirect replaces what the rider was looking at, and the whole point of
        // the address bar at the top of it is that there is nothing of ours around it.
        composable(
            route = Screen.SslCommerzTopUp.route,
            arguments = listOf(navArgument("amount") { type = NavType.StringType })
        ) { backStackEntry ->
            val card = mrtPassViewModel.selectedCard

            // No card to credit means nothing to collect for — a process rebuild that landed
            // back on this route with the portal's selection gone.
            if (card == null) {
                LaunchedEffect(Unit) { navController.popOnce() }
            }

            /** Back to the method list, carrying why the money never moved. */
            val returnUnpaid: (String) -> Unit = { message ->
                navController.previousBackStackEntry
                    ?.savedStateHandle?.set(GatewayNoticeKey, message)
                navController.popOnce()
            }

            if (card != null) {
                SslCommerzTopUpCheckoutScreen(
                    // The total the recharge page worked out, gateway fee included — the
                    // rider is charged that, and the card is credited the amount underneath it.
                    amount = backStackEntry.arguments?.getString("amount")?.toDoubleOrNull() ?: 0.0,
                    mrtCardNumber = card.cardNumber,
                    cardHolderName = card.cardName,
                    onCancel = { returnUnpaid("Payment cancelled. Nothing was charged.") },
                    onResult = { ipn ->
                        when (ipn.status) {
                            SslCommerzStatus.VALID -> {
                                // Credited against the gateway's own reference, so the row it
                                // leaves in the history is the one on the rider's receipt.
                                mrtPassViewModel.recharge(ipn.tranId)
                                navController.navigate(Screen.MRTPassDashboard.route) {
                                    popUpTo(Screen.MRTPassDashboard.route) { inclusive = true }
                                    launchSingleTop = true
                                }
                            }

                            SslCommerzStatus.CANCELLED ->
                                returnUnpaid("Payment cancelled. Nothing was charged.")

                            SslCommerzStatus.FAILED ->
                                returnUnpaid("The bank declined the transaction. Try another method.")

                            SslCommerzStatus.EXPIRED ->
                                returnUnpaid("The payment session timed out. Nothing was charged — start again.")
                        }
                    }
                )
            }
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
                        launchSingleTop = true
                    }
                },
                onClose = { navController.popOnce() }
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
                        launchSingleTop = true
                    }
                },
                onBack = { navController.popOnce() }
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
                        launchSingleTop = true
                    }
                },
                onClose = { navController.popOnce() }
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
                            launchSingleTop = true
                        }
                    } else {
                        navController.popOnce()
                    }
                }
            )
        }

        composable(Screen.MRTPassWebView.route) {
            MRTPassWebViewScreen(onBack = { navController.popOnce() })
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

            // A session that came back unpaid says so here rather than on the gateway page it
            // was abandoned on. Held on this entry's own handle, so it survives the gateway
            // being popped and dies with the fare page itself.
            val gatewayNotice by backStackEntry.savedStateHandle
                .getStateFlow<String?>(GatewayNoticeKey, null)
                .collectAsState()

            QuickPayScreen(
                fromId = fromId,
                toId = toId,
                onBack = { navController.popOnce() },
                gatewayNotice = gatewayNotice,
                ticketViewModel = ticketViewModel,
                onTicketClick = { ticketId ->
                    navController.navigate(Screen.TicketDetails.createRoute(ticketId)) { launchSingleTop = true }
                },
                onProceedToPayment = { payFromId, payToId ->
                    // Last attempt's notice belongs to last attempt.
                    backStackEntry.savedStateHandle[GatewayNoticeKey] = null
                    navController.navigate(
                        Screen.SslCommerzCheckout.createRoute(payFromId, payToId)
                    ) { launchSingleTop = true }
                }
            )
        }

        // A dialog destination, not a page: the gateway comes up over the fare page and dims
        // it, the way the hosted Easy Checkout overlays the merchant's own site. Its own X and
        // back gesture are the only ways out, so a tap on the dimmed page cannot drop a live
        // session — hence both dismissals off.
        dialog(
            route = Screen.SslCommerzCheckout.route,
            dialogProperties = DialogProperties(
                dismissOnBackPress = false,
                dismissOnClickOutside = false,
                usePlatformDefaultWidth = false
            ),
            arguments = listOf(
                navArgument("fromId") { type = NavType.IntType },
                navArgument("toId") { type = NavType.IntType }
            )
        ) { backStackEntry ->
            val fromId = backStackEntry.arguments?.getInt("fromId") ?: 0
            val toId = backStackEntry.arguments?.getInt("toId") ?: 0
            val fromStation = StationData.stations.find { it.id == fromId }
            val toStation = StationData.stations.find { it.id == toId }

            /** Back to the fare page, carrying why the money never moved. */
            val returnUnpaid: (String) -> Unit = { message ->
                navController.previousBackStackEntry
                    ?.savedStateHandle?.set(GatewayNoticeKey, message)
                navController.popOnce()
            }

            SslCommerzCheckoutScreen(
                // Re-read from the ids rather than passed through the route: the gateway must
                // charge for the journey the ticket will be issued for, not a stale fare.
                amount = FareCalculator.fare(fromStation, toStation),
                productName = "Single Journey · ${fromStation?.name ?: "?"} → ${toStation?.name ?: "?"}",
                onCancel = { returnUnpaid("Payment cancelled. Nothing was charged.") },
                onResult = { result ->
                    when (result.status) {
                        SslCommerzStatus.VALID -> {
                            val newTicket = ticketViewModel.addTicket(
                                from = fromStation,
                                to = toStation
                            )
                            // The gateway and the fare page both go: the payment is done, and
                            // neither is somewhere to come back to from a live ticket.
                            navController.navigate(Screen.TicketDetails.createRoute(newTicket.id)) {
                                popUpTo(Screen.Home.route)
                                launchSingleTop = true
                            }
                        }

                        SslCommerzStatus.CANCELLED ->
                            returnUnpaid("Payment cancelled. Nothing was charged.")

                        SslCommerzStatus.FAILED ->
                            returnUnpaid("The bank declined the transaction. Try another method.")

                        // The popup keeps no clock of its own, so this only arrives if the
                        // session was already dead when it was opened. Same landing either way.
                        SslCommerzStatus.EXPIRED ->
                            returnUnpaid("The payment session expired. Nothing was charged.")
                    }
                }
            )
        }

        composable(Screen.MyTickets.route) {
            MyTicketsScreen(
                viewModel = ticketViewModel,
                onBack = { navController.popOnce() },
                onTicketClick = { ticket ->
                    navController.navigate(Screen.TicketDetails.createRoute(ticket.id)) { launchSingleTop = true }
                }
            )
        }

        composable(
            route = Screen.TicketDetails.route,
            arguments = listOf(navArgument("ticketId") { type = NavType.StringType })
        ) { backStackEntry ->
            val ticketId = backStackEntry.arguments?.getString("ticketId") ?: ""
            val ticket = ticketViewModel.tickets.find { it.id == ticketId }

            // The ticket can be gone — cleared, or the process restarted with the id still
            // in the back stack. Rendering nothing leaves a blank screen, so step back out.
            if (ticket == null) {
                LaunchedEffect(ticketId) { navController.popOnce() }
            }

            if (ticket != null) {
                TicketDetailsScreen(
                    ticket = ticket,
                    onBack = { navController.popOnce() },
                    onClose = {
                        navController.navigate(Screen.Home.route) {
                            popUpTo(Screen.Home.route) { inclusive = true }
                            launchSingleTop = true
                        }
                    },
                    onValidate = {
                        // Gate reader accepted the QR — the rider is now inside the station.
                        ticketViewModel.validateTicket(ticketId)
                        navController.navigate(Screen.Journey.createRoute(ticketId)) { launchSingleTop = true }
                    },
                    onOpenJourney = {
                        navController.navigate(Screen.Journey.createRoute(ticketId)) { launchSingleTop = true }
                    },
                    onEntryWindowLapsed = { ticketViewModel.expireTicket(ticketId) }
                )
            }
        }

        composable(
            route = Screen.Journey.route,
            arguments = listOf(
                navArgument("ticketId") { type = NavType.StringType },
                navArgument("exit") {
                    type = NavType.BoolType
                    defaultValue = false
                }
            )
        ) { backStackEntry ->
            val ticketId = backStackEntry.arguments?.getString("ticketId") ?: ""
            val openExitGate = backStackEntry.arguments?.getBoolean("exit") ?: false
            val ticket = ticketViewModel.tickets.find { it.id == ticketId }

            if (ticket == null) {
                LaunchedEffect(ticketId) { navController.popOnce() }
            }

            if (ticket != null) {
                JourneyScreen(
                    ticket = ticket,
                    openExitGateOnLaunch = openExitGate,
                    onBack = { navController.popOnce() },
                    onExtendJourney = { station, paymentMethod ->
                        ticketViewModel.extendJourney(ticketId, station, paymentMethod)
                    },
                    onCompleteJourney = { ticketViewModel.completeJourney(ticketId) },
                    onFinish = {
                        navController.navigate(Screen.Home.route) {
                            popUpTo(Screen.Home.route) { inclusive = true }
                            launchSingleTop = true
                        }
                    }
                )
            }
        }
    }
}