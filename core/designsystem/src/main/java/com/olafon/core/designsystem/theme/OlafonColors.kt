package com.olafon.core.designsystem.theme

import androidx.compose.ui.graphics.Color

/** The Olafon palette: anthracite grounds, lime accent. Dark only, as drawn on the design canvas. */
object OlafonColors {
    val Background = Color(0xFF121214)
    val Surface = Color(0xFF1E1E21)
    val SurfaceHigh = Color(0xFF2A2A2E)
    val Outline = Color(0xFF3A3A3F)
    val OnSurface = Color(0xFFF5F5F2)
    val Muted = Color(0xFFA0A0A6)
    val Dim = Color(0xFF5E5E64)
    val Lime = Color(0xFFD4F955)
    val OnLime = Color(0xFF121214)
    val Error = Color(0xFFFF8A80)
}

/** Kept for callers of the engine steps; prefer [OlafonColors.Lime]. */
val Lime: Color = OlafonColors.Lime
