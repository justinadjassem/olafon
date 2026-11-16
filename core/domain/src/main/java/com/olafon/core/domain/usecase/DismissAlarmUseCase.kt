package com.olafon.core.domain.usecase

import com.olafon.core.domain.model.RingingAlarm
import com.olafon.core.domain.repository.WakeSessionRepository
import com.olafon.core.domain.scheduler.AlarmScheduler
import java.time.Clock
import java.time.Instant
import javax.inject.Inject

/** Ends a ring once the challenges are solved. The next occurrence was already armed when the ring started. */
class DismissAlarmUseCase @Inject constructor(
    private val sessions: WakeSessionRepository,
    private val scheduler: AlarmScheduler,
    private val clock: Clock,
) {
    suspend operator fun invoke(ringing: RingingAlarm) {
        sessions.dismiss(ringing.session.id, Instant.now(clock))
        scheduler.cancelSnooze(ringing.alarm.id)
    }
}
