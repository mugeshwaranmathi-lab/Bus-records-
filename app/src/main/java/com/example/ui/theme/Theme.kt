package com.example.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val IosLightColorScheme = lightColorScheme(
    primary = IosBlue,
    onPrimary = Color.White,
    primaryContainer = IosLightBlueTint,
    onPrimaryContainer = IosDarkBlue,
    secondary = IosDarkBlue,
    onSecondary = Color.White,
    secondaryContainer = IosSegmentedBg,
    onSecondaryContainer = IosTextPrimary,
    background = IosBackground,
    onBackground = IosTextPrimary,
    surface = IosCardBackground,
    onSurface = IosTextPrimary,
    surfaceVariant = IosSecondaryCard,
    onSurfaceVariant = IosTextSecondary,
    outline = IosBorder,
    outlineVariant = Color(0xFFE5E5EA),
    error = IosErrorRed,
    onError = Color.White
)

@Composable
fun BusEntryTheme(
    content: @Composable () -> Unit
) {
    MaterialTheme(
        colorScheme = IosLightColorScheme,
        typography = Typography,
        content = content
    )
}
