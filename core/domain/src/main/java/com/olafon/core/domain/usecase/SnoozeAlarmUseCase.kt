package com.olafon.core.domain.usecase

import com.olafon.core.domain.model.RingingAlarm
import com.olafon.core.domain.repository.WakeSessionRepository
import com.olafon.core.domain.scheduler.AlarmScheduler
import java.time.Clock
import java.time.Duration
import java.time.Instant
import javax.inject.Inject

/** Postpones the ring by [SNOOZE_DURATION]. Returns false when the alarm's snooze limit is reached. */
class SnoozeAlarmUseCase @Inject constructor(
    private val sessions: WakeSessionRepository,
    private val scheduler: AlarmScheduler,
    private val clock: Clock,
) {
    suspend operator fun invoke(ringing: RingingAlarm): Boolean {
        if (!ringing.canSnooze) return false
        sessions.recordSnooze(ringing.session.id)
        scheduler.scheduleSnooze(ringing.alarm, ringing.session.id, Instant.now(clock).plus(SNOOZE_DURATION))
        return true
    }

    companion object {
        val SNOOZE_DURATION: Duration = Duration.ofMinutes(5)
    }
}
