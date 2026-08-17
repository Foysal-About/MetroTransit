package com.example.metrotransit.ui.screens

import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DirectionsSubway
import androidx.compose.material.icons.filled.QrCodeScanner
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.metrotransit.data.QRTicket
import com.example.metrotransit.ui.theme.MetroTransitTheme
import kotlinx.coroutines.delay

/** Height the bar occupies, excluding system insets — screens reserve this much space. */
val ActiveJourneyBarHeight = 76.dp

/**
 * Persistent reminder that a journey is running, shown over every screen until the rider
 * taps out. Validating a QR means they are inside the paid area and must tap out to leave.
 */
@Composable
fun ActiveJourneyBar(
    ticket: QRTicket,
    onTapOut: () -> Unit,
    modifier: Modifier = Modifier,
    onOpenJourney: (() -> Unit)? = null
) {
    val extendedColors = MetroTransitTheme.extendedColors

    var nowMillis by remember { mutableLongStateOf(System.currentTimeMillis()) }

    LaunchedEffect(ticket.id) {
        while (true) {
            nowMillis = System.currentTimeMillis()
            delay(1000)
        }
    }

    val secondsLeft = ticket.remainingSeconds(nowMillis)
    val overdue = secondsLeft <= 0

    val pulse by rememberInfiniteTransition(
        label = "journeyPulse"
    ).animateFloat(
        initialValue = 0.3f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(1100),
            repeatMode = RepeatMode.Reverse
        ),
        label = "journeyPulseAlpha"
    )

    Surface(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 12.dp)
            .height(ActiveJourneyBarHeight)
            .then(
                if (onOpenJourney != null) {
                    Modifier.clickable {
                        onOpenJourney()
                    }
                } else {
                    Modifier
                }
            ),

        shape = RoundedCornerShape(22.dp),

        // Glass effect instead of pure transparent to avoid M3 shadow issues
        color = extendedColors.glass,

        border = BorderStroke(
            width = 1.dp,
            color = extendedColors.glassBorder.copy(alpha = 0.35f)
        ),

        // Keep a subtle shadow
        shadowElevation = 6.dp
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(ActiveJourneyBarHeight)
                .padding(horizontal = 14.dp),

            verticalAlignment = Alignment.CenterVertically
        ) {

            // Subway icon
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier.size(40.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .background(
                            color = TicketAmber.copy(alpha = pulse * 0.25f),
                            shape = CircleShape
                        )
                )

                Icon(
                    imageVector = Icons.Default.DirectionsSubway,
                    contentDescription = null,
                    tint = TicketAmber,
                    modifier = Modifier.size(20.dp)
                )
            }

            Spacer(modifier = Modifier.width(12.dp))

            // Journey information
            Column(
                modifier = Modifier.weight(1f)
            ) {

                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {

                    Text(
                        text = "IN TRANSIT",
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Black,
                        color = TicketAmber,
                        fontSize = 9.sp
                    )

                    Text(
                        text = " · ",
                        style = MaterialTheme.typography.labelSmall,
                        color = extendedColors.textSecondary,
                        fontSize = 9.sp
                    )

                    Text(
                        text = if (overdue) {
                            "exit window over"
                        } else {
                            "${formatCountdown(secondsLeft)} to exit"
                        },
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold,
                        color = if (overdue) {
                            MaterialTheme.colorScheme.error
                        } else {
                            extendedColors.textSecondary
                        },
                        fontSize = 9.sp
                    )
                }

                Spacer(modifier = Modifier.height(3.dp))

                Text(
                    text = "${ticket.fromStation} → ${ticket.toStation}",
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Bold,
                    color = extendedColors.textPrimary,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }

            Spacer(modifier = Modifier.width(10.dp))

            // Tap Out button
            Button(
                onClick = onTapOut,

                modifier = Modifier.height(44.dp),

                shape = RoundedCornerShape(14.dp),

                contentPadding = PaddingValuesCompact,

                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.primary
                )
            ) {
                Icon(
                    imageVector = Icons.Default.QrCodeScanner,
                    contentDescription = null,
                    tint = Color.White,
                    modifier = Modifier.size(16.dp)
                )

                Spacer(modifier = Modifier.width(6.dp))

                Text(
                    text = "Tap Out",
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
            }
        }
    }
}

private val PaddingValuesCompact =
    PaddingValues(
        horizontal = 16.dp,
        vertical = 8.dp
    )