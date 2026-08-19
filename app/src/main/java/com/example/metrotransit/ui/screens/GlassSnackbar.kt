package com.example.metrotransit.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.CheckCircle
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SnackbarData
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.lerp
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.example.metrotransit.ui.theme.MetroSuccess
import com.example.metrotransit.ui.theme.MetroTransitTheme

/**
 * The app's own snackbar, in glass rather than Material's opaque slab.
 *
 * The default snackbar is a solid light panel with inverted text, which reads as a piece of
 * another app on these dark glass pages. This one is built from the same [LiquidGlassSurface]
 * as every panel, with the journey bar's own width and corner, so a confirmation looks like
 * it belongs to the surface it slides over instead of covering it.
 *
 * [icon] and [iconTint] carry the tone — a green tick for something that worked, and a
 * caller can pass a warning mark and the error colour for something that did not.
 */
@Composable
fun GlassSnackbar(
    data: SnackbarData,
    modifier: Modifier = Modifier,
    icon: ImageVector = Icons.Rounded.CheckCircle,
    iconTint: Color = MetroSuccess
) {
    val extendedColors = MetroTransitTheme.extendedColors

    LiquidGlassSurface(
        modifier = modifier
            .fillMaxWidth()
            // Matches the floating journey bar's inset, so the two stack as one column.
            .padding(horizontal = 12.dp),
        cornerRadius = SnackbarCorner,
        // Carried higher than a page panel on purpose. Panel glass lands near 40% opacity,
        // which is right over a background gradient and wrong over a dense list — the row
        // underneath read straight through the message. Half a coat of the theme's surface
        // takes it to roughly two thirds: still glass, but the text has a ground to sit on.
        // The whisper of accent keeps it from looking like a plain grey card.
        tint = lerp(extendedColors.surface, MaterialTheme.colorScheme.primary, 0.07f)
            .copy(alpha = 0.5f)
    ) {
        Row(
            modifier = Modifier
                .heightIn(min = 56.dp)
                .padding(start = 16.dp, end = 8.dp, top = 12.dp, bottom = 12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Icon(
                icon,
                contentDescription = null,
                tint = iconTint,
                modifier = Modifier.size(20.dp)
            )

            Text(
                data.visuals.message,
                modifier = Modifier.weight(1f),
                color = extendedColors.textPrimary,
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.Medium,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis
            )

            data.visuals.actionLabel?.let { label ->
                TextButton(onClick = { data.performAction() }) {
                    Text(
                        label,
                        color = MaterialTheme.colorScheme.primary,
                        style = MaterialTheme.typography.labelLarge,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    }
}

/** Softer than a page panel, tighter than a pill — the same corner the journey bar carries. */
private val SnackbarCorner = 22.dp
