package com.olafon.feature.home.edit

import androidx.compose.foundation.gestures.snapping.rememberSnapFlingBehavior
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.dp
import com.olafon.core.designsystem.theme.OlafonColors
import kotlinx.coroutines.flow.distinctUntilChanged

/**
 * A vertical picker that shows the neighbours of the selected number dimmed above and below it, like the hour
 * and minute columns of the canvas. Scrolling snaps on an item; [onValueChange] fires when the centred one changes.
 */
@Composable
fun NumberWheel(
    range: IntRange,
    value: Int,
    onValueChange: (Int) -> Unit,
    modifier: Modifier = Modifier,
    format: (Int) -> String = { "%02d".format(it) },
) {
    val items = remember(range) { range.toList() }
    val listState = rememberLazyListState(initialFirstVisibleItemIndex = (value - range.first).coerceIn(items.indices))
    val itemHeightPx = with(LocalDensity.current) { ITEM_HEIGHT.toPx() }
    val centered by remember {
        derivedStateOf {
            val extra = if (listState.firstVisibleItemScrollOffset > itemHeightPx / 2) 1 else 0
            (listState.firstVisibleItemIndex + extra).coerceIn(items.indices)
        }
    }

    LaunchedEffect(listState) {
        snapshotFlow { centered }.distinctUntilChanged().collect { onValueChange(items[it]) }
    }

    LazyColumn(
        state = listState,
        flingBehavior = rememberSnapFlingBehavior(listState),
        contentPadding = PaddingValues(vertical = ITEM_HEIGHT),
        modifier = modifier
            .height(ITEM_HEIGHT * VISIBLE_ITEMS)
            .width(WHEEL_WIDTH),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        items(items.size) { index ->
            val selected = index == centered
            Box(Modifier.height(ITEM_HEIGHT), contentAlignment = Alignment.Center) {
                Text(
                    text = format(items[index]),
                    style = with(MaterialTheme.typography) { if (selected) displayMedium else titleLarge },
                    color = if (selected) OlafonColors.Lime else OlafonColors.Dim,
                )
            }
        }
    }
}

private val ITEM_HEIGHT = 64.dp
private val WHEEL_WIDTH = 96.dp
private const val VISIBLE_ITEMS = 3
