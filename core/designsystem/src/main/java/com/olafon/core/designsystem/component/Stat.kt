package com.olafon.core.designsystem.component

/** One cell of a [StatRow]: a muted label over a lime value. Tapping it cycles the value when [onClick] is set. */
data class Stat(val label: String, val value: String, val onClick: (() -> Unit)? = null)
