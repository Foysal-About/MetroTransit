package com.example.metrotransit.ui.screens

import androidx.compose.foundation.Image
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import com.example.metrotransit.R
import com.example.metrotransit.data.FareCalculator
import com.example.metrotransit.data.MetroStation
import com.example.metrotransit.data.StationData
import com.example.metrotransit.ui.theme.AppFont
import com.example.metrotransit.ui.theme.MetroSuccess
import com.example.metrotransit.ui.theme.MetroTransitTheme
import com.example.metrotransit.viewmodel.TicketViewModel
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun QuickPayScreen(
    fromId: Int,
    toId: Int,
    onBack: () -> Unit,
    /**
     * Confirming the fare hands the rider to the SSLCOMMERZ page. Carries the stations actually
     * on screen rather than the ones in the route — the rider can change them here.
     */
    onProceedToPayment: (Int, Int) -> Unit,
    /** What a cancelled or declined session left behind, said once above the confirm card. */
    gatewayNotice: String? = null,
    ticketViewModel: TicketViewModel? = null,
    onTicketClick: (String) -> Unit = {}
) {
    // Seeded from the route, then owned by this screen: the journey can be re-picked from
    // the summary card without going back to the home page. Held as ids and saved, so a
    // rotation or a theme switch does not quietly put the original journey back.
    var fromStationId by rememberSaveable(fromId) { mutableIntStateOf(fromId) }
    var toStationId by rememberSaveable(toId) { mutableIntStateOf(toId) }

    val fromStation = StationData.stations.find { it.id == fromStationId }
    val toStation = StationData.stations.find { it.id == toStationId }
    
    val extendedColors = MetroTransitTheme.extendedColors
    val pagerState = rememberPagerState { 2 }
    val coroutineScope = rememberCoroutineScope()

    Scaffold(
        containerColor = Color.Transparent,
        topBar = {
            Column {
                TopAppBar(
                    title = { NavTitle("Ticket Portal") },
                    navigationIcon = {
                        IconButton(onClick = onBack) {
                            BackIcon()
                        }
                    },
                    colors = TopAppBarDefaults.topAppBarColors(
                        containerColor = Color.Transparent
                    )
                )
                
                PrimaryTabRow(
                    selectedTabIndex = pagerState.currentPage,
                    containerColor = Color.Transparent,
                    contentColor = MaterialTheme.colorScheme.primary,
                    divider = {}
                ) {
                    Tab(
                        selected = pagerState.currentPage == 0,
                        onClick = { coroutineScope.launch { pagerState.animateScrollToPage(0) } },
                        text = { Text("Buy Ticket", fontWeight = FontWeight.Bold) }
                    )
                    Tab(
                        selected = pagerState.currentPage == 1,
                        onClick = { coroutineScope.launch { pagerState.animateScrollToPage(1) } },
                        text = { Text("My Tickets", fontWeight = FontWeight.Bold) }
                    )
                }
            }
        }
    ) { padding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(extendedColors.backgroundGradient)
        ) {
            HorizontalPager(
                state = pagerState,
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding),
                verticalAlignment = Alignment.Top
            ) { page ->
                when (page) {
                    0 -> BuyTicketContent(
                        fromStation = fromStation,
                        toStation = toStation,
                        onFromChange = { fromStationId = it.id },
                        onToChange = { toStationId = it.id },
                        onSwap = {
                            val previousFrom = fromStationId
                            fromStationId = toStationId
                            toStationId = previousFrom
                        },
                        onProceedToPayment = onProceedToPayment,
                        gatewayNotice = gatewayNotice
                    )
                    1 -> MyTicketsContent(
                        viewModel = ticketViewModel,
                        onTicketClick = onTicketClick
                    )
                }
            }
        }
    }
}

@Composable
fun BuyTicketContent(
    fromStation: MetroStation?,
    toStation: MetroStation?,
    onFromChange: (MetroStation) -> Unit,
    onToChange: (MetroStation) -> Unit,
    onSwap: () -> Unit,
    onProceedToPayment: (Int, Int) -> Unit,
    gatewayNotice: String? = null
) {
    val scrollState = rememberScrollState()

    val amount = FareCalculator.fare(fromStation, toStation)

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(scrollState)
            .padding(bottom = LocalJourneyBarInset.current)
    ) {
        JourneyCard(
            fromStation = fromStation,
            toStation = toStation,
            amount = amount,
            onFromChange = onFromChange,
            onToChange = onToChange,
            onSwap = onSwap
        )

        // ── Payment ──────────────────────────────────────────────────
        // The fare is confirmed here and paid somewhere else. SSLCOMMERZ's hosted checkout is
        // a page of its own, so this one ends at the confirm button — no card fields sit next
        // to the ticket they would be paying for.
        if (gatewayNotice != null) {
            GatewayNotice(
                message = gatewayNotice,
                modifier = Modifier.padding(start = 16.dp, end = 16.dp, bottom = 8.dp)
            )
        }

        ConfirmPaymentCard(
            amount = amount,
            // A journey needs both of its ends before there is anything to charge for.
            enabled = fromStation != null && toStation != null,
            onConfirm = { onProceedToPayment(fromStation?.id ?: 0, toStation?.id ?: 0) }
        )

        Spacer(modifier = Modifier.height(20.dp))
    }
}

/**
 * The handover: what happens next, and the button that starts it.
 *
 * The button carries the amount rather than the destination — the rider is confirming a fare,
 * and the gateway page is only how it gets paid — so the card above it says where they are
 * about to be taken and what will come back.
 */
@Composable
private fun ConfirmPaymentCard(
    amount: Int,
    enabled: Boolean,
    onConfirm: () -> Unit
) {
    val extendedColors = MetroTransitTheme.extendedColors

    LiquidGlassSurface(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp),
        cornerRadius = 24.dp
    ) {
        Column(modifier = Modifier.padding(20.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    Icons.Default.Lock,
                    contentDescription = null,
                    tint = MetroSuccess,
                    modifier = Modifier.size(15.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    "Secure payment",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold,
                    color = extendedColors.textPrimary
                )
            }

            Spacer(modifier = Modifier.height(6.dp))

            Text(
                "The SSLCOMMERZ payment window opens over this page to take a card, mobile " +
                    "wallet or bank. The ticket is issued once the payment comes back approved.",
                style = MaterialTheme.typography.bodySmall,
                color = extendedColors.textSecondary
            )

            Spacer(modifier = Modifier.height(16.dp))

            Button(
                onClick = onConfirm,
                enabled = enabled,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp),
                shape = RoundedCornerShape(16.dp)
            ) {
                Text("Proceed to Payment · ৳$amount", fontSize = 16.sp, fontWeight = FontWeight.Bold)
                Spacer(modifier = Modifier.width(8.dp))
                Icon(
                    Icons.AutoMirrored.Filled.ArrowForward,
                    contentDescription = null,
                    modifier = Modifier.size(18.dp)
                )
            }
        }
    }
}

/** A cancelled or declined session, said once above the confirm card. */
@Composable
private fun GatewayNotice(message: String, modifier: Modifier = Modifier) {
    val extendedColors = MetroTransitTheme.extendedColors

    Surface(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        color = MaterialTheme.colorScheme.error.copy(alpha = 0.10f)
    ) {
        Row(
            modifier = Modifier.padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                Icons.Default.Info,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.error,
                modifier = Modifier.size(16.dp)
            )
            Spacer(modifier = Modifier.width(10.dp))
            Text(
                message,
                style = MaterialTheme.typography.bodySmall,
                color = extendedColors.textPrimary
            )
        }
    }
}

@Composable
fun MyTicketsContent(
    viewModel: TicketViewModel?,
    onTicketClick: (String) -> Unit
) {
    val extendedColors = MetroTransitTheme.extendedColors
    
    Box(
        modifier = Modifier.fillMaxSize()
    ) {
        if (viewModel == null || viewModel.tickets.isEmpty()) {
            Column(
                modifier = Modifier.fillMaxSize(),
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Icon(
                    Icons.Default.ConfirmationNumber,
                    contentDescription = null,
                    modifier = Modifier.size(64.dp),
                    tint = extendedColors.textSecondary.copy(alpha = 0.3f)
                )
                Spacer(modifier = Modifier.height(16.dp))
                Text(
                    "No tickets yet",
                    style = MaterialTheme.typography.titleMedium,
                    color = extendedColors.textSecondary
                )
            }
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(
                    start = 16.dp,
                    top = 16.dp,
                    end = 16.dp,
                    bottom = 16.dp + LocalJourneyBarInset.current
                ),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                itemsIndexed(viewModel.tickets) { index, ticket ->
                    TicketCard(
                        ticket = ticket,
                        onClick = { onTicketClick(ticket.id) },
                        light = liquidGlassLightAt(index)
                    )
                }
            }
        }
    }
}


/**
 * The journey being paid for: where it starts, where it ends, and what it costs.
 *
 * Both ends are pickers rather than labels — a rider who got here with the wrong station
 * should not have to walk back to the home page to fix it — and the fare below re-reads
 * itself from whatever the pair currently is.
 *
 * The card is built on a 4dp rhythm: [CardPadding] around everything, [SectionGap] between
 * the journey and the money, and nothing else free-floating.
 */
@Composable
private fun JourneyCard(
    fromStation: MetroStation?,
    toStation: MetroStation?,
    amount: Int,
    onFromChange: (MetroStation) -> Unit,
    onToChange: (MetroStation) -> Unit,
    onSwap: () -> Unit
) {
    val extendedColors = MetroTransitTheme.extendedColors
    val accent = MaterialTheme.colorScheme.primary

    LiquidGlassSurface(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 12.dp),
        cornerRadius = 28.dp,
        // A wash rather than a fill: the panel keeps its own light and the text underneath
        // it stays on the theme's own colours, which the old blue slab could not do.
        tint = accent.copy(alpha = 0.10f)
    ) {
        Column(modifier = Modifier.padding(CardPadding)) {
            // Bottom-aligned so the track runs through the station names rather than
            // floating up between them and their labels.
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.Bottom
            ) {
                JourneyStationPicker(
                    label = "From",
                    station = fromStation,
                    exclude = toStation,
                    dotColor = accent,
                    onSelect = onFromChange,
                    modifier = Modifier.weight(1f)
                )

                JourneyTrack(onSwap = onSwap)

                JourneyStationPicker(
                    label = "To",
                    station = toStation,
                    exclude = fromStation,
                    dotColor = MetroSuccess,
                    alignEnd = true,
                    onSelect = onToChange,
                    modifier = Modifier.weight(1f)
                )
            }

            HorizontalDivider(
                modifier = Modifier.padding(top = SectionGap, bottom = SectionGap),
                thickness = 0.5.dp,
                color = extendedColors.glassBorder
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Bottom
            ) {
                Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                    Text(
                        "Fare",
                        color = extendedColors.textSecondary,
                        style = MaterialTheme.typography.labelMedium
                    )
                    Text(
                        "৳$amount",
                        color = accent,
                        fontFamily = AppFont.display,
                        fontSize = 34.sp,
                        fontWeight = FontWeight.Black
                    )
                }

                Column(
                    horizontalAlignment = Alignment.End,
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Surface(
                        color = accent.copy(alpha = 0.12f),
                        shape = CircleShape
                    ) {
                        Text(
                            "Single Journey",
                            color = accent,
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold
                        )
                    }
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            Icons.Default.AccessTime,
                            contentDescription = null,
                            tint = extendedColors.textSecondary,
                            modifier = Modifier.size(14.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            "Next: 4 mins",
                            color = extendedColors.textSecondary,
                            style = MaterialTheme.typography.labelSmall
                        )
                    }
                }
            }
        }
    }
}

/** The card's outer breathing room. */
private val CardPadding = 20.dp

/**
 * The gap that separates the journey from the money below it, above and below the divider.
 * Kept tight: the station rows already carry 8dp of their own tap padding underneath, so a
 * larger value here opened a band of dead glass across the middle of the card.
 */
private val SectionGap = 8.dp

/**
 * The line between the two stations: a dashed track with a train on it, pointing at the
 * destination so the direction of travel is readable at a glance rather than inferred from
 * which side the labels are on. Tapping the train turns the journey around, where [onSwap]
 * is given — the journey page shows the same track for a pair that is already paid for and
 * can no longer be reversed.
 */
@Composable
fun JourneyTrack(onSwap: (() -> Unit)? = null) {
    val accent = MaterialTheme.colorScheme.primary
    val trackColor = accent.copy(alpha = 0.35f)

    Box(
        modifier = Modifier
            .width(84.dp)
            .height(44.dp),
        contentAlignment = Alignment.Center
    ) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val y = size.height / 2
            val head = 5.dp.toPx()
            val end = size.width - head

            drawLine(
                color = trackColor,
                start = Offset(2.dp.toPx(), y),
                end = Offset(end, y),
                strokeWidth = 1.5.dp.toPx(),
                pathEffect = PathEffect.dashPathEffect(
                    floatArrayOf(3.dp.toPx(), 3.dp.toPx())
                )
            )

            // Arrow head, pointing at the destination column.
            val tip = Offset(size.width, y)
            drawPath(
                path = Path().apply {
                    moveTo(tip.x, tip.y)
                    lineTo(tip.x - head, tip.y - head * 0.8f)
                    lineTo(tip.x - head, tip.y + head * 0.8f)
                    close()
                },
                color = trackColor
            )
        }

        Surface(
            modifier = Modifier
                .size(38.dp)
                .clip(CircleShape)
                .then(if (onSwap != null) Modifier.clickable { onSwap() } else Modifier),
            shape = CircleShape,
            color = accent.copy(alpha = 0.14f)
        ) {
            Icon(
                Icons.Default.DirectionsSubway,
                contentDescription = if (onSwap != null) "Reverse the journey" else null,
                tint = accent,
                modifier = Modifier.padding(9.dp)
            )
        }
    }
}

/**
 * One end of the journey. Tapping it opens the line, with the station picked at the other
 * end left out — a ticket from a station to itself is not a journey.
 */
@Composable
private fun JourneyStationPicker(
    label: String,
    station: MetroStation?,
    exclude: MetroStation?,
    dotColor: Color,
    onSelect: (MetroStation) -> Unit,
    modifier: Modifier = Modifier,
    alignEnd: Boolean = false
) {
    val extendedColors = MetroTransitTheme.extendedColors
    var expanded by remember { mutableStateOf(false) }

    Box(modifier = modifier) {
        Column(
            modifier = Modifier
                .clip(RoundedCornerShape(14.dp))
                .clickable { expanded = true }
                .padding(horizontal = 6.dp, vertical = 8.dp),
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

            Spacer(modifier = Modifier.height(6.dp))

            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    station?.name ?: "Select",
                    color = extendedColors.textPrimary,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    textAlign = if (alignEnd) TextAlign.End else TextAlign.Start,
                    modifier = Modifier.weight(1f, fill = false)
                )
                Icon(
                    Icons.Default.KeyboardArrowDown,
                    contentDescription = "Change $label station",
                    tint = extendedColors.textSecondary,
                    modifier = Modifier.size(18.dp)
                )
            }
        }

        MaterialTheme(shapes = MaterialTheme.shapes.copy(extraSmall = RoundedCornerShape(16.dp))) {
            DropdownMenu(
                expanded = expanded,
                onDismissRequest = { expanded = false },
                modifier = Modifier.background(extendedColors.surface)
            ) {
                StationData.stations
                    .filter { it.id != exclude?.id }
                    .forEach { option ->
                        val isSelected = option.id == station?.id
                        DropdownMenuItem(
                            text = {
                                Text(
                                    option.name,
                                    style = MaterialTheme.typography.bodyLarge,
                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                    color = if (isSelected) MaterialTheme.colorScheme.primary else extendedColors.textPrimary
                                )
                            },
                            leadingIcon = {
                                Icon(
                                    Icons.Default.Train,
                                    contentDescription = null,
                                    modifier = Modifier.size(18.dp),
                                    tint = if (isSelected) MaterialTheme.colorScheme.primary else extendedColors.textSecondary
                                )
                            },
                            onClick = {
                                onSelect(option)
                                expanded = false
                            }
                        )
                    }
            }
        }
    }
}

/**
 * One selectable payment method. Every row is the same height with the same tile in the
 * same place; picking one tints its glass in the provider's colour and rings it, so the
 * choice is obvious without breaking the row out of the material.
 *
 * All rows share one light frame on purpose — the cycling frames used elsewhere make a list
 * of near-identical rows look smudged rather than varied.
 */
@Composable
fun PaymentMethodItem(
    brand: PaymentBrand,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    val extendedColors = MetroTransitTheme.extendedColors
    LiquidGlassSurface(
        modifier = Modifier.fillMaxWidth(),
        cornerRadius = 20.dp,
        light = LiquidGlassLight.Panel,
        tint = if (isSelected) brand.color.copy(alpha = 0.1f) else Color.Unspecified,
        border = if (isSelected) {
            androidx.compose.foundation.BorderStroke(width = 2.dp, color = brand.color)
        } else {
            null
        },
        onClick = onClick
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 14.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            PaymentBrandTile(brand = brand)
            Spacer(modifier = Modifier.width(14.dp))
            Text(
                brand.name,
                style = MaterialTheme.typography.bodyLarge,
                fontWeight = FontWeight.Bold,
                color = extendedColors.textPrimary,
                modifier = Modifier.weight(1f)
            )
            RadioButton(
                selected = isSelected,
                onClick = onClick,
                colors = RadioButtonDefaults.colors(selectedColor = brand.color)
            )
        }
    }
}
