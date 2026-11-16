package com.olafon.core.domain.model

/** User-level settings. The emergency exit code itself is never exposed here, only whether one is set. */
data class UserPreferences(
    val firstLaunchDone: Boolean = false,
    val theme: ThemeMode = ThemeMode.SYSTEM,
    val hasEmergencyExitCode: Boolean = false,
    val eveningSyncHour: Int = DEFAULT_EVENING_SYNC_HOUR,
    /** Calendars of the phone left out of the briefing. Every other calendar is included. */
    val excludedCalendarIds: Set<Long> = emptySet(),
) {
    companion object {
        const val DEFAULT_EVENING_SYNC_HOUR = 22
    }
}

enum class ThemeMode { SYSTEM, LIGHT, DARK }
