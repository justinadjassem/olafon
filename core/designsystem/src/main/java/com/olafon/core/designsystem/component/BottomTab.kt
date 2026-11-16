package com.olafon.core.designsystem.component

/** A tab of the [BottomBar]. [enabled] false greys it out for a section that is not built yet. */
data class BottomTab(
    val label: String,
    val icon: Int,
    val selected: Boolean,
    val enabled: Boolean = true,
    val onClick: () -> Unit,
)
