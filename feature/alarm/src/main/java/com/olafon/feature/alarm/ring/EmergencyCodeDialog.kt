package com.olafon.feature.alarm.ring

import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import com.olafon.core.designsystem.theme.OlafonColors
import com.olafon.feature.alarm.R

/** Asks the six-digit emergency exit code. A wrong code keeps the dialog open with an error. */
@Composable
internal fun EmergencyCodeDialog(
    rejected: Boolean,
    onEdited: () -> Unit,
    onSubmit: (String) -> Unit,
    onDismiss: () -> Unit,
) {
    var code by rememberSaveable { mutableStateOf("") }
    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = OlafonColors.Surface,
        titleContentColor = OlafonColors.OnSurface,
        textContentColor = OlafonColors.Muted,
        title = { Text(stringResource(R.string.emergency_dialog_title)) },
        text = {
            androidx.compose.foundation.layout.Column {
                Text(stringResource(R.string.emergency_dialog_text))
                OutlinedTextField(
                    value = code,
                    onValueChange = { value ->
                        code = value.filter(Char::isDigit).take(CODE_LENGTH)
                        onEdited()
                    },
                    singleLine = true,
                    isError = rejected,
                    supportingText = if (rejected) {
                        (
                            {
                                Text(stringResource(R.string.emergency_code_rejected))
                            }
                            )
                    } else {
                        null
                    },
                    visualTransformation = PasswordVisualTransformation(),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.NumberPassword),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = OlafonColors.Lime,
                        unfocusedBorderColor = OlafonColors.Outline,
                        cursorColor = OlafonColors.Lime,
                        focusedTextColor = OlafonColors.OnSurface,
                        unfocusedTextColor = OlafonColors.OnSurface,
                    ),
                )
            }
        },
        confirmButton = {
            TextButton(onClick = { onSubmit(code) }, enabled = code.length == CODE_LENGTH) {
                Text(stringResource(R.string.action_confirm), color = OlafonColors.Lime)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text(stringResource(R.string.action_cancel), color = OlafonColors.Muted) }
        },
    )
}

private const val CODE_LENGTH = 6
