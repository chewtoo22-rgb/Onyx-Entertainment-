package com.onyx.avhub.core.common.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable

private val OnyxDarkColors = darkColorScheme(
    background = OnyxBackground,
    surface = OnyxSurface,
    surfaceVariant = OnyxSurfaceVariant,
    primary = OnyxAccent,
    secondary = OnyxAccentVariant,
    error = OnyxDanger,
    onBackground = OnyxOnBackground,
    onSurface = OnyxOnBackground,
    onSurfaceVariant = OnyxOnSurfaceMuted,
)

// The hub is designed console-first (dark), but a light scheme is provided
// for accessibility/system preference rather than forcing dark mode.
private val OnyxLightColors = lightColorScheme(
    primary = OnyxAccentVariant,
    secondary = OnyxAccent,
    error = OnyxDanger,
)

@Composable
fun OnyxAvHubTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit,
) {
    val colors = if (darkTheme) OnyxDarkColors else OnyxLightColors
    MaterialTheme(
        colorScheme = colors,
        typography = OnyxTypography,
        content = content,
    )
}
