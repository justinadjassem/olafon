package com.olafon.feature.alarm.onboarding

import android.content.Intent
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.LifecycleResumeEffect
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.olafon.feature.alarm.R

/** First-launch checklist of the permissions a reliable alarm needs. [onDone] fires once the required ones are set. */
@Composable
fun OnboardingScreen(onDone: () -> Unit, viewModel: OnboardingViewModel = hiltViewModel()) {
    val status by viewModel.status.collectAsStateWithLifecycle()
    val context = LocalContext.current
    val permissions = viewModel.permissions

    LifecycleResumeEffect(Unit) {
        viewModel.refresh()
        onPauseOrDispose { }
    }
    val requestNotifications = rememberLauncherForActivityResult(ActivityResultContracts.RequestPermission()) {
        viewModel.refresh()
    }
    val open: (Intent?) -> Unit = { intent -> intent?.let(context::startActivity) }

    Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 20.dp, vertical = 16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Text(stringResource(R.string.onboarding_title), style = MaterialTheme.typography.headlineMedium)
            Text(stringResource(R.string.onboarding_intro), style = MaterialTheme.typography.bodyMedium)

            PermissionCard(
                title = stringResource(R.string.onboarding_notifications_title),
                text = stringResource(R.string.onboarding_notifications_text),
                granted = status.notifications,
                actionLabel = stringResource(R.string.onboarding_action_allow),
                onAction = {
                    if (permissions.notificationsNeedRuntimeRequest) {
                        requestNotifications.launch(android.Manifest.permission.POST_NOTIFICATIONS)
                    } else {
                        open(permissions.notificationSettingsIntent())
                    }
                },
            )
            PermissionCard(
                title = stringResource(R.string.onboarding_full_screen_title),
                text = stringResource(R.string.onboarding_full_screen_text),
                granted = status.fullScreenIntent,
                actionLabel = stringResource(R.string.onboarding_action_open_settings),
                onAction = { open(permissions.fullScreenSettingsIntent()) },
            )
            PermissionCard(
                title = stringResource(R.string.onboarding_exact_alarm_title),
                text = stringResource(R.string.onboarding_exact_alarm_text),
                granted = status.exactAlarms,
                actionLabel = stringResource(R.string.onboarding_action_open_settings),
                onAction = { open(permissions.exactAlarmSettingsIntent()) },
            )
            PermissionCard(
                title = stringResource(R.string.onboarding_battery_title),
                text = stringResource(R.string.onboarding_battery_text) + "\n\n" +
                    stringResource(permissions.batteryTipRes()),
                granted = status.batteryUnrestricted,
                actionLabel = stringResource(R.string.onboarding_action_open_settings),
                onAction = { open(permissions.batterySettingsIntent()) },
            )

            Spacer(Modifier.height(8.dp))
            Button(
                onClick = { viewModel.onContinue(onDone) },
                enabled = status.requiredGranted,
                modifier = Modifier.fillMaxWidth(),
            ) {
                Text(stringResource(R.string.onboarding_continue))
            }
        }
    }
}

@Composable
private fun PermissionCard(title: String, text: String, granted: Boolean, actionLabel: String, onAction: () -> Unit) {
    Card(modifier = Modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Text(title, style = MaterialTheme.typography.titleMedium)
                Text(
                    text = stringResource(
                        if (granted) R.string.onboarding_status_granted else R.string.onboarding_status_missing,
                    ),
                    style = MaterialTheme.typography.labelLarge,
                    color = if (granted) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.error,
                )
            }
            Text(text, style = MaterialTheme.typography.bodyMedium)
            if (!granted) {
                TextButton(onClick = onAction) { Text(actionLabel) }
            }
        }
    }
}
