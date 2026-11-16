package com.olafon.feature.challenges.math

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
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
import com.olafon.core.designsystem.component.PillButton
import com.olafon.core.designsystem.theme.OlafonColors
import com.olafon.core.domain.challenge.MathChallenge
import com.olafon.feature.challenges.R

/** One operation typed on the numeric keypad. The same operation stays for a few tries, then a new one is drawn. */
@Composable
fun MathChallengeScreen(
    challenge: MathChallenge,
    lastAnswerWrong: Boolean,
    newAfterFailures: Boolean,
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
        Spacer(Modifier.height(32.dp))
        Text(
            text = "${challenge.left} ${challenge.operation.symbol} ${challenge.right}",
            style = MaterialTheme.typography.displayMedium,
            color = OlafonColors.Lime,
        )
        Spacer(Modifier.height(8.dp))
        val hint = when {
            newAfterFailures -> stringResource(R.string.math_new)
            lastAnswerWrong -> stringResource(R.string.math_wrong)
            else -> stringResource(R.string.math_hint)
        }
        Text(
            hint,
            style = MaterialTheme.typography.bodySmall,
            color = if (lastAnswerWrong) OlafonColors.Error else OlafonColors.Muted,
        )
        Spacer(Modifier.weight(1f))
        OutlinedTextField(
            value = input,
            onValueChange = { value ->
                input = value.filter(Char::isDigit)
                onEdited()
            },
            label = { Text(stringResource(R.string.math_answer)) },
            singleLine = true,
            textStyle = MaterialTheme.typography.headlineSmall,
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number, imeAction = ImeAction.Done),
            keyboardActions = KeyboardActions(onDone = { submit() }),
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
        Spacer(Modifier.height(12.dp))
        PillButton(
            text = stringResource(R.string.action_validate),
            onClick = submit,
            enabled = input.isNotEmpty(),
            height = 56,
            modifier = Modifier.fillMaxWidth(),
        )
    }
}
