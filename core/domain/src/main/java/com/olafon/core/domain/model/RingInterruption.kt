package com.olafon.core.domain.model

import java.time.Instant

/**
 * A ring cut short before it was dismissed: the phone was powered off, rebooted or the process died. Recorded as a
 * fact, not a verdict; a reboot may be an escape as well as a flat battery or a system update.
 */
data class RingInterruption(
    /** When the system announced the shutdown, or when the ring resumed if no announcement was seen. */
    val at: Instant,
    /** True when the system announced a clean shutdown or reboot while the alarm rang. */
    val cleanShutdown: Boolean,
    /** Battery level at the shutdown announcement, to tell a flat battery from an escape. */
    val batteryPercent: Int? = null,
)
