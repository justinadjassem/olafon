package com.olafon.feature.challenges.math

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.olafon.core.domain.challenge.MathChallenge
import com.olafon.feature.challenges.R
import com.olafon.feature.challenges.session.ChallengeSessionViewModel

/** One operation typed on the numeric keypad. The same operation stays for a few tries, then a new one is drawn. */
@Composable
fun MathChallengeScreen(
    challenge: MathChallenge,
    failedAttempts: Int,
    lastAnswerWrong: Boolean,
    onSubmit: (Int?) -> Unit,
    onEdited: () -> Unit,
    modifier: Modifier = Modifier,
) {
    var input by remember(challenge) { mutableStateOf("") }
    val submit = {
        onSubmit(input.toIntOrNull())
        input = ""
    }

    Column(modifier = modifier.fillMaxWidth(), horizontalAlignment = Alignment.CenterHorizontally) {
        Text(
            text = "${challenge.left} ${challenge.operation.symbol} ${challenge.right} = ?",
            style = MaterialTheme.typography.displayMedium,
        )
        Spacer(Modifier.height(8.dp))
        val feedback = when {
            lastAnswerWrong && failedAttempts == 0 -> stringResource(R.string.math_new)
            lastAnswerWrong -> stringResource(
                R.string.math_wrong,
                ChallengeSessionViewModel.MAX_ATTEMPTS - failedAttempts,
            )
            else -> ""
        }
        Text(text = feedback, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.error)
        Spacer(Modifier.height(16.dp))
        OutlinedTextField(
            value = input,
            onValueChange = { value ->
                input = value.filter(Char::isDigit)
                onEdited()
            },
            label = { Text(stringResource(R.string.math_answer)) },
            singleLine = true,
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number, imeAction = ImeAction.Done),
            keyboardActions = KeyboardActions(onDone = { submit() }),
            modifier = Modifier.fillMaxWidth(),
        )
        Spacer(Modifier.height(16.dp))
        Button(onClick = submit, enabled = input.isNotEmpty(), modifier = Modifier.fillMaxWidth()) {
            Text(stringResource(R.string.action_validate))
        }
    }
}
