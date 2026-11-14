package com.olafon.core.domain.usecase

import com.olafon.core.domain.model.RingingAlarm
import com.olafon.core.domain.repository.PreferencesRepository
import com.olafon.core.domain.repository.WakeSessionRepository
import com.olafon.core.domain.scheduler.AlarmScheduler
import java.time.Clock
import java.time.Instant
import javax.inject.Inject

/** Ends a ring without solving the challenges. Returns false when [code] is not the emergency exit code. */
class EmergencyDismissUseCase @Inject constructor(
    private val preferences: PreferencesRepository,
    private val sessions: WakeSessionRepository,
    private val scheduler: AlarmScheduler,
    private val clock: Clock,
) {
    suspend operator fun invoke(ringing: RingingAlarm, code: String): Boolean {
        if (!preferences.verifyEmergencyExitCode(code)) return false
        sessions.dismiss(ringing.session.id, Instant.now(clock), emergency = true)
        scheduler.cancelSnooze(ringing.alarm.id)
        return true
    }
}
