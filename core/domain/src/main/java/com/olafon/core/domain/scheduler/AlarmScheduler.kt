package com.olafon.core.domain.scheduler

import com.olafon.core.domain.model.Alarm
import java.time.Instant

/** Contract implemented by the platform alarm engine (`:feature:alarm`). */
interface AlarmScheduler {
    /** Arms the regular occurrence of [alarm]. Replaces any previous occurrence of the same alarm. */
    fun schedule(alarm: Alarm, triggerAt: Instant)

    fun cancel(alarmId: Long)

    /** Arms a snooze of [alarm] that resumes the wake session [sessionId]. Independent from the regular occurrence. */
    fun scheduleSnooze(alarm: Alarm, sessionId: Long, triggerAt: Instant)

    fun cancelSnooze(alarmId: Long)
}
