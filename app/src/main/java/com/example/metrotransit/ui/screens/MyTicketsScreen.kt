package com.example.metrotransit.ui.screens

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.ConfirmationNumber
import androidx.compose.material.icons.filled.Train
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.metrotransit.data.QRTicket
import com.example.metrotransit.ui.theme.MetroTransitTheme
import com.example.metrotransit.viewmodel.TicketViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MyTicketsScreen(
    viewModel: TicketViewModel,
    onBack: () -> Unit,
    onTicketClick: (QRTicket) -> Unit
) {
    val extendedColors = MetroTransitTheme.extendedColors
    
    Scaffold(
        containerColor = Color.Transparent,
        topBar = {
            TopAppBar(
                title = { Text("My Tickets", fontWeight = FontWeight.Bold, color = extendedColors.textPrimary) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back", tint = extendedColors.textPrimary)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.Transparent)
            )
        }
    ) { padding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                // The gradient the ticket details page already uses. Glass needs something
                // with depth behind it — on a flat fill the panels have nothing to refract.
                .background(extendedColors.backgroundGradient)
        ) {
            if (viewModel.tickets.isEmpty()) {
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
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(padding),
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
                            onClick = { onTicketClick(ticket) },
                            light = liquidGlassLightAt(index)
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun TicketCard(
    ticket: QRTicket,
    onClick: () -> Unit,
    light: LiquidGlassLight = LiquidGlassLight.Panel
) {
    val extendedColors = MetroTransitTheme.extendedColors
    val statusStyle = ticketStatusStyle(ticket.status)
    val statusColor = statusStyle.content
    val statusBgColor = statusStyle.container

    LiquidGlassSurface(
        modifier = Modifier.fillMaxWidth(),
        cornerRadius = 24.dp,
        light = light,
        // The perforation below bites real holes out of this panel.
        punchable = true,
        onClick = onClick
    ) {
        Column {
            Row(
                modifier = Modifier.padding(16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // QR Icon Container
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = statusBgColor,
                    modifier = Modifier.size(52.dp)
                ) {
                    Icon(
                        statusStyle.icon,
                        contentDescription = null,
                        tint = statusColor,
                        modifier = Modifier.padding(12.dp)
                    )
                }
                
                Spacer(modifier = Modifier.width(16.dp))
                
                Column(modifier = Modifier.weight(1f)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            ticket.fromStation,
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = extendedColors.textPrimary
                        )
                        Icon(
                            Icons.Default.Train,
                            contentDescription = null,
                            modifier = Modifier.padding(horizontal = 6.dp).size(16.dp),
                            tint = extendedColors.textSecondary
                        )
                        Text(
                            ticket.toStation,
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = extendedColors.textPrimary
                        )
                    }
                    
                    Spacer(modifier = Modifier.height(4.dp))
                    
                    Text(
                        ticket.dateTime,
                        style = MaterialTheme.typography.labelSmall,
                        color = extendedColors.textSecondary
                    )
                }
                
                Column(horizontalAlignment = Alignment.End) {
                    Text(
                        ticket.fare,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Black,
                        color = MaterialTheme.colorScheme.primary
                    )
                    
                    Spacer(modifier = Modifier.height(4.dp))
                    
                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = statusBgColor,
                    ) {
                        Text(
                            ticket.status,
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold,
                            color = statusColor
                        )
                    }
                }
            }
            
            // Dotted divider between the stub and the ticket body, notched at both edges.
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(20.dp),
                contentAlignment = Alignment.Center
            ) {
                val dashColor = extendedColors.textSecondary.copy(alpha = 0.35f)
                Canvas(modifier = Modifier.fillMaxSize()) {
                    val pathEffect = PathEffect.dashPathEffect(floatArrayOf(10f, 10f), 0f)
                    val notchRadius = 10.dp

                    drawGlassNotches(radius = notchRadius)

                    drawLine(
                        color = dashColor,
                        start = Offset(notchRadius.toPx() + 4.dp.toPx(), size.height / 2),
                        end = Offset(size.width - notchRadius.toPx() - 4.dp.toPx(), size.height / 2),
                        pathEffect = pathEffect,
                        strokeWidth = 1.dp.toPx()
                    )
                }
            }
            
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(start = 20.dp, end = 20.dp, bottom = 16.dp, top = 8.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    "Ticket ID: ${ticket.id}",
                    style = MaterialTheme.typography.labelSmall,
                    color = extendedColors.textSecondary
                )
                Text(
                    "Tap to View Details",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.primary,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}

