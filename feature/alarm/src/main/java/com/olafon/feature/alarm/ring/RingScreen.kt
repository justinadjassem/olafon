package com.olafon.feature.alarm.ring

import androidx.compose.foundation.background
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.olafon.core.designsystem.R as DsR
import com.olafon.core.designsystem.component.LocalOutlineColor
import com.olafon.core.designsystem.component.OlafonCard
import com.olafon.core.designsystem.component.PillButton
import com.olafon.core.designsystem.component.PillStyle
import com.olafon.core.designsystem.theme.OlafonColors
import com.olafon.core.domain.model.RingInterruption
import com.olafon.core.domain.model.RingingAlarm
import com.olafon.feature.alarm.R
import com.olafon.feature.challenges.session.ChallengeSession
import com.olafon.feature.challenges.session.label
import java.time.Duration
import java.time.ZoneId
import java.time.ZonedDateTime
import java.time.format.DateTimeFormatter
import java.util.Locale
import kotlinx.coroutines.delay

/** Screen 3 of the canvas, then the challenges (screens 4 and 5) and the success panel, in one activity. */
@Composable
fun RingScreen(viewModel: RingViewModel, onFinished: () -> Unit) {
    val state by viewModel.state.collectAsStateWithLifecycle()

    LaunchedEffect(state.finished) {
        if (state.finished) onFinished()
    }
    // Opened by a stale notification while nothing rings any more: close rather than show a spinner forever.
    LaunchedEffect(state.ringing, state.summary) {
        if (state.ringing == null && state.summary == null) {
            delay(LOADING_TIMEOUT_MS)
            onFinished()
        }
    }

    val ringing = state.ringing
    val summary = state.summary
    when {
        summary != null -> SuccessPanel(summary, onDone = viewModel::onFinish)
        ringing == null -> Loading()
        state.challengesStarted -> Challenges(
            ringing = ringing,
            hasEmergencyCode = state.hasEmergencyCode,
            emergencyCodeRejected = state.emergencyCodeRejected,
            onSolved = viewModel::onChallengesSolved,
            onEmergencyCode = viewModel::onEmergencyCode,
            onEmergencyCodeEdited = viewModel::onEmergencyCodeEdited,
        )
        else -> RingContent(
            ringing = ringing,
            hasEmergencyCode = state.hasEmergencyCode,
            emergencyCodeRejected = state.emergencyCodeRejected,
            onSnooze = viewModel::onSnooze,
            onStartChallenges = viewModel::onStartChallenges,
            onEmergencyCode = viewModel::onEmergencyCode,
            onEmergencyCodeEdited = viewModel::onEmergencyCodeEdited,
        )
    }
}

@Composable
private fun Loading() {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(OlafonColors.Lime),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
    ) {
        CircularProgressIndicator(color = OlafonColors.OnLime)
        Spacer(Modifier.height(16.dp))
        Text(stringResource(R.string.ring_loading), color = OlafonColors.OnLime)
    }
}

/** The lime screen: date, time, « Debout ! », the chain of challenges and the three ways out. */
@Composable
private fun RingContent(
    ringing: RingingAlarm,
    hasEmergencyCode: Boolean,
    emergencyCodeRejected: Boolean,
    onSnooze: () -> Unit,
    onStartChallenges: () -> Unit,
    onEmergencyCode: (String) -> Unit,
    onEmergencyCodeEdited: () -> Unit,
) {
    var askingCode by rememberSaveable { mutableStateOf(false) }

    CompositionLocalProvider(
        LocalContentColor provides OlafonColors.OnLime,
        LocalOutlineColor provides OlafonColors.OnLime,
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(OlafonColors.Lime)
                .statusBarsPadding()
                .navigationBarsPadding()
                .padding(start = 20.dp, end = 20.dp, top = 24.dp, bottom = 28.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Row(
                modifier = Modifier
                    .clip(CircleShape)
                    .background(OlafonColors.OnLime.copy(alpha = CHIP_ALPHA))
                    .padding(horizontal = 14.dp, vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                Icon(painterResource(DsR.drawable.ic_lock), contentDescription = null, modifier = Modifier.size(16.dp))
                Text(stringResource(R.string.ring_volume_locked), style = MaterialTheme.typography.labelMedium)
            }
            Spacer(Modifier.height(56.dp))
            Text(ringing.session.ringAt.dateText(), style = MaterialTheme.typography.titleMedium)
            Text(
                text = "%02d:%02d".format(ringing.alarm.hour, ringing.alarm.minute),
                style = MaterialTheme.typography.displayLarge,
            )
            Spacer(Modifier.height(20.dp))
            Text(
                ringing.alarm.label.ifBlank {
                    stringResource(R.string.ring_title)
                },
                style = MaterialTheme.typography.headlineLarge,
            )
            Spacer(Modifier.height(8.dp))
            Text(
                text = pluralChallenges(ringing.alarm.challenges.size),
                style = MaterialTheme.typography.bodyLarge,
                textAlign = TextAlign.Center,
            )
            ringing.session.interruption?.let { interruption ->
                Spacer(Modifier.height(8.dp))
                Text(
                    interruptionText(interruption),
                    style = MaterialTheme.typography.bodyMedium,
                    textAlign = TextAlign.Center,
                )
            }
            Spacer(Modifier.height(20.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                ringing.alarm.challenges.forEachIndexed { index, type ->
                    val first = index == 0
                    Text(
                        text = "${index + 1} · ${type.label()}",
                        style = MaterialTheme.typography.labelLarge,
                        color = if (first) OlafonColors.Lime else OlafonColors.OnLime,
                        modifier = Modifier
                            .clip(CircleShape)
                            .background(
                                if (first) OlafonColors.OnLime else OlafonColors.OnLime.copy(alpha = CHIP_ALPHA),
                            )
                            .padding(horizontal = 16.dp, vertical = 8.dp),
                    )
                }
            }
            Spacer(Modifier.weight(1f))
            PillButton(
                onClick = onStartChallenges,
                style = PillStyle.DARK,
                modifier = Modifier.fillMaxWidth(),
                height = 60,
            ) {
                Text(stringResource(R.string.ring_start_challenges), style = MaterialTheme.typography.titleMedium)
                Spacer(Modifier.size(8.dp))
                Icon(
                    painterResource(DsR.drawable.ic_arrow_right),
                    contentDescription = null,
                    modifier = Modifier.size(20.dp),
                )
            }
            Spacer(Modifier.height(10.dp))
            PillButton(
                text = if (ringing.canSnooze) {
                    stringResource(R.string.ring_snooze, ringing.snoozesLeft)
                } else {
                    stringResource(R.string.ring_snooze_exhausted)
                },
                onClick = onSnooze,
                style = PillStyle.OUTLINE,
                enabled = ringing.canSnooze,
                height = 54,
                modifier = Modifier.fillMaxWidth(),
            )
            if (hasEmergencyCode) {
                TextButton(onClick = { askingCode = true }) {
                    Text(stringResource(R.string.ring_emergency_exit), color = OlafonColors.OnLime)
                }
            }
        }
    }

    if (askingCode) {
        EmergencyCodeDialog(
            rejected = emergencyCodeRejected,
            onEdited = onEmergencyCodeEdited,
            onSubmit = onEmergencyCode,
            onDismiss = { askingCode = false },
        )
    }
}

/** Dark screen hosting the challenge chain; the emergency exit stays reachable at the bottom. */
@Composable
private fun Challenges(
    ringing: RingingAlarm,
    hasEmergencyCode: Boolean,
    emergencyCodeRejected: Boolean,
    onSolved: () -> Unit,
    onEmergencyCode: (String) -> Unit,
    onEmergencyCodeEdited: () -> Unit,
) {
    var askingCode by rememberSaveable { mutableStateOf(false) }
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(OlafonColors.Background)
            .statusBarsPadding()
            .navigationBarsPadding()
            .padding(horizontal = 12.dp, vertical = 12.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        ChallengeSession(
            types = ringing.alarm.challenges,
            difficulty = ringing.alarm.difficulty,
            sessionId = ringing.session.id,
            onAllSolved = onSolved,
            modifier = Modifier.weight(1f),
        )
        if (hasEmergencyCode) {
            TextButton(onClick = { askingCode = true }) {
                Text(stringResource(R.string.ring_emergency_exit), color = OlafonColors.Muted)
            }
        }
    }
    if (askingCode) {
        EmergencyCodeDialog(
            rejected = emergencyCodeRejected,
            onEdited = onEmergencyCodeEdited,
            onSubmit = onEmergencyCode,
            onDismiss = { askingCode = false },
        )
    }
}

/** Bottom of screen 5: « Tu es réveillé ! », how long it took, the streak, and the way out. */
@Composable
private fun SuccessPanel(summary: WakeSummary, onDone: () -> Unit) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(OlafonColors.Background)
            .statusBarsPadding()
            .navigationBarsPadding()
            .padding(12.dp),
        contentAlignment = Alignment.BottomCenter,
    ) {
        OlafonCard(
            color = OlafonColors.Lime,
            shape = MaterialTheme.shapes.extraLarge,
            contentPadding = PaddingValues(start = 18.dp, end = 18.dp, top = 30.dp, bottom = 18.dp),
        ) {
            CompositionLocalProvider(LocalContentColor provides OlafonColors.OnLime) {
                Column(Modifier.fillMaxWidth(), horizontalAlignment = Alignment.CenterHorizontally) {
                    Box(
                        Modifier
                            .size(104.dp)
                            .clip(CircleShape)
                            .background(OlafonColors.OnLime.copy(alpha = GLOW_ALPHA)),
                        contentAlignment = Alignment.Center,
                    ) {
                        Box(
                            Modifier
                                .size(76.dp)
                                .clip(CircleShape)
                                .background(OlafonColors.OnLime),
                            contentAlignment = Alignment.Center,
                        ) {
                            Icon(
                                painterResource(DsR.drawable.ic_check),
                                contentDescription = null,
                                tint = OlafonColors.Lime,
                                modifier = Modifier.size(38.dp),
                            )
                        }
                    }
                    Spacer(Modifier.height(16.dp))
                    Text(stringResource(R.string.ring_awake_title), style = MaterialTheme.typography.headlineMedium)
                    Spacer(Modifier.height(10.dp))
                    Text(
                        text = stringResource(R.string.ring_awake_duration, summary.duration.text()),
                        style = MaterialTheme.typography.bodyMedium,
                        textAlign = TextAlign.Center,
                    )
                    Text(
                        text = stringResource(R.string.ring_awake_streak, summary.streakDays),
                        style = MaterialTheme.typography.bodyMedium,
                        textAlign = TextAlign.Center,
                    )
                    Spacer(Modifier.height(20.dp))
                    PillButton(
                        text = stringResource(R.string.ring_awake_done),
                        onClick = onDone,
                        style = PillStyle.DARK,
                        modifier = Modifier.fillMaxWidth(),
                    )
                }
            }
        }
    }
}

@Composable
private fun pluralChallenges(count: Int): String =
    androidx.compose.ui.res.pluralStringResource(R.plurals.ring_challenge_count, count, count)

private fun java.time.Instant.dateText(): String {
    val text = ZonedDateTime.ofInstant(this, ZoneId.systemDefault())
        .format(DateTimeFormatter.ofPattern("EEEE d MMMM", Locale.FRENCH))
    return text.replaceFirstChar { it.titlecase(Locale.FRENCH) }
}

private fun Duration.text(): String {
    val minutes = toMinutes()
    val seconds = seconds % SECONDS_PER_MINUTE
    return if (minutes == 0L) "$seconds s" else "$minutes min $seconds s"
}

@Composable
private fun interruptionText(interruption: RingInterruption): String = if (interruption.cleanShutdown) {
    val time = interruption.at.atZone(ZoneId.systemDefault()).format(DateTimeFormatter.ofPattern("HH:mm"))
    stringResource(R.string.ring_interrupted_shutdown, time)
} else {
    stringResource(R.string.ring_interrupted_unknown)
}

private const val LOADING_TIMEOUT_MS = 10_000L
private const val CHIP_ALPHA = 0.1f
private const val GLOW_ALPHA = 0.12f
private const val SECONDS_PER_MINUTE = 60
