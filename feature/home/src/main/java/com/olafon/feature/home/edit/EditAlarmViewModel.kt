package com.olafon.feature.home.edit

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.olafon.core.domain.model.Alarm
import com.olafon.core.domain.model.ChallengeType
import com.olafon.core.domain.model.Difficulty
import com.olafon.core.domain.repository.AlarmRepository
import com.olafon.core.domain.scheduler.AlarmScheduler
import com.olafon.core.domain.usecase.SaveAlarmUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import java.time.DayOfWeek
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class EditAlarmUiState(
    /** Null until an existing alarm is loaded; a new alarm starts from [Alarm] defaults at 06:00. */
    val draft: Alarm? = null,
    val isNew: Boolean = true,
    /** True once saved or deleted: the screen closes. */
    val done: Boolean = false,
)

enum class AlarmOption { DIFFICULTY, SNOOZES, VOLUME }

/** Screen 2 of the canvas. The route argument [ARG_ALARM_ID] is 0 for a new alarm. */
@HiltViewModel
class EditAlarmViewModel @Inject constructor(
    savedState: SavedStateHandle,
    private val alarms: AlarmRepository,
    private val saveAlarm: SaveAlarmUseCase,
    private val scheduler: AlarmScheduler,
) : ViewModel() {
    private val alarmId: Long = savedState.get<Long>(ARG_ALARM_ID) ?: NEW_ALARM_ID
    private val _state = MutableStateFlow(EditAlarmUiState())
    val state: StateFlow<EditAlarmUiState> = _state.asStateFlow()

    init {
        viewModelScope.launch {
            val existing = alarmId.takeIf { it != NEW_ALARM_ID }?.let { alarms.get(it) }
            _state.update {
                it.copy(draft = existing ?: Alarm(hour = DEFAULT_HOUR, minute = 0), isNew = existing == null)
            }
        }
    }

    fun onTimeChange(hour: Int, minute: Int) = edit { copy(hour = hour, minute = minute) }

    fun onDayToggle(day: DayOfWeek) = edit { copy(days = if (day in days) days - day else days + day) }

    /** Adds the type at the end of the chain, or removes it. The last challenge cannot be removed. */
    fun onChallengeToggle(type: ChallengeType) = edit {
        when {
            type in challenges && challenges.size > 1 -> copy(challenges = challenges - type)
            type in challenges -> this
            else -> copy(challenges = challenges + type)
        }
    }

    /** The three stats of the editor cycle on tap: difficulty, snooze count, volume ramp. */
    fun onOptionCycle(option: AlarmOption) = edit {
        when (option) {
            AlarmOption.DIFFICULTY -> copy(difficulty = difficulty.next())
            AlarmOption.SNOOZES -> copy(maxSnoozes = (maxSnoozes + 1) % (MAX_SNOOZES + 1))
            AlarmOption.VOLUME -> copy(progressiveVolume = !progressiveVolume)
        }
    }

    fun onRingtoneChange(uri: String?) = edit { copy(ringtoneUri = uri) }

    fun onBriefingToggle(enabled: Boolean) = edit { copy(briefingEnabled = enabled) }

    fun onLabelChange(label: String) = edit { copy(label = label.take(MAX_LABEL)) }

    fun onSave() {
        val draft = _state.value.draft ?: return
        viewModelScope.launch {
            saveAlarm(draft.copy(enabled = true))
            _state.update { it.copy(done = true) }
        }
    }

    fun onDelete() {
        if (alarmId == NEW_ALARM_ID) return
        viewModelScope.launch {
            scheduler.cancel(alarmId)
            scheduler.cancelSnooze(alarmId)
            alarms.delete(alarmId)
            _state.update { it.copy(done = true) }
        }
    }

    private fun edit(change: Alarm.() -> Alarm) {
        _state.update { state -> state.copy(draft = state.draft?.change()) }
    }

    companion object {
        const val ARG_ALARM_ID = "alarmId"
        const val NEW_ALARM_ID = 0L
        private const val DEFAULT_HOUR = 6
        private const val MAX_SNOOZES = 3
        private const val MAX_LABEL = 40
    }
}

private fun Difficulty.next(): Difficulty = Difficulty.entries[(ordinal + 1) % Difficulty.entries.size]
