package com.olafon.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.olafon.core.domain.model.Alarm
import com.olafon.core.domain.repository.AlarmRepository
import com.olafon.core.domain.scheduler.AlarmScheduler
import com.olafon.core.domain.usecase.SaveAlarmUseCase
import com.olafon.core.domain.usecase.ToggleAlarmUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import java.time.Clock
import java.time.ZonedDateTime
import javax.inject.Inject
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

/** Temporary driver for the alarm engine until the real home screen of step 4. */
@HiltViewModel
class TestAlarmViewModel @Inject constructor(
    private val repository: AlarmRepository,
    private val saveAlarm: SaveAlarmUseCase,
    private val toggleAlarm: ToggleAlarmUseCase,
    private val scheduler: AlarmScheduler,
    private val clock: Clock,
) : ViewModel() {
    val alarms: StateFlow<List<Alarm>> = repository.observeAll()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(STOP_TIMEOUT_MS), emptyList())

    fun addTestAlarm(inMinutes: Long) {
        viewModelScope.launch {
            val at = ZonedDateTime.now(clock).plusMinutes(inMinutes)
            saveAlarm(Alarm(hour = at.hour, minute = at.minute, label = "Test $inMinutes min"))
        }
    }

    fun toggle(alarm: Alarm, enabled: Boolean) {
        viewModelScope.launch { toggleAlarm(alarm.id, enabled) }
    }

    fun delete(alarm: Alarm) {
        viewModelScope.launch {
            scheduler.cancel(alarm.id)
            repository.delete(alarm.id)
        }
    }

    private companion object {
        const val STOP_TIMEOUT_MS = 5_000L
    }
}
