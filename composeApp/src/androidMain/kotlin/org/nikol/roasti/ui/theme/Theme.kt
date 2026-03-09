package org.nikol.roasti.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalInspectionMode

private val LightColorScheme = lightColorScheme(
    primary = Orange600,
    onPrimary = LightPrimaryFg,
    primaryContainer = Orange100,
    onPrimaryContainer = Orange800,
    secondary = LightSecondary,
    onSecondary = LightForeground,
    secondaryContainer = LightSecondary,
    onSecondaryContainer = LightForeground,
    background = LightBackground,
    onBackground = LightForeground,
    surface = LightCard,
    onSurface = LightForeground,
    surfaceVariant = LightMuted,
    onSurfaceVariant = LightMutedFg,
    outline = LightBorder,
    error = Red600,
    onError = LightPrimaryFg,
    errorContainer = Red50,
    onErrorContainer = Red700,
)

private val DarkColorScheme = darkColorScheme(
    primary = Orange500,
    onPrimary = DarkPrimaryFg,
    primaryContainer = Orange900,
    onPrimaryContainer = Orange100,
    secondary = DarkSurface,
    onSecondary = DarkForeground,
    secondaryContainer = DarkSurface,
    onSecondaryContainer = DarkForeground,
    background = DarkBackground,
    onBackground = DarkForeground,
    surface = DarkCard,
    onSurface = DarkForeground,
    surfaceVariant = DarkSurface,
    onSurfaceVariant = DarkMutedFg,
    outline = DarkBorder,
    error = DarkDestructive,
    onError = DarkForeground,
)

@Composable
fun RoastiTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit,
) {
    MaterialTheme(
        colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme,
        typography = RoastiTypography,
        shapes = RoastiShapes,
        content = content,
    )
}
