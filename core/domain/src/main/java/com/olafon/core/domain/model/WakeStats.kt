package com.olafon.core.domain.model

/** Figures for the home screen, computed from recent wake sessions. */
data class WakeStats(
    /** Rings of the current week (Monday to today) ended through the challenges. */
    val weekSuccess: Int = 0,
    val weekTotal: Int = 0,
    /** Consecutive wake days, most recent first, ended through the challenges without any snooze. */
    val streakDays: Int = 0,
)
