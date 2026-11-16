package com.olafon.core.domain.usecase

import com.olafon.core.domain.model.Alarm
import com.olafon.core.domain.repository.AlarmRepository
import com.olafon.core.domain.scheduler.AlarmScheduler
import javax.inject.Inject

/** Persists an alarm, then schedules or cancels it with the platform. Returns the saved alarm with its id. */
class SaveAlarmUseCase @Inject constructor(
    private val repository: AlarmRepository,
    private val scheduler: AlarmScheduler,
    private val computeNextTrigger: ComputeNextTriggerUseCase,
) {
    suspend operator fun invoke(alarm: Alarm): Alarm {
        val id = repository.upsert(alarm)
        val saved = alarm.copy(id = id)
        if (saved.enabled) {
            scheduler.schedule(saved, computeNextTrigger(saved))
        } else {
            scheduler.cancel(id)
        }
        return saved
    }
}
