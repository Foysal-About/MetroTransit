package com.example.metrotransit.ui.screens

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
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
import androidx.compose.foundation.lazy.items
import com.example.metrotransit.R
import com.example.metrotransit.data.StationData
import com.example.metrotransit.ui.theme.MetroTransitTheme
import com.example.metrotransit.viewmodel.TicketViewModel
import kotlinx.coroutines.launch
import kotlin.math.abs

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun QuickPayScreen(
    fromId: Int,
    toId: Int,
    onBack: () -> Unit,
    onPaymentSuccess: () -> Unit,
    ticketViewModel: TicketViewModel? = null,
    onTicketClick: (String) -> Unit = {}
) {
    val fromStation = StationData.stations.find { it.id == fromId }
    val toStation = StationData.stations.find { it.id == toId }
    
    val extendedColors = MetroTransitTheme.extendedColors
    val pagerState = rememberPagerState { 2 }
    val coroutineScope = rememberCoroutineScope()

    Scaffold(
        containerColor = Color.Transparent,
        topBar = {
            Column {
                TopAppBar(
                    title = { Text("Ticket Portal", fontWeight = FontWeight.Bold, color = extendedColors.textPrimary) },
                    navigationIcon = {
                        IconButton(onClick = onBack) {
                            Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back", tint = extendedColors.textPrimary)
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
                        onPaymentSuccess = onPaymentSuccess
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
    fromStation: com.example.metrotransit.data.MetroStation?,
    toStation: com.example.metrotransit.data.MetroStation?,
    onPaymentSuccess: () -> Unit
) {
    val scrollState = rememberScrollState()
    val extendedColors = MetroTransitTheme.extendedColors
    
    val diff = abs(StationData.stations.indexOf(fromStation) - StationData.stations.indexOf(toStation))
    val amount = 20 + (diff * 5)
    
    var selectedPaymentMethod by remember { mutableStateOf<String?>(null) }
    var isProcessing by remember { mutableStateOf(false) }

    LaunchedEffect(isProcessing) {
        if (isProcessing) {
            kotlinx.coroutines.delay(1500)
            onPaymentSuccess()
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(scrollState)
    ) {
        // ── Trip Summary Card ──────────────────────────────────────
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            shape = RoundedCornerShape(28.dp),
            color = MaterialTheme.colorScheme.primary,
            shadowElevation = 8.dp
        ) {
            Column(modifier = Modifier.padding(24.dp)) {
                Text(
                    "Journey Summary",
                    color = Color.White.copy(alpha = 0.8f),
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(8.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(fromStation?.name ?: "Unknown", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 18.sp)
                        Text("Departure", color = Color.White.copy(alpha = 0.7f), fontSize = 12.sp)
                    }
                    Icon(Icons.AutoMirrored.Filled.ArrowForward, contentDescription = null, tint = Color.White, modifier = Modifier.padding(horizontal = 12.dp))
                    Column(modifier = Modifier.weight(1f), horizontalAlignment = Alignment.End) {
                        Text(toStation?.name ?: "Unknown", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 18.sp, textAlign = TextAlign.End)
                        Text("Destination", color = Color.White.copy(alpha = 0.7f), fontSize = 12.sp, textAlign = TextAlign.End)
                    }
                }
                
                HorizontalDivider(modifier = Modifier.padding(vertical = 16.dp), color = Color.White.copy(alpha = 0.2f))
                
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.Bottom
                ) {
                    Column {
                        Text("Fare Amount", color = Color.White.copy(alpha = 0.7f), fontSize = 12.sp)
                        Text("৳$amount", color = Color.White, fontSize = 32.sp, fontWeight = FontWeight.Black)
                    }
                    Column(horizontalAlignment = Alignment.End) {
                        Surface(
                            color = Color.White.copy(alpha = 0.2f),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Text(
                                "Single Journey",
                                color = Color.White,
                                modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.Bold
                            )
                        }
                        Spacer(modifier = Modifier.height(8.dp))
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.AccessTime, contentDescription = null, tint = Color.White.copy(alpha = 0.8f), modifier = Modifier.size(14.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                "Next: 4 mins",
                                color = Color.White.copy(alpha = 0.8f),
                                style = MaterialTheme.typography.labelSmall
                            )
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        // ── Payment Methods ──────────────────────────────────────────
        Text(
            "Select Payment Method",
            modifier = Modifier.padding(horizontal = 24.dp, vertical = 8.dp),
            style = MaterialTheme.typography.titleSmall,
            fontWeight = FontWeight.Bold,
            color = extendedColors.textPrimary
        )

        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            PaymentMethodItem(
                name = "bKash",
                iconRes = R.drawable.bkash_logo,
                color = Color(0xFFE2136E),
                isSelected = selectedPaymentMethod == "bKash",
                onClick = { selectedPaymentMethod = "bKash" }
            )
            PaymentMethodItem(
                name = "Nagad",
                iconRes = R.drawable.nagad_logo,
                color = Color(0xFFED1C24),
                isSelected = selectedPaymentMethod == "Nagad",
                onClick = { selectedPaymentMethod = "Nagad" }
            )
            PaymentMethodItem(
                name = "Debit/Credit Card",
                icon = Icons.Default.CreditCard,
                color = Color(0xFF007AFF),
                isSelected = selectedPaymentMethod == "Card",
                onClick = { selectedPaymentMethod = "Card" }
            )
        }

        Spacer(modifier = Modifier.height(24.dp))

        Button(
            onClick = { isProcessing = true },
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp)
                .height(56.dp),
            shape = RoundedCornerShape(16.dp),
            enabled = selectedPaymentMethod != null && !isProcessing,
            colors = ButtonDefaults.buttonColors(
                containerColor = MaterialTheme.colorScheme.primary,
                disabledContainerColor = extendedColors.textSecondary.copy(alpha = 0.2f)
            )
        ) {
            if (isProcessing) {
                CircularProgressIndicator(color = Color.White, modifier = Modifier.size(24.dp))
            } else {
                Text(
                    if (selectedPaymentMethod != null) "Pay ৳$amount" else "Select a Method",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        }
        
        Spacer(modifier = Modifier.height(32.dp))
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
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                items(viewModel.tickets) { ticket ->
                    TicketCard(ticket = ticket, onClick = { onTicketClick(ticket.id) })
                }
            }
        }
    }
}


@Composable
fun PaymentMethodItem(
    name: String,
    icon: ImageVector? = null,
    iconRes: Int? = null,
    color: Color,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    val extendedColors = MetroTransitTheme.extendedColors
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() },
        shape = RoundedCornerShape(20.dp),
        color = if (isSelected) color.copy(alpha = 0.1f) else extendedColors.glass,
        border = androidx.compose.foundation.BorderStroke(
            width = if (isSelected) 2.dp else 1.dp,
            color = if (isSelected) color else extendedColors.glassBorder
        )
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Surface(
                shape = RoundedCornerShape(12.dp),
                color = if (iconRes != null) Color.Transparent else color.copy(alpha = 0.1f),
                modifier = Modifier.size(48.dp)
            ) {
                if (iconRes != null) {
                    Image(
                        painter = painterResource(id = iconRes),
                        contentDescription = name,
                        modifier = Modifier.padding(8.dp)
                    )
                } else if (icon != null) {
                    Icon(
                        icon,
                        contentDescription = name,
                        tint = color,
                        modifier = Modifier.padding(12.dp)
                    )
                }
            }
            Spacer(modifier = Modifier.width(16.dp))
            Text(
                name,
                style = MaterialTheme.typography.bodyLarge,
                fontWeight = FontWeight.Bold,
                color = extendedColors.textPrimary,
                modifier = Modifier.weight(1f)
            )
            RadioButton(
                selected = isSelected,
                onClick = onClick,
                colors = RadioButtonDefaults.colors(selectedColor = color)
            )
        }
    }
}
