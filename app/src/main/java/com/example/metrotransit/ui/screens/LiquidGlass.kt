package com.example.metrotransit.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.BlendMode
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.CompositingStrategy
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.luminance
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.example.metrotransit.ui.theme.MetroTransitTheme

/**
 * Where the light sits inside a glass panel. The three flows place the pooled highlights and
 * [sheen] slides the specular band; hand them constants for a still panel, or animated values
 * for glass that moves (see ActiveJourneyBar).
 */
@Immutable
data class LiquidGlassLight(
    val flowA: Float,
    val flowB: Float,
    val flowC: Float,
    val sheen: Float,
    val strengthScale: Float = 1f,
    /**
     * How much of the warm amber pool to keep. The journey bar wants all of it — amber is
     * its status colour — while a plain ticket panel only wants a trace, or the glass starts
     * to look stained rather than lit.
     */
    val warmth: Float = 1f
) {
    companion object {
        /**
         * The still frame every page uses: white light gathered top-left, the accent pooling
         * to the right, warm light low and centre. Picked to look like a caught reflection
         * rather than a paused animation.
         */
        val Panel = LiquidGlassLight(
            flowA = 0.22f,
            flowB = 0.15f,
            flowC = 0.6f,
            sheen = 0.3f,
            warmth = 0.45f
        )

        /**
         * The same glass with the light mirrored and dialled back, for panels sitting inside
         * another panel — stacked glass reads as depth only if the layers differ.
         */
        val Inset = LiquidGlassLight(
            flowA = 0.75f,
            flowB = 0.85f,
            flowC = 0.2f,
            sheen = 0.7f,
            strengthScale = 0.65f,
            warmth = 0.45f
        )
    }
}

/** Default corner for glass panels; cards override it where the old surface was rounder. */
val GlassPanelCorner = 24.dp

/**
 * Still frames for a run of panels. The same glass repeated down a list reads as a printed
 * pattern, so the light walks along a short cycle instead — each pane looks like its own
 * piece of glass catching the room from a slightly different angle.
 */
private val PanelCycle = listOf(
    LiquidGlassLight.Panel,
    LiquidGlassLight.Panel.copy(flowA = 0.62f, flowB = 0.6f, flowC = 0.25f, sheen = 0.68f),
    LiquidGlassLight.Panel.copy(flowA = 0.9f, flowB = 0.28f, flowC = 0.85f, sheen = 0.44f)
)

/** The frame for the panel at [index] in a list. See [PanelCycle]. */
fun liquidGlassLightAt(index: Int): LiquidGlassLight = PanelCycle[index.mod(PanelCycle.size)]

/**
 * A liquid glass panel: a translucent tinted fill, soft pooled light inside it, a specular
 * sweep across the face and a rim that is lit at the top and shadowed at the bottom. With a
 * constant [light] nothing about it animates, so it is safe behind text and in long lists.
 *
 * [tint] washes a colour over the glass for panels that carry a state (an expired timer, an
 * accented note). [border] replaces the rim stroke where a panel has to show selection.
 * [punchable] lets the content cut real holes through the panel with [drawGlassNotches] —
 * used for the ticket perforation.
 */
@Composable
fun LiquidGlassSurface(
    modifier: Modifier = Modifier,
    cornerRadius: Dp = GlassPanelCorner,
    light: LiquidGlassLight = LiquidGlassLight.Panel,
    tint: Color = Color.Unspecified,
    border: BorderStroke? = null,
    punchable: Boolean = false,
    onClick: (() -> Unit)? = null,
    content: @Composable () -> Unit
) {
    val extendedColors = MetroTransitTheme.extendedColors

    // Read off the theme rather than the system setting, so a forced theme still matches.
    val darkTheme = MaterialTheme.colorScheme.background.luminance() < 0.5f

    // Light glass sits on a bright backdrop, so its highlights have to be much stronger
    // than the dark theme's to register at all.
    val strength = (if (darkTheme) 1.8f else 2.6f) * light.strengthScale
    val accent = MaterialTheme.colorScheme.primary

    Surface(
        modifier = modifier
            // Content-drawn holes can only erase what is inside this panel's own layer.
            .then(
                if (punchable) {
                    Modifier.graphicsLayer(compositingStrategy = CompositingStrategy.Offscreen)
                } else {
                    Modifier
                }
            )
            .then(
                if (onClick != null) {
                    Modifier.clickable { onClick() }
                } else {
                    Modifier
                }
            ),

        shape = RoundedCornerShape(cornerRadius),

        // Thinner than a solid panel so the layers below read as depth in the glass, but
        // never fully transparent — M3 draws its shadow under the fill.
        color = extendedColors.glass.copy(alpha = if (darkTheme) 0.56f else 0.48f),

        // Lit along the top edge and fading down, the way a glass rim catches light.
        border = border ?: BorderStroke(
            width = 1.dp,
            brush = Brush.verticalGradient(
                colors = listOf(
                    extendedColors.glassBorder.copy(alpha = 0.7f),
                    extendedColors.glassBorder.copy(alpha = 0.18f),
                    extendedColors.glassBorder.copy(alpha = 0.45f)
                )
            )
        )
    ) {
        Box {
            // Blurred on API 31+, where the blobs melt into each other properly. Below that
            // the radial gradients are soft enough on their own, just a little more defined.
            Box(
                modifier = Modifier
                    .matchParentSize()
                    .blur(GlassBlobBlur)
                    .drawBehind {
                        drawGlassBlobs(
                            light = light,
                            accent = accent,
                            strength = strength
                        )
                    }
            )

            // Sheen, sweep and rim go over the blobs but still behind the content, so text
            // keeps its full contrast. Surface clips to its shape, so nothing spills out.
            Box(
                modifier = Modifier
                    .matchParentSize()
                    .drawBehind {
                        drawGlassSheen(sheen = light.sheen, strength = strength)

                        if (tint != Color.Unspecified) {
                            drawRect(color = tint)
                        }

                        drawGlassRim(strength = strength, cornerRadius = cornerRadius)
                    }
            )

            content()
        }
    }
}

/** Wide enough that the blobs bleed into each other rather than reading as three circles. */
private val GlassBlobBlur = 26.dp

/**
 * Pooled light inside the glass. Three oversized radial gradients — a cool white one, the
 * primary blue, and the journey's amber — placed by the light's flows. Radii follow the
 * panel's short side so a tall card and a slim bar get the same quality of light.
 */
internal fun DrawScope.drawGlassBlobs(
    light: LiquidGlassLight,
    accent: Color,
    strength: Float
) {
    fun blob(color: Color, centerX: Float, centerY: Float, radius: Float, alpha: Float) {
        val center = Offset(centerX, centerY)

        drawCircle(
            brush = Brush.radialGradient(
                colors = listOf(
                    color.copy(alpha = (alpha * strength).coerceAtMost(1f)),
                    Color.Transparent
                ),
                center = center,
                radius = radius
            ),
            radius = radius,
            center = center
        )
    }

    val span = size.minDimension

    blob(
        color = Color.White,
        centerX = size.width * (0.08f + 0.55f * light.flowA),
        centerY = size.height * 0.05f,
        radius = span * 1.25f,
        alpha = 0.16f
    )

    blob(
        color = accent,
        centerX = size.width * (0.9f - 0.5f * light.flowB),
        centerY = size.height * 1.0f,
        radius = span * 1.45f,
        alpha = 0.14f
    )

    blob(
        color = TicketAmber,
        centerX = size.width * (0.3f + 0.35f * light.flowC),
        centerY = size.height * 0.95f,
        radius = span * 0.95f,
        alpha = 0.10f * light.warmth
    )
}

/**
 * The top-lit face of the glass plus the specular band across it. The band is held at a
 * diagonal so it crosses the whole face rather than sliding along one edge.
 */
internal fun DrawScope.drawGlassSheen(sheen: Float, strength: Float) {
    drawRect(
        brush = Brush.verticalGradient(
            colors = listOf(
                Color.White.copy(alpha = (0.13f * strength).coerceAtMost(1f)),
                Color.Transparent,
                Color.White.copy(alpha = (0.05f * strength).coerceAtMost(1f))
            )
        )
    )

    val bandWidth = size.width * 0.4f
    val bandStart = -bandWidth + (size.width + bandWidth) * sheen

    drawRect(
        brush = Brush.linearGradient(
            colors = listOf(
                Color.Transparent,
                Color.White.copy(alpha = (0.1f * strength).coerceAtMost(1f)),
                Color.Transparent
            ),
            start = Offset(bandStart, 0f),
            end = Offset(bandStart + bandWidth, size.height)
        )
    )
}

/**
 * The thick refractive edge. A real glass rim gathers light at the top-left and drops it
 * into shadow at the bottom-right, and that pair of strokes is what stops a panel from
 * looking like a flat tinted rectangle.
 */
internal fun DrawScope.drawGlassRim(strength: Float, cornerRadius: Dp) {
    val inset = 2.dp.toPx()
    val stroke = Stroke(width = 1.dp.toPx())
    val corner = CornerRadius(cornerRadius.toPx() - inset)
    val innerSize = Size(size.width - inset * 2, size.height - inset * 2)

    drawRoundRect(
        brush = Brush.linearGradient(
            colors = listOf(
                Color.White.copy(alpha = (0.3f * strength).coerceAtMost(1f)),
                Color.Transparent,
                Color.White.copy(alpha = (0.12f * strength).coerceAtMost(1f))
            ),
            start = Offset(0f, 0f),
            end = Offset(size.width * 0.4f, size.height)
        ),
        topLeft = Offset(inset, inset),
        size = innerSize,
        cornerRadius = corner,
        style = stroke
    )

    drawRoundRect(
        brush = Brush.verticalGradient(
            colors = listOf(
                Color.Transparent,
                Color.Black.copy(alpha = 0.12f)
            )
        ),
        topLeft = Offset(inset, inset),
        size = innerSize,
        cornerRadius = corner,
        style = stroke
    )
}

/**
 * The ticket perforation: two holes bitten out of the panel's left and right edges, level
 * with wherever this is drawn. Real holes, not background-coloured circles — whatever is
 * behind the panel shows through, so they survive any backdrop.
 *
 * Only works inside a [LiquidGlassSurface] marked `punchable`; without that layer the clear
 * has nothing to erase.
 *
 * [edgeInset] is how far the panel's edges are outside this drawing area — the content
 * padding the notches have to reach back across.
 */
internal fun DrawScope.drawGlassNotches(radius: Dp, edgeInset: Dp = 0.dp) {
    val centerY = size.height / 2
    val overshoot = edgeInset.toPx()

    listOf(-overshoot, size.width + overshoot).forEach { x ->
        drawCircle(
            color = Color.Black,
            radius = radius.toPx(),
            center = Offset(x, centerY),
            blendMode = BlendMode.Clear
        )
    }
}
