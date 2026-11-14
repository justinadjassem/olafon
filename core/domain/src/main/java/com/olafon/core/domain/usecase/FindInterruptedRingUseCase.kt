package com.olafon.core.domain.usecase

import com.olafon.core.domain.model.WakeSession
import com.olafon.core.domain.repository.AlarmRepository
import com.olafon.core.domain.repository.WakeSessionRepository
import java.time.Clock
import java.time.Duration
import java.time.Instant
import javax.inject.Inject

/**
 * Finds a ring that was cut short by a power-off or a crash: a wake session that started recently and was never
 * dismissed, neither by the challenges nor by the emergency code. Turning the phone off is the one escape the
 * platform cannot block, so the engine rings again as soon as it boots.
 */
class FindInterruptedRingUseCase @Inject constructor(
    private val sessions: WakeSessionRepository,
    private val alarms: AlarmRepository,
    private val clock: Clock,
) {
    /** Returns the session to resume, or null when the last ring was dismissed, too old, or its alarm is gone. */
    suspend operator fun invoke(): WakeSession? {
        val session = sessions.latestOpenSince(Instant.now(clock).minus(MAX_AGE)) ?: return null
        return session.takeIf { alarms.get(it.alarmId) != null }
    }

    companion object {
        /** Past this delay the session is treated as abandoned rather than escaped. */
        val MAX_AGE: Duration = Duration.ofHours(2)
    }
}
