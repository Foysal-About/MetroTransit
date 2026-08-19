package com.example.metrotransit.ui.screens

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.ConfirmationNumber
import androidx.compose.material.icons.rounded.DirectionsSubway
import androidx.compose.material.icons.rounded.EventBusy
import androidx.compose.material.icons.rounded.TaskAlt
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

/**
 * The icons say what the ticket *is*, not what a screen does with it: a ticket stub while it
 * is yours to use, a train while you are riding on it, a completed tick once you are out, and
 * a struck-through date when its day has passed. The rounded set matches the app's chevrons.
 */
@Composable
fun ticketStatusStyle(status: String): TicketStatusStyle {
    val extendedColors = MetroTransitTheme.extendedColors
    return when (status) {
        TicketStatus.ACTIVE -> TicketStatusStyle(TicketGreen, TicketGreen.copy(alpha = 0.15f), Icons.Rounded.ConfirmationNumber)
        TicketStatus.IN_TRANSIT -> TicketStatusStyle(TicketAmber, TicketAmber.copy(alpha = 0.18f), Icons.Rounded.DirectionsSubway)
        TicketStatus.COMPLETED -> TicketStatusStyle(
            MaterialTheme.colorScheme.primary,
            MaterialTheme.colorScheme.primary.copy(alpha = 0.14f),
            Icons.Rounded.TaskAlt
        )
        else -> TicketStatusStyle(
            extendedColors.textSecondary,
            extendedColors.textSecondary.copy(alpha = 0.14f),
            Icons.Rounded.EventBusy
        )
    }
}
