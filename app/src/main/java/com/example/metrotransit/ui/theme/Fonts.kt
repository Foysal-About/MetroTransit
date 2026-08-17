package com.example.metrotransit.ui.theme

import android.content.Context
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight

/**
 * The app's two type families, following Apple's optical-size split: SF Pro Display for
 * large headings and numbers, SF Pro Text for everything set below 20sp.
 *
 * [isSanFrancisco] reports whether the licensed font files were actually found, so the
 * app can be checked at a glance instead of guessing which family is on screen.
 */
@Immutable
data class AppFontFamilies(
    val display: FontFamily,
    val text: FontFamily,
    val isSanFrancisco: Boolean
)

/**
 * File suffix → weight, matching how Apple names the SF Pro cuts. Only the weights that
 * are present get registered, so a partial drop-in still works.
 */
private val SanFranciscoWeights = listOf(
    "light" to FontWeight.Light,
    "regular" to FontWeight.Normal,
    "medium" to FontWeight.Medium,
    "semibold" to FontWeight.SemiBold,
    "bold" to FontWeight.Bold,
    "heavy" to FontWeight.ExtraBold,
    "black" to FontWeight.Black
)

/**
 * Builds one SF Pro family from `res/font/sf_pro_<variant>_<weight>`.
 *
 * The resources are resolved by name at runtime rather than through generated `R.font`
 * constants: the project must keep compiling when the font files are absent, because
 * SF Pro is licensed by Apple and cannot be committed to this repository. See FONTS.md.
 */
private fun sanFranciscoFamily(context: Context, variant: String): FontFamily? {
    val fonts = SanFranciscoWeights.mapNotNull { (suffix, weight) ->
        val resId = context.resources.getIdentifier(
            "sf_pro_${variant}_$suffix",
            "font",
            context.packageName
        )
        if (resId == 0) null else Font(resId, weight)
    }
    return if (fonts.isEmpty()) null else FontFamily(fonts)
}

fun appFontFamilies(context: Context): AppFontFamilies {
    val display = sanFranciscoFamily(context, "display")
    val text = sanFranciscoFamily(context, "text")

    // Either family alone is enough to switch the app over — one stands in for the other
    // until both are supplied. With neither, the platform font keeps the UI intact.
    val resolvedDisplay = display ?: text
    val resolvedText = text ?: display

    return AppFontFamilies(
        display = resolvedDisplay ?: FontFamily.SansSerif,
        text = resolvedText ?: FontFamily.SansSerif,
        isSanFrancisco = resolvedDisplay != null
    )
}

val LocalAppFonts = staticCompositionLocalOf {
    AppFontFamilies(
        display = FontFamily.SansSerif,
        text = FontFamily.SansSerif,
        isSanFrancisco = false
    )
}

/**
 * For the handful of call sites that set `fontSize` directly instead of using a
 * `MaterialTheme.typography` style. Use [display] at 20sp and above, [text] below it.
 */
object AppFont {
    val display: FontFamily
        @Composable get() = LocalAppFonts.current.display

    val text: FontFamily
        @Composable get() = LocalAppFonts.current.text
}
