package com.olafon.core.domain.scheduler

import com.olafon.core.domain.model.Alarm
import java.time.Instant

/** Contract implemented by the platform alarm engine (`:feature:alarm`). */
interface AlarmScheduler {
    fun schedule(alarm: Alarm, triggerAt: Instant)

    fun cancel(alarmId: Long)
}
