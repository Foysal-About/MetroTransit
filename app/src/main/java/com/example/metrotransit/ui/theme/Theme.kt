package com.example.metrotransit.ui.theme

import android.app.Activity
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ProvideTextStyle
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.remember
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.text.TextStyle
import androidx.core.view.WindowCompat

@Immutable
data class ExtendedColors(
    val glass: Color,
    val glassBorder: Color,
    val backgroundGradient: Brush,
    val textPrimary: Color,
    val textSecondary: Color,
    val surface: Color
)

val LocalExtendedColors = staticCompositionLocalOf {
    ExtendedColors(
        glass = Color.Unspecified,
        glassBorder = Color.Unspecified,
        backgroundGradient = Brush.verticalGradient(listOf(Color.Transparent, Color.Transparent)),
        textPrimary = Color.Unspecified,
        textSecondary = Color.Unspecified,
        surface = Color.Unspecified
    )
}

private val DarkColorScheme = darkColorScheme(
    primary = PrimaryBlue,
    onPrimary = Color.White,
    secondary = SecondaryBlue,
    tertiary = MetroRed,
    background = DarkBackground,
    surface = DarkSurface,
    onBackground = DarkTextPrimary,
    onSurface = DarkTextPrimary
)

private val LightColorScheme = lightColorScheme(
    primary = PrimaryBlue,
    onPrimary = Color.White,
    secondary = SecondaryBlue,
    tertiary = MetroRed,
    background = LightBackground,
    surface = LightSurface,
    onBackground = LightTextPrimary,
    onSurface = LightTextPrimary
)

@Composable
fun MetroTransitTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    val colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme
    
    val extendedColors = if (darkTheme) {
        ExtendedColors(
            glass = DarkGlass,
            glassBorder = DarkGlassBorder,
            backgroundGradient = Brush.verticalGradient(
                colors = listOf(
                    Color(0xFF0F172A),
                    Color(0xFF1E293B),
                    Color(0xFF0F172A)
                )
            ),
            textPrimary = DarkTextPrimary,
            textSecondary = DarkTextSecondary,
            surface = DarkSurface
        )
    } else {
        ExtendedColors(
            glass = LightGlass,
            glassBorder = LightGlassBorder,
            backgroundGradient = Brush.verticalGradient(
                colors = listOf(
                    Color(0xFFF1F5F9),
                    Color(0xFFE2E8F0),
                    Color(0xFFCBD5E1)
                )
            ),
            textPrimary = LightTextPrimary,
            textSecondary = LightTextSecondary,
            surface = LightSurface
        )
    }

    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as Activity).window
            val insetsController = WindowCompat.getInsetsController(window, view)
            insetsController.isAppearanceLightStatusBars = !darkTheme
            insetsController.isAppearanceLightNavigationBars = !darkTheme
        }
    }

    val context = LocalContext.current
    val fonts = remember(context) { appFontFamilies(context) }
    val typography = remember(fonts) { appTypography(fonts.display, fonts.text) }

    CompositionLocalProvider(
        LocalExtendedColors provides extendedColors,
        LocalAppFonts provides fonts
    ) {
        MaterialTheme(
            colorScheme = colorScheme,
            typography = typography
        ) {
            // Carries the family — and nothing else — into `Text` calls that pass a raw
            // fontSize instead of a typography style, so the switch reaches every screen
            // without altering a single size, weight or spacing value.
            ProvideTextStyle(TextStyle(fontFamily = fonts.text), content)
        }
    }
}

object MetroTransitTheme {
    val extendedColors: ExtendedColors
        @Composable
        get() = LocalExtendedColors.current

    val fonts: AppFontFamilies
        @Composable
        get() = LocalAppFonts.current
}
