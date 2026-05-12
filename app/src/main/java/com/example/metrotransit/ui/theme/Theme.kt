package com.example.metrotransit.ui.theme

import android.app.Activity
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalView
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
            WindowCompat.getInsetsController(window, view).isAppearanceLightStatusBars = !darkTheme
        }
    }

    CompositionLocalProvider(LocalExtendedColors provides extendedColors) {
        MaterialTheme(
            colorScheme = colorScheme,
            typography = Typography,
            content = content
        )
    }
}

object MetroTransitTheme {
    val extendedColors: ExtendedColors
        @Composable
        get() = LocalExtendedColors.current
}
