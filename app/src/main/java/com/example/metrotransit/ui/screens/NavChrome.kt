package com.example.metrotransit.ui.screens

import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.ArrowBackIosNew
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.metrotransit.ui.theme.MetroTransitTheme

/**
 * The chrome every navigation bar in the app shares.
 *
 * Both pieces follow iOS's own metrics, which is what the app's SF Pro type is cut for: a
 * bare chevron for back — no arrowhead, no container — and a 17pt semibold title carrying
 * the slight negative tracking SF Pro takes at that size. Screens differ in what they put
 * beside these; they no longer differ in the two things every screen has.
 */

/** The navigation-bar title size. */
val NavTitleSize = 17.sp

/** …and its weight. Semibold, not bold: bold at 17pt reads heavy next to a hairline chevron. */
val NavTitleWeight = FontWeight.SemiBold

/**
 * The back affordance: a chevron, sized to sit optically level with a 17pt title.
 * Wrap it in the screen's own `IconButton` so the touch target and any container stay local.
 */
@Composable
fun BackIcon(
    tint: Color = MetroTransitTheme.extendedColors.textPrimary,
    modifier: Modifier = Modifier
) {
    Icon(
        Icons.Rounded.ArrowBackIosNew,
        contentDescription = "Back",
        tint = tint,
        modifier = modifier.size(18.dp)
    )
}

/** A navigation-bar title. */
@Composable
fun NavTitle(
    text: String,
    modifier: Modifier = Modifier,
    color: Color = MetroTransitTheme.extendedColors.textPrimary
) {
    Text(
        text,
        modifier = modifier,
        color = color,
        fontSize = NavTitleSize,
        fontWeight = NavTitleWeight,
        letterSpacing = (-0.2).sp,
        maxLines = 1,
        overflow = TextOverflow.Ellipsis
    )
}
