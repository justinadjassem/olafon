package com.olafon.core.designsystem.theme

import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Shapes
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.unit.dp

private val OlafonColorScheme = darkColorScheme(
    primary = OlafonColors.Lime,
    onPrimary = OlafonColors.OnLime,
    primaryContainer = OlafonColors.Lime,
    onPrimaryContainer = OlafonColors.OnLime,
    secondary = OlafonColors.Muted,
    onSecondary = OlafonColors.OnLime,
    secondaryContainer = OlafonColors.SurfaceHigh,
    onSecondaryContainer = OlafonColors.OnSurface,
    background = OlafonColors.Background,
    onBackground = OlafonColors.OnSurface,
    surface = OlafonColors.Background,
    onSurface = OlafonColors.OnSurface,
    surfaceVariant = OlafonColors.Surface,
    onSurfaceVariant = OlafonColors.Muted,
    surfaceContainer = OlafonColors.Surface,
    surfaceContainerHigh = OlafonColors.SurfaceHigh,
    surfaceContainerHighest = OlafonColors.SurfaceHigh,
    outline = OlafonColors.Outline,
    outlineVariant = OlafonColors.Outline,
    error = OlafonColors.Error,
    onError = OlafonColors.OnLime,
)

val OlafonShapes = Shapes(
    extraSmall = RoundedCornerShape(10.dp),
    small = RoundedCornerShape(14.dp),
    medium = RoundedCornerShape(22.dp),
    large = RoundedCornerShape(32.dp),
    extraLarge = RoundedCornerShape(40.dp),
)

/** Anthracite and lime, always dark: the canvas has no light variant. */
@Composable
fun OlafonTheme(content: @Composable () -> Unit) {
    MaterialTheme(colorScheme = OlafonColorScheme, typography = Typography, shapes = OlafonShapes) {
        // Screens draw on plain boxes, not on Surface: give text its default colour here once.
        CompositionLocalProvider(LocalContentColor provides OlafonColors.OnSurface, content = content)
    }
}
