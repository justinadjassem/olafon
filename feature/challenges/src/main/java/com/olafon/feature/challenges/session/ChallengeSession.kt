package com.olafon.feature.challenges.session

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.olafon.core.domain.challenge.MathChallenge
import com.olafon.core.domain.challenge.PatternChallenge
import com.olafon.core.domain.challenge.QuestionChallenge
import com.olafon.core.domain.model.ChallengeType
import com.olafon.core.domain.model.Difficulty
import com.olafon.feature.challenges.R
import com.olafon.feature.challenges.math.MathChallengeScreen
import com.olafon.feature.challenges.pattern.PatternChallengeScreen
import com.olafon.feature.challenges.question.QuestionChallengeScreen

/**
 * Plays the alarm's challenges one after the other and calls [onAllSolved] once the last one is passed. The host
 * (the ring screen) owns the dismissal, so the alarm keeps ringing until it decides otherwise.
 */
@Composable
fun ChallengeSession(
    types: List<ChallengeType>,
    difficulty: Difficulty,
    sessionId: Long,
    onAllSolved: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: ChallengeSessionViewModel = hiltViewModel(),
) {
    val state by viewModel.state.collectAsStateWithLifecycle()

    LaunchedEffect(Unit) { viewModel.start(types, difficulty, sessionId) }
    LaunchedEffect(state.completed) { if (state.completed) onAllSolved() }

    Column(modifier = modifier.fillMaxWidth(), horizontalAlignment = Alignment.CenterHorizontally) {
        val challenge = state.challenge
        if (challenge != null) {
            Text(
                text = stringResource(R.string.challenge_progress, state.step + 1, state.total) +
                    " · " + stringResource(challenge.type.label),
                style = MaterialTheme.typography.labelLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            Spacer(Modifier.height(16.dp))
        }
        when (challenge) {
            is PatternChallenge -> PatternChallengeScreen(
                challenge = challenge,
                failedAttempts = state.failedAttempts,
                lastAnswerWrong = state.lastAnswerWrong,
                onSubmit = viewModel::submitPattern,
            )
            is QuestionChallenge -> QuestionChallengeScreen(
                challenge = challenge,
                lastAnswerWrong = state.lastAnswerWrong,
                onChoice = viewModel::submitChoice,
            )
            is MathChallenge -> MathChallengeScreen(
                challenge = challenge,
                failedAttempts = state.failedAttempts,
                lastAnswerWrong = state.lastAnswerWrong,
                onSubmit = viewModel::submitMath,
                onEdited = viewModel::onWrongFeedbackShown,
            )
            null -> if (!state.completed) {
                CircularProgressIndicator()
                Spacer(Modifier.height(8.dp))
                Text(stringResource(R.string.challenge_loading))
            }
        }
    }
}

private val ChallengeType.label: Int
    get() = when (this) {
        ChallengeType.PATTERN -> R.string.challenge_type_pattern
        ChallengeType.QUESTION -> R.string.challenge_type_question
        ChallengeType.MATH -> R.string.challenge_type_math
    }
