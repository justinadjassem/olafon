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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.dp
import com.olafon.core.designsystem.theme.Lime

/**
 * A square grid of dots. In display mode ([shown] not null) it draws that path and ignores touches; otherwise the
 * finger joins dots by dragging and [onComplete] receives the path when it lifts. A dot is taken when the finger
 * passes within [HIT_RADIUS] of its centre; dots already on the path are skipped, like the unlock pattern.
 */
@Composable
fun PatternPad(
    gridSize: Int,
    modifier: Modifier = Modifier,
    shown: List<Int>? = null,
    onComplete: (List<Int>) -> Unit = {},
) {
    var path by remember(gridSize, shown) { mutableStateOf(listOf<Int>()) }
    var finger by remember { mutableStateOf<Offset?>(null) }
    val haptic = LocalHapticFeedback.current
    val dotColor = Color.Gray
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
                        hitDot(start, size, gridSize, hitRadius)?.let { path = listOf(it) }
                    },
                    onDrag = { change, _ ->
                        finger = change.position
                        val dot = hitDot(change.position, size, gridSize, hitRadius)
                        if (dot != null && dot !in path) {
                            path = path + dot
                            haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                        }
                    },
                    onDragEnd = {
                        val drawn = path
                        path = emptyList()
                        finger = null
                        if (drawn.isNotEmpty()) onComplete(drawn)
                    },
                    onDragCancel = {
                        path = emptyList()
                        finger = null
                    },
                )
            },
    ) {
        val drawnPath = shown ?: path
        val cell = size.width / gridSize
        val dotRadius = DOT_RADIUS.toPx()
        val stroke = STROKE.toPx()
        val centers = (0 until gridSize * gridSize).map { index -> center(index, cell, gridSize) }

        drawnPath.zipWithNext { a, b ->
            drawLine(Lime, centers[a], centers[b], strokeWidth = stroke, cap = StrokeCap.Round)
        }
        val last = drawnPath.lastOrNull()
        val tip = finger
        if (interactive && last != null && tip != null) {
            drawLine(Lime.copy(alpha = 0.6f), centers[last], tip, strokeWidth = stroke, cap = StrokeCap.Round)
        }
        centers.forEachIndexed { index, c ->
            val onPath = index in drawnPath
            drawCircle(if (onPath) Lime else dotColor, radius = if (onPath) dotRadius * 1.5f else dotRadius, center = c)
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
private val HIT_RADIUS: Dp = 40.dp
private val DOT_RADIUS: Dp = 10.dp
private val STROKE: Dp = 6.dp
