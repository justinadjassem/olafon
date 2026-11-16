package com.olafon.feature.briefing

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.olafon.core.designsystem.component.OlafonCard
import com.olafon.core.designsystem.component.PillButton
import com.olafon.core.designsystem.theme.OlafonColors
import com.olafon.core.domain.briefing.Briefing
import com.olafon.core.domain.briefing.ScheduleItem
import com.olafon.core.domain.briefing.TaskItem
import java.time.LocalDate
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.Locale

/**
 * Screen 6 of the canvas: the day as cached the evening before. [day] defaults to today. [closeLabel] names the
 * bottom action (« Terminer » after a ring, « Retour » from the home tab).
 */
@Composable
fun BriefingScreen(
    closeLabel: String,
    onClose: () -> Unit,
    modifier: Modifier = Modifier,
    day: LocalDate? = null,
    viewModel: BriefingViewModel = hiltViewModel(),
) {
    val shownDay = day ?: viewModel.today
    val flow = remember(shownDay) { viewModel.state(shownDay) }
    val state by flow.collectAsStateWithLifecycle(initialValue = BriefingUiState.Loading)

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(OlafonColors.Background)
            .statusBarsPadding()
            .navigationBarsPadding()
            .padding(horizontal = 12.dp)
            .padding(top = 12.dp, bottom = 16.dp),
    ) {
        Column(Modifier.padding(horizontal = 8.dp, vertical = 8.dp)) {
            Text(stringResource(R.string.briefing_title), style = MaterialTheme.typography.headlineLarge)
            Text(
                text = shownDay.format(DateTimeFormatter.ofPattern("EEEE d MMMM", Locale.FRENCH))
                    .replaceFirstChar { it.titlecase(Locale.FRENCH) },
                style = MaterialTheme.typography.bodyLarge,
                color = OlafonColors.Muted,
            )
        }
        Column(
            modifier = Modifier
                .weight(1f)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            when (val current = state) {
                BriefingUiState.Loading -> Unit
                BriefingUiState.NoData -> OlafonCard {
                    Text(stringResource(R.string.briefing_no_data_title), style = MaterialTheme.typography.titleMedium)
                    Spacer(Modifier.height(6.dp))
                    Text(
                        stringResource(R.string.briefing_no_data_text),
                        style = MaterialTheme.typography.bodyMedium,
                        color = OlafonColors.Muted,
                    )
                }
                is BriefingUiState.Ready -> Content(current.briefing)
            }
        }
        PillButton(
            text = closeLabel,
            onClick = onClose,
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 10.dp),
        )
    }
}

@Composable
private fun Content(briefing: Briefing) {
    OlafonCard {
        Row(
            Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.Bottom,
        ) {
            Text(stringResource(R.string.briefing_agenda), style = MaterialTheme.typography.titleMedium)
            Text(
                pluralStringResource(R.plurals.briefing_event_count, briefing.events.size, briefing.events.size),
                style = MaterialTheme.typography.labelMedium,
                color = OlafonColors.Muted,
            )
        }
        Spacer(Modifier.height(12.dp))
        if (briefing.events.isEmpty()) {
            Text(
                stringResource(R.string.briefing_no_event),
                style = MaterialTheme.typography.bodyMedium,
                color = OlafonColors.Muted,
            )
        }
        briefing.events.forEachIndexed { index, event ->
            EventRow(event, first = event == briefing.firstTimedEvent)
            if (index < briefing.events.lastIndex) Spacer(Modifier.height(8.dp))
        }
    }
    if (briefing.tasks.isNotEmpty()) {
        OlafonCard {
            Text(stringResource(R.string.briefing_tasks), style = MaterialTheme.typography.titleMedium)
            Spacer(Modifier.height(12.dp))
            briefing.tasks.forEach { TaskRow(it) }
        }
    }
    briefing.syncedAt?.let { syncedAt ->
        Text(
            text = stringResource(
                R.string.briefing_synced_at,
                syncedAt.atZone(
                    ZoneId.systemDefault(),
                ).format(DateTimeFormatter.ofPattern("d MMM 'à' HH:mm", Locale.FRENCH)),
            ),
            style = MaterialTheme.typography.bodySmall,
            color = OlafonColors.Muted,
            modifier = Modifier.padding(horizontal = 8.dp),
        )
    }
}

@Composable
private fun EventRow(event: ScheduleItem, first: Boolean) {
    val time = DateTimeFormatter.ofPattern("HH:mm")
    val zone = ZoneId.systemDefault()
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(MaterialTheme.shapes.medium)
            .background(OlafonColors.SurfaceHigh)
            .padding(horizontal = 14.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        Column(Modifier.width(56.dp)) {
            if (event.allDay) {
                Text(
                    stringResource(R.string.briefing_all_day),
                    style = MaterialTheme.typography.labelMedium,
                    color = OlafonColors.Lime,
                )
            } else {
                Text(
                    event.start.atZone(zone).format(time),
                    style = MaterialTheme.typography.titleMedium,
                    color = if (first) OlafonColors.Lime else OlafonColors.OnSurface,
                )
                Text(
                    event.end.atZone(zone).format(time),
                    style = MaterialTheme.typography.bodySmall,
                    color = OlafonColors.Muted,
                )
            }
        }
        Box(
            Modifier
                .size(width = 4.dp, height = 36.dp)
                .clip(CircleShape)
                .background(if (first || event.allDay) OlafonColors.Lime else OlafonColors.Outline),
        )
        Text(
            event.title.ifBlank { stringResource(R.string.briefing_untitled) },
            style = MaterialTheme.typography.bodyLarge,
            modifier = Modifier.weight(1f),
        )
    }
}

@Composable
private fun TaskRow(task: TaskItem) {
    Row(
        Modifier.padding(vertical = 6.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(10.dp),
    ) {
        Box(
            Modifier
                .size(10.dp)
                .clip(CircleShape)
                .background(if (task.overdue) OlafonColors.Error else OlafonColors.Lime),
        )
        Text(task.title, style = MaterialTheme.typography.bodyLarge)
    }
}
