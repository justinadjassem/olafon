package com.olafon.core.domain.usecase

import com.olafon.core.domain.repository.AlarmRepository
import com.olafon.core.domain.scheduler.AlarmScheduler
import javax.inject.Inject

/**
 * Re-arms every enabled alarm. The platform drops scheduled alarms on reboot and on app update, and a time or
 * time zone change moves the wall-clock instant they must fire at.
 */
class RescheduleAlarmsUseCase @Inject constructor(
    private val alarms: AlarmRepository,
    private val scheduler: AlarmScheduler,
    private val computeNextTrigger: ComputeNextTriggerUseCase,
) {
    /** Returns the number of alarms armed. */
    suspend operator fun invoke(): Int {
        val enabled = alarms.enabled()
        enabled.forEach { scheduler.schedule(it, computeNextTrigger(it)) }
        return enabled.size
    }
}
