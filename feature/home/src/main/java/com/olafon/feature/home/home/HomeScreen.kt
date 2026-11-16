package com.olafon.feature.home.home

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.olafon.core.designsystem.R as DsR
import com.olafon.core.designsystem.component.BottomBar
import com.olafon.core.designsystem.component.BottomTab
import com.olafon.core.designsystem.component.OlafonCard
import com.olafon.core.designsystem.component.Ring
import com.olafon.core.designsystem.component.RingProgress
import com.olafon.core.designsystem.component.ToggleSwitch
import com.olafon.core.designsystem.component.TopRoundedShape
import com.olafon.core.designsystem.theme.OlafonColors
import com.olafon.core.domain.model.Alarm
import com.olafon.core.domain.model.WakeStats
import com.olafon.feature.home.R
import com.olafon.feature.home.daysText
import com.olafon.feature.home.shortLabel
import com.olafon.feature.home.timeText
import java.time.Duration
import java.time.ZonedDateTime
import java.time.format.DateTimeFormatter
import java.util.Locale

/** Screen 1 of the canvas: next alarm, tomorrow at a glance, the alarm list and the floating bar. */
@Composable
fun HomeScreen(
    onNewAlarm: () -> Unit,
    onEditAlarm: (Long) -> Unit,
    onSettings: () -> Unit,
    showTestActions: Boolean,
    viewModel: HomeViewModel = hiltViewModel(),
) {
    val state by viewModel.state.collectAsStateWithLifecycle()

    Box(
        Modifier
            .fillMaxSize()
            .background(OlafonColors.Background),
    ) {
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .statusBarsPadding(),
            contentPadding = PaddingValues(start = 12.dp, end = 12.dp, top = 8.dp, bottom = 110.dp),
        ) {
            item { Header(state.stats, onSettings) }
            item { NextAlarmCard(state.nextAlarm, state.loaded) }
            item { Spacer(Modifier.height(10.dp)) }
            item { TomorrowCard(state.stats) }
            item { Spacer(Modifier.height(10.dp)) }
            item {
                OlafonCard(
                    shape = TopRoundedShape,
                    contentPadding = PaddingValues(top = 20.dp, bottom = 6.dp, start = 14.dp, end = 14.dp),
                ) {
                    Row(
                        Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 8.dp, vertical = 4.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Text(stringResource(R.string.home_my_alarms), style = MaterialTheme.typography.titleMedium)
                        if (showTestActions) {
                            Text(
                                text = stringResource(R.string.home_test_alarm),
                                style = MaterialTheme.typography.labelMedium,
                                color = OlafonColors.Lime,
                                modifier = Modifier
                                    .clip(CircleShape)
                                    .clickable(onClick = viewModel::onAddTestAlarm, role = Role.Button)
                                    .padding(horizontal = 10.dp, vertical = 8.dp),
                            )
                        }
                    }
                    if (state.loaded && state.alarms.isEmpty()) {
                        Text(
                            stringResource(R.string.home_no_alarm),
                            style = MaterialTheme.typography.bodyMedium,
                            color = OlafonColors.Muted,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 12.dp),
                        )
                    }
                }
            }
            items(state.alarms, key = Alarm::id) { alarm ->
                Box(
                    Modifier
                        .fillMaxWidth()
                        .background(OlafonColors.Surface)
                        .padding(horizontal = 14.dp, vertical = 4.dp),
                ) {
                    AlarmRow(alarm, onClick = { onEditAlarm(alarm.id) }, onToggle = { viewModel.onToggle(alarm, it) })
                }
            }
        }
        BottomBar(
            leading = listOf(
                BottomTab(stringResource(R.string.tab_home), DsR.drawable.ic_home, selected = true) {},
                BottomTab(
                    stringResource(R.string.tab_day),
                    DsR.drawable.ic_calendar,
                    selected = false,
                    enabled = false,
                ) {
                },
            ),
            trailing = listOf(
                BottomTab(
                    stringResource(R.string.tab_stats),
                    DsR.drawable.ic_stats,
                    selected = false,
                    enabled = false,
                ) {
                },
                BottomTab(
                    stringResource(R.string.tab_tasks),
                    DsR.drawable.ic_tasks,
                    selected = false,
                    enabled = false,
                ) {
                },
            ),
            onAdd = onNewAlarm,
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .navigationBarsPadding()
                .padding(horizontal = 12.dp, vertical = 16.dp),
        )
    }
}

@Composable
private fun Header(stats: WakeStats, onSettings: () -> Unit) {
    val hour = ZonedDateTime.now().hour
    val greeting = when {
        hour < NOON -> R.string.greeting_morning
        hour < EVENING -> R.string.greeting_afternoon
        else -> R.string.greeting_evening
    }
    Row(
        Modifier
            .fillMaxWidth()
            .padding(horizontal = 8.dp, vertical = 6.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            Box(
                Modifier
                    .size(48.dp)
                    .clip(CircleShape)
                    .background(OlafonColors.Lime)
                    .clickable(onClick = onSettings, role = Role.Button),
                contentAlignment = Alignment.Center,
            ) {
                Icon(
                    painterResource(DsR.drawable.ic_settings),
                    contentDescription = stringResource(R.string.settings_title),
                    tint = OlafonColors.OnLime,
                    modifier = Modifier.size(22.dp),
                )
            }
            Column {
                Text(stringResource(greeting), style = MaterialTheme.typography.titleLarge)
                Text(
                    stringResource(R.string.greeting_subtitle),
                    style = MaterialTheme.typography.labelMedium,
                    color = OlafonColors.Muted,
                )
            }
        }
        Row(
            Modifier
                .clip(CircleShape)
                .background(OlafonColors.SurfaceHigh)
                .padding(horizontal = 14.dp, vertical = 11.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(6.dp),
        ) {
            Icon(
                painterResource(DsR.drawable.ic_flame),
                contentDescription = stringResource(R.string.streak),
                tint = OlafonColors.Lime,
                modifier = Modifier.size(18.dp),
            )
            Text(stats.streakDays.toString(), style = MaterialTheme.typography.labelLarge, color = OlafonColors.Lime)
        }
    }
}

@Composable
private fun NextAlarmCard(next: NextAlarm?, loaded: Boolean) {
    OlafonCard {
        Row(
            Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(stringResource(R.string.home_next_alarm), style = MaterialTheme.typography.titleMedium)
            Text(
                text = next?.at?.format(DateTimeFormatter.ofPattern("EEE d MMM", Locale.FRENCH)).orEmpty(),
                style = MaterialTheme.typography.labelMedium,
                color = OlafonColors.Muted,
            )
        }
        Spacer(Modifier.height(12.dp))
        Row(
            Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.Bottom,
        ) {
            Text(
                text = next?.alarm?.timeText() ?: if (loaded) "--:--" else "",
                style = MaterialTheme.typography.displayMedium,
                color = OlafonColors.Lime,
            )
            if (next != null) {
                Column(horizontalAlignment = Alignment.End, modifier = Modifier.padding(bottom = 6.dp)) {
                    Text(
                        stringResource(R.string.home_rings_in),
                        style = MaterialTheme.typography.bodySmall,
                        color = OlafonColors.Muted,
                    )
                    Text(next.at.untilText(), style = MaterialTheme.typography.titleSmall)
                }
            }
        }
        Spacer(Modifier.height(12.dp))
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            next?.alarm?.challenges?.forEach { Chip(it.shortLabel()) }
            if (next?.alarm?.briefingEnabled == true) Chip(stringResource(R.string.home_chip_briefing))
            if (next == null && loaded) Chip(stringResource(R.string.home_no_next_alarm))
        }
    }
}

@Composable
private fun TomorrowCard(stats: WakeStats) {
    OlafonCard {
        Row(
            Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Text(stringResource(R.string.home_tomorrow), style = MaterialTheme.typography.titleMedium)
                Figure(stringResource(R.string.home_events), "–", stringResource(R.string.home_briefing_soon))
                Figure(stringResource(R.string.home_tasks), "–", null)
            }
            RingProgress(
                rings = listOf(
                    Ring(
                        fraction = if (stats.weekTotal ==
                            0
                        ) {
                            0f
                        } else {
                            stats.weekSuccess.toFloat() / stats.weekTotal
                        },
                        stroke = 12.dp,
                    ),
                    Ring(fraction = (stats.streakDays / STREAK_GOAL).coerceAtMost(1f), stroke = 10.dp, alpha = 0.55f),
                ),
                value = "${stats.weekSuccess}/${stats.weekTotal}",
                caption = stringResource(R.string.home_wakes),
            )
        }
    }
}

@Composable
private fun Figure(label: String, value: String, detail: String?) {
    Column {
        Text(label, style = MaterialTheme.typography.bodySmall, color = OlafonColors.Muted)
        Row(verticalAlignment = Alignment.Bottom, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
            Text(
                value,
                style = MaterialTheme.typography.headlineSmall,
                color = if (value ==
                    "–"
                ) {
                    OlafonColors.Muted
                } else {
                    OlafonColors.Lime
                },
            )
            if (detail != null) {
                Text(
                    detail,
                    style = MaterialTheme.typography.bodySmall,
                    color = OlafonColors.Muted,
                    modifier = Modifier.padding(bottom = 3.dp),
                )
            }
        }
    }
}

@Composable
private fun Chip(text: String) {
    Text(
        text = text,
        style = MaterialTheme.typography.labelMedium,
        modifier = Modifier
            .clip(CircleShape)
            .background(OlafonColors.SurfaceHigh)
            .padding(horizontal = 12.dp, vertical = 6.dp),
    )
}

@Composable
private fun AlarmRow(alarm: Alarm, onClick: () -> Unit, onToggle: (Boolean) -> Unit) {
    val tint = if (alarm.enabled) OlafonColors.OnSurface else OlafonColors.Muted
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(MaterialTheme.shapes.medium)
            .background(OlafonColors.SurfaceHigh)
            .clickable(onClick = onClick)
            .padding(horizontal = 14.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        Box(
            Modifier
                .size(40.dp)
                .clip(CircleShape)
                .background(OlafonColors.Background),
            contentAlignment = Alignment.Center,
        ) {
            Icon(
                painterResource(DsR.drawable.ic_alarm),
                contentDescription = null,
                tint = if (alarm.enabled) OlafonColors.Lime else OlafonColors.Muted,
                modifier = Modifier.size(20.dp),
            )
        }
        Column(Modifier.weight(1f)) {
            val subtitle = listOf(alarm.label, alarm.daysText()).filter { it.isNotBlank() }.joinToString(" · ")
            Text(subtitle, style = MaterialTheme.typography.bodySmall, color = OlafonColors.Muted)
            Text(alarm.timeText(), style = MaterialTheme.typography.titleLarge, color = tint)
        }
        ToggleSwitch(checked = alarm.enabled, onCheckedChange = onToggle)
        Spacer(Modifier.width(0.dp))
    }
}

@Composable
private fun ZonedDateTime.untilText(): String {
    val minutes = Duration.between(ZonedDateTime.now(), this).toMinutes().coerceAtLeast(0)
    return stringResource(R.string.duration_h_min, minutes / MINUTES_PER_HOUR, minutes % MINUTES_PER_HOUR)
}

private const val NOON = 12
private const val EVENING = 18
private const val MINUTES_PER_HOUR = 60
private const val STREAK_GOAL = 7f
