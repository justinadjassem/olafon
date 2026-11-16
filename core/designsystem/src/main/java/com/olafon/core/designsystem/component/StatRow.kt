package com.olafon.core.designsystem.component

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.olafon.core.designsystem.theme.OlafonColors

/** Three stats side by side, separated by thin outlines, like « Difficulté | Snooze | Volume » on the editor. */
@Composable
fun StatRow(stats: List<Stat>, modifier: Modifier = Modifier) {
    Row(modifier = modifier.fillMaxWidth()) {
        stats.forEachIndexed { index, stat ->
            Column(
                modifier = Modifier
                    .weight(1f)
                    .let { base -> stat.onClick?.let { base.clickable(onClick = it) } ?: base }
                    .padding(vertical = 8.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                Text(stat.label, style = MaterialTheme.typography.bodySmall, color = OlafonColors.Muted)
                Text(stat.value, style = MaterialTheme.typography.titleSmall, color = OlafonColors.Lime)
            }
            if (index < stats.lastIndex) {
                Box(
                    Modifier
                        .width(1.dp)
                        .height(44.dp)
                        .align(Alignment.CenterVertically)
                        .padding(vertical = 0.dp),
                ) { HorizontalDivider(Modifier.fillMaxWidth().height(44.dp), color = OlafonColors.Outline) }
            }
        }
    }
}
