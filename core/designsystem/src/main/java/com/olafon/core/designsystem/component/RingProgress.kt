package com.olafon.core.designsystem.component

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.size
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.olafon.core.designsystem.theme.OlafonColors

/** Concentric lime rings on an outline track, with a value and a caption in the middle (the home « 5/7 réveils »). */
@Composable
fun RingProgress(rings: List<Ring>, value: String, caption: String, modifier: Modifier = Modifier, size: Dp = 124.dp) {
    Box(modifier = modifier.size(size), contentAlignment = Alignment.Center) {
        Canvas(Modifier.size(size)) {
            var inset = 0f
            rings.forEach { ring ->
                val strokePx = ring.stroke.toPx()
                val diameter = this.size.minDimension - 2 * inset - strokePx
                val topLeft = Offset(inset + strokePx / 2, inset + strokePx / 2)
                val arcSize = Size(diameter, diameter)
                drawArc(OlafonColors.SurfaceHigh, 0f, FULL_TURN, false, topLeft, arcSize, style = Stroke(strokePx))
                drawArc(
                    color = OlafonColors.Lime,
                    startAngle = TOP,
                    sweepAngle = FULL_TURN * ring.fraction.coerceIn(0f, 1f),
                    useCenter = false,
                    topLeft = topLeft,
                    size = arcSize,
                    alpha = ring.alpha,
                    style = Stroke(strokePx, cap = StrokeCap.Round),
                )
                inset += strokePx + RING_GAP.toPx()
            }
        }
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(value, style = MaterialTheme.typography.titleLarge)
            Text(caption, style = MaterialTheme.typography.labelSmall, color = OlafonColors.Muted)
        }
    }
}

private const val FULL_TURN = 360f
private const val TOP = -90f
private val RING_GAP = 6.dp
