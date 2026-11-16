package com.olafon.core.designsystem.component

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.olafon.core.designsystem.theme.OlafonColors

/** Full-height pill (58 dp) used for every main action. */
@Composable
fun PillButton(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    style: PillStyle = PillStyle.LIME,
    enabled: Boolean = true,
    height: Int = PILL_HEIGHT_DP,
    content: @Composable RowScope.() -> Unit,
) {
    val sized = modifier.height(height.dp)
    when (style) {
        PillStyle.LIME -> Button(
            onClick = onClick,
            modifier = sized,
            enabled = enabled,
            shape = CircleShape,
            colors = ButtonDefaults.buttonColors(
                containerColor = OlafonColors.Lime,
                contentColor = OlafonColors.OnLime,
                disabledContainerColor = OlafonColors.SurfaceHigh,
                disabledContentColor = OlafonColors.Dim,
            ),
            contentPadding = PaddingValues(horizontal = 24.dp),
            content = content,
        )
        PillStyle.DARK -> Button(
            onClick = onClick,
            modifier = sized,
            enabled = enabled,
            shape = CircleShape,
            colors = ButtonDefaults.buttonColors(
                containerColor = OlafonColors.Background,
                contentColor = OlafonColors.Lime,
                disabledContainerColor = OlafonColors.SurfaceHigh,
                disabledContentColor = OlafonColors.Dim,
            ),
            contentPadding = PaddingValues(horizontal = 24.dp),
            content = content,
        )
        PillStyle.OUTLINE -> OutlinedButton(
            onClick = onClick,
            modifier = sized,
            enabled = enabled,
            shape = CircleShape,
            border = BorderStroke(2.dp, if (enabled) LocalOutlineColor.current else OlafonColors.Outline),
            colors = ButtonDefaults.outlinedButtonColors(
                contentColor = LocalOutlineColor.current,
                disabledContentColor = OlafonColors.Dim,
            ),
            contentPadding = PaddingValues(horizontal = 24.dp),
            content = content,
        )
    }
}

/** Text-only pill: a [PillButton] with a plain label. */
@Composable
fun PillButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    style: PillStyle = PillStyle.LIME,
    enabled: Boolean = true,
    height: Int = PILL_HEIGHT_DP,
) {
    PillButton(onClick = onClick, modifier = modifier, style = style, enabled = enabled, height = height) {
        Text(text, style = MaterialTheme.typography.titleMedium)
    }
}

/** Colour of the outline pill's border and text: anthracite on a lime screen, white on a dark one. */
val LocalOutlineColor = androidx.compose.runtime.compositionLocalOf { Color(0xFFF5F5F2) }

const val PILL_HEIGHT_DP = 58
