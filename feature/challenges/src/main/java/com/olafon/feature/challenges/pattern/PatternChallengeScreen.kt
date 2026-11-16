package com.olafon.feature.challenges.pattern

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
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
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.olafon.core.domain.challenge.PatternChallenge
import com.olafon.feature.challenges.R
import com.olafon.feature.challenges.session.ChallengeSessionViewModel
import kotlinx.coroutines.delay

/**
 * Phase 1 shows the pattern dot by dot for [PatternChallenge.showMillis], then hides it. Phase 2 lets the user
 * draw it back. A new [challenge] instance restarts from phase 1.
 */
@Composable
fun PatternChallengeScreen(
    challenge: PatternChallenge,
    failedAttempts: Int,
    lastAnswerWrong: Boolean,
    onSubmit: (List<Int>) -> Unit,
    modifier: Modifier = Modifier,
) {
    // -1 = replay phase; otherwise how many dots of the pattern are revealed so far.
    var revealed by remember(challenge) { mutableIntStateOf(0) }
    val memorizing = revealed >= 0

    LaunchedEffect(challenge) {
        val stepMillis = challenge.showMillis / (challenge.sequence.size + 1)
        for (count in 1..challenge.sequence.size) {
            delay(stepMillis)
            revealed = count
        }
        delay(stepMillis)
        revealed = -1
    }

    Column(modifier = modifier.fillMaxWidth(), horizontalAlignment = Alignment.CenterHorizontally) {
        Text(
            text = stringResource(if (memorizing) R.string.pattern_memorize else R.string.pattern_replay),
            style = MaterialTheme.typography.titleMedium,
        )
        Spacer(Modifier.height(4.dp))
        val feedback = when {
            memorizing -> ""
            lastAnswerWrong && failedAttempts == 0 -> stringResource(R.string.pattern_new)
            lastAnswerWrong -> stringResource(
                R.string.pattern_wrong,
                ChallengeSessionViewModel.MAX_ATTEMPTS - failedAttempts,
            )
            else -> ""
        }
        Text(
            text = feedback,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.error,
        )
        Spacer(Modifier.height(16.dp))
        PatternPad(
            gridSize = challenge.gridSize,
            shown = if (memorizing) challenge.sequence.take(revealed) else null,
            onComplete = onSubmit,
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp),
        )
    }
}
