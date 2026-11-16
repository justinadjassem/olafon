package com.olafon.feature.challenges.session

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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.olafon.core.designsystem.R as DsR
import com.olafon.core.designsystem.theme.OlafonColors
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
 * (the ring screen) owns the dismissal, so the alarm keeps ringing until it decides otherwise. Screens 4 and 5 of
 * the canvas: a dark card with the « Défi n / N » chip and « Ça sonne toujours », then the attempt counter.
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

    val challenge = state.challenge
    Column(modifier = modifier.fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(10.dp)) {
        Column(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
                .clip(CardShape)
                .background(OlafonColors.Surface)
                .padding(start = 22.dp, end = 22.dp, top = 20.dp, bottom = 26.dp),
        ) {
            Row(
                Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(
                    text = stringResource(R.string.challenge_progress, state.step + 1, state.total),
                    style = MaterialTheme.typography.labelLarge,
                    modifier = Modifier
                        .clip(CircleShape)
                        .background(OlafonColors.Background)
                        .padding(horizontal = 14.dp, vertical = 9.dp),
                )
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                ) {
                    Icon(
                        painterResource(DsR.drawable.ic_volume),
                        contentDescription = null,
                        tint = OlafonColors.Lime,
                        modifier = Modifier.size(16.dp),
                    )
                    Text(
                        stringResource(R.string.challenge_still_ringing),
                        style = MaterialTheme.typography.labelMedium,
                        color = OlafonColors.Lime,
                    )
                }
            }
            when (challenge) {
                is PatternChallenge -> PatternChallengeScreen(
                    challenge = challenge,
                    lastAnswerWrong = state.lastAnswerWrong,
                    newAfterFailures = state.lastAnswerWrong && state.failedAttempts == 0,
                    onSubmit = viewModel::submitPattern,
                    modifier = Modifier.weight(1f),
                )
                is QuestionChallenge -> QuestionChallengeScreen(
                    challenge = challenge,
                    lastAnswerWrong = state.lastAnswerWrong,
                    onChoice = viewModel::submitChoice,
                    modifier = Modifier.weight(1f),
                )
                is MathChallenge -> MathChallengeScreen(
                    challenge = challenge,
                    lastAnswerWrong = state.lastAnswerWrong,
                    newAfterFailures = state.lastAnswerWrong && state.failedAttempts == 0,
                    onSubmit = viewModel::submitMath,
                    onEdited = viewModel::onWrongFeedbackShown,
                    modifier = Modifier.weight(1f),
                )
                null -> if (!state.completed) {
                    Box(Modifier.weight(1f).fillMaxWidth(), contentAlignment = Alignment.Center) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            CircularProgressIndicator(color = OlafonColors.Lime)
                            Spacer(Modifier.height(8.dp))
                            Text(stringResource(R.string.challenge_loading), color = OlafonColors.Muted)
                        }
                    }
                }
            }
        }
        if (challenge != null && challenge !is QuestionChallenge) {
            Box(
                Modifier
                    .fillMaxWidth()
                    .clip(CardShape)
                    .background(OlafonColors.Surface)
                    .padding(14.dp),
                contentAlignment = Alignment.Center,
            ) {
                Text(
                    text = buildAnnotatedString {
                        append(stringResource(R.string.challenge_attempt_prefix))
                        withStyle(MaterialTheme.typography.labelLarge.toSpanStyle().copy(color = OlafonColors.Lime)) {
                            append(" ${state.failedAttempts + 1} ")
                        }
                        append(
                            stringResource(R.string.challenge_attempt_suffix, ChallengeSessionViewModel.MAX_ATTEMPTS),
                        )
                    },
                    style = MaterialTheme.typography.labelMedium,
                    color = OlafonColors.Muted,
                )
            }
        }
    }
}

/** Short name of a challenge type: « Schéma », « Question », « Calcul ». */
@Composable
fun ChallengeType.label(): String = stringResource(
    when (this) {
        ChallengeType.PATTERN -> R.string.challenge_type_pattern
        ChallengeType.QUESTION -> R.string.challenge_type_question
        ChallengeType.MATH -> R.string.challenge_type_math
    },
)

private val CardShape = RoundedCornerShape(36.dp)
