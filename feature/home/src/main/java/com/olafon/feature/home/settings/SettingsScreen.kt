package com.olafon.feature.home.settings

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.olafon.core.designsystem.R as DsR
import com.olafon.core.designsystem.component.OlafonCard
import com.olafon.core.designsystem.component.PillButton
import com.olafon.core.designsystem.component.PillStyle
import com.olafon.core.designsystem.theme.OlafonColors
import com.olafon.feature.home.R

/** Settings: today only the emergency exit code. Theme and sources come with later steps. */
@Composable
fun SettingsScreen(onBack: () -> Unit, viewModel: SettingsViewModel = hiltViewModel()) {
    val hasCode by viewModel.hasEmergencyCode.collectAsStateWithLifecycle()
    var code by rememberSaveable { mutableStateOf("") }
    var rejected by rememberSaveable { mutableStateOf(false) }

    Column(
        Modifier
            .fillMaxSize()
            .background(OlafonColors.Background)
            .statusBarsPadding()
            .navigationBarsPadding()
            .padding(horizontal = 12.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp),
    ) {
        Row(
            Modifier
                .fillMaxWidth()
                .padding(horizontal = 8.dp, vertical = 8.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Box(
                Modifier
                    .size(44.dp)
                    .clip(MaterialTheme.shapes.small)
                    .background(OlafonColors.SurfaceHigh)
                    .clickable(onClick = onBack, role = Role.Button),
                contentAlignment = Alignment.Center,
            ) {
                Icon(
                    painterResource(DsR.drawable.ic_back),
                    contentDescription = stringResource(R.string.action_back),
                    modifier = Modifier.size(20.dp),
                )
            }
            Text(stringResource(R.string.settings_title), style = MaterialTheme.typography.titleMedium)
            Spacer(Modifier.size(44.dp))
        }
        OlafonCard {
            Text(stringResource(R.string.settings_emergency_title), style = MaterialTheme.typography.titleMedium)
            Spacer(Modifier.height(6.dp))
            Text(
                stringResource(R.string.settings_emergency_text),
                style = MaterialTheme.typography.bodyMedium,
                color = OlafonColors.Muted,
            )
            Spacer(Modifier.height(16.dp))
            Text(
                text = stringResource(
                    if (hasCode) R.string.settings_emergency_set else R.string.settings_emergency_unset,
                ),
                style = MaterialTheme.typography.labelMedium,
                color = if (hasCode) OlafonColors.Lime else OlafonColors.Muted,
            )
            Spacer(Modifier.height(12.dp))
            OutlinedTextField(
                value = code,
                onValueChange = { value ->
                    code = value.filter(Char::isDigit).take(SettingsViewModel.CODE_LENGTH)
                    rejected = false
                },
                label = { Text(stringResource(R.string.settings_emergency_new_code)) },
                singleLine = true,
                isError = rejected,
                supportingText = if (rejected) {
                    (
                        {
                            Text(stringResource(R.string.settings_emergency_invalid))
                        }
                        )
                } else {
                    null
                },
                visualTransformation = PasswordVisualTransformation(),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.NumberPassword),
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
                text = stringResource(R.string.settings_emergency_save),
                onClick = {
                    if (viewModel.onSetEmergencyCode(code)) code = "" else rejected = true
                },
                enabled = code.isNotEmpty(),
                modifier = Modifier.fillMaxWidth(),
            )
            if (hasCode) {
                Spacer(Modifier.height(8.dp))
                PillButton(
                    text = stringResource(R.string.settings_emergency_remove),
                    onClick = viewModel::onClearEmergencyCode,
                    style = PillStyle.OUTLINE,
                    modifier = Modifier.fillMaxWidth(),
                )
            }
        }
    }
}
