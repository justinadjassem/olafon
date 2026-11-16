package com.olafon.feature.home.edit

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.media.RingtoneManager
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.produceState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.unit.dp
import androidx.core.content.IntentCompat
import androidx.core.net.toUri
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.olafon.core.designsystem.R as DsR
import com.olafon.core.designsystem.component.OlafonCard
import com.olafon.core.designsystem.component.PillButton
import com.olafon.core.designsystem.component.SettingRow
import com.olafon.core.designsystem.component.Stat
import com.olafon.core.designsystem.component.StatRow
import com.olafon.core.designsystem.component.ToggleSwitch
import com.olafon.core.designsystem.theme.OlafonColors
import com.olafon.core.domain.model.Alarm
import com.olafon.core.domain.model.ChallengeType
import com.olafon.feature.home.R
import com.olafon.feature.home.initial
import com.olafon.feature.home.label
import java.time.DayOfWeek
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

/** Screen 2 of the canvas: time wheels, repeat days, challenge chain, options, save. */
@Composable
fun EditAlarmScreen(onClose: () -> Unit, viewModel: EditAlarmViewModel = hiltViewModel()) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    LaunchedEffect(state.done) { if (state.done) onClose() }
    val draft = state.draft

    Column(
        Modifier
            .fillMaxSize()
            .background(OlafonColors.Background)
            .statusBarsPadding()
            .navigationBarsPadding()
            .padding(horizontal = 12.dp)
            .padding(bottom = 16.dp),
    ) {
        TopBar(
            title = stringResource(if (state.isNew) R.string.edit_title_new else R.string.edit_title_edit),
            onBack = onClose,
        )
        if (draft != null) {
            Column(
                Modifier
                    .weight(1f)
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(10.dp),
            ) {
                TimeCard(draft, viewModel)
                ChallengesCard(draft, viewModel::onChallengeToggle)
                OptionsCard(draft, viewModel)
                if (!state.isNew) {
                    Text(
                        text = stringResource(R.string.edit_delete),
                        style = MaterialTheme.typography.labelLarge,
                        color = OlafonColors.Error,
                        modifier = Modifier
                            .align(Alignment.CenterHorizontally)
                            .clip(CircleShape)
                            .clickable(onClick = viewModel::onDelete, role = Role.Button)
                            .padding(horizontal = 16.dp, vertical = 12.dp),
                    )
                }
                Spacer(Modifier.height(4.dp))
            }
            PillButton(
                text = stringResource(R.string.edit_save),
                onClick = viewModel::onSave,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 10.dp),
            )
        }
    }
}

@Composable
private fun TopBar(title: String, onBack: () -> Unit) {
    Row(
        Modifier
            .fillMaxWidth()
            .padding(horizontal = 8.dp, vertical = 8.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Box(
            Modifier
                .size(44.dp)
                .clip(MaterialTheme.shapes.small)
                .background(OlafonColors.SurfaceHigh)
                .clickable(onClick = onBack, role = Role.Button),
            contentAlignment = Alignment.Center,
        ) {
            Icon(
                painterResource(DsR.drawable.ic_back),
                contentDescription = stringResource(R.string.action_back),
                modifier = Modifier.size(20.dp),
            )
        }
        Text(title, style = MaterialTheme.typography.titleMedium)
        Spacer(Modifier.size(44.dp))
    }
}

@Composable
private fun TimeCard(draft: Alarm, viewModel: EditAlarmViewModel) {
    OlafonCard(contentPadding = PaddingValues(22.dp)) {
        Row(
            Modifier.align(Alignment.CenterHorizontally),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            NumberWheel(range = 0..MAX_HOUR, value = draft.hour, onValueChange = {
                viewModel.onTimeChange(it, draft.minute)
            })
            Text(":", style = MaterialTheme.typography.displaySmall, color = OlafonColors.Lime)
            NumberWheel(range = 0..MAX_MINUTE, value = draft.minute, onValueChange = {
                viewModel.onTimeChange(draft.hour, it)
            })
        }
        Spacer(Modifier.height(16.dp))
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(6.dp)) {
            DayOfWeek.entries.forEach { day ->
                val on = day in draft.days
                Box(
                    Modifier
                        .weight(1f)
                        .height(44.dp)
                        .clip(CircleShape)
                        .background(if (on) OlafonColors.Lime else OlafonColors.SurfaceHigh)
                        .clickable(onClick = { viewModel.onDayToggle(day) }, role = Role.Checkbox),
                    contentAlignment = Alignment.Center,
                ) {
                    Text(
                        day.initial(),
                        style = MaterialTheme.typography.labelLarge,
                        color = if (on) OlafonColors.OnLime else OlafonColors.Muted,
                    )
                }
            }
        }
        Spacer(Modifier.height(16.dp))
        OutlinedTextField(
            value = draft.label,
            onValueChange = viewModel::onLabelChange,
            label = { Text(stringResource(R.string.edit_label)) },
            singleLine = true,
            shape = MaterialTheme.shapes.medium,
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = OlafonColors.Lime,
                unfocusedBorderColor = OlafonColors.Outline,
                focusedLabelColor = OlafonColors.Lime,
                unfocusedLabelColor = OlafonColors.Muted,
                cursorColor = OlafonColors.Lime,
            ),
            modifier = Modifier.fillMaxWidth(),
        )
    }
}

@Composable
private fun ChallengesCard(draft: Alarm, onToggle: (ChallengeType) -> Unit) {
    OlafonCard(contentPadding = PaddingValues(start = 14.dp, end = 14.dp, top = 20.dp, bottom = 14.dp)) {
        Row(
            Modifier
                .fillMaxWidth()
                .padding(start = 8.dp, end = 8.dp, bottom = 8.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.Bottom,
        ) {
            Text(stringResource(R.string.edit_challenges), style = MaterialTheme.typography.titleMedium)
            Text(
                text = stringResource(R.string.edit_challenges_count, draft.challenges.size).uppercase(),
                style = MaterialTheme.typography.labelSmall,
                color = OlafonColors.Muted,
            )
        }
        ChallengeType.entries.forEachIndexed { index, type ->
            val position = draft.challenges.indexOf(type)
            ChallengeRow(type, position, onClick = { onToggle(type) })
            if (index < ChallengeType.entries.lastIndex) Spacer(Modifier.height(8.dp))
        }
    }
}

@Composable
private fun ChallengeRow(type: ChallengeType, position: Int, onClick: () -> Unit) {
    val selected = position >= 0
    val shape = MaterialTheme.shapes.medium
    Row(
        Modifier
            .fillMaxWidth()
            .clip(shape)
            .then(
                if (selected) {
                    Modifier.background(
                        OlafonColors.SurfaceHigh,
                    )
                } else {
                    Modifier.border(1.5.dp, OlafonColors.Outline, shape)
                },
            )
            .clickable(onClick = onClick, role = Role.Checkbox)
            .padding(12.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        Box(
            Modifier
                .size(56.dp)
                .clip(MaterialTheme.shapes.small)
                .background(if (selected) OlafonColors.Background else OlafonColors.SurfaceHigh),
            contentAlignment = Alignment.Center,
        ) {
            Icon(
                painterResource(type.icon()),
                contentDescription = null,
                tint = if (selected) OlafonColors.Lime else OlafonColors.Muted,
                modifier = Modifier.size(28.dp),
            )
        }
        Column(Modifier.weight(1f)) {
            Text(
                type.label(),
                style = MaterialTheme.typography.titleSmall,
                color = if (selected) OlafonColors.Lime else OlafonColors.OnSurface,
            )
            Text(
                stringResource(type.description()),
                style = MaterialTheme.typography.bodySmall,
                color = OlafonColors.Muted,
            )
        }
        if (selected) {
            Box(
                Modifier
                    .size(28.dp)
                    .clip(CircleShape)
                    .background(OlafonColors.Lime),
                contentAlignment = Alignment.Center,
            ) {
                Text(
                    (position + 1).toString(),
                    style = MaterialTheme.typography.labelMedium,
                    color = OlafonColors.OnLime,
                )
            }
        } else {
            Icon(
                painterResource(DsR.drawable.ic_plus),
                contentDescription = null,
                tint = OlafonColors.Muted,
                modifier = Modifier.size(20.dp),
            )
        }
    }
}

@Composable
private fun OptionsCard(draft: Alarm, viewModel: EditAlarmViewModel) {
    val context = LocalContext.current
    val picker = rememberLauncherForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            val uri = result.data?.let {
                IntentCompat.getParcelableExtra(it, RingtoneManager.EXTRA_RINGTONE_PICKED_URI, Uri::class.java)
            }
            viewModel.onRingtoneChange(uri?.toString())
        }
    }
    val ringtoneTitle by produceState(initialValue = "", draft.ringtoneUri) {
        value = withContext(Dispatchers.IO) { ringtoneTitle(context, draft.ringtoneUri) }
    }
    OlafonCard {
        StatRow(
            stats = listOf(
                Stat(stringResource(R.string.edit_difficulty), draft.difficulty.label()) {
                    viewModel.onOptionCycle(AlarmOption.DIFFICULTY)
                },
                Stat(stringResource(R.string.edit_snooze), snoozesText(draft.maxSnoozes)) {
                    viewModel.onOptionCycle(AlarmOption.SNOOZES)
                },
                Stat(
                    stringResource(R.string.edit_volume),
                    stringResource(
                        if (draft.progressiveVolume) R.string.edit_volume_progressive else R.string.edit_volume_full,
                    ),
                ) { viewModel.onOptionCycle(AlarmOption.VOLUME) },
            ),
            modifier = Modifier.padding(bottom = 8.dp),
        )
        HorizontalDivider(color = OlafonColors.Outline)
        SettingRow(
            title = stringResource(R.string.edit_ringtone),
            subtitle = ringtoneTitle,
            onClick = {
                picker.launch(
                    Intent(RingtoneManager.ACTION_RINGTONE_PICKER)
                        .putExtra(RingtoneManager.EXTRA_RINGTONE_TYPE, RingtoneManager.TYPE_ALARM)
                        .putExtra(RingtoneManager.EXTRA_RINGTONE_SHOW_DEFAULT, true)
                        .putExtra(RingtoneManager.EXTRA_RINGTONE_SHOW_SILENT, false)
                        .putExtra(RingtoneManager.EXTRA_RINGTONE_TITLE, context.getString(R.string.edit_ringtone))
                        .putExtra(RingtoneManager.EXTRA_RINGTONE_EXISTING_URI, draft.ringtoneUri?.toUri()),
                )
            },
        )
        SettingRow(
            title = stringResource(R.string.edit_briefing),
            subtitle = stringResource(R.string.edit_briefing_subtitle),
            divider = false,
            trailing = { ToggleSwitch(checked = draft.briefingEnabled, onCheckedChange = viewModel::onBriefingToggle) },
        )
    }
}

@Composable
private fun snoozesText(count: Int): String = when (count) {
    0 -> stringResource(R.string.edit_snooze_none)
    1 -> stringResource(R.string.edit_snooze_once)
    else -> stringResource(R.string.edit_snooze_times, count)
}

private fun ringtoneTitle(context: Context, uri: String?): String {
    val resolved = uri?.toUri() ?: RingtoneManager.getActualDefaultRingtoneUri(context, RingtoneManager.TYPE_ALARM)
    val title = resolved?.let {
        runCatching { RingtoneManager.getRingtone(context, it)?.getTitle(context) }.getOrNull()
    }
    return if (uri ==
        null
    ) {
        context.getString(R.string.edit_ringtone_default, title.orEmpty()).trim()
    } else {
        title.orEmpty()
    }
}

private fun ChallengeType.icon(): Int = when (this) {
    ChallengeType.PATTERN -> DsR.drawable.ic_pattern
    ChallengeType.QUESTION -> DsR.drawable.ic_question
    ChallengeType.MATH -> DsR.drawable.ic_math
}

private fun ChallengeType.description(): Int = when (this) {
    ChallengeType.PATTERN -> R.string.challenge_pattern_desc
    ChallengeType.QUESTION -> R.string.challenge_question_desc
    ChallengeType.MATH -> R.string.challenge_math_desc
}

private const val MAX_HOUR = 23
private const val MAX_MINUTE = 59
