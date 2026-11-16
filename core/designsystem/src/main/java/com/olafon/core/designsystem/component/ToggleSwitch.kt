package com.olafon.core.designsystem.component

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.selection.toggleable
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.unit.dp
import com.olafon.core.designsystem.theme.OlafonColors

/**
 * The 50 × 30 dp switch of the canvas: lime track with a dark knob when on, outline track with a grey knob when
 * off.
 */
@Composable
fun ToggleSwitch(
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
) {
    val track by animateColorAsState(if (checked) OlafonColors.Lime else OlafonColors.Outline, label = "track")
    val knob by animateColorAsState(if (checked) OlafonColors.Background else OlafonColors.Muted, label = "knob")
    val offset by animateDpAsState(if (checked) 20.dp else 0.dp, label = "offset")
    Box(
        modifier = modifier
            .size(width = 50.dp, height = 30.dp)
            .clip(CircleShape)
            .background(track)
            .toggleable(value = checked, enabled = enabled, role = Role.Switch, onValueChange = onCheckedChange)
            .padding(4.dp),
        contentAlignment = Alignment.CenterStart,
    ) {
        Box(
            Modifier
                .offset(x = offset)
                .size(22.dp)
                .clip(CircleShape)
                .background(knob),
        )
    }
}
