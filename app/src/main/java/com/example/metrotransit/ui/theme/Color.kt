package com.example.metrotransit.ui.theme

import androidx.compose.ui.graphics.Color

val MetroGreen = Color(0xFF006A4E)
val MetroRed = Color(0xFFF42A41)
val MetroGrey = Color(0xFF58595B)

val PrimaryBlue = Color(0xFF3269B5)
val SecondaryBlue = Color(0xFF5A67D8)

// Light Mode Colors
val LightBackground = Color(0xFFF1F5F9)
val LightSurface = Color(0xFFFFFFFF)
val LightTextPrimary = Color(0xFF1E293B)
val LightTextSecondary = Color(0xFF64748B)
val LightGlass = Color.White.copy(alpha = 0.6f)
val LightGlassBorder = Color.White.copy(alpha = 0.5f)

// Dark Mode Colors
val DarkBackground = Color(0xFF0F172A)
val DarkSurface = Color(0xFF1E293B)
val DarkTextPrimary = Color(0xFFF1F5F9)
val DarkTextSecondary = Color(0xFF94A3B8)
val DarkGlass = Color(0xFF1E293B).copy(alpha = 0.7f)
val DarkGlassBorder = Color(0xFF334155).copy(alpha = 0.5f)

// Section accents. Every home card carries one of these, and the dark variants are lifted
// off the brand values — a translucent fill in the true brand green disappears into the
// navy backdrop, so the dark theme uses a brighter sibling of the same hue.
val AccentPassLight = MetroGreen
val AccentPassDark = Color(0xFF12A87C)
val AccentNfcLight = Color(0xFF5E42F3)
val AccentNfcDark = Color(0xFF8B7BFF)

/** The "arrived / destination" green, shared by the journey dot and success states. */
val MetroSuccess = Color(0xFF10B981)

/**
 * MRT Line-6's colour on the official network map (#006747). The dark variant is lifted:
 * the map green is nearly black against the dark theme's navy.
 */
val Line6Light = Color(0xFF006747)
val Line6Dark = Color(0xFF19B37B)
