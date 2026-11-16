package com.olafon.feature.challenges.pattern

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import com.olafon.core.designsystem.theme.OlafonColors
import com.olafon.core.domain.challenge.PatternChallenge
import com.olafon.feature.challenges.R
import kotlinx.coroutines.delay

/**
 * Screen 4 of the canvas. Phase 1 shows the pattern dot by dot for [PatternChallenge.showMillis] while a bar runs
 * down, then hides it. Phase 2 lets the user draw it back; the counter shows how many dots are joined. A new
 * [challenge] instance restarts from phase 1.
 */
@Composable
fun PatternChallengeScreen(
    challenge: PatternChallenge,
    lastAnswerWrong: Boolean,
    newAfterFailures: Boolean,
    onSubmit: (List<Int>) -> Unit,
    modifier: Modifier = Modifier,
) {
    // -1 = replay phase; otherwise how many dots of the pattern are revealed so far.
    var revealed by remember(challenge) { mutableIntStateOf(0) }
    var joined by remember(challenge) { mutableIntStateOf(0) }
    val memorizing = revealed >= 0
    val timer = remember(challenge) { Animatable(1f) }

    LaunchedEffect(challenge) {
        val stepMillis = challenge.showMillis / (challenge.sequence.size + 1)
        for (count in 1..challenge.sequence.size) {
            delay(stepMillis)
            revealed = count
        }
        delay(stepMillis)
        revealed = -1
    }
    LaunchedEffect(challenge) {
        timer.animateTo(0f, tween(challenge.showMillis.toInt(), easing = LinearEasing))
    }

    Column(modifier = modifier.fillMaxWidth(), horizontalAlignment = Alignment.CenterHorizontally) {
        Spacer(Modifier.height(24.dp))
        PatternPad(
            gridSize = challenge.gridSize,
            shown = if (memorizing) challenge.sequence.take(revealed) else null,
            onPathChange = { joined = it },
            onComplete = onSubmit,
            modifier = Modifier
                .size(300.dp)
                .padding(4.dp),
        )
        Spacer(Modifier.weight(1f))
        Row(
            Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.Bottom,
        ) {
            Text(
                text = stringResource(if (memorizing) R.string.pattern_memorize else R.string.pattern_replay),
                style = MaterialTheme.typography.headlineMedium,
            )
            Text(
                text = buildAnnotatedString {
                    withStyle(MaterialTheme.typography.displaySmall.toSpanStyle().copy(color = OlafonColors.Lime)) {
                        append((if (memorizing) revealed else joined).toString())
                    }
                    withStyle(MaterialTheme.typography.titleLarge.toSpanStyle().copy(color = OlafonColors.Muted)) {
                        append("/${challenge.sequence.size}")
                    }
                },
            )
        }
        Spacer(Modifier.height(6.dp))
        val hint = when {
            memorizing -> stringResource(R.string.pattern_hint_memorize, challenge.showMillis / MILLIS_PER_SECOND)
            newAfterFailures -> stringResource(R.string.pattern_new)
            lastAnswerWrong -> stringResource(R.string.pattern_wrong)
            else -> stringResource(R.string.pattern_hint_replay)
        }
        Text(
            text = hint,
            style = MaterialTheme.typography.bodySmall,
            color = if (!memorizing && lastAnswerWrong) OlafonColors.Error else OlafonColors.Muted,
        )
        Spacer(Modifier.height(12.dp))
        val fraction = if (memorizing) timer.value else joined.toFloat() / challenge.sequence.size
        Box(
            Modifier
                .fillMaxWidth()
                .height(6.dp)
                .clip(CircleShape)
                .background(OlafonColors.Outline),
        ) {
            Box(
                Modifier
                    .fillMaxWidth(fraction.coerceIn(0f, 1f))
                    .height(6.dp)
                    .clip(CircleShape)
                    .background(OlafonColors.Lime),
            )
        }
    }
}

private const val MILLIS_PER_SECOND = 1_000L
