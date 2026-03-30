package com.apptolast.spaindecides.presentation.ui.theme

import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.ui.graphics.Color

// Primary blue color (matching mockups)
private val Blue500 = Color(0xFF2196F3) // Standard Material Blue
private val Blue700 = Color(0xFF1976D2) // Darker blue
private val Blue200 = Color(0xFF90CAF9) // Lighter blue
private val Blue50 = Color(0xFFE3F2FD) // Very light blue

// Secondary colors
private val BlueGrey900 = Color(0xFF263238)
private val BlueGrey700 = Color(0xFF455A64)
private val BlueGrey100 = Color(0xFFCFD8DC)
private val BlueGrey50 = Color(0xFFECEFF1)

// Neutral colors
private val Grey50 = Color(0xFFFAFAFA)
private val Grey100 = Color(0xFFF5F5F5)
private val Grey900 = Color(0xFF212121)

/**
 * Light color scheme for the app.
 * Follows Material 3 design with blue as primary color.
 */
internal val LightColorScheme = lightColorScheme(
    primary = Blue700,
    onPrimary = Color.White,
    primaryContainer = Blue50,
    onPrimaryContainer = Blue700,

    secondary = BlueGrey700,
    onSecondary = Color.White,
    secondaryContainer = BlueGrey100,
    onSecondaryContainer = BlueGrey900,

    tertiary = Blue500,
    onTertiary = Color.White,

    background = Grey50,
    onBackground = Grey900,

    surface = Color.White,
    onSurface = Grey900,
    surfaceVariant = Grey100,
    onSurfaceVariant = BlueGrey900,

    outline = BlueGrey100,
    outlineVariant = BlueGrey50,

    error = Color(0xFFD32F2F),
    onError = Color.White
)

/**
 * Dark color scheme for the app.
 * Follows Material 3 design with blue as primary color.
 */
internal val DarkColorScheme = darkColorScheme(
    primary = Blue200,
    onPrimary = Blue700,
    primaryContainer = Blue700,
    onPrimaryContainer = Blue50,

    secondary = BlueGrey100,
    onSecondary = BlueGrey900,
    secondaryContainer = BlueGrey700,
    onSecondaryContainer = BlueGrey50,

    tertiary = Blue500,
    onTertiary = Blue700,

    background = Color(0xFF121212),
    onBackground = Color(0xFFE1E1E1),

    surface = Color(0xFF1E1E1E),
    onSurface = Color(0xFFE1E1E1),
    surfaceVariant = Color(0xFF2C2C2C),
    onSurfaceVariant = BlueGrey100,

    outline = BlueGrey700,
    outlineVariant = BlueGrey900,

    error = Color(0xFFEF5350),
    onError = Color(0xFF370B0B)
)
