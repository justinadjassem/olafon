package com.olafon.core.domain.usecase

import com.olafon.core.domain.model.Alarm
import com.olafon.core.domain.model.RingingAlarm
import com.olafon.core.domain.model.WakeSession
import com.olafon.core.domain.repository.AlarmRepository
import com.olafon.core.domain.repository.WakeSessionRepository
import com.olafon.core.domain.scheduler.AlarmScheduler
import java.time.Clock
import java.time.Instant
import java.time.ZonedDateTime
import javax.inject.Inject

/**
 * Called by the alarm engine the moment an alarm fires. Opens the wake session and immediately arms the next
 * occurrence (or disables a one-shot alarm), so a crash or a reboot during the ring never loses tomorrow's alarm.
 */
class StartRingingUseCase @Inject constructor(
    private val alarms: AlarmRepository,
    private val sessions: WakeSessionRepository,
    private val scheduler: AlarmScheduler,
    private val computeNextTrigger: ComputeNextTriggerUseCase,
    private val clock: Clock,
) {
    /** [sessionId] is set when the ring resumes after a snooze. Returns null when the alarm no longer exists. */
    suspend operator fun invoke(alarmId: Long, sessionId: Long? = null): RingingAlarm? {
        val alarm = alarms.get(alarmId) ?: return null
        val resumed = sessionId?.let { sessions.get(it) }
        return RingingAlarm(alarm, resumed ?: openSession(alarm))
    }

    private suspend fun openSession(alarm: Alarm): WakeSession {
        val now = Instant.now(clock)
        val id = sessions.start(alarm.id, now)
        armNextOccurrence(alarm, now)
        return WakeSession(id = id, alarmId = alarm.id, ringAt = now)
    }

    private suspend fun armNextOccurrence(alarm: Alarm, now: Instant) {
        if (alarm.repeats) {
            // The platform may fire a few milliseconds early: skip the occurrence that is ringing right now.
            val from = ZonedDateTime.ofInstant(now, clock.zone).plusMinutes(1)
            scheduler.schedule(alarm, computeNextTrigger(alarm, from))
        } else {
            alarms.upsert(alarm.copy(enabled = false))
        }
    }
}
