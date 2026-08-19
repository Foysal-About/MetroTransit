package com.example.metrotransit.ui.screens

import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.ArrowBackIosNew
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.AccessTime
import androidx.compose.material.icons.filled.AddLocationAlt
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.CreditCard
import androidx.compose.material.icons.filled.DirectionsSubway
import androidx.compose.material.icons.filled.Flag
import androidx.compose.material.icons.filled.MyLocation
import androidx.compose.material.icons.filled.QrCodeScanner
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.luminance
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.metrotransit.R
import com.example.metrotransit.data.FareCalculator
import com.example.metrotransit.data.MetroStation
import com.example.metrotransit.data.QRTicket
import com.example.metrotransit.ui.theme.AppFont
import com.example.metrotransit.ui.theme.MetroSuccess
import com.example.metrotransit.ui.theme.MetroTransitTheme
import kotlinx.coroutines.delay
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

private fun clockTime(millis: Long): String =
    SimpleDateFormat("hh:mm a", Locale.getDefault()).format(Date(millis))

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun JourneyScreen(
    ticket: QRTicket,
    onBack: () -> Unit,
    onExtendJourney: (MetroStation, String) -> Unit,
    onCompleteJourney: () -> Unit,
    onFinish: () -> Unit,
    openExitGateOnLaunch: Boolean = false
) {
    val extendedColors = MetroTransitTheme.extendedColors
    val scrollState = rememberScrollState()
    val snackbarHostState = remember { SnackbarHostState() }

    val entryMillis = ticket.validatedAtMillis ?: ticket.createdAtMillis
    var nowMillis by remember { mutableLongStateOf(System.currentTimeMillis()) }

    LaunchedEffect(ticket.isCompleted) {
        while (!ticket.isCompleted) {
            nowMillis = System.currentTimeMillis()
            delay(1000)
        }
    }

    // The route is the trip the ticket paid for, not a position: validating only proves the
    // rider passed an entry gate. Which train they boarded, and where it is now, is not
    // something this app can know, so nothing here pretends to track it.
    val route = FareCalculator.route(ticket.fromStationId, ticket.toStationId)

    val onwardStations = FareCalculator.onwardStations(ticket.fromStationId, ticket.toStationId)

    var showExtendSheet by remember { mutableStateOf(false) }
    // Set when a station is picked straight off the timeline, skipping the browse step.
    var extendTarget by remember { mutableStateOf<MetroStation?>(null) }
    // Saveable so an activity recreation (theme switch, rotation) doesn't drop the
    // receipt or silently reopen a gate the rider already cleared.
    var showExitGate by rememberSaveable {
        mutableStateOf(openExitGateOnLaunch && !ticket.isCompleted)
    }
    var showSummary by rememberSaveable { mutableStateOf(false) }

    // Confirms the extension the moment the ticket picks it up.
    var knownExtensions by remember { mutableIntStateOf(ticket.extensions.size) }
    LaunchedEffect(ticket.extensions.size) {
        if (ticket.extensions.size > knownExtensions) {
            val added = ticket.extensions.last()
            snackbarHostState.showSnackbar(
                "Destination extended to ${added.toStationName} · ${FareCalculator.format(added.extraFare)} paid"
            )
        }
        knownExtensions = ticket.extensions.size
    }

    if (showExtendSheet) {
        AddDestinationSheet(
            ticket = ticket,
            onwardStations = onwardStations,
            initialStation = extendTarget,
            onDismiss = {
                showExtendSheet = false
                extendTarget = null
            },
            onConfirm = { station, method ->
                onExtendJourney(station, method)
                showExtendSheet = false
                extendTarget = null
            }
        )
    }

    if (showExitGate) {
        ExitGateSheet(
            ticket = ticket,
            canExtend = onwardStations.isNotEmpty(),
            onDismiss = { showExitGate = false },
            // Changed their mind at the gate — hand over to the "going further" sheet.
            onAddStop = {
                showExitGate = false
                extendTarget = null
                showExtendSheet = true
            },
            onGateAccepted = {
                onCompleteJourney()
                showExitGate = false
                showSummary = true
            }
        )
    }

    if (showSummary) {
        JourneySummaryDialog(
            ticket = ticket,
            entryMillis = entryMillis,
            onDone = onFinish
        )
    }

    Scaffold(
        containerColor = Color.Transparent,
        snackbarHost = {
            // Lifted clear of the floating journey bar: this screen only exists while a
            // journey is running, so the bar is always over the bottom of it and an
            // unpadded host slides the confirmation in underneath.
            SnackbarHost(
                snackbarHostState,
                modifier = Modifier.padding(bottom = LocalJourneyBarInset.current)
            ) { data ->
                GlassSnackbar(data)
            }
        },
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        NavTitle(
                            if (ticket.isCompleted) "Journey Complete" else "Journey in Progress"
                        )
                        Text(
                            "Ticket ${ticket.id}",
                            style = MaterialTheme.typography.labelSmall,
                            color = extendedColors.textSecondary
                        )
                    }
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(
                            Icons.Rounded.ArrowBackIosNew,
                            contentDescription = "Back",
                            tint = extendedColors.textPrimary
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.Transparent)
            )
        }
        // No bottom bar: tapping out lives in the floating journey bar that follows the
        // rider across the app, and each onward station carries its own "Add ৳X" action.
    ) { padding ->
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
                    .padding(horizontal = 16.dp)
                    .padding(bottom = LocalJourneyBarInset.current)
            ) {
                EntryStatusCard(
                    ticket = ticket,
                    route = route,
                    entryMillis = entryMillis,
                    secondsLeft = ticket.remainingSeconds(nowMillis)
                )

                Spacer(modifier = Modifier.height(16.dp))

                RouteTimelineCard(
                    ticket = ticket,
                    route = route,
                    onwardStations = if (ticket.isCompleted) emptyList() else onwardStations,
                    onAddDestination = { station ->
                        extendTarget = station
                        showExtendSheet = true
                    }
                )

                Spacer(modifier = Modifier.height(16.dp))

                FareBreakdownCard(ticket = ticket)

                Spacer(modifier = Modifier.height(24.dp))

                if (ticket.isCompleted) {
                    Button(
                        onClick = onFinish,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(56.dp),
                        shape = RoundedCornerShape(16.dp)
                    ) {
                        Text("Back to Home", fontWeight = FontWeight.Bold)
                    }
                    Spacer(modifier = Modifier.height(24.dp))
                }
            }
        }
    }
}

/**
 * What the app actually knows about the ride, in the same glass card the Ticket Portal uses
 * for a journey: the gate accepted this ticket at a station, at a time, and the exit window
 * is running. No position, no "next station" — a validated QR says the rider is inside the
 * paid area, nothing about which train they took.
 *
 * The countdown is the hero number, where the portal card puts the fare: it is the only
 * thing on this page that changes, and the only thing the rider has to act on.
 */
@Composable
private fun EntryStatusCard(
    ticket: QRTicket,
    route: List<MetroStation>,
    entryMillis: Long,
    secondsLeft: Int
) {
    val extendedColors = MetroTransitTheme.extendedColors
    val accent = MaterialTheme.colorScheme.primary

    // The one thing genuinely counting down: how much of the exit window has been used.
    val windowSeconds = (ticket.validityMinutes * 60).coerceAtLeast(1)
    val windowUsed = ((windowSeconds - secondsLeft).toFloat() / windowSeconds).coerceIn(0f, 1f)

    val stops = (route.size - 1).coerceAtLeast(0)
    val rideMinutes = FareCalculator.travelMinutes(ticket.fromStationId, ticket.toStationId)

    val overdue = !ticket.isCompleted && secondsLeft <= 0
    // Five minutes is about one hop plus the walk to a gate — past that it is worth shouting.
    val expiring = !ticket.isCompleted && secondsLeft in 1..299

    LiquidGlassSurface(
        modifier = Modifier.fillMaxWidth(),
        cornerRadius = 28.dp,
        // A wash of whichever colour the state is in, so the card reads before it is read.
        tint = when {
            overdue || expiring -> MaterialTheme.colorScheme.error.copy(alpha = 0.14f)
            ticket.isCompleted -> MetroSuccess.copy(alpha = 0.12f)
            else -> accent.copy(alpha = 0.12f)
        }
    ) {
        Column(modifier = Modifier.padding(20.dp)) {
            StatusPill(
                text = when {
                    ticket.isCompleted -> "JOURNEY COMPLETE"
                    overdue -> "EXIT WINDOW OVER"
                    else -> "INSIDE THE PAID AREA"
                },
                color = when {
                    ticket.isCompleted -> MetroSuccess
                    overdue || expiring -> MaterialTheme.colorScheme.error
                    else -> TicketAmber
                },
                live = !ticket.isCompleted
            )

            Spacer(modifier = Modifier.height(18.dp))

            // The pair the ticket was bought for, drawn the way the Ticket Portal draws it.
            // Bottom-aligned so the track runs through the station names.
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.Bottom
            ) {
                GateEnd(
                    label = "Entered",
                    station = ticket.fromStation,
                    caption = clockTime(entryMillis),
                    dotColor = accent,
                    modifier = Modifier.weight(1f)
                )

                JourneyTrack()

                GateEnd(
                    label = if (ticket.isCompleted) "Exited" else "Exit at",
                    station = ticket.toStation,
                    caption = "$stops stop${if (stops == 1) "" else "s"} · ~$rideMinutes min",
                    dotColor = MetroSuccess,
                    alignEnd = true,
                    modifier = Modifier.weight(1f)
                )
            }

            Spacer(modifier = Modifier.height(18.dp))

            // Not glassBorder: on dark glass the sheen swallows its left half, and a hairline
            // that only appears under half the card reads as a rendering fault.
            HorizontalDivider(
                thickness = 0.5.dp,
                color = extendedColors.textSecondary.copy(alpha = 0.2f)
            )

            Spacer(modifier = Modifier.height(16.dp))

            if (ticket.isCompleted) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.Bottom
                ) {
                    Column {
                        Text(
                            "Tapped out",
                            style = MaterialTheme.typography.labelMedium,
                            color = extendedColors.textSecondary
                        )
                        Text(
                            clockTime(ticket.completedAtMillis ?: entryMillis),
                            color = extendedColors.textPrimary,
                            fontFamily = AppFont.display,
                            fontSize = 28.sp,
                            fontWeight = FontWeight.Black
                        )
                    }
                    Column(horizontalAlignment = Alignment.End) {
                        Text(
                            "Fare paid",
                            style = MaterialTheme.typography.labelMedium,
                            color = extendedColors.textSecondary
                        )
                        Text(
                            ticket.fare,
                            color = MetroSuccess,
                            fontWeight = FontWeight.Bold,
                            style = MaterialTheme.typography.titleMedium
                        )
                    }
                }
            } else {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.Bottom
                ) {
                    Column {
                        Text(
                            if (overdue) "Exit window" else "Exit within",
                            style = MaterialTheme.typography.labelMedium,
                            color = extendedColors.textSecondary
                        )
                        Text(
                            if (overdue) "OVER" else formatCountdown(secondsLeft),
                            color = if (overdue || expiring) {
                                MaterialTheme.colorScheme.error
                            } else {
                                extendedColors.textPrimary
                            },
                            fontFamily = AppFont.display,
                            fontSize = 34.sp,
                            fontWeight = FontWeight.Black
                        )
                    }
                    Column(horizontalAlignment = Alignment.End) {
                        Text(
                            "Window",
                            style = MaterialTheme.typography.labelMedium,
                            color = extendedColors.textSecondary
                        )
                        Text(
                            "${ticket.validityMinutes} min",
                            color = extendedColors.textPrimary,
                            fontWeight = FontWeight.Bold,
                            style = MaterialTheme.typography.titleMedium
                        )
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Time, not distance. The bar fills as the exit window runs down, which is
                // the only thing about this ride the app can measure.
                LinearProgressIndicator(
                    progress = { windowUsed },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(6.dp),
                    color = if (overdue || expiring) MaterialTheme.colorScheme.error else accent,
                    trackColor = extendedColors.textSecondary.copy(alpha = 0.2f),
                    strokeCap = androidx.compose.ui.graphics.StrokeCap.Round,
                    gapSize = 0.dp,
                    drawStopIndicator = {}
                )

                if (overdue) {
                    Spacer(modifier = Modifier.height(10.dp))
                    Text(
                        "Tap out at any gate — a fare adjustment may apply.",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.error
                    )
                }
            }
        }
    }
}

/** One end of the paid journey: the gate, when it happened, and how far it reaches. */
@Composable
private fun GateEnd(
    label: String,
    station: String,
    caption: String,
    dotColor: Color,
    modifier: Modifier = Modifier,
    alignEnd: Boolean = false
) {
    val extendedColors = MetroTransitTheme.extendedColors

    Column(
        modifier = modifier,
        horizontalAlignment = if (alignEnd) Alignment.End else Alignment.Start
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            if (!alignEnd) {
                Box(
                    modifier = Modifier
                        .size(8.dp)
                        .clip(CircleShape)
                        .background(dotColor)
                )
                Spacer(modifier = Modifier.width(6.dp))
            }
            Text(
                label,
                color = extendedColors.textSecondary,
                style = MaterialTheme.typography.labelSmall,
                fontWeight = FontWeight.Bold
            )
            if (alignEnd) {
                Spacer(modifier = Modifier.width(6.dp))
                Box(
                    modifier = Modifier
                        .size(8.dp)
                        .clip(CircleShape)
                        .background(dotColor)
                )
            }
        }

        Spacer(modifier = Modifier.height(4.dp))

        Text(
            station,
            color = extendedColors.textPrimary,
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            textAlign = if (alignEnd) TextAlign.End else TextAlign.Start,
            // Long names ("Bangladesh Secretariat") wrap rather than being cut mid-word.
            maxLines = 2
        )
        Text(
            caption,
            color = extendedColors.textSecondary,
            style = MaterialTheme.typography.labelSmall,
            textAlign = if (alignEnd) TextAlign.End else TextAlign.Start
        )
    }
}

/**
 * The state of the ticket in one pill. [live] adds the slow pulse the floating journey bar
 * uses, so a running journey looks running rather than frozen.
 */
@Composable
private fun StatusPill(text: String, color: Color, live: Boolean) {
    val alpha = if (live) {
        val transition = rememberInfiniteTransition(label = "statusPulse")
        val pulse by transition.animateFloat(
            initialValue = 0.35f,
            targetValue = 1f,
            animationSpec = infiniteRepeatable(animation = tween(1100), repeatMode = RepeatMode.Reverse),
            label = "statusPulseAlpha"
        )
        pulse
    } else {
        1f
    }

    Surface(shape = RoundedCornerShape(10.dp), color = color.copy(alpha = 0.16f)) {
        Row(
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(7.dp)
                    .clip(CircleShape)
                    .background(color.copy(alpha = alpha))
            )
            Spacer(modifier = Modifier.width(7.dp))
            Text(
                text,
                color = color,
                style = MaterialTheme.typography.labelSmall,
                fontWeight = FontWeight.Black,
                fontSize = 10.sp,
                letterSpacing = 0.6.sp
            )
        }
    }
}

@Composable
private fun RouteTimelineCard(
    ticket: QRTicket,
    route: List<MetroStation>,
    onwardStations: List<MetroStation>,
    onAddDestination: (MetroStation) -> Unit
) {
    val extendedColors = MetroTransitTheme.extendedColors
    val addedStops = ticket.extensions.associate { it.toStationId to it.extraFare }
    val originalDestinationId = ticket.originalDestinationId

    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(24.dp),
        color = MaterialTheme.colorScheme.surface,
        shadowElevation = 2.dp
    ) {
        Column(modifier = Modifier.padding(20.dp)) {
            Text(
                "Route",
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold,
                color = extendedColors.textPrimary
            )
            Spacer(modifier = Modifier.height(12.dp))

            route.forEachIndexed { index, station ->
                val isExit = index == route.lastIndex
                // The gates are the only stations the app has been told about: the entry gate
                // that validated this ticket, and — once tapped out — the exit gate too. The
                // stations between them are a printed itinerary, not a record of a trip.
                val isScanned = ticket.isCompleted || index == 0
                val extraFare = addedStops[station.id]

                StationTimelineRow(
                    station = station,
                    isFirst = index == 0,
                    isLast = isExit,
                    isScanned = isScanned,
                    // Only a finished journey has a confirmed run of stations to draw a solid
                    // rail through; mid-ride it stays dashed-out grey below the entry gate.
                    isRailBelowScanned = ticket.isCompleted,
                    trailing = when {
                        index == 0 -> "Entry"
                        isExit -> "Exit"
                        station.id == originalDestinationId && ticket.extensions.isNotEmpty() -> "Original exit"
                        else -> null
                    },
                    extraFare = extraFare
                )
            }

            // ── Stations the rider hasn't paid for yet ───────────────────
            // Each carries its own "Add ৳X" action so extending is one tap away.
            if (onwardStations.isEmpty() && !ticket.isCompleted) {
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    "${ticket.toStation} is the last station on MRT Line-6 — the journey can’t be extended further.",
                    style = MaterialTheme.typography.labelSmall,
                    color = extendedColors.textSecondary,
                    fontSize = 10.sp,
                    modifier = Modifier.padding(start = 40.dp)
                )
            }

            if (onwardStations.isNotEmpty()) {
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    "Need to go further? Add a stop and pay only the difference — your exit time grows too.",
                    style = MaterialTheme.typography.labelSmall,
                    color = extendedColors.textSecondary,
                    fontSize = 10.sp,
                    modifier = Modifier.padding(start = 40.dp, bottom = 4.dp)
                )

                onwardStations.forEachIndexed { index, station ->
                    OnwardTimelineRow(
                        station = station,
                        isLast = index == onwardStations.lastIndex,
                        extraFare = FareCalculator.extensionFare(
                            entryId = ticket.fromStationId,
                            currentToId = ticket.toStationId,
                            newToId = station.id
                        ),
                        extraMinutes = FareCalculator.journeyMinutes(ticket.fromStationId, station.id) -
                            ticket.journeyMinutes,
                        onAdd = { onAddDestination(station) }
                    )
                }
            }
        }
    }
}

/**
 * A station beyond the paid exit gate: dashed rail, and an "Add ৳X" pill that takes the
 * rider straight to paying the difference for that station.
 */
@Composable
private fun OnwardTimelineRow(
    station: MetroStation,
    isLast: Boolean,
    extraFare: Int,
    extraMinutes: Int,
    onAdd: () -> Unit
) {
    val extendedColors = MetroTransitTheme.extendedColors
    val primary = MaterialTheme.colorScheme.primary
    val railColor = extendedColors.textSecondary.copy(alpha = 0.3f)

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(52.dp)
    ) {
        Column(
            modifier = Modifier
                .width(28.dp)
                .fillMaxHeight(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            DashedRail(modifier = Modifier.weight(1f), color = railColor)
            Box(
                modifier = Modifier
                    .size(11.dp)
                    .border(2.dp, primary.copy(alpha = 0.55f), CircleShape)
            )
            DashedRail(
                modifier = Modifier.weight(1f),
                color = if (isLast) Color.Transparent else railColor
            )
        }

        Spacer(modifier = Modifier.width(12.dp))

        Row(
            modifier = Modifier
                .weight(1f)
                .fillMaxHeight(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    station.name,
                    style = MaterialTheme.typography.bodyMedium,
                    color = extendedColors.textSecondary
                )
                Text(
                    "Not on your ticket · +$extraMinutes min",
                    style = MaterialTheme.typography.labelSmall,
                    color = extendedColors.textSecondary.copy(alpha = 0.7f),
                    fontSize = 9.sp
                )
            }

            Surface(
                modifier = Modifier.clickable { onAdd() },
                shape = RoundedCornerShape(10.dp),
                color = primary.copy(alpha = 0.12f),
                border = BorderStroke(1.dp, primary.copy(alpha = 0.45f))
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        Icons.Default.AddLocationAlt,
                        contentDescription = null,
                        tint = primary,
                        modifier = Modifier.size(12.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        "Add ${FareCalculator.format(extraFare)}",
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.Bold,
                        color = primary
                    )
                }
            }
        }
    }
}

@Composable
private fun DashedRail(modifier: Modifier = Modifier, color: Color) {
    Canvas(modifier = modifier.width(2.dp)) {
        drawLine(
            color = color,
            start = Offset(size.width / 2, 0f),
            end = Offset(size.width / 2, size.height),
            strokeWidth = size.width,
            pathEffect = PathEffect.dashPathEffect(floatArrayOf(6f, 8f), 0f)
        )
    }
}

@Composable
private fun StationTimelineRow(
    station: MetroStation,
    isFirst: Boolean,
    isLast: Boolean,
    /** True where a gate actually scanned this ticket — drawn solid and ticked. */
    isScanned: Boolean,
    /** True when the rail continuing below this row is also confirmed. */
    isRailBelowScanned: Boolean,
    trailing: String?,
    extraFare: Int?
) {
    val extendedColors = MetroTransitTheme.extendedColors
    val primary = MaterialTheme.colorScheme.primary
    val upcomingColor = extendedColors.textSecondary.copy(alpha = 0.3f)

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(52.dp)
    ) {
        // ── Rail with the position dot ──────────────────────────────
        Column(
            modifier = Modifier
                .width(28.dp)
                .fillMaxHeight(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Box(
                modifier = Modifier
                    .weight(1f)
                    .width(2.dp)
                    .background(if (isFirst) Color.Transparent else if (isScanned) primary else upcomingColor)
            )
            Box(contentAlignment = Alignment.Center, modifier = Modifier.size(18.dp)) {
                if (isScanned) {
                    Box(
                        modifier = Modifier
                            .size(14.dp)
                            .background(primary, CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            Icons.Default.Check,
                            contentDescription = null,
                            tint = Color.White,
                            modifier = Modifier.size(9.dp)
                        )
                    }
                } else {
                    Box(
                        modifier = Modifier
                            .size(10.dp)
                            .background(upcomingColor, CircleShape)
                    )
                }
            }
            Box(
                modifier = Modifier
                    .weight(1f)
                    .width(2.dp)
                    .background(
                        if (isLast) Color.Transparent
                        else if (isScanned && isRailBelowScanned) primary
                        else upcomingColor
                    )
            )
        }

        Spacer(modifier = Modifier.width(12.dp))

        Row(
            modifier = Modifier
                .weight(1f)
                .fillMaxHeight(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    station.name,
                    style = MaterialTheme.typography.bodyMedium,
                    // The two ends of the ticket carry the weight; the stops between them are
                    // reference, so they stay quiet.
                    fontWeight = if (isFirst || isLast) FontWeight.Bold else FontWeight.Normal,
                    color = if (isScanned || isLast) extendedColors.textPrimary
                    else extendedColors.textSecondary
                )
                if (extraFare != null) {
                    Text(
                        "Added stop · +${FareCalculator.format(extraFare)}",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.primary,
                        fontSize = 10.sp
                    )
                }
            }

            if (trailing != null) {
                val chipIcon = when (trailing) {
                    "Entry" -> Icons.Default.MyLocation
                    "Exit" -> Icons.Default.Flag
                    else -> null
                }
                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = extendedColors.textSecondary.copy(alpha = 0.12f)
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        if (chipIcon != null) {
                            Icon(
                                chipIcon,
                                contentDescription = null,
                                tint = extendedColors.textSecondary,
                                modifier = Modifier.size(10.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                        }
                        Text(
                            trailing,
                            style = MaterialTheme.typography.labelSmall,
                            color = extendedColors.textSecondary,
                            fontSize = 10.sp
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun FareBreakdownCard(ticket: QRTicket) {
    val extendedColors = MetroTransitTheme.extendedColors

    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(24.dp),
        color = extendedColors.glass,
        border = BorderStroke(1.dp, extendedColors.glassBorder)
    ) {
        Column(modifier = Modifier.padding(20.dp)) {
            Text(
                "Fare Breakdown",
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold,
                color = extendedColors.textPrimary
            )
            Spacer(modifier = Modifier.height(12.dp))

            FareLine(
                label = "${ticket.fromStation} → ${FareCalculator.station(ticket.originalDestinationId)?.name ?: ticket.toStation}",
                caption = "Base fare",
                amount = FareCalculator.format(ticket.baseFare)
            )

            ticket.extensions.forEach { extension ->
                FareLine(
                    label = "${extension.fromStationName} → ${extension.toStationName}",
                    caption = "Extension · ${extension.paymentMethod} · ${extension.addedAt.substringAfter(", ")}",
                    amount = "+${FareCalculator.format(extension.extraFare)}"
                )
            }

            HorizontalDivider(
                modifier = Modifier.padding(vertical = 12.dp),
                color = extendedColors.glassBorder
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    "Total paid",
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Bold,
                    color = extendedColors.textPrimary
                )
                Text(
                    FareCalculator.format(ticket.totalFare),
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Black,
                    color = MaterialTheme.colorScheme.primary
                )
            }

            if (ticket.extensions.isNotEmpty()) {
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    "Exit window extended by ${ticket.extensions.sumOf { it.extraMinutes }} min",
                    style = MaterialTheme.typography.labelSmall,
                    color = extendedColors.textSecondary,
                    fontSize = 10.sp
                )
            }
        }
    }
}

@Composable
private fun FareLine(label: String, caption: String, amount: String) {
    val extendedColors = MetroTransitTheme.extendedColors
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 6.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(
                label,
                style = MaterialTheme.typography.bodySmall,
                fontWeight = FontWeight.Bold,
                color = extendedColors.textPrimary
            )
            Text(
                caption,
                style = MaterialTheme.typography.labelSmall,
                color = extendedColors.textSecondary,
                fontSize = 10.sp
            )
        }
        Text(
            amount,
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.Bold,
            color = extendedColors.textPrimary
        )
    }
}

/**
 * The exit gate. The rider presents the very same QR that opened the entry gate; only
 * once the reader accepts it is the journey closed and the fare settled.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ExitGateSheet(
    ticket: QRTicket,
    canExtend: Boolean,
    onDismiss: () -> Unit,
    onAddStop: () -> Unit,
    onGateAccepted: () -> Unit
) {
    val extendedColors = MetroTransitTheme.extendedColors
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    var isReading by remember { mutableStateOf(false) }

    LaunchedEffect(isReading) {
        if (isReading) {
            delay(1500)
            onGateAccepted()
        }
    }

    ModalBottomSheet(
        onDismissRequest = { if (!isReading) onDismiss() },
        sheetState = sheetState,
        containerColor = MaterialTheme.colorScheme.surface,
        shape = RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 20.dp)
                .padding(bottom = 28.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                "Scan at Exit Gate",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Black,
                color = extendedColors.textPrimary
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                "Hold this QR under the reader at ${ticket.toStation} to finish your journey.",
                style = MaterialTheme.typography.bodySmall,
                color = extendedColors.textSecondary,
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(20.dp))

            TicketQrCard(
                data = ticket.qrData,
                modifier = Modifier.size(230.dp),
                alpha = if (isReading) 0.3f else 1f
            ) {
                if (isReading) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(44.dp),
                        strokeWidth = 3.dp,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            Text(
                "ID: ${ticket.id}",
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.Bold,
                color = extendedColors.textPrimary
            )

            Spacer(modifier = Modifier.height(16.dp))

            Surface(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                color = extendedColors.glass,
                border = BorderStroke(1.dp, extendedColors.glassBorder)
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    JourneySummaryRow("Exit station", ticket.toStation)
                    JourneySummaryRow("Fare settled", FareCalculator.format(ticket.totalFare))
                    if (ticket.extensions.isNotEmpty()) {
                        JourneySummaryRow("Stops added", "${ticket.extensions.size}")
                    }
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            Button(
                onClick = { isReading = true },
                enabled = !isReading,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp),
                shape = RoundedCornerShape(16.dp)
            ) {
                if (isReading) {
                    CircularProgressIndicator(
                        color = Color.White,
                        strokeWidth = 2.dp,
                        modifier = Modifier.size(22.dp)
                    )
                    Spacer(modifier = Modifier.width(12.dp))
                    Text("Reading QR…", fontWeight = FontWeight.Bold)
                } else {
                    Icon(Icons.Default.QrCodeScanner, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Gate Scanned — Complete Journey", fontWeight = FontWeight.Bold)
                }
            }

            if (canExtend && !isReading) {
                Spacer(modifier = Modifier.height(10.dp))
                OutlinedButton(
                    onClick = onAddStop,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(52.dp),
                    shape = RoundedCornerShape(16.dp),
                    border = BorderStroke(1.5.dp, MaterialTheme.colorScheme.primary)
                ) {
                    Icon(
                        Icons.Default.AddLocationAlt,
                        contentDescription = null,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Add Stop — go further instead", fontWeight = FontWeight.Bold)
                }
            }

            if (!isReading) {
                TextButton(onClick = onDismiss) {
                    Text("Not at the gate yet", color = extendedColors.textSecondary)
                }
            }
        }
    }
}

/**
 * Uber-style "add another stop": pick a station further down the line, pay only the
 * fare difference, and the same QR ticket keeps working at the new exit gate.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun AddDestinationSheet(
    ticket: QRTicket,
    onwardStations: List<MetroStation>,
    initialStation: MetroStation?,
    onDismiss: () -> Unit,
    onConfirm: (MetroStation, String) -> Unit
) {
    val extendedColors = MetroTransitTheme.extendedColors
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    // Pre-filled when the rider tapped "Add ৳X" on a station in the timeline.
    var selectedStation by remember { mutableStateOf(initialStation) }
    var selectedMethod by remember { mutableStateOf<String?>(null) }
    var isProcessing by remember { mutableStateOf(false) }

    val station = selectedStation
    val extraFare = station?.let {
        FareCalculator.extensionFare(ticket.fromStationId, ticket.toStationId, it.id)
    } ?: 0
    val extraMinutes = station?.let {
        FareCalculator.journeyMinutes(ticket.fromStationId, it.id) - ticket.journeyMinutes
    } ?: 0

    LaunchedEffect(isProcessing) {
        if (isProcessing && station != null && selectedMethod != null) {
            delay(1500)
            onConfirm(station, selectedMethod!!)
        }
    }

    ModalBottomSheet(
        onDismissRequest = { if (!isProcessing) onDismiss() },
        sheetState = sheetState,
        containerColor = MaterialTheme.colorScheme.surface,
        shape = RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                // The sheet's own content scrolls. Without a scrollable here an upward fling
                // is handed to the sheet itself, which drags to full height and bounces back
                // — the flicker. With one, nested scroll absorbs the gesture, and tall
                // content stays reachable on a short screen.
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 20.dp)
                .padding(bottom = 28.dp)
        ) {
            Text(
                if (station == null) "Going further?" else "Confirm extra fare",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Black,
                color = extendedColors.textPrimary
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                if (station == null) {
                    "Your ticket ends at ${ticket.toStation}. Pick a new destination and pay only the difference."
                } else {
                    "Your exit gate moves from ${ticket.toStation} to ${station.name}."
                },
                style = MaterialTheme.typography.bodySmall,
                color = extendedColors.textSecondary
            )

            Spacer(modifier = Modifier.height(20.dp))

            if (station == null) {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    onwardStations.forEach { onward ->
                        val fare = FareCalculator.extensionFare(
                            ticket.fromStationId,
                            ticket.toStationId,
                            onward.id
                        )
                        OnwardStationRow(
                            station = onward,
                            extraFare = fare,
                            extraStops = FareCalculator.hops(ticket.toStationId, onward.id),
                            onClick = { selectedStation = onward }
                        )
                    }
                }
            } else {
                // ── Fare difference ──────────────────────────────────
                // The same glass card, gate pair and hero number the journey page uses, so
                // the sheet reads as that page asking a question rather than a separate form.
                LiquidGlassSurface(
                    modifier = Modifier.fillMaxWidth(),
                    cornerRadius = 20.dp,
                    tint = MaterialTheme.colorScheme.primary.copy(alpha = 0.12f)
                ) {
                    Column(modifier = Modifier.padding(18.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.Bottom
                        ) {
                            GateEnd(
                                label = "Exit now",
                                station = ticket.toStation,
                                caption = "${ticket.fare} paid",
                                dotColor = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.weight(1f)
                            )

                            JourneyTrack()

                            GateEnd(
                                label = "Exit instead",
                                station = station.name,
                                caption = "+$extraMinutes min window",
                                dotColor = MetroSuccess,
                                alignEnd = true,
                                modifier = Modifier.weight(1f)
                            )
                        }

                        Spacer(modifier = Modifier.height(16.dp))

                        HorizontalDivider(
                            thickness = 0.5.dp,
                            color = extendedColors.textSecondary.copy(alpha = 0.2f)
                        )

                        Spacer(modifier = Modifier.height(14.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.Bottom
                        ) {
                            Column {
                                Text(
                                    "Additional fare",
                                    style = MaterialTheme.typography.labelMedium,
                                    color = extendedColors.textSecondary
                                )
                                Text(
                                    FareCalculator.format(extraFare),
                                    color = MaterialTheme.colorScheme.primary,
                                    fontSize = 30.sp,
                                    fontFamily = AppFont.display,
                                    fontWeight = FontWeight.Black
                                )
                            }
                            Column(horizontalAlignment = Alignment.End) {
                                Text(
                                    "New total",
                                    style = MaterialTheme.typography.labelMedium,
                                    color = extendedColors.textSecondary
                                )
                                Text(
                                    FareCalculator.format(ticket.totalFare + extraFare),
                                    color = extendedColors.textPrimary,
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(20.dp))

                Text(
                    "Pay With",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold,
                    color = extendedColors.textPrimary
                )
                Spacer(modifier = Modifier.height(12.dp))

                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    paymentBrands.forEach { brand ->
                        PaymentMethodItem(
                            brand = brand,
                            isSelected = selectedMethod == brand.id,
                            onClick = { if (!isProcessing) selectedMethod = brand.id }
                        )
                    }
                }

                Spacer(modifier = Modifier.height(20.dp))

                Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    OutlinedButton(
                        onClick = { selectedStation = null },
                        enabled = !isProcessing,
                        modifier = Modifier.height(56.dp),
                        shape = RoundedCornerShape(16.dp)
                    ) {
                        Text("Change")
                    }
                    Button(
                        onClick = { isProcessing = true },
                        enabled = selectedMethod != null && !isProcessing,
                        modifier = Modifier
                            .weight(1f)
                            .height(56.dp),
                        shape = RoundedCornerShape(16.dp)
                    ) {
                        if (isProcessing) {
                            CircularProgressIndicator(
                                color = Color.White,
                                strokeWidth = 2.dp,
                                modifier = Modifier.size(22.dp)
                            )
                        } else {
                            Text(
                                if (selectedMethod == null) "Select a method"
                                else "Pay ${FareCalculator.format(extraFare)}",
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun OnwardStationRow(
    station: MetroStation,
    extraFare: Int,
    extraStops: Int,
    onClick: () -> Unit
) {
    val extendedColors = MetroTransitTheme.extendedColors
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() },
        shape = RoundedCornerShape(16.dp),
        color = extendedColors.glass,
        border = BorderStroke(1.dp, extendedColors.glassBorder)
    ) {
        Row(
            modifier = Modifier.padding(14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Surface(
                shape = CircleShape,
                color = MaterialTheme.colorScheme.primary.copy(alpha = 0.12f),
                modifier = Modifier.size(36.dp)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Text(
                        station.code,
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold,
                        fontSize = 9.sp,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
            }
            Spacer(modifier = Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    station.name,
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Bold,
                    color = extendedColors.textPrimary
                )
                Text(
                    "$extraStops stop${if (extraStops > 1) "s" else ""} further · ~${extraStops * 2} min",
                    style = MaterialTheme.typography.labelSmall,
                    color = extendedColors.textSecondary,
                    fontSize = 10.sp
                )
            }
            Text(
                "+${FareCalculator.format(extraFare)}",
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Black,
                color = MaterialTheme.colorScheme.primary
            )
        }
    }
}

@Composable
private fun JourneySummaryDialog(ticket: QRTicket, entryMillis: Long, onDone: () -> Unit) {
    val exitMillis = ticket.completedAtMillis ?: System.currentTimeMillis()
    val stops = FareCalculator.hops(ticket.fromStationId, ticket.toStationId)

    // Demo rides last seconds, real ones minutes — read naturally either way.
    val elapsedSeconds = ((exitMillis - entryMillis) / 1000L).coerceAtLeast(0L)
    val duration =
        if (elapsedSeconds < 60) "$elapsedSeconds sec" else "${elapsedSeconds / 60} min"

    // Small settle-in on the tick so the receipt feels like a confirmation, not a form.
    var appeared by remember { mutableStateOf(false) }
    LaunchedEffect(Unit) { appeared = true }
    val tickScale by animateFloatAsState(
        targetValue = if (appeared) 1f else 0.5f,
        animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy, stiffness = Spring.StiffnessLow),
        label = "tickScale"
    )

    val palette = passPalette()

    Dialog(
        onDismissRequest = onDone,
        // Opt out of the narrow platform dialog width so the pass can use the screen.
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        // One pass, top to bottom — a mixed dark/light card reads as two objects.
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp),
            shape = RoundedCornerShape(28.dp),
            color = palette.surface,
            shadowElevation = 12.dp
        ) {
            Box {
                // Gradient and map texture span the whole card, so there is no seam
                // between the hero and the total — it is one printed pass.
                Box(
                    modifier = Modifier
                        .matchParentSize()
                        .background(palette.gradient)
                )
                MetroMapTexture(
                    lineColor = palette.textureLine,
                    dotColor = palette.textureDot,
                    modifier = Modifier.matchParentSize()
                )

                Column(modifier = Modifier.verticalScroll(rememberScrollState())) {
                    // ── Boarding-pass hero: the journey at a glance ───
                    JourneyPassPanel(
                        ticket = ticket,
                        entryMillis = entryMillis,
                        exitMillis = exitMillis,
                        stops = stops,
                        duration = duration,
                        tickScale = tickScale,
                        palette = palette
                    )

                    // ── What it cost: the total only ─────────────────
                    Column(modifier = Modifier.padding(horizontal = 22.dp)) {
                        HorizontalDivider(color = palette.hairline)
                        Spacer(modifier = Modifier.height(16.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                "Total paid",
                                style = MaterialTheme.typography.bodyLarge,
                                fontWeight = FontWeight.Bold,
                                color = palette.onPass
                            )
                            Text(
                                FareCalculator.format(ticket.totalFare),
                                fontFamily = AppFont.display,
                                fontSize = 28.sp,
                                fontWeight = FontWeight.Black,
                                color = palette.accent
                            )
                        }

                        Spacer(modifier = Modifier.height(18.dp))

                        Button(
                            onClick = onDone,
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(54.dp),
                            shape = RoundedCornerShape(16.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = palette.buttonContainer,
                                contentColor = palette.buttonContent
                            )
                        ) {
                            Text("Done", fontWeight = FontWeight.Bold)
                        }

                        Spacer(modifier = Modifier.height(22.dp))
                    }
                }
            }
        }
    }
}

private val PassInk = Color(0xFF0B1220)

/** Every colour the pass needs, so the card is one coherent object in either theme. */
private data class PassPalette(
    val surface: Color,
    val gradient: Brush,
    val onPass: Color,
    val muted: Color,
    val hairline: Color,
    val accent: Color,
    val textureLine: Color,
    val textureDot: Color,
    val buttonContainer: Color,
    val buttonContent: Color
)

@Composable
private fun passPalette(): PassPalette {
    // Reads the resolved scheme rather than the system setting, so the in-app
    // light/dark override is respected too.
    val dark = MaterialTheme.colorScheme.surface.luminance() < 0.5f
    val extendedColors = MetroTransitTheme.extendedColors

    return if (dark) {
        PassPalette(
            surface = PassInk,
            gradient = Brush.verticalGradient(listOf(Color(0xFF16233D), PassInk)),
            onPass = Color.White,
            muted = Color.White.copy(alpha = 0.55f),
            hairline = Color.White.copy(alpha = 0.12f),
            accent = Color(0xFF7DB3FF),
            textureLine = Color.White.copy(alpha = 0.05f),
            textureDot = Color.White.copy(alpha = 0.08f),
            buttonContainer = Color.White,
            buttonContent = PassInk
        )
    } else {
        PassPalette(
            surface = Color.White,
            gradient = Brush.verticalGradient(listOf(Color.White, Color(0xFFEEF2F8))),
            onPass = extendedColors.textPrimary,
            muted = extendedColors.textSecondary,
            hairline = Color.Black.copy(alpha = 0.10f),
            accent = MaterialTheme.colorScheme.primary,
            textureLine = Color.Black.copy(alpha = 0.05f),
            textureDot = Color.Black.copy(alpha = 0.07f),
            buttonContainer = MaterialTheme.colorScheme.primary,
            buttonContent = Color.White
        )
    }
}

/**
 * Dark "pass" panel that opens the receipt: station codes at a glance, the way a boarding
 * pass shows origin and destination, over a faint metro-map texture.
 */
@Composable
private fun JourneyPassPanel(
    ticket: QRTicket,
    entryMillis: Long,
    exitMillis: Long,
    stops: Int,
    duration: String,
    tickScale: Float,
    palette: PassPalette
) {
    val fromCode = FareCalculator.station(ticket.fromStationId)?.code ?: "—"
    val toCode = FareCalculator.station(ticket.toStationId)?.code ?: "—"

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 22.dp)
            .padding(top = 22.dp, bottom = 18.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
            Surface(
                shape = CircleShape,
                color = TicketGreen,
                modifier = Modifier
                    .size(44.dp)
                    .scale(tickScale)
            ) {
                Icon(
                    Icons.Default.Check,
                    contentDescription = null,
                    tint = Color.White,
                    modifier = Modifier.padding(11.dp)
                )
            }
            Spacer(modifier = Modifier.height(10.dp))
            Text(
                "Journey Complete",
                fontFamily = AppFont.display,
                fontSize = 20.sp,
                fontWeight = FontWeight.Black,
                color = palette.onPass
            )
            Text(
                "Exit gate cleared · ${clockTime(exitMillis)}",
                style = MaterialTheme.typography.labelSmall,
                color = palette.muted
            )

            Spacer(modifier = Modifier.height(20.dp))

            // ── Origin → destination, boarding-pass style ────────────
            Row(modifier = Modifier.fillMaxWidth()) {
                PassEndpoint(
                    code = fromCode,
                    station = ticket.fromStation,
                    palette = palette,
                    modifier = Modifier.weight(1f)
                )
                Column(
                    modifier = Modifier.padding(top = 8.dp, start = 6.dp, end = 6.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    // Train then arrow, so the pass reads as travel *towards* the
                    // destination rather than a static pair of stations. The arrow is
                    // auto-mirrored, so it still points at the exit in an RTL locale.
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            Icons.Default.DirectionsSubway,
                            contentDescription = null,
                            tint = palette.muted,
                            modifier = Modifier.size(17.dp)
                        )
                        Spacer(modifier = Modifier.width(2.dp))
                        Icon(
                            Icons.AutoMirrored.Filled.ArrowForward,
                            contentDescription = "towards $toCode",
                            tint = palette.muted,
                            modifier = Modifier.size(13.dp)
                        )
                    }
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        duration,
                        style = MaterialTheme.typography.labelSmall,
                        color = palette.muted,
                        fontSize = 10.sp
                    )
                }
                PassEndpoint(
                    code = toCode,
                    station = ticket.toStation,
                    palette = palette,
                    alignment = Alignment.End,
                    modifier = Modifier.weight(1f)
                )
            }

            Spacer(modifier = Modifier.height(18.dp))
            HorizontalDivider(color = palette.hairline)
            Spacer(modifier = Modifier.height(14.dp))

            Row(modifier = Modifier.fillMaxWidth()) {
                PassDetail("Entry", clockTime(entryMillis), palette, Modifier.weight(1f))
                PassDetail("Exit", clockTime(exitMillis), palette, Modifier.weight(1f), Alignment.End)
            }
            Spacer(modifier = Modifier.height(12.dp))
            Row(modifier = Modifier.fillMaxWidth()) {
                PassDetail(
                    "Stations",
                    "$stops stop${if (stops == 1) "" else "s"}",
                    palette,
                    Modifier.weight(1f)
                )
                PassDetail("Line", "MRT Line-6", palette, Modifier.weight(1f), Alignment.End)
            }
    }
}

@Composable
private fun PassEndpoint(
    code: String,
    station: String,
    palette: PassPalette,
    modifier: Modifier = Modifier,
    alignment: Alignment.Horizontal = Alignment.Start
) {
    Column(modifier = modifier, horizontalAlignment = alignment) {
        Text(
            code,
            fontFamily = AppFont.display,
            fontSize = 34.sp,
            fontWeight = FontWeight.Black,
            color = palette.onPass,
            maxLines = 1
        )
        Text(
            station,
            style = MaterialTheme.typography.labelSmall,
            color = palette.muted,
            fontSize = 10.sp,
            maxLines = 2,
            textAlign = if (alignment == Alignment.End) TextAlign.End else TextAlign.Start
        )
    }
}

@Composable
private fun PassDetail(
    label: String,
    value: String,
    palette: PassPalette,
    modifier: Modifier = Modifier,
    alignment: Alignment.Horizontal = Alignment.Start
) {
    Column(modifier = modifier, horizontalAlignment = alignment) {
        Text(
            label,
            style = MaterialTheme.typography.labelSmall,
            color = palette.muted.copy(alpha = 0.8f),
            fontSize = 9.sp
        )
        Text(
            value,
            style = MaterialTheme.typography.labelLarge,
            fontWeight = FontWeight.Bold,
            color = palette.onPass
        )
    }
}

/**
 * Faint schematic metro lines behind the pass — the local answer to the world map on a
 * flight pass. Fixed coordinates, so it renders identically every time.
 */
@Composable
private fun MetroMapTexture(
    lineColor: Color,
    dotColor: Color,
    modifier: Modifier = Modifier
) {
    // Normalised polylines: gentle 45° doglegs, the way transit maps are drawn.
    val routes = listOf(
        listOf(0f to 0.82f, 0.28f to 0.82f, 0.44f to 0.62f, 1f to 0.62f),
        listOf(0f to 0.30f, 0.22f to 0.30f, 0.40f to 0.12f, 0.78f to 0.12f),
        listOf(0.12f to 1f, 0.12f to 0.55f, 0.30f to 0.38f, 0.30f to 0f),
        listOf(0.62f to 0f, 0.62f to 0.34f, 0.82f to 0.52f, 0.82f to 1f)
    )

    Canvas(modifier = modifier) {
        val stroke = 2.dp.toPx()
        val dotRadius = 3.dp.toPx()
        routes.forEach { route ->
            val points = route.map { (x, y) -> Offset(x * size.width, y * size.height) }
            points.zipWithNext { start, end ->
                drawLine(
                    color = lineColor,
                    start = start,
                    end = end,
                    strokeWidth = stroke,
                    cap = StrokeCap.Round
                )
            }
            // Interchange dots on the bends only — keeps the texture quiet.
            points.drop(1).dropLast(1).forEach { drawCircle(dotColor, dotRadius, it) }
        }
    }
}


@Composable
private fun JourneySummaryRow(label: String, value: String) {
    val extendedColors = MetroTransitTheme.extendedColors
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 3.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(label, style = MaterialTheme.typography.labelMedium, color = extendedColors.textSecondary)
        Text(
            value,
            style = MaterialTheme.typography.labelMedium,
            fontWeight = FontWeight.Bold,
            color = extendedColors.textPrimary
        )
    }
}
