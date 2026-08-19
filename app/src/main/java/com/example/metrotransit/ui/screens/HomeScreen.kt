package com.example.metrotransit.ui.screens

import android.app.Activity
import android.content.pm.PackageManager
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.AltRoute
import androidx.compose.material.icons.automirrored.filled.FactCheck
import androidx.compose.material.icons.automirrored.filled.Login
import androidx.compose.material.icons.automirrored.filled.TrendingUp
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.AsyncImage
import com.airbnb.lottie.compose.*
import com.example.metrotransit.data.MetroStation
import com.example.metrotransit.data.StationData
import com.example.metrotransit.nfc.NfcManager
import com.example.metrotransit.ui.theme.MetroSuccess
import com.example.metrotransit.ui.theme.MetroTransitTheme
import com.example.metrotransit.viewmodel.HomeViewModel
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority
import com.google.android.gms.tasks.CancellationTokenSource
import kotlinx.coroutines.tasks.await

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    onShowTrains: (Int, Int) -> Unit,
    onQuickPay: (Int, Int) -> Unit,
    onViewTickets: () -> Unit,
    onViewLine: () -> Unit,
    onNavigateToMRTPass: () -> Unit,
    onNavigateToNFCResult: () -> Unit,
    onNavigateToFareCalculator: () -> Unit,
    onNavigateToPartnerOffers: () -> Unit = {},
    viewModel: HomeViewModel = viewModel()
) {
    val scrollState = rememberScrollState()
    val sheetState = rememberModalBottomSheetState()
    val context = LocalContext.current
    val activity = context as Activity
    val nfcManager = remember { NfcManager(activity) }
    
    val extendedColors = MetroTransitTheme.extendedColors
    
    var rotationAngle by remember { mutableStateOf(0f) }
    val animatedRotation by animateFloatAsState(
        targetValue = rotationAngle,
        animationSpec = spring(stiffness = Spring.StiffnessMedium)
    )

    val locationClient = remember { LocationServices.getFusedLocationProviderClient(context) }

    val permissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        if (permissions[android.Manifest.permission.ACCESS_FINE_LOCATION] == true ||
            permissions[android.Manifest.permission.ACCESS_COARSE_LOCATION] == true
        ) {
            viewModel.isLocating = true
        }
    }

    if (viewModel.isLocating) {
        LaunchedEffect(Unit) {
            try {
                if (ContextCompat.checkSelfPermission(context, android.Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED ||
                    ContextCompat.checkSelfPermission(context, android.Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
                    
                    var location = locationClient.lastLocation.await()
                    
                    if (location == null) {
                        val cts = CancellationTokenSource()
                        location = locationClient.getCurrentLocation(
                            Priority.PRIORITY_HIGH_ACCURACY,
                            cts.token
                        ).await()
                    }

                    if (location != null) {
                        kotlinx.coroutines.delay(1000)
                        viewModel.findNearestStation(location.latitude, location.longitude)
                    }
                }
            } catch (e: Exception) {
                android.util.Log.e("HomeScreen", "Error finding location: ${e.message}")
            } finally {
                viewModel.isLocating = false
            }
        }
    }

    if (viewModel.showScanSheet) {
        DisposableEffect(Unit) {
            nfcManager.startScanning(
                onScanningStatusChange = { viewModel.isScanning = it },
                onResponseRead = { response ->
                    viewModel.processNfcResponse(response)
                },
                onError = { error ->
                    viewModel.scanError = error
                }
            )
            onDispose {
                nfcManager.stopScanning()
            }
        }
    }

    val isScanning = viewModel.isScanning
    val hasResult = viewModel.scannedBalance != null || viewModel.scanError != null

    LaunchedEffect(isScanning, hasResult) {
        if (!isScanning && hasResult && viewModel.showScanSheet) {
            viewModel.showScanSheet = false
            onNavigateToNFCResult()
        }
    }

    if (viewModel.showScanSheet) {
        ModalBottomSheet(
            onDismissRequest = {
                viewModel.showScanSheet = false
                viewModel.resetScan()
            },
            sheetState = sheetState,
            dragHandle = null,
            containerColor = Color(0xFF1C1C1E),
            shape = RoundedCornerShape(topStart = 32.dp, topEnd = 32.dp)
        ) {
            NFCScanBottomSheetContent(
                isScanning = isScanning,
                onCancel = {
                    viewModel.showScanSheet = false
                    viewModel.resetScan()
                }
            )
        }
    }

    Scaffold(
        containerColor = Color.Transparent,
        topBar = {
            HomeTopBar(
                fromStation = viewModel.fromStation,
                toStation = viewModel.toStation,
                onViewTickets = onViewTickets,
                onViewLine = onViewLine
            )
        }
    ) { padding ->
        val routeReady = viewModel.fromStation != null &&
            viewModel.toStation != null &&
            viewModel.fromStation!!.id != viewModel.toStation!!.id

        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(extendedColors.backgroundGradient)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
                    .verticalScroll(scrollState)
                    // Inside the scroll, so an active journey bar can be scrolled clear of.
                    .padding(bottom = LocalJourneyBarInset.current),
                // One rhythm for the whole page. Every block below owns its horizontal
                // padding only — vertical spacing is the column's job, so nothing drifts.
                verticalArrangement = Arrangement.spacedBy(HomeSectionSpacing)
            ) {
                // ── Station selector card ─────────────────────────────────────
                HomePanel(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = HomeGutter)
                ) {
                    Column(
                        modifier = Modifier.padding(20.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            JourneyPoint(
                                icon = Icons.Default.MyLocation,
                                color = MaterialTheme.colorScheme.primary
                            )
                            Spacer(modifier = Modifier.width(16.dp))
                            StationSelector(
                                label = "From Station",
                                selectedStation = viewModel.fromStation,
                                onStationSelected = { viewModel.setFrom(it) },
                                modifier = Modifier.weight(1f),
                                isLocating = viewModel.isLocating
                            )

                            IconButton(
                                onClick = {
                                    if (ContextCompat.checkSelfPermission(context, android.Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
                                        viewModel.isLocating = true
                                    } else {
                                        permissionLauncher.launch(arrayOf(
                                            android.Manifest.permission.ACCESS_FINE_LOCATION,
                                            android.Manifest.permission.ACCESS_COARSE_LOCATION
                                        ))
                                    }
                                },
                                modifier = Modifier
                                    .size(40.dp)
                                    .clip(CircleShape)
                                    .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.12f))
                            ) {
                                if (viewModel.isLocating) {
                                    CircularProgressIndicator(modifier = Modifier.size(20.dp), strokeWidth = 2.dp, color = MaterialTheme.colorScheme.primary)
                                } else {
                                    Icon(
                                        Icons.Default.MyLocation,
                                        contentDescription = "Find Nearest",
                                        modifier = Modifier.size(20.dp),
                                        tint = MaterialTheme.colorScheme.primary
                                    )
                                }
                            }
                        }

                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.height(32.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .width(26.dp)
                                    .fillMaxHeight(),
                                contentAlignment = Alignment.Center
                            ) {
                                Box(
                                    modifier = Modifier
                                        .fillMaxHeight()
                                        .width(2.dp)
                                        .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.2f))
                                )
                            }
                            Spacer(modifier = Modifier.width(8.dp))
                            IconButton(
                                onClick = {
                                    viewModel.swapStations()
                                    rotationAngle += 180f
                                },
                                modifier = Modifier
                                    .size(36.dp)
                                    .clip(CircleShape)
                                    .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.12f))
                            ) {
                                Icon(
                                    Icons.Default.SwapVert,
                                    contentDescription = "Swap",
                                    modifier = Modifier
                                        .size(20.dp)
                                        .graphicsLayer { rotationZ = animatedRotation },
                                    tint = MaterialTheme.colorScheme.primary
                                )
                            }
                        }

                        Row(verticalAlignment = Alignment.CenterVertically) {
                            JourneyPoint(
                                icon = Icons.Default.LocationOn,
                                color = MetroSuccess
                            )
                            Spacer(modifier = Modifier.width(16.dp))
                            StationSelector(
                                label = "To Station",
                                selectedStation = viewModel.toStation,
                                onStationSelected = { viewModel.setTo(it) }
                            )
                        }
                    }
                }

                // ── Journey actions ───────────────────────────────────────────
                // Both buttons act on the same pair of stations, so they share a state:
                // neither is offered until a real route is picked.
                Column(
                    modifier = Modifier.padding(horizontal = HomeGutter),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    HomeActionButton(
                        text = "Find Next Trains",
                        icon = Icons.AutoMirrored.Filled.AltRoute,
                        accent = MaterialTheme.colorScheme.primary,
                        enabled = routeReady,
                        onClick = {
                            onShowTrains(
                                viewModel.fromStation?.id ?: 0,
                                viewModel.toStation?.id ?: 0
                            )
                        }
                    )

                    HomeActionButton(
                        text = "Buy QR Ticket",
                        icon = Icons.Default.QrCodeScanner,
                        accent = MaterialTheme.colorScheme.primary,
                        style = HomeButtonStyle.Tonal,
                        enabled = routeReady,
                        onClick = {
                            onQuickPay(
                                viewModel.fromStation?.id ?: 0,
                                viewModel.toStation?.id ?: 0
                            )
                        }
                    )

                    if (!routeReady) {
                        Text(
                            "Pick a start and destination station to continue",
                            style = MaterialTheme.typography.labelSmall,
                            color = extendedColors.textSecondary,
                            textAlign = TextAlign.Center,
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                }

                HomeSectionHeader(title = "Services & Portal")

                // ── MRT Pass Portal ───────────────────────────────────────────
                ServiceCard(
                    icon = Icons.Default.CreditCard,
                    accent = extendedColors.accentPass,
                    title = "MRT Pass Portal",
                    subtitle = "Manage cards & recharges",
                    features = listOf(
                        Icons.Default.AccountBalanceWallet to "Balance",
                        Icons.Default.AddCard to "Recharge",
                        Icons.AutoMirrored.Filled.FactCheck to "Status"
                    ),
                    actionText = "Access Portal",
                    actionIcon = Icons.AutoMirrored.Filled.Login,
                    onAction = onNavigateToMRTPass
                )

                // ── Metro Buddy / NFC ─────────────────────────────────────────
                ServiceCard(
                    icon = Icons.Default.Contactless,
                    accent = extendedColors.accentNfc,
                    title = "Metro Buddy",
                    subtitle = "Scan physical card via NFC",
                    features = listOf(
                        Icons.Default.AccountBalanceWallet to "Read Card",
                        Icons.Default.History to "History",
                        Icons.AutoMirrored.Filled.TrendingUp to "Insights"
                    ),
                    actionText = "Tap Card to Scan",
                    actionIcon = Icons.Default.Contactless,
                    onAction = {
                        viewModel.resetScan()
                        viewModel.showScanSheet = true
                    }
                )

                // ── Fare Calculator ───────────────────────────────────────────
                ServiceCard(
                    icon = Icons.Default.Calculate,
                    accent = MaterialTheme.colorScheme.primary,
                    title = "Fare Calculator",
                    subtitle = "Check timetable & journey fare",
                    features = emptyList(),
                    actionText = "Check Schedule & Fare",
                    actionIcon = Icons.Default.Search,
                    onAction = onNavigateToFareCalculator
                )

                HomeSectionHeader(
                    title = "Featured Deals",
                    actionText = "See all",
                    onAction = onNavigateToPartnerOffers
                )

                // ── Advertisements (horizontally scrollable) ──────────────────
                LazyRow(
                    contentPadding = PaddingValues(horizontal = HomeGutter),
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
//                    val ads = listOf(
////                        AdItem(
////                            title = "১০% ইনস্ট্যান্ট ক্যাশব্যাক",
////                            description = "বিকাশ অ্যাপ দিয়ে পেমেন্ট করলেই অফারটি উপভোগ করুন",
////                            themeColor = Color(0xFFE2136E),
////                            icon = Icons.Default.Payments,
////                            imageUrl = "https://www.bkash.com/uploads/images/Campaign-Banner-En.jpg"
////                        ),
////                        AdItem("Foodpanda", "Hungry? Order now and get free delivery to stations!", Color(0xFFFF2B44), Icons.Default.Restaurant),
////                        AdItem("Travel Insurance", "Insure your journey for just ৳5 per trip.", Color(0xFF007AFF), Icons.Default.Security)
//                    )
                    // Bank / card-scheme promotions (designed banners)
                    items(featuredPromos) { promo ->
                        PromoBannerCard(promo, onClick = onNavigateToPartnerOffers)
                    }
//                    items(ads) { ad ->
//                        AdvertisementCard(ad)
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))
            }
        }
    }
//}

/**
 * The home header, built around the line the app actually serves.
 *
 * MRT Line-6 is the only line running in Dhaka today — DMTCL's first — so the bar names it,
 * carries its map colour (#006747, see [com.example.metrotransit.ui.theme.Line6Light]) in
 * the mark, and draws the line itself underneath: one dot per station from Uttara North to
 * the southern terminus, with the rider's own journey lit along it.
 */
@Composable
private fun HomeTopBar(
    fromStation: MetroStation?,
    toStation: MetroStation?,
    onViewTickets: () -> Unit,
    onViewLine: () -> Unit
) {
    val extendedColors = MetroTransitTheme.extendedColors
    val line = extendedColors.line6

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .statusBarsPadding()
            .padding(top = 6.dp, bottom = 12.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = HomeGutter + 4.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // The line mark: Line-6 green, the metro glyph, and the route number the way
            // every metro in the world labels a line.
            Surface(
                shape = RoundedCornerShape(13.dp),
                color = line,
                modifier = Modifier.size(42.dp)
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    Icon(
                        Icons.Default.DirectionsSubway,
                        contentDescription = null,
                        tint = Color.White,
                        modifier = Modifier.size(17.dp)
                    )
//                    Text(
//                        "6",
//                        color = Color.White,
//                        fontSize = 10.sp,
//                        fontWeight = FontWeight.Black,
//                        lineHeight = 11.sp
//                    )
                }
            }

            Spacer(modifier = Modifier.width(12.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    "Dhaka Metro Rail",
                    fontSize = 17.sp,
                    fontWeight = FontWeight.SemiBold,
                    letterSpacing = (-0.2).sp,
                    color = extendedColors.textPrimary
                )
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        "MRT Line 6",
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold,
                        color = line
                    )
                    Text(
                        "  ·  DMTCL",
                        style = MaterialTheme.typography.labelSmall,
                        color = extendedColors.textSecondary
                    )
                }
            }

            IconButton(
                onClick = onViewTickets,
                modifier = Modifier
                    .size(40.dp)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.12f))
            ) {
                Icon(
                    Icons.Default.ConfirmationNumber,
                    contentDescription = "My Tickets",
                    modifier = Modifier.size(20.dp),
                    tint = MaterialTheme.colorScheme.primary
                )
            }
        }

        Spacer(modifier = Modifier.height(10.dp))

        Line6Strip(
            fromStation = fromStation,
            toStation = toStation,
            onClick = onViewLine
        )
    }
}

/**
 * The line, drawn to scale: every station as a dot in running order, the picked journey
 * filled in Line-6 green between its two ends. Doubles as the header's ornament and as a
 * read on where the rider is going, without a word of explanation.
 */
@Composable
private fun Line6Strip(
    fromStation: MetroStation?,
    toStation: MetroStation?,
    onClick: () -> Unit
) {
    val extendedColors = MetroTransitTheme.extendedColors
    val line = extendedColors.line6
    val stations = StationData.stations

    val fromIndex = stations.indexOfFirst { it.id == fromStation?.id }
    val toIndex = stations.indexOfFirst { it.id == toStation?.id }
    val hasJourney = fromIndex >= 0 && toIndex >= 0 && fromIndex != toIndex

    // The line is the one thing on this page that stands for the whole network, so it is
    // also the way into the station list — which otherwise had no entry point at all.
    Box(
        modifier = Modifier
            .clickable(onClick = onClick)
            .padding(horizontal = HomeGutter + 8.dp, vertical = 4.dp)
    ) {
        Canvas(
            modifier = Modifier
                .fillMaxWidth()
                .height(14.dp)
        ) {
            if (stations.size < 2) return@Canvas

            val y = size.height / 2
            val step = size.width / (stations.size - 1)

            drawLine(
                color = line.copy(alpha = 0.22f),
                start = Offset(0f, y),
                end = Offset(size.width, y),
                strokeWidth = 3.dp.toPx(),
                cap = StrokeCap.Round
            )

            if (hasJourney) {
                val startX = step * minOf(fromIndex, toIndex)
                val endX = step * maxOf(fromIndex, toIndex)
                drawLine(
                    color = line,
                    start = Offset(startX, y),
                    end = Offset(endX, y),
                    strokeWidth = 3.dp.toPx(),
                    cap = StrokeCap.Round
                )
            }

            stations.forEachIndexed { index, _ ->
                val x = step * index
                val isEnd = index == fromIndex || index == toIndex

                if (isEnd) {
                    // A ring, the way an interchange is marked on a metro map.
                    drawCircle(color = line, radius = 4.5.dp.toPx(), center = Offset(x, y))
                    drawCircle(
                        color = extendedColors.surface,
                        radius = 2.dp.toPx(),
                        center = Offset(x, y)
                    )
                } else {
                    drawCircle(
                        color = line.copy(alpha = 0.35f),
                        radius = 2.dp.toPx(),
                        center = Offset(x, y)
                    )
                }
            }
        }

    }
}

/** The page gutter. Cards, buttons and headers all start on this line. */
private val HomeGutter = 16.dp

/** Vertical rhythm between the page's blocks. */
private val HomeSectionSpacing = 16.dp

/** One corner radius for every card on the page. */
private val HomeCardCorner = 24.dp

/** Panels nested inside a card, one step tighter so the nesting is legible. */
private val HomeInnerCorner = 16.dp

/** Buttons: one height and one corner, whatever the card they sit in. */
private val HomeButtonHeight = 56.dp
private val HomeButtonCorner = 16.dp

/**
 * How solid a home surface is. Just short of opaque, so the page's gradient shows through
 * as a tint and the panels still read as one flat, quiet material.
 */
private const val HomePanelAlpha = 0.9f

/**
 * A plain home panel: one solid-ish fill, one hairline border, no light and no texture.
 */
@Composable
private fun HomePanel(
    modifier: Modifier = Modifier,
    cornerRadius: androidx.compose.ui.unit.Dp = HomeCardCorner,
    onClick: (() -> Unit)? = null,
    content: @Composable () -> Unit
) {
    val extendedColors = MetroTransitTheme.extendedColors

    Surface(
        modifier = if (onClick != null) modifier.clickable { onClick() } else modifier,
        shape = RoundedCornerShape(cornerRadius),
        color = extendedColors.surface.copy(alpha = HomePanelAlpha),
        border = androidx.compose.foundation.BorderStroke(1.dp, extendedColors.glassBorder),
        content = content
    )
}

/** How loudly a [HomeActionButton] speaks. */
private enum class HomeButtonStyle { Filled, Tonal }

/**
 * The page's action button: a flat accent fill, or a light tint of the same accent for the
 * secondary of a pair. Same height, same corner, same type — only the colour changes.
 */
@Composable
private fun HomeActionButton(
    text: String,
    icon: ImageVector,
    accent: Color,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    style: HomeButtonStyle = HomeButtonStyle.Filled
) {
    val extendedColors = MetroTransitTheme.extendedColors
    val filled = style == HomeButtonStyle.Filled

    Button(
        onClick = onClick,
        enabled = enabled,
        modifier = modifier
            .fillMaxWidth()
            .height(HomeButtonHeight),
        shape = RoundedCornerShape(HomeButtonCorner),
        elevation = null,
        border = if (filled) null else androidx.compose.foundation.BorderStroke(1.dp, accent.copy(alpha = 0.35f)),
        colors = ButtonDefaults.buttonColors(
            containerColor = if (filled) accent else accent.copy(alpha = 0.12f),
            contentColor = if (filled) Color.White else accent,
            disabledContainerColor = extendedColors.textSecondary.copy(alpha = 0.12f),
            disabledContentColor = extendedColors.textSecondary.copy(alpha = 0.6f)
        )
    ) {
        Icon(icon, contentDescription = null, modifier = Modifier.size(20.dp))
        Spacer(modifier = Modifier.width(10.dp))
        Text(text, style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)
    }
}

/** The rounded accent tile that heads a card, and the smaller one in a feature strip. */
@Composable
private fun HomeIconTile(
    icon: ImageVector,
    accent: Color,
    size: androidx.compose.ui.unit.Dp = 52.dp,
    cornerRadius: androidx.compose.ui.unit.Dp = 16.dp
) {
    Surface(
        modifier = Modifier.size(size),
        shape = RoundedCornerShape(cornerRadius),
        color = accent.copy(alpha = 0.14f)
    ) {
        Icon(
            icon,
            contentDescription = null,
            tint = accent,
            modifier = Modifier.padding(size * 0.27f)
        )
    }
}

/**
 * A titled break in the page, with an optional trailing action. Using one composable for
 * both headers keeps "Services & Portal" and "Featured Deals" on the same baseline.
 *
 * Named apart from the package's other `SectionHeader` on purpose — a single-argument call
 * would otherwise resolve to that one and pick up its overline styling.
 */
@Composable
private fun HomeSectionHeader(
    title: String,
    actionText: String? = null,
    onAction: (() -> Unit)? = null
) {
    val extendedColors = MetroTransitTheme.extendedColors

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = HomeGutter + 4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            title,
            style = MaterialTheme.typography.titleSmall,
            fontWeight = FontWeight.Bold,
            color = extendedColors.textPrimary
        )
        Spacer(modifier = Modifier.weight(1f))
        if (actionText != null && onAction != null) {
            Row(
                modifier = Modifier
                    .clip(RoundedCornerShape(8.dp))
                    .clickable(onClick = onAction)
                    .padding(horizontal = 6.dp, vertical = 2.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    actionText,
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )
                Icon(
                    Icons.Default.ChevronRight,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(16.dp)
                )
            }
        }
    }
}

/**
 * One service on the home page: an accented header, an optional strip of what the service
 * covers, and a single action. Every card on the page is built from this, so they can only
 * differ in the ways they are meant to — their accent, their words and their light.
 */
@Composable
private fun ServiceCard(
    icon: ImageVector,
    accent: Color,
    title: String,
    subtitle: String,
    features: List<Pair<ImageVector, String>>,
    actionText: String,
    actionIcon: ImageVector,
    onAction: () -> Unit
) {
    val extendedColors = MetroTransitTheme.extendedColors

    HomePanel(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = HomeGutter),
        onClick = onAction
    ) {
        Column(
            modifier = Modifier.padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                HomeIconTile(icon = icon, accent = accent)
                Spacer(modifier = Modifier.width(14.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        title,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = extendedColors.textPrimary
                    )
                    Text(
                        subtitle,
                        style = MaterialTheme.typography.labelSmall,
                        color = extendedColors.textSecondary
                    )
                }
                Icon(
                    Icons.Default.ChevronRight,
                    contentDescription = null,
                    tint = extendedColors.textSecondary.copy(alpha = 0.7f),
                    modifier = Modifier.size(20.dp)
                )
            }

            if (features.isNotEmpty()) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(HomeInnerCorner))
                        .background(extendedColors.textSecondary.copy(alpha = 0.08f))
                        .padding(vertical = 14.dp, horizontal = 12.dp),
                    horizontalArrangement = Arrangement.SpaceAround
                ) {
                    features.forEach { (featureIcon, label) ->
                        CardFeatureItem(icon = featureIcon, label = label, themeColor = accent)
                    }
                }
            }

            HomeActionButton(
                text = actionText,
                icon = actionIcon,
                accent = accent,
                onClick = onAction
            )
        }
    }
}

@Composable
fun AdvertisementCard(ad: AdItem) {
    val extendedColors = MetroTransitTheme.extendedColors
    HomePanel(
        modifier = Modifier
            .width(300.dp)
            .height(FEATURED_CARD_HEIGHT)
    ) {
        if (ad.imageUrl != null) {
            Box(modifier = Modifier.fillMaxSize()) {
                AsyncImage(
                    model = ad.imageUrl,
                    contentDescription = ad.title,
                    modifier = Modifier.fillMaxSize(),
                    contentScale = androidx.compose.ui.layout.ContentScale.FillBounds,
                    onError = { 
                        android.util.Log.e("Coil", "Failed to load image: ${it.result.throwable.message}")
                    }
                )
                // Fallback text if image fails to load or as overlay
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(androidx.compose.ui.graphics.Brush.verticalGradient(listOf(Color.Transparent, Color.Black.copy(alpha = 0.5f))))
                        .padding(16.dp),
                    verticalArrangement = Arrangement.Bottom
                ) {
                    Text(
                        ad.title,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                }
            }
        } else {
            Row(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                HomeIconTile(icon = ad.icon, accent = ad.themeColor, size = 60.dp)

                Spacer(modifier = Modifier.width(16.dp))
                
                Column {
                    Text(
                        ad.title,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = extendedColors.textPrimary
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        ad.description,
                        style = MaterialTheme.typography.bodySmall,
                        color = extendedColors.textSecondary,
                        maxLines = 3
                    )
                }
            }
        }
    }
}

data class AdItem(
    val title: String,
    val description: String,
    val themeColor: Color,
    val icon: ImageVector,
    val imageUrl: String? = null
)

@Composable
fun NFCScanBottomSheetContent(
    isScanning: Boolean,
    onCancel: () -> Unit
) {
    val composition by rememberLottieComposition(LottieCompositionSpec.Asset("nfcLottie.json"))
    val progress by animateLottieCompositionAsState(
        composition = composition,
        iterations = LottieConstants.IterateForever
    )

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.End
        ) {
            IconButton(
                onClick = onCancel,
                modifier = Modifier
                    .size(32.dp)
                    .clip(CircleShape)
                    .background(Color.White.copy(alpha = 0.15f))
            ) {
                Icon(
                    Icons.Default.Close,
                    contentDescription = "Close",
                    tint = Color.White,
                    modifier = Modifier.size(16.dp)
                )
            }
        }

        Text(
            if (isScanning) "Reading Card…" else "Ready to Scan",
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold,
            color = Color.White
        )

        Spacer(modifier = Modifier.height(12.dp))

        Text(
            if (isScanning)
                "Hold the card still while we read it"
            else
                "Hold your phone near your transit card",
            style = MaterialTheme.typography.bodyMedium,
            color = Color.White.copy(alpha = 0.6f),
            textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(32.dp))

        Box(
            modifier = Modifier.size(200.dp),
            contentAlignment = Alignment.Center
        ) {
            if (isScanning) {
                CircularProgressIndicator(
                    color = Color(0xFF4CAF50),
                    modifier = Modifier.size(80.dp),
                    strokeWidth = 6.dp
                )
            } else {
                LottieAnimation(
                    composition = composition,
                    progress = { progress },
                    modifier = Modifier.fillMaxSize()
                )
            }
        }

        Spacer(modifier = Modifier.height(32.dp))

        Button(
            onClick = onCancel,
            enabled = !isScanning,
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp),
            shape = RoundedCornerShape(16.dp),
            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF007AFF))
        ) {
            Text("Cancel", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = Color.White)
        }

        Spacer(modifier = Modifier.height(16.dp))
    }
}

@Composable
fun CardFeatureItem(icon: ImageVector, label: String, themeColor: Color) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        HomeIconTile(icon = icon, accent = themeColor, size = 36.dp, cornerRadius = 12.dp)
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            label,
            style = MaterialTheme.typography.labelSmall,
            color = MetroTransitTheme.extendedColors.textPrimary,
            fontWeight = FontWeight.Medium
        )
    }
}

@Composable
fun JourneyPoint(icon: ImageVector, color: Color) {
    Surface(
        shape = CircleShape,
        color = color.copy(alpha = 0.14f),
        modifier = Modifier.size(26.dp)
    ) {
        Icon(
            icon,
            contentDescription = null,
            tint = color,
            modifier = Modifier.padding(4.dp)
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StationSelector(
    label: String,
    selectedStation: MetroStation?,
    onStationSelected: (MetroStation) -> Unit,
    modifier: Modifier = Modifier,
    isLocating: Boolean = false
) {
    var expanded by remember { mutableStateOf(false) }
    val extendedColors = MetroTransitTheme.extendedColors

    ExposedDropdownMenuBox(
        expanded = expanded,
        onExpandedChange = { expanded = !expanded },
        modifier = modifier
    ) {
        TextField(
            value = if (isLocating) "Detecting nearby station..." else (selectedStation?.name ?: ""),
            onValueChange = {},
            readOnly = true,
            placeholder = { Text("Select Station", color = extendedColors.textSecondary) },
            label = { 
                Text(
                    label, 
                    style = MaterialTheme.typography.labelSmall,
                    color = if (expanded) MaterialTheme.colorScheme.primary else extendedColors.textSecondary,
                    fontWeight = FontWeight.Bold
                ) 
            },
            supportingText = null,
            trailingIcon = { 
                if (!isLocating) {
                    Icon(
                        if (expanded) Icons.Default.KeyboardArrowUp else Icons.Default.KeyboardArrowDown,
                        contentDescription = null,
                        tint = if (expanded) MaterialTheme.colorScheme.primary else extendedColors.textSecondary
                    )
                }
            },
            colors = TextFieldDefaults.colors(
                focusedContainerColor = Color.Transparent,
                unfocusedContainerColor = Color.Transparent,
                disabledContainerColor = Color.Transparent,
                // Both fields keep a rule under them, so the pair reads as one control —
                // only its colour says which of the two is open.
                focusedIndicatorColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.5f),
                unfocusedIndicatorColor = extendedColors.textSecondary.copy(alpha = 0.22f),
            ),
            textStyle = (if (isLocating) MaterialTheme.typography.bodyMedium else MaterialTheme.typography.bodyLarge).copy(
                fontWeight = FontWeight.Bold,
                color = if (isLocating) MaterialTheme.colorScheme.primary else if (selectedStation == null) extendedColors.textSecondary else extendedColors.textPrimary
            ),
            modifier = Modifier
                .menuAnchor(ExposedDropdownMenuAnchorType.PrimaryNotEditable)
                .fillMaxWidth()
        )

        MaterialTheme(
            shapes = MaterialTheme.shapes.copy(extraSmall = RoundedCornerShape(16.dp))
        ) {
            ExposedDropdownMenu(
                expanded = expanded,
                onDismissRequest = { expanded = false },
                modifier = Modifier
                    .background(extendedColors.surface)
                    .border(1.dp, extendedColors.glassBorder, RoundedCornerShape(16.dp))
            ) {
                StationData.stations.forEach { station ->
                    val isSelected = selectedStation?.id == station.id
                    DropdownMenuItem(
                        text = { 
                            Text(
                                station.name,
                                style = MaterialTheme.typography.bodyLarge,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                color = if (isSelected) MaterialTheme.colorScheme.primary else extendedColors.textPrimary
                            ) 
                        },
                        leadingIcon = {
                            Surface(
                                shape = CircleShape,
                                color = if (isSelected) MaterialTheme.colorScheme.primary.copy(alpha = 0.1f) else extendedColors.textSecondary.copy(alpha = 0.1f),
                                modifier = Modifier.size(32.dp)
                            ) {
                                Box(contentAlignment = Alignment.Center) {
                                    Icon(
                                        Icons.Default.Train,
                                        contentDescription = null,
                                        modifier = Modifier.size(16.dp),
                                        tint = if (isSelected) MaterialTheme.colorScheme.primary else extendedColors.textSecondary
                                    )
                                }
                            }
                        },
                        onClick = {
                            onStationSelected(station)
                            expanded = false
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(if (isSelected) MaterialTheme.colorScheme.primary.copy(alpha = 0.05f) else Color.Transparent),
                        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp)
                    )
                    if (station != StationData.stations.last()) {
                        HorizontalDivider(
                            modifier = Modifier.padding(horizontal = 16.dp),
                            thickness = 0.5.dp,
                            color = extendedColors.glassBorder
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun InfoCard(title: String, value: String, modifier: Modifier = Modifier) {
    val extendedColors = MetroTransitTheme.extendedColors
    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(20.dp),
        color = extendedColors.glass,
        border = androidx.compose.foundation.BorderStroke(1.dp, extendedColors.glassBorder)
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(title, style = MaterialTheme.typography.labelSmall, color = extendedColors.textSecondary)
            Text(value, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = extendedColors.textPrimary)
        }
    }
}
