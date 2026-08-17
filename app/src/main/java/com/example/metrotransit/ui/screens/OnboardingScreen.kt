package com.example.metrotransit.ui.screens

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
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
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
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
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.metrotransit.ui.theme.AppFont
import com.example.metrotransit.ui.theme.MetroGreen
import com.example.metrotransit.ui.theme.MetroTransitTheme
import kotlin.math.roundToInt

/**
 * One drifting band, either of feature phrases or of station names: the text, how long a
 * full pass takes (lower is faster), where in that cycle the band starts, and how present
 * it looks — the bands higher up sit further back.
 */
private data class FeatureBand(
    val phrases: List<String>,
    val durationMillis: Int,
    val phase: Float,
    val alpha: Float,
    /** Station bands carry a route marker, so the drift reads as a line going past. */
    val isRoute: Boolean = false
)

/** MRT Line 6, north to south — the whole line the app covers. */
private val Line6Stations = listOf(
    "Uttara North", "Uttara Center", "Uttara South", "Pallabi", "Mirpur 11",
    "Mirpur 10", "Kazipara", "Shewrapara", "Agargaon", "Bijoy Sarani",
    "Farmgate", "Kawran Bazar", "Shahbagh", "Dhaka University",
    "Bangladesh Secretariat", "Motijheel", "Kamalapur"
)

/**
 * What the app can do, in the rider's words — interleaved with stretches of the line, so
 * the screen feels like the view from a train window pulling out of a platform.
 */
private val FeatureBands = listOf(
    FeatureBand(
        listOf("How much to Motijheel?", "Buy a QR ticket in seconds"),
        durationMillis = 32_000, phase = 0.00f, alpha = 0.22f
    ),
    FeatureBand(
        Line6Stations.subList(0, 5),
        durationMillis = 30_000, phase = 0.50f, alpha = 0.30f, isRoute = true
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
        Line6Stations.subList(5, 10),
        durationMillis = 22_000, phase = 0.08f, alpha = 0.58f, isRoute = true
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
        Line6Stations.subList(10, 17),
        durationMillis = 19_000, phase = 0.72f, alpha = 0.92f, isRoute = true
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
 * First-run welcome, dressed as the platform it stands on: the line runs across the top
 * with a rake working its length, stations and features drift past like passing scenery,
 * and the way in sits behind the platform edge.
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
        // Line colour bleeding down from the top, the way platform signage washes the
        // ceiling — it ties the drifting bands to the route strip above them.
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    Brush.verticalGradient(
                        0f to MetroGreen.copy(alpha = 0.20f),
                        0.45f to MetroGreen.copy(alpha = 0.05f),
                        1f to Color.Transparent
                    )
                )
        )

        // Spread over the height below the route diagram, so the bands run edge to edge and
        // the lowest ones pass behind the copy rather than stopping short and leaving a gap.
        // The top inset keeps the diagram in a lane of its own instead of behind a band.
        Column(
            modifier = Modifier
                .fillMaxSize()
                .statusBarsPadding()
                .padding(top = 104.dp, bottom = 18.dp),
            verticalArrangement = Arrangement.SpaceEvenly
        ) {
            FeatureBands.forEach { band ->
                FeatureBandRow(band = band)
            }
        }

        // The line itself, pinned to the top like the route diagram over a train door.
        RouteHeader(
            modifier = Modifier
                .align(Alignment.TopCenter)
                .statusBarsPadding()
        )

        // Lets the lower bands slide out of sight behind the copy instead of stopping dead.
        Box(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .fillMaxWidth()
                .background(
                    Brush.verticalGradient(
                        // Solid by the time the platform edge lands, so the copy below it
                        // never has a pill sliding through it.
                        0f to Color.Transparent,
                        0.14f to background.copy(alpha = 0.90f),
                        0.22f to background,
                        1f to background
                    )
                )
        ) {
            Column(
                modifier = Modifier
                    .navigationBarsPadding()
                    .padding(top = 72.dp, bottom = 32.dp)
            ) {
                // Platform edge: the tactile strip you stand behind, in the line's colour.
                PlatformEdge()

                Column(modifier = Modifier.padding(horizontal = 28.dp)) {
                    Spacer(modifier = Modifier.height(22.dp))

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

                    // Transparent, so the line's wash and the drifting bands carry straight
                    // through the way in — only the route-coloured edge holds its shape.
                    Button(
                        onClick = onContinue,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(56.dp),
                        shape = RoundedCornerShape(16.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color.Transparent,
                            contentColor = extendedColors.textPrimary
                        ),
                        elevation = ButtonDefaults.buttonElevation(defaultElevation = 0.dp),
                        border = BorderStroke(1.5.dp, MetroGreen.copy(alpha = 0.75f))
                    ) {
                        Text(
                            "Continue",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(modifier = Modifier.width(10.dp))
                        Icon(
                            Icons.AutoMirrored.Filled.ArrowForward,
                            contentDescription = null,
                            tint = MetroGreen,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }
            }
        }
    }
}

/**
 * The line's name on a solid plaque, the way it appears over a station entrance — it carries
 * its own contrast, so the bands can keep drifting behind it without muddying the text.
 */
@Composable
private fun RouteHeader(modifier: Modifier = Modifier) {
    Column(modifier = modifier.fillMaxWidth()) {
        Surface(
            modifier = Modifier.padding(horizontal = 28.dp, vertical = 16.dp),
            shape = RoundedCornerShape(12.dp),
            color = MetroGreen
        ) {
            Row(
                modifier = Modifier.padding(horizontal = 12.dp, vertical = 9.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Surface(shape = CircleShape, color = Color.White) {
                    Text(
                        "M",
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp),
                        fontFamily = AppFont.display,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Black,
                        color = MetroGreen
                    )
                }
                Spacer(modifier = Modifier.width(10.dp))
                Text(
                    "MRT LINE 6",
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 2.sp,
                    color = Color.White
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    "UTTARA NORTH → KAMALAPUR",
                    style = MaterialTheme.typography.labelSmall,
                    letterSpacing = 1.sp,
                    color = Color.White.copy(alpha = 0.75f)
                )
            }
        }

        RouteTrack(
            modifier = Modifier
                .fillMaxWidth()
                .height(24.dp)
        )
    }
}

/**
 * The whole of Line 6 as one horizontal run: a tick per station, terminals called out, and
 * a rake sweeping the length of it — down the line and back, the way the service actually
 * runs. The sweep is slow on purpose; it should register as movement, not demand attention.
 */
@Composable
private fun RouteTrack(modifier: Modifier = Modifier) {
    val progress by rememberInfiniteTransition(label = "rake").animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(22_000, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "rakePosition"
    )

    Canvas(modifier = modifier) {
        val y = size.height / 2f
        val inset = 28.dp.toPx()
        val startX = inset
        val endX = size.width - inset
        val span = endX - startX

        drawLine(
            color = MetroGreen.copy(alpha = 0.30f),
            start = Offset(startX, y),
            end = Offset(endX, y),
            strokeWidth = 3.dp.toPx(),
            cap = StrokeCap.Round
        )

        Line6Stations.indices.forEach { index ->
            val x = startX + span * index / (Line6Stations.lastIndex.toFloat())
            val isTerminal = index == 0 || index == Line6Stations.lastIndex
            drawCircle(
                color = MetroGreen.copy(alpha = if (isTerminal) 0.85f else 0.45f),
                radius = (if (isTerminal) 4f else 2.5f).dp.toPx(),
                center = Offset(x, y)
            )
        }

        // The rake: a lit head with the track behind it still glowing from the pass.
        val headX = startX + span * progress
        val trailX = (headX - 68.dp.toPx()).coerceAtLeast(startX)
        drawLine(
            brush = Brush.horizontalGradient(
                colors = listOf(Color.Transparent, MetroGreen),
                startX = trailX,
                endX = headX
            ),
            start = Offset(trailX, y),
            end = Offset(headX, y),
            strokeWidth = 3.dp.toPx(),
            cap = StrokeCap.Round
        )
        drawCircle(
            color = MetroGreen.copy(alpha = 0.22f),
            radius = 10.dp.toPx(),
            center = Offset(headX, y)
        )
        drawCircle(
            color = MetroGreen,
            radius = 4.dp.toPx(),
            center = Offset(headX, y)
        )
    }
}

/** Dashes across the full width, in the line's colour: stand behind this. */
@Composable
private fun PlatformEdge() {
    Canvas(
        modifier = Modifier
            .fillMaxWidth()
            .height(3.dp)
    ) {
        drawLine(
            color = MetroGreen.copy(alpha = 0.55f),
            start = Offset(0f, size.height / 2f),
            end = Offset(size.width, size.height / 2f),
            strokeWidth = size.height,
            pathEffect = PathEffect.dashPathEffect(floatArrayOf(14f, 10f), 0f)
        )
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
                isRoute = band.isRoute,
                modifier = Modifier.onSizeChanged { setWidth = it.width }
            )
            FeaturePillSet(phrases = band.phrases, isRoute = band.isRoute)
        }
    }
}

@Composable
private fun FeaturePillSet(
    phrases: List<String>,
    isRoute: Boolean,
    modifier: Modifier = Modifier
) {
    val extendedColors = MetroTransitTheme.extendedColors

    Row(
        modifier = modifier.wrapContentWidth(align = Alignment.Start, unbounded = true),
        verticalAlignment = Alignment.CenterVertically
    ) {
        phrases.forEachIndexed { index, phrase ->
            // Trailing gap lives inside the set, so its measured width is exactly the
            // loop distance — no seam when the second set comes round.
            Surface(
                modifier = Modifier.padding(end = 12.dp),
                shape = RoundedCornerShape(22.dp),
                color = extendedColors.glass,
                border = BorderStroke(
                    1.dp,
                    if (isRoute) MetroGreen.copy(alpha = 0.35f) else extendedColors.glassBorder
                )
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 18.dp, vertical = 12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    if (isRoute) {
                        // Station marker, as it appears on the strip map inside the train.
                        Box(
                            modifier = Modifier
                                .size(7.dp)
                                .background(MetroGreen, CircleShape)
                        )
                        Spacer(modifier = Modifier.width(9.dp))
                    }
                    Text(
                        phrase,
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = if (isRoute) FontWeight.SemiBold else FontWeight.Normal,
                        color = extendedColors.textPrimary,
                        maxLines = 1
                    )
                }
            }

            // Between two stations of the same stretch, the track carries on across the gap.
            if (isRoute && index < phrases.lastIndex) {
                Box(
                    modifier = Modifier
                        .padding(end = 12.dp)
                        .size(width = 14.dp, height = 2.dp)
                        .background(MetroGreen.copy(alpha = 0.35f))
                )
            }
        }
    }
}
