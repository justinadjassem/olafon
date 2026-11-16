package com.olafon.core.domain.model

/** An alarm currently ringing, together with the wake session that tracks this ring. */
data class RingingAlarm(val alarm: Alarm, val session: WakeSession) {
    val snoozesLeft: Int get() = (alarm.maxSnoozes - session.snoozes).coerceAtLeast(0)
    val canSnooze: Boolean get() = snoozesLeft > 0
}
