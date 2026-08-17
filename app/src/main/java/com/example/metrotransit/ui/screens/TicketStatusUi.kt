package com.example.metrotransit.ui.screens

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.DirectionsSubway
import androidx.compose.material.icons.filled.QrCode
import androidx.compose.material.icons.filled.TimerOff
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import com.example.metrotransit.data.TicketStatus
import com.example.metrotransit.ui.theme.MetroTransitTheme

val TicketGreen = Color(0xFF10B981)
val TicketAmber = Color(0xFFF59E0B)

/** mm:ss for the entry and exit countdowns. */
fun formatCountdown(totalSeconds: Int): String {
    val safe = totalSeconds.coerceAtLeast(0)
    return String.format(java.util.Locale.getDefault(), "%02d:%02d", safe / 60, safe % 60)
}

/** Shared look for a ticket status so every screen labels a ticket the same way. */
data class TicketStatusStyle(
    val content: Color,
    val container: Color,
    val icon: ImageVector
)

@Composable
fun ticketStatusStyle(status: String): TicketStatusStyle {
    val extendedColors = MetroTransitTheme.extendedColors
    return when (status) {
        TicketStatus.ACTIVE -> TicketStatusStyle(TicketGreen, TicketGreen.copy(alpha = 0.15f), Icons.Default.QrCode)
        TicketStatus.IN_TRANSIT -> TicketStatusStyle(TicketAmber, TicketAmber.copy(alpha = 0.18f), Icons.Default.DirectionsSubway)
        TicketStatus.COMPLETED -> TicketStatusStyle(
            MaterialTheme.colorScheme.primary,
            MaterialTheme.colorScheme.primary.copy(alpha = 0.14f),
            Icons.Default.CheckCircle
        )
        else -> TicketStatusStyle(
            extendedColors.textSecondary,
            extendedColors.textSecondary.copy(alpha = 0.14f),
            Icons.Default.TimerOff
        )
    }
}
