package com.olafon.core.designsystem.component

import androidx.compose.ui.unit.Dp

/** One ring of a [RingProgress]: a fraction in 0..1 and its stroke. */
data class Ring(val fraction: Float, val stroke: Dp, val alpha: Float = 1f)
