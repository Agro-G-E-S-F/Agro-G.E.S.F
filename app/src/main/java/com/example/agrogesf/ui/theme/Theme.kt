package com.example.agrogesf.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val LightColorScheme = lightColorScheme(
    primary = GreenPrimary,
    onPrimary = TextOnPrimary,
    primaryContainer = GreenLight,
    onPrimaryContainer = TextPrimary,
    secondary = YellowAccent,
    onSecondary = TextPrimary,
    background = BackgroundLight,
    onBackground = TextPrimary,
    surface = BackgroundCard,
    onSurface = TextPrimary,
    error = ErrorRed,
    onError = Color.White
)

@Composable
fun AgroGESFTheme(
    content: @Composable () -> Unit
) {
    MaterialTheme(
        colorScheme = LightColorScheme,
        typography = Typography,
        content = content
    )
}