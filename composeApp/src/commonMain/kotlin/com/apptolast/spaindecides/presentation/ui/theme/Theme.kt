package com.apptolast.spaindecides.presentation.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable

/**
 * Main theme composable for the SpainDecides app.
 * Applies Material 3 design with blue color scheme.
 *
 * @param darkTheme Whether to use dark theme (defaults to system setting)
 * @param content The content to be themed
 */
@Composable
fun SpainDecidesTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    val colorScheme = if (darkTheme) {
        DarkColorScheme
    } else {
        LightColorScheme
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = AppTypography,
        content = content
    )
}
