package com.example.metrotransit.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.ConfirmationNumber
import androidx.compose.material.icons.filled.QrCode
import androidx.compose.material.icons.filled.Train
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
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
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    items(viewModel.tickets) { ticket ->
                        TicketCard(ticket = ticket, onClick = { onTicketClick(ticket) })
                    }
                }
            }
        }
    }
}

@Composable
fun TicketCard(ticket: QRTicket, onClick: () -> Unit) {
    val extendedColors = MetroTransitTheme.extendedColors
    val isActive = ticket.status == "Active"
    
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() },
        shape = RoundedCornerShape(24.dp),
        color = extendedColors.glass,
        border = androidx.compose.foundation.BorderStroke(1.dp, extendedColors.glassBorder)
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Surface(
                shape = RoundedCornerShape(16.dp),
                color = if (isActive) Color(0xFF10B981).copy(alpha = 0.1f) else extendedColors.textSecondary.copy(alpha = 0.1f),
                modifier = Modifier.size(56.dp)
            ) {
                Icon(
                    Icons.Default.QrCode,
                    contentDescription = null,
                    tint = if (isActive) Color(0xFF10B981) else extendedColors.textSecondary,
                    modifier = Modifier.padding(14.dp)
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
                        modifier = Modifier.padding(horizontal = 4.dp).size(14.dp),
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
                
                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = if (isActive) Color(0xFF10B981).copy(alpha = 0.1f) else Color.Gray.copy(alpha = 0.1f),
                ) {
                    Text(
                        ticket.status,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold,
                        color = if (isActive) Color(0xFF10B981) else Color.Gray
                    )
                }
            }
        }
    }
}
