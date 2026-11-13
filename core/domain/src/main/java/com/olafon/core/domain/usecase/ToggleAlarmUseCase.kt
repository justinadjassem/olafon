package com.olafon.core.domain.usecase

import com.olafon.core.domain.repository.AlarmRepository
import javax.inject.Inject

/** Enables or disables an existing alarm. Returns false when the alarm does not exist. */
class ToggleAlarmUseCase @Inject constructor(
    private val repository: AlarmRepository,
    private val saveAlarm: SaveAlarmUseCase,
) {
    suspend operator fun invoke(alarmId: Long, enabled: Boolean): Boolean {
        val alarm = repository.get(alarmId) ?: return false
        if (alarm.enabled != enabled) saveAlarm(alarm.copy(enabled = enabled))
        return true
    }
}
