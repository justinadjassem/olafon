package com.olafon.core.domain.usecase

import com.olafon.core.domain.model.RingInterruption
import com.olafon.core.domain.model.RingingAlarm
import com.olafon.core.domain.repository.WakeSessionRepository
import java.time.Clock
import java.time.Instant
import javax.inject.Inject

/** Called when the system announces a shutdown or reboot while an alarm rings. Must finish before the power goes. */
class RecordRingShutdownUseCase @Inject constructor(
    private val sessions: WakeSessionRepository,
    private val clock: Clock,
) {
    suspend operator fun invoke(ringing: RingingAlarm, batteryPercent: Int?) {
        sessions.recordInterruption(
            ringing.session.id,
            RingInterruption(at = Instant.now(clock), cleanShutdown = true, batteryPercent = batteryPercent),
        )
    }
}
