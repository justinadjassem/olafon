package com.olafon.feature.alarm.ring

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.olafon.core.domain.model.RingingAlarm
import com.olafon.feature.alarm.R
import kotlinx.coroutines.delay

/** Plain ring screen for the engine step; the lime design of step 4 and the challenges of step 3 replace it. */
@Composable
fun RingScreen(viewModel: RingViewModel, onFinished: () -> Unit) {
    val state by viewModel.state.collectAsStateWithLifecycle()

    LaunchedEffect(state.finished) {
        if (state.finished) onFinished()
    }
    // Opened by a stale notification while nothing rings any more: close rather than show a spinner forever.
    LaunchedEffect(state.ringing) {
        if (state.ringing == null) {
            delay(LOADING_TIMEOUT_MS)
            onFinished()
        }
    }

    Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
        val ringing = state.ringing
        if (ringing == null) {
            Loading(Modifier.padding(innerPadding))
        } else {
            RingContent(
                ringing = ringing,
                hasEmergencyCode = state.hasEmergencyCode,
                emergencyCodeRejected = state.emergencyCodeRejected,
                onSnooze = viewModel::onSnooze,
                onChallengesSolved = viewModel::onChallengesSolved,
                onEmergencyCode = viewModel::onEmergencyCode,
                onEmergencyCodeEdited = viewModel::onEmergencyCodeEdited,
                modifier = Modifier.padding(innerPadding),
            )
        }
    }
}

@Composable
private fun Loading(modifier: Modifier = Modifier) {
    Column(
        modifier = modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
    ) {
        CircularProgressIndicator()
        Spacer(Modifier.height(16.dp))
        Text(stringResource(R.string.ring_loading))
    }
}

@Composable
private fun RingContent(
    ringing: RingingAlarm,
    hasEmergencyCode: Boolean,
    emergencyCodeRejected: Boolean,
    onSnooze: () -> Unit,
    onChallengesSolved: () -> Unit,
    onEmergencyCode: (String) -> Unit,
    onEmergencyCodeEdited: () -> Unit,
    modifier: Modifier = Modifier,
) {
    var askingCode by rememberSaveable { mutableStateOf(false) }

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 24.dp, vertical = 32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.SpaceBetween,
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(
                text = "%02d:%02d".format(ringing.alarm.hour, ringing.alarm.minute),
                style = MaterialTheme.typography.displayLarge,
            )
            val label = ringing.alarm.label.ifBlank { stringResource(R.string.ring_title) }
            Text(text = label, style = MaterialTheme.typography.titleLarge)
        }

        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Button(onClick = onChallengesSolved, modifier = Modifier.fillMaxWidth()) {
                Text(stringResource(R.string.ring_start_challenges))
            }
            Spacer(Modifier.height(12.dp))
            OutlinedButton(
                onClick = onSnooze,
                enabled = ringing.canSnooze,
                modifier = Modifier.fillMaxWidth(),
            ) {
                val text = if (ringing.canSnooze) {
                    pluralStringResource(R.plurals.ring_snooze, ringing.snoozesLeft, ringing.snoozesLeft)
                } else {
                    stringResource(R.string.ring_snooze_exhausted)
                }
                Text(text)
            }
            if (hasEmergencyCode) {
                Spacer(Modifier.height(24.dp))
                TextButton(onClick = { askingCode = true }) {
                    Text(stringResource(R.string.ring_emergency_exit))
                }
            }
        }
    }

    if (askingCode) {
        EmergencyCodeDialog(
            rejected = emergencyCodeRejected,
            onEdited = onEmergencyCodeEdited,
            onConfirm = onEmergencyCode,
            onDismiss = { askingCode = false },
        )
    }
}

@Composable
private fun EmergencyCodeDialog(
    rejected: Boolean,
    onEdited: () -> Unit,
    onConfirm: (String) -> Unit,
    onDismiss: () -> Unit,
) {
    var code by rememberSaveable { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(stringResource(R.string.emergency_dialog_title)) },
        text = {
            Column {
                Text(stringResource(R.string.emergency_dialog_text))
                Spacer(Modifier.height(12.dp))
                OutlinedTextField(
                    value = code,
                    onValueChange = { value ->
                        code = value.filter(Char::isDigit).take(EMERGENCY_CODE_LENGTH)
                        onEdited()
                    },
                    singleLine = true,
                    isError = rejected,
                    supportingText = if (rejected) {
                        { Text(stringResource(R.string.emergency_code_rejected)) }
                    } else {
                        null
                    },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.NumberPassword),
                    visualTransformation = PasswordVisualTransformation(),
                )
            }
        },
        confirmButton = {
            TextButton(onClick = { onConfirm(code) }, enabled = code.length == EMERGENCY_CODE_LENGTH) {
                Text(stringResource(R.string.action_confirm))
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text(stringResource(R.string.action_cancel)) }
        },
    )
}

private const val LOADING_TIMEOUT_MS = 10_000L
private const val EMERGENCY_CODE_LENGTH = 6
