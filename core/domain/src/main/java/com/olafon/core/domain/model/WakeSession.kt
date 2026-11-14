package com.olafon.core.domain.model

import java.time.Instant

/** One ring of an alarm: when it rang, how the user fought it, and when it was finally dismissed. */
data class WakeSession(
    val id: Long = 0,
    val alarmId: Long,
    val ringAt: Instant,
    val dismissedAt: Instant? = null,
    val attempts: Int = 0,
    val snoozes: Int = 0,
    /** True when the ring was stopped with the emergency exit code instead of the challenges. */
    val emergencyExit: Boolean = false,
) {
    val dismissed: Boolean get() = dismissedAt != null
}
