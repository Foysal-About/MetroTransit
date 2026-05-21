package com.example.metrotransit.ui.screens

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.metrotransit.data.QRTicket
import com.example.metrotransit.ui.theme.MetroTransitTheme
import com.example.metrotransit.utils.QRCodeGenerator
import kotlinx.coroutines.delay
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TicketDetailsScreen(
    ticket: QRTicket,
    onBack: () -> Unit,
    onClose: () -> Unit
) {
    val extendedColors = MetroTransitTheme.extendedColors
    val scrollState = rememberScrollState()
    
    val isExpired = ticket.status == "Expired"
    var timeLeft by remember { mutableIntStateOf(if (isExpired) 0 else ticket.validityMinutes * 60) }
    
    LaunchedEffect(isExpired) {
        if (!isExpired) {
            while (timeLeft > 0) {
                delay(1000)
                timeLeft--
            }
        }
    }

    val minutes = timeLeft / 60
    val seconds = timeLeft % 60
    val timeString = String.format(java.util.Locale.getDefault(), "%02d:%02d", minutes, seconds)

    Scaffold(
        containerColor = Color.Transparent,
        topBar = {
            TopAppBar(
                title = { Text("Metro Ticket", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
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
                    .padding(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // ── Ticket Card ──────────────────────────────────────────
                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(32.dp),
                    color = MaterialTheme.colorScheme.surface,
                    shadowElevation = 8.dp
                ) {
                    Column(
                        modifier = Modifier.padding(24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            "Entry Ticket",
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = FontWeight.Bold,
                            color = extendedColors.textSecondary
                        )
                        
                        Spacer(modifier = Modifier.height(24.dp))
                        
                        // QR Code
                        Surface(
                            modifier = Modifier
                                .size(240.dp)
                                .border(1.dp, extendedColors.textSecondary.copy(alpha = 0.2f), RoundedCornerShape(24.dp)),
                            color = MaterialTheme.colorScheme.surfaceVariant,
                            shape = RoundedCornerShape(24.dp)
                        ) {
                            Box(contentAlignment = Alignment.Center, modifier = Modifier.padding(20.dp)) {
                                val qrData = "MT_TICKET_${ticket.id}"
                                var qrBitmap by remember { mutableStateOf<android.graphics.Bitmap?>(null) }
                                
                                LaunchedEffect(qrData) {
                                    withContext(Dispatchers.Default) {
                                        qrBitmap = QRCodeGenerator.generateQRCode(qrData, 512)
                                    }
                                }
                                
                                if (qrBitmap != null) {
                                    Image(
                                        bitmap = qrBitmap!!.asImageBitmap(),
                                        contentDescription = "QR Code",
                                        modifier = Modifier.fillMaxSize(),
                                        alpha = if (isExpired) 0.1f else 1f,
                                        colorFilter = ColorFilter.tint(MaterialTheme.colorScheme.onSurface)
                                    )
                                } else {
                                    CircularProgressIndicator(
                                        modifier = Modifier.size(40.dp),
                                        strokeWidth = 3.dp,
                                        color = MaterialTheme.colorScheme.primary.copy(alpha = 0.5f)
                                    )
                                }

                                if (isExpired) {
                                    Surface(
                                        color = MaterialTheme.colorScheme.error.copy(alpha = 0.1f),
                                        shape = RoundedCornerShape(8.dp),
                                        modifier = Modifier.rotate(-15f)
                                    ) {
                                        Text(
                                            "EXPIRED",
                                            style = MaterialTheme.typography.headlineSmall,
                                            fontWeight = FontWeight.Black,
                                            color = MaterialTheme.colorScheme.error.copy(alpha = 0.6f),
                                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                                        )
                                    }
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
                        
                        val statusActive = ticket.status == "Active"
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = if (statusActive) Color(0xFFD1FAE5) else MaterialTheme.colorScheme.surfaceVariant
                        ) {
                            Text(
                                ticket.status,
                                modifier = Modifier.padding(horizontal = 12.dp, vertical = 4.dp),
                                color = if (statusActive) Color(0xFF10B981) else extendedColors.textSecondary,
                                fontWeight = FontWeight.Bold,
                                style = MaterialTheme.typography.labelSmall
                            )
                        }

                        Spacer(modifier = Modifier.height(24.dp))
                        
                        // Divider with circles on sides
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(40.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            val cutoutColor = MaterialTheme.colorScheme.background
                            Canvas(modifier = Modifier.fillMaxSize()) {
                                val pathEffect = PathEffect.dashPathEffect(floatArrayOf(15f, 15f), 0f)
                                
                                // Left Cutout
                                drawCircle(
                                    color = cutoutColor,
                                    radius = 12.dp.toPx(),
                                    center = Offset(-24.dp.toPx(), size.height / 2)
                                )
                                
                                // Right Cutout
                                drawCircle(
                                    color = cutoutColor,
                                    radius = 12.dp.toPx(),
                                    center = Offset(size.width + 24.dp.toPx(), size.height / 2)
                                )
                                
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
                        
                        Spacer(modifier = Modifier.height(32.dp))
                        
                        // Validity Timer
                        val timerBgColor = if (isExpired) MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.5f) else MaterialTheme.colorScheme.surfaceVariant
                        val timerTextColor = if (isExpired || timeLeft < 300) MaterialTheme.colorScheme.error else extendedColors.textPrimary
                        
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(timerBgColor, RoundedCornerShape(16.dp))
                                .padding(16.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text(
                                if (isExpired) "Ticket Status" else "Valid For",
                                style = MaterialTheme.typography.labelSmall,
                                color = extendedColors.textSecondary
                            )
                            Text(
                                if (isExpired) "EXPIRED" else timeString,
                                style = MaterialTheme.typography.headlineMedium,
                                fontWeight = FontWeight.Black,
                                color = timerTextColor
                            )
                        }
                    }
                }
                
                Spacer(modifier = Modifier.height(24.dp))
                
                Button(
                    onClick = { /* Share ticket */ },
                    modifier = Modifier.fillMaxWidth().height(56.dp),
                    shape = RoundedCornerShape(16.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Color.White.copy(alpha = 0.2f))
                ) {
                    Icon(Icons.Default.Share, contentDescription = null, tint = extendedColors.textPrimary)
                    Spacer(modifier = Modifier.width(12.dp))
                    Text("Share Ticket", color = extendedColors.textPrimary, fontWeight = FontWeight.Bold)
                }
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
