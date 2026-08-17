package com.example.metrotransit.ui.theme

import androidx.compose.material3.Typography
import androidx.compose.ui.text.font.FontFamily

/**
 * The app's type scale in San Francisco.
 *
 * Every style is the Material 3 default with only the family swapped, so sizes, line
 * heights and letter spacing — and therefore every existing layout — stay exactly as they
 * were. The split follows Apple's guidance: SF Pro Display from 20sp up (display,
 * headline, titleLarge), SF Pro Text below it (titles, body, labels, buttons, navigation).
 */
fun appTypography(display: FontFamily, text: FontFamily): Typography {
    val base = Typography()
    return Typography(
        // ── SF Pro Display: large headings and numbers ───────────────────
        displayLarge = base.displayLarge.copy(fontFamily = display),
        displayMedium = base.displayMedium.copy(fontFamily = display),
        displaySmall = base.displaySmall.copy(fontFamily = display),
        headlineLarge = base.headlineLarge.copy(fontFamily = display),
        headlineMedium = base.headlineMedium.copy(fontFamily = display),
        headlineSmall = base.headlineSmall.copy(fontFamily = display),
        titleLarge = base.titleLarge.copy(fontFamily = display),

        // ── SF Pro Text: body, labels, buttons, navigation ───────────────
        titleMedium = base.titleMedium.copy(fontFamily = text),
        titleSmall = base.titleSmall.copy(fontFamily = text),
        bodyLarge = base.bodyLarge.copy(fontFamily = text),
        bodyMedium = base.bodyMedium.copy(fontFamily = text),
        bodySmall = base.bodySmall.copy(fontFamily = text),
        labelLarge = base.labelLarge.copy(fontFamily = text),
        labelMedium = base.labelMedium.copy(fontFamily = text),
        labelSmall = base.labelSmall.copy(fontFamily = text)
    )
}
