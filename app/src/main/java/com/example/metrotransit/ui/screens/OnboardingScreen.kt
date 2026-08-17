package com.example.metrotransit.ui.screens

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.metrotransit.ui.theme.AppFont
import com.example.metrotransit.ui.theme.MetroTransitTheme
import kotlin.math.roundToInt

/**
 * One drifting band of feature phrases: the text, how long a full pass takes (lower is
 * faster), where in that cycle the band starts, and how present it looks — the bands
 * higher up sit further back.
 */
private data class FeatureBand(
    val phrases: List<String>,
    val durationMillis: Int,
    val phase: Float,
    val alpha: Float
)

/** What the app can do, in the rider's words. */
private val FeatureBands = listOf(
    FeatureBand(
        listOf("How much to Motijheel?", "Buy a QR ticket in seconds"),
        durationMillis = 32_000, phase = 0.00f, alpha = 0.22f
    ),
    FeatureBand(
        listOf("Tap in. Tap out.", "When does the last train leave?"),
        durationMillis = 26_000, phase = 0.35f, alpha = 0.34f
    ),
    FeatureBand(
        listOf("Add a stop mid-journey", "Pay only the difference"),
        durationMillis = 38_000, phase = 0.62f, alpha = 0.48f
    ),
    FeatureBand(
        listOf("Check your MRT Pass balance", "Scan the card with NFC"),
        durationMillis = 29_000, phase = 0.12f, alpha = 0.64f
    ),
    FeatureBand(
        listOf("Recharge with bKash", "Nagad or card — your call"),
        durationMillis = 35_000, phase = 0.78f, alpha = 0.80f
    ),
    FeatureBand(
        listOf("Track your ride live", "17 stations, one line"),
        durationMillis = 24_000, phase = 0.45f, alpha = 0.94f
    ),
    FeatureBand(
        listOf("Fare from Uttara to Agargaon?", "First train at 6:40 AM"),
        durationMillis = 31_000, phase = 0.20f, alpha = 1f
    ),
    FeatureBand(
        listOf("Find my nearest station", "Every ticket, kept in one place"),
        durationMillis = 27_000, phase = 0.68f, alpha = 1f
    ),
    FeatureBand(
        listOf("Skip the counter queue", "Timetable for Friday?"),
        durationMillis = 33_000, phase = 0.05f, alpha = 1f
    )
)

/**
 * First-run welcome. The app's features drift past as pills, right to left, then settle
 * behind the headline and the way in.
 */
@Composable
fun OnboardingScreen(onContinue: () -> Unit) {
    val extendedColors = MetroTransitTheme.extendedColors
    val background = MaterialTheme.colorScheme.background

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(background)
    ) {
        // Spread over the full height so the bands run edge to edge and the lowest ones
        // pass behind the copy, rather than stopping short and leaving a gap.
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(vertical = 18.dp),
            verticalArrangement = Arrangement.SpaceEvenly
        ) {
            FeatureBands.forEach { band ->
                FeatureBandRow(band = band)
            }
        }

        // Lets the lower bands slide out of sight behind the copy instead of stopping dead.
        Box(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .fillMaxWidth()
                .background(
                    Brush.verticalGradient(
                        listOf(
                            Color.Transparent,
                            background.copy(alpha = 0.92f),
                            background
                        )
                    )
                )
        ) {
            Column(
                modifier = Modifier
                    .navigationBarsPadding()
                    .padding(horizontal = 28.dp)
                    .padding(top = 64.dp, bottom = 32.dp)
            ) {
                Text(
                    "Dhaka Metro, end to end",
                    fontFamily = AppFont.display,
                    fontSize = 26.sp,
                    fontWeight = FontWeight.Black,
                    color = extendedColors.textPrimary
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    "Tickets, fares, passes and live journeys —\nit all starts here.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = extendedColors.textSecondary,
                    lineHeight = 20.sp
                )

                Spacer(modifier = Modifier.height(26.dp))

                Button(
                    onClick = onContinue,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(56.dp),
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Text(
                        "Continue",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    }
}

@Composable
private fun FeatureBandRow(band: FeatureBand) {
    // Width of one full set of pills — the distance to travel before the loop repeats.
    var setWidth by remember { mutableIntStateOf(0) }

    val progress by rememberInfiniteTransition(label = "band").animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(band.durationMillis, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "bandProgress"
    )

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clipToBounds()
    ) {
        Row(
            modifier = Modifier
                .wrapContentWidth(align = Alignment.Start, unbounded = true)
                .offset {
                    // Two identical sets, shifted by up to one set width: as the first
                    // set leaves, the second has taken its place, so the loop never jumps.
                    val shift = ((progress + band.phase) % 1f) * setWidth
                    IntOffset(-shift.roundToInt(), 0)
                }
                .graphicsLayer { alpha = band.alpha }
        ) {
            FeaturePillSet(
                phrases = band.phrases,
                modifier = Modifier.onSizeChanged { setWidth = it.width }
            )
            FeaturePillSet(phrases = band.phrases)
        }
    }
}

@Composable
private fun FeaturePillSet(phrases: List<String>, modifier: Modifier = Modifier) {
    val extendedColors = MetroTransitTheme.extendedColors

    Row(modifier = modifier.wrapContentWidth(align = Alignment.Start, unbounded = true)) {
        phrases.forEach { phrase ->
            // Trailing gap lives inside the set, so its measured width is exactly the
            // loop distance — no seam when the second set comes round.
            Surface(
                modifier = Modifier.padding(end = 12.dp),
                shape = RoundedCornerShape(22.dp),
                color = extendedColors.glass,
                border = BorderStroke(1.dp, extendedColors.glassBorder)
            ) {
                Text(
                    phrase,
                    modifier = Modifier.padding(horizontal = 18.dp, vertical = 12.dp),
                    style = MaterialTheme.typography.bodyMedium,
                    color = extendedColors.textPrimary,
                    maxLines = 1
                )
            }
        }
    }
}