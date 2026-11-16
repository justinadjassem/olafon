package com.olafon.feature.challenges.pattern

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.dp
import com.olafon.core.designsystem.theme.OlafonColors

/**
 * A square grid of dots, drawn as on the canvas: outline dots, lime dots with a soft halo once on the path, lime
 * segments, and a dotted segment following the finger. In display mode ([shown] not null) it draws that path and
 * ignores touches; otherwise the finger joins dots by dragging and [onComplete] receives the path when it lifts.
 * A dot is taken when the finger passes within [HIT_RADIUS] of its centre; dots already on the path are skipped.
 */
@Composable
fun PatternPad(
    gridSize: Int,
    modifier: Modifier = Modifier,
    shown: List<Int>? = null,
    onPathChange: (Int) -> Unit = {},
    onComplete: (List<Int>) -> Unit = {},
) {
    var path by remember(gridSize, shown) { mutableStateOf(listOf<Int>()) }
    var finger by remember { mutableStateOf<Offset?>(null) }
    val haptic = LocalHapticFeedback.current
    val interactive = shown == null

    Canvas(
        modifier = modifier
            .aspectRatio(1f)
            .pointerInput(gridSize, interactive) {
                if (!interactive) return@pointerInput
                val hitRadius = HIT_RADIUS.toPx()
                detectDragGestures(
                    onDragStart = { start ->
                        finger = start
                        hitDot(start, size, gridSize, hitRadius)?.let {
                            path = listOf(it)
                            onPathChange(1)
                        }
                    },
                    onDrag = { change, _ ->
                        finger = change.position
                        val dot = hitDot(change.position, size, gridSize, hitRadius)
                        if (dot != null && dot !in path) {
                            path = path + dot
                            onPathChange(path.size)
                            haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                        }
                    },
                    onDragEnd = {
                        val drawn = path
                        path = emptyList()
                        finger = null
                        onPathChange(0)
                        if (drawn.isNotEmpty()) onComplete(drawn)
                    },
                    onDragCancel = {
                        path = emptyList()
                        finger = null
                        onPathChange(0)
                    },
                )
            },
    ) {
        val drawnPath = shown ?: path
        val cell = size.width / gridSize
        val dot = DOT_RADIUS.toPx()
        val onDot = ON_DOT_RADIUS.toPx()
        val halo = HALO_RADIUS.toPx()
        val stroke = STROKE.toPx()
        val centers = (0 until gridSize * gridSize).map { index -> center(index, cell, gridSize) }

        drawnPath.zipWithNext { a, b ->
            drawLine(OlafonColors.Lime, centers[a], centers[b], strokeWidth = stroke, cap = StrokeCap.Round)
        }
        val last = drawnPath.lastOrNull()
        val tip = finger
        if (interactive && last != null && tip != null) {
            drawLine(
                color = OlafonColors.Lime,
                start = centers[last],
                end = tip,
                strokeWidth = stroke,
                cap = StrokeCap.Round,
                alpha = TRAIL_ALPHA,
                pathEffect = PathEffect.dashPathEffect(floatArrayOf(2.dp.toPx(), 16.dp.toPx())),
            )
        }
        centers.forEachIndexed { index, c ->
            if (index in drawnPath) {
                drawCircle(OlafonColors.Lime, radius = halo, center = c, alpha = HALO_ALPHA)
                drawCircle(OlafonColors.Lime, radius = onDot, center = c)
            } else {
                drawCircle(OlafonColors.Outline, radius = dot, center = c)
            }
        }
        if (interactive && tip != null) {
            drawCircle(OlafonColors.OnSurface, radius = halo, center = tip, alpha = FINGER_ALPHA)
        }
    }
}

private fun center(index: Int, cell: Float, gridSize: Int): Offset =
    Offset((index % gridSize + HALF) * cell, (index / gridSize + HALF) * cell)

/** The dot whose centre is within [hitRadius] of [position], if any. */
internal fun hitDot(position: Offset, size: IntSize, gridSize: Int, hitRadius: Float): Int? {
    val cell = size.width.toFloat() / gridSize
    return (0 until gridSize * gridSize).firstOrNull { index ->
        (center(index, cell, gridSize) - position).getDistance() <= hitRadius
    }
}

private const val HALF = 0.5f
private const val TRAIL_ALPHA = 0.6f
private const val HALO_ALPHA = 0.2f
private const val FINGER_ALPHA = 0.1f
private val HIT_RADIUS: Dp = 40.dp
private val DOT_RADIUS: Dp = 9.dp
private val ON_DOT_RADIUS: Dp = 12.dp
private val HALO_RADIUS: Dp = 24.dp
private val STROKE: Dp = 10.dp
