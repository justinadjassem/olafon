package com.olafon.home

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.olafon.R
import com.olafon.core.domain.model.Alarm

/** Temporary home: lets you arm a test alarm and run the manual checklist on a real phone. Replaced in step 4. */
@Composable
fun TestAlarmScreen(viewModel: TestAlarmViewModel = hiltViewModel()) {
    val alarms by viewModel.alarms.collectAsStateWithLifecycle()

    Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = 20.dp, vertical = 16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Text(stringResource(R.string.test_home_title), style = MaterialTheme.typography.headlineMedium)
            Text(stringResource(R.string.test_home_intro), style = MaterialTheme.typography.bodyMedium)
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Button(onClick = { viewModel.addTestAlarm(1) }) { Text(stringResource(R.string.test_home_in_1_min)) }
                Button(onClick = { viewModel.addTestAlarm(2) }) { Text(stringResource(R.string.test_home_in_2_min)) }
            }
            LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                items(alarms, key = Alarm::id) { alarm ->
                    AlarmRow(
                        alarm = alarm,
                        onToggle = { viewModel.toggle(alarm, it) },
                        onDelete = { viewModel.delete(alarm) },
                    )
                }
            }
        }
    }
}

@Composable
private fun AlarmRow(alarm: Alarm, onToggle: (Boolean) -> Unit, onDelete: () -> Unit) {
    Card(modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 8.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Column {
                Text("%02d:%02d".format(alarm.hour, alarm.minute), style = MaterialTheme.typography.titleLarge)
                Text(alarm.label, style = MaterialTheme.typography.bodySmall)
            }
            Row(verticalAlignment = Alignment.CenterVertically) {
                TextButton(onClick = onDelete) { Text(stringResource(R.string.test_home_delete)) }
                Switch(checked = alarm.enabled, onCheckedChange = onToggle)
            }
        }
    }
}
