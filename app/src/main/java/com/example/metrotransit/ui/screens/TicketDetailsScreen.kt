package com.example.metrotransit.ui.screens

import android.content.Context
import android.content.Intent
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.AddLocationAlt
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.DirectionsSubway
import androidx.compose.material.icons.filled.QrCodeScanner
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.metrotransit.data.FareCalculator
import com.example.metrotransit.data.QRTicket
import com.example.metrotransit.data.TicketStatus
import com.example.metrotransit.ui.theme.MetroTransitTheme
import kotlinx.coroutines.delay

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TicketDetailsScreen(
    ticket: QRTicket,
    onBack: () -> Unit,
    onClose: () -> Unit,
    onValidate: () -> Unit = {},
    onOpenJourney: () -> Unit = {},
    onEntryWindowLapsed: () -> Unit = {}
) {
    val extendedColors = MetroTransitTheme.extendedColors
    val scrollState = rememberScrollState()
    val context = LocalContext.current

    val isExpired = ticket.isExpired
    // Counts the entry window before validation and the ride window after it. Both are
    // derived from timestamps on the ticket, so the countdown survives leaving the screen.
    var timeLeft by remember(ticket.id) { mutableIntStateOf(ticket.remainingSeconds()) }

    LaunchedEffect(ticket.id, ticket.validityMinutes, ticket.isValidated, isExpired) {
        while (!isExpired) {
            timeLeft = ticket.remainingSeconds()
            if (timeLeft <= 0) {
                // An un-validated ticket that runs out of entry time is no longer usable.
                if (!ticket.isValidated) onEntryWindowLapsed()
                break
            }
            delay(1000)
        }
    }

    val timeString = formatCountdown(timeLeft)
    val statusStyle = ticketStatusStyle(ticket.status)

    Scaffold(
        containerColor = Color.Transparent,
        topBar = {
            TopAppBar(
                title = { NavTitle("Metro Ticket", color = MaterialTheme.colorScheme.onSurface) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        BackIcon(tint = MaterialTheme.colorScheme.onSurface)
                    }
                },
                actions = {
                    IconButton(onClick = onClose) {
                        Icon(Icons.Default.Close, contentDescription = "Close")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.Transparent)
            )
        }
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
                    .padding(16.dp)
                    .padding(bottom = LocalJourneyBarInset.current),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // ── Ticket Card ──────────────────────────────────────────
                LiquidGlassSurface(
                    modifier = Modifier.fillMaxWidth(),
                    cornerRadius = 32.dp,
                    // The perforation halfway down bites real holes out of this panel.
                    punchable = true
                ) {
                    Column(
                        modifier = Modifier.padding(24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            if (ticket.isInTransit) "Boarding Pass" else "Entry Ticket",
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = FontWeight.Bold,
                            color = extendedColors.textSecondary
                        )

                        Spacer(modifier = Modifier.height(24.dp))

                        // QR Code
                        val isStamped = isExpired || ticket.isCompleted

                        TicketQrCard(
                            data = ticket.qrData,
                            modifier = Modifier.size(240.dp),
                            alpha = if (isStamped) 0.12f else 1f
                        ) {
                            if (isStamped) {
                                val stampColor =
                                    if (isExpired) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.primary
                                Surface(
                                    color = stampColor.copy(alpha = 0.12f),
                                    shape = RoundedCornerShape(8.dp),
                                    modifier = Modifier.rotate(-15f)
                                ) {
                                    Text(
                                        if (isExpired) "EXPIRED" else "COMPLETED",
                                        style = MaterialTheme.typography.headlineSmall,
                                        fontWeight = FontWeight.Black,
                                        color = stampColor.copy(alpha = 0.7f),
                                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                                    )
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(16.dp))

                        Text(
                            "ID: ${ticket.id}",
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.Bold,
                            color = extendedColors.textPrimary
                        )

                        Spacer(modifier = Modifier.height(8.dp))

                        Surface(shape = RoundedCornerShape(8.dp), color = statusStyle.container) {
                            Row(
                                modifier = Modifier.padding(horizontal = 12.dp, vertical = 4.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    statusStyle.icon,
                                    contentDescription = null,
                                    tint = statusStyle.content,
                                    modifier = Modifier.size(12.dp)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    ticket.status,
                                    color = statusStyle.content,
                                    fontWeight = FontWeight.Bold,
                                    style = MaterialTheme.typography.labelSmall
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(24.dp))

                        // Divider with circles on sides
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(40.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Canvas(modifier = Modifier.fillMaxSize()) {
                                val pathEffect = PathEffect.dashPathEffect(floatArrayOf(15f, 15f), 0f)

                                // The divider sits inside the card's 24dp padding, so the
                                // notches have to reach back out to the panel's edges.
                                drawGlassNotches(radius = 12.dp, edgeInset = 24.dp)

                                drawLine(
                                    color = extendedColors.textSecondary.copy(alpha = 0.3f),
                                    start = Offset(0f, size.height / 2),
                                    end = Offset(size.width, size.height / 2),
                                    pathEffect = pathEffect,
                                    strokeWidth = 1.dp.toPx()
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(24.dp))

                        // Details
                        Row(modifier = Modifier.fillMaxWidth()) {
                            DetailItem("From", ticket.fromStation, Modifier.weight(1f), extendedColors = extendedColors)
                            DetailItem("To", ticket.toStation, Modifier.weight(1f), Alignment.End, extendedColors = extendedColors)
                        }

                        Spacer(modifier = Modifier.height(20.dp))

                        Row(modifier = Modifier.fillMaxWidth()) {
                            DetailItem("Date & Time", ticket.dateTime, Modifier.weight(1f), extendedColors = extendedColors)
                            DetailItem("Fare", ticket.fare, Modifier.weight(1f), Alignment.End, extendedColors = extendedColors)
                        }

                        if (ticket.extensions.isNotEmpty()) {
                            Spacer(modifier = Modifier.height(20.dp))
                            ExtensionSummary(ticket)
                        }

                        Spacer(modifier = Modifier.height(32.dp))

                        // ── Validity + gate validation ─────────────────────
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            val timerTextColor = when {
                                ticket.isCompleted -> MaterialTheme.colorScheme.primary
                                isExpired || timeLeft < 300 -> MaterialTheme.colorScheme.error
                                else -> extendedColors.textPrimary
                            }

                            LiquidGlassSurface(
                                modifier = Modifier
                                    .weight(1f)
                                    .height(96.dp),
                                cornerRadius = 16.dp,
                                // Glass inside glass: mirrored, weaker light, so the panel
                                // still separates from the card it sits on.
                                light = LiquidGlassLight.Inset,
                                // An expired ticket tints its own glass red rather than
                                // relying on the countdown text alone.
                                tint = if (isExpired) {
                                    MaterialTheme.colorScheme.error.copy(alpha = 0.14f)
                                } else {
                                    Color.Unspecified
                                }
                            ) {
                                Column(
                                    modifier = Modifier.fillMaxSize(),
                                    verticalArrangement = Arrangement.Center,
                                    horizontalAlignment = Alignment.CenterHorizontally
                                ) {
                                    Text(
                                        when {
                                            isExpired -> "Ticket Status"
                                            ticket.isCompleted -> "Journey"
                                            ticket.isValidated -> "Exit Within"
                                            else -> "Validate Within"
                                        },
                                        style = MaterialTheme.typography.labelSmall,
                                        color = extendedColors.textSecondary
                                    )
                                    Text(
                                        when {
                                            isExpired -> "EXPIRED"
                                            ticket.isCompleted -> "DONE"
                                            else -> timeString
                                        },
                                        style = MaterialTheme.typography.headlineMedium,
                                        fontWeight = FontWeight.Black,
                                        color = timerTextColor
                                    )
                                }
                            }

                            GateValidationAction(
                                ticket = ticket,
                                timeLeft = timeLeft,
                                onValidate = onValidate,
                                onOpenJourney = onOpenJourney
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))


                Spacer(modifier = Modifier.height(16.dp))

                LiquidGlassSurface(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(56.dp),
                    cornerRadius = 16.dp,
                    onClick = { context.shareTicket(ticket, timeString) }
                ) {
                    Row(
                        modifier = Modifier.fillMaxSize(),
                        horizontalArrangement = Arrangement.Center,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(Icons.Default.Share, contentDescription = null, tint = extendedColors.textPrimary)
                        Spacer(modifier = Modifier.width(12.dp))
                        Text("Share Ticket", color = extendedColors.textPrimary, fontWeight = FontWeight.Bold)
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))
            }
        }
    }
}

/**
 * Hands the ticket to whatever the rider shares with — chat, mail, notes. Plain text on
 * purpose: the QR code is only good at the gate that scans this phone, so what travels is
 * the trip itself plus the ticket ID a station desk can look up.
 */
private fun Context.shareTicket(ticket: QRTicket, timeString: String) {
    val summary = buildString {
        appendLine(if (ticket.isInTransit) "Metro Boarding Pass" else "Metro Entry Ticket")
        appendLine()
        appendLine("${ticket.fromStation} \u2192 ${ticket.toStation}")
        appendLine(ticket.dateTime)
        appendLine("Fare: ${ticket.fare}")
        appendLine("Status: ${ticket.status}")
        // The countdown only means something while a window is still running.
        if (!ticket.isExpired && !ticket.isCompleted) {
            appendLine(if (ticket.isValidated) "Exit within: $timeString" else "Validate within: $timeString")
        }
        ticket.extensions.forEach { extension ->
            appendLine("Extended to ${extension.toStationName} (+${FareCalculator.format(extension.extraFare)})")
        }
        appendLine()
        append("Ticket ID: ${ticket.id}")
    }

    val intent = Intent(Intent.ACTION_SEND).apply {
        type = "text/plain"
        putExtra(Intent.EXTRA_SUBJECT, "Metro Ticket ${ticket.id}")
        putExtra(Intent.EXTRA_TEXT, summary)
    }
    startActivity(Intent.createChooser(intent, "Share Ticket"))
}

/**
 * Stands in for the station gate reader. Tapping it means "the reader scanned my QR",
 * which starts the live journey; afterwards it becomes the way back into that journey.
 */
@Composable
private fun GateValidationAction(
    ticket: QRTicket,
    timeLeft: Int,
    onValidate: () -> Unit,
    onOpenJourney: () -> Unit
) {
    val extendedColors = MetroTransitTheme.extendedColors
    val canValidate = ticket.status == TicketStatus.ACTIVE && !ticket.isExpired && timeLeft > 0

    val label: String
    val icon = when {
        ticket.isInTransit -> Icons.Default.DirectionsSubway
        else -> Icons.Default.QrCodeScanner
    }
    val container: Color
    val content: Color

    when {
        canValidate -> {
            label = "Validate"
            container = MaterialTheme.colorScheme.primary
            content = Color.White
        }
        ticket.isInTransit -> {
            label = "Journey"
            container = TicketAmber
            content = Color.White
        }
        ticket.isCompleted -> {
            label = "Used"
            container = extendedColors.textSecondary.copy(alpha = 0.15f)
            content = extendedColors.textSecondary
        }
        else -> {
            label = "Invalid"
            container = extendedColors.textSecondary.copy(alpha = 0.15f)
            content = extendedColors.textSecondary
        }
    }

    val enabled = canValidate || ticket.isInTransit

    // A soft pulse tells the rider this is the thing to press at the gate.
    val pulse by rememberInfiniteTransition(label = "gatePulse").animateFloat(
        initialValue = 0.55f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(900, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "gatePulseAlpha"
    )

    val body = @Composable {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(8.dp),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(
                icon,
                contentDescription = label,
                tint = if (canValidate) content.copy(alpha = pulse) else content,
                modifier = Modifier.size(28.dp)
            )
            Spacer(modifier = Modifier.height(6.dp))
            Text(
                label,
                style = MaterialTheme.typography.labelMedium,
                fontWeight = FontWeight.Bold,
                color = content,
                textAlign = TextAlign.Center
            )
        }
    }

    val size = Modifier
        .width(104.dp)
        .height(96.dp)

    // A live action stays solid: this is the button a rider hunts for at the gate, and glass
    // would make it recede. Once it is spent, it becomes glass like everything around it.
    if (enabled) {
        Surface(
            modifier = size.clickable { if (canValidate) onValidate() else onOpenJourney() },
            shape = RoundedCornerShape(16.dp),
            color = container,
            shadowElevation = 4.dp,
            content = body
        )
    } else {
        LiquidGlassSurface(
            modifier = size,
            cornerRadius = 16.dp,
            light = LiquidGlassLight.Inset,
            content = body
        )
    }
}

/** Shows what the rider added mid-journey, so the fare on the ticket is explainable. */
@Composable
private fun ExtensionSummary(ticket: QRTicket) {
    val extendedColors = MetroTransitTheme.extendedColors
    LiquidGlassSurface(
        modifier = Modifier.fillMaxWidth(),
        cornerRadius = 16.dp,
        light = LiquidGlassLight.Inset,
        tint = MaterialTheme.colorScheme.primary.copy(alpha = 0.07f)
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    Icons.Default.AddLocationAlt,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(14.dp)
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    "Extended ${ticket.extensions.size}×",
                    style = MaterialTheme.typography.labelSmall,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )
            }
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                "Base fare ${FareCalculator.format(ticket.baseFare)}",
                style = MaterialTheme.typography.labelSmall,
                color = extendedColors.textSecondary
            )
            ticket.extensions.forEach { extension ->
                Text(
                    "→ ${extension.toStationName}  +${FareCalculator.format(extension.extraFare)} · ${extension.paymentMethod}",
                    style = MaterialTheme.typography.labelSmall,
                    color = extendedColors.textSecondary,
                    fontSize = 11.sp
                )
            }
        }
    }
}

@Composable
fun DetailItem(label: String, value: String, modifier: Modifier = Modifier, alignment: Alignment.Horizontal = Alignment.Start, extendedColors: com.example.metrotransit.ui.theme.ExtendedColors) {
    Column(modifier = modifier, horizontalAlignment = alignment) {
        Text(label, style = MaterialTheme.typography.labelSmall, color = extendedColors.textSecondary)
        Text(value, style = MaterialTheme.typography.bodyLarge, fontWeight = FontWeight.Bold, color = extendedColors.textPrimary)
    }
}
