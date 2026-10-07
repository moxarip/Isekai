package com.example.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable

private val DarkFantasyColorScheme = darkColorScheme(
    primary = AmberGold,
    onPrimary = AlleyBackground,
    primaryContainer = AmberDark,
    onPrimaryContainer = TextGold,
    secondary = FrostCyan,
    onSecondary = AlleyBackground,
    secondaryContainer = FrostDark,
    onSecondaryContainer = TextPrimary,
    tertiary = SuspicionPurple,
    onTertiary = TextPrimary,
    background = AlleyBackground,
    onBackground = TextPrimary,
    surface = AlleySurface,
    onSurface = TextPrimary,
    surfaceVariant = AlleyCard,
    onSurfaceVariant = TextSecondary,
    outline = AlleyCardBorder,
    error = SuspicionCritical
)

@Composable
fun MyApplicationTheme(
    content: @Composable () -> Unit
) {
    MaterialTheme(
        colorScheme = DarkFantasyColorScheme,
        typography = Typography,
        content = content
    )
}
