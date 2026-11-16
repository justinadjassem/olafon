package com.olafon.feature.challenges.question

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.olafon.core.domain.challenge.QuestionChallenge
import com.olafon.feature.challenges.R

/** One multiple-choice question. A wrong choice is replaced by a fresh question by the session. */
@Composable
fun QuestionChallengeScreen(
    challenge: QuestionChallenge,
    lastAnswerWrong: Boolean,
    onChoice: (Int) -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(modifier = modifier.fillMaxWidth(), horizontalAlignment = Alignment.CenterHorizontally) {
        if (lastAnswerWrong) {
            Text(
                text = stringResource(R.string.question_wrong),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.error,
            )
            Spacer(Modifier.height(8.dp))
        }
        Text(
            text = challenge.prompt,
            style = MaterialTheme.typography.titleLarge,
            textAlign = TextAlign.Center,
        )
        Spacer(Modifier.height(24.dp))
        challenge.choices.forEachIndexed { index, choice ->
            OutlinedButton(onClick = { onChoice(index) }, modifier = Modifier.fillMaxWidth()) {
                Text(choice)
            }
            Spacer(Modifier.height(8.dp))
        }
    }
}
