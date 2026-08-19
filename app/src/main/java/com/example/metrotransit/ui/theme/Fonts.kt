package com.example.metrotransit.ui.theme

import android.content.Context
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight

/**
 * The typefaces the app knows how to wear.
 *
 * Each one names the `res/font` prefixes its files are looked up under. Nothing here is
 * resolved through generated `R.font` constants, because both custom families are licensed
 * and cannot be committed to this repository — an absent family simply falls back. See
 * FONTS.md for the file names.
 */
enum class AppTypeface(
    /** For a debug readout or a settings row. */
    val label: String,
    /** Prefix for the large-size cut, or null to use the platform font. */
    internal val displayPrefix: String?,
    /** Prefix for the reading-size cut, or null to use the platform font. */
    internal val textPrefix: String?
) {
    /** Apple's system font, split by optical size the way Apple specifies. */
    SanFrancisco("SF Pro", "sf_pro_display", "sf_pro_text"),

    /**
     * One family at every size: Avenir Next ships no optical-size pair, so the 20sp
     * display/text split collapses onto the same files. That is correct, not a shortcut.
     */
    AvenirNext("Avenir Next", "avenir_next", "avenir_next"),

    /** No custom font at all — whatever the device ships. Useful for A/B-ing the other two. */
    System("System", null, null);

    companion object {
        /**
         * The family the app asks for. **This is the switch.** Change the value here for a
         * permanent choice, or assign to it at runtime (it is snapshot state, so the whole
         * UI restyles on the next frame) to flip families from a debug row:
         *
         * ```
         * AppTypeface.Preferred = AppTypeface.AvenirNext
         * ```
         *
         * Whatever is chosen, a family whose files are missing falls back to the other one
         * before falling back to the platform font, so dropping in either set is enough.
         */
        var Preferred by mutableStateOf(SanFrancisco)
    }
}

/**
 * The two families a screen draws with, plus which typeface actually got loaded.
 *
 * [typeface] is null when no font files were found and the platform font is standing in;
 * [isSanFrancisco] and [isAvenirNext] are there to check at a glance rather than guessing
 * from the shapes on screen.
 */
@Immutable
data class AppFontFamilies(
    val display: FontFamily,
    val text: FontFamily,
    val typeface: AppTypeface?
) {
    val isSanFrancisco: Boolean get() = typeface == AppTypeface.SanFrancisco
    val isAvenirNext: Boolean get() = typeface == AppTypeface.AvenirNext
}

/**
 * Weight → the file suffixes that stand for it, in the order they are tried.
 *
 * The list spans both naming conventions on purpose — Apple ships `heavy`, Avenir ships
 * `demibold` and `ultralight` — and lookup is by exact resource name, so a suffix from one
 * family can never match a file from the other. Only weights that are present get
 * registered; Compose synthesises the rest.
 */
private val FontWeightSuffixes = listOf(
    FontWeight.Light to listOf("light", "ultralight"),
    FontWeight.Normal to listOf("regular", "book"),
    FontWeight.Medium to listOf("medium"),
    FontWeight.SemiBold to listOf("semibold", "demibold"),
    FontWeight.Bold to listOf("bold"),
    FontWeight.ExtraBold to listOf("heavy", "extrabold"),
    FontWeight.Black to listOf("black")
)

/** Builds one family from `res/font/<prefix>_<weight>`, or null when none of it is there. */
private fun fontFamilyFor(context: Context, prefix: String): FontFamily? {
    val fonts = FontWeightSuffixes.mapNotNull { (weight, suffixes) ->
        val resId = suffixes.firstNotNullOfOrNull { suffix ->
            context.resources
                .getIdentifier("${prefix}_$suffix", "font", context.packageName)
                .takeIf { it != 0 }
        }
        resId?.let { Font(it, weight) }
    }
    return if (fonts.isEmpty()) null else FontFamily(fonts)
}

private val PlatformFamilies =
    AppFontFamilies(FontFamily.SansSerif, FontFamily.SansSerif, typeface = null)

/**
 * Resolves [typeface] against what is actually in `res/font`.
 *
 * The chosen family is tried first, then the other custom family, then the platform font.
 * [AppTypeface.System] is taken literally and skips the search — it is how you ask for the
 * device font on purpose rather than by accident.
 */
fun appFontFamilies(
    context: Context,
    typeface: AppTypeface = AppTypeface.Preferred
): AppFontFamilies {
    if (typeface == AppTypeface.System) return PlatformFamilies

    val order = listOf(typeface) +
        AppTypeface.values().filter { it != typeface && it != AppTypeface.System }

    for (candidate in order) {
        val display = candidate.displayPrefix?.let { fontFamilyFor(context, it) }
        val text = candidate.textPrefix?.let { fontFamilyFor(context, it) }

        // Either cut alone is enough to switch the app over — one stands in for the other
        // until both are supplied.
        val resolvedDisplay = display ?: text ?: continue
        val resolvedText = text ?: display ?: continue

        return AppFontFamilies(resolvedDisplay, resolvedText, candidate)
    }

    return PlatformFamilies
}

val LocalAppFonts = staticCompositionLocalOf { PlatformFamilies }

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
