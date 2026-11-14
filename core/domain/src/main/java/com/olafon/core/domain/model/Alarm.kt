package com.olafon.core.domain.model

import java.time.DayOfWeek

/**
 * An alarm as the user configured it.
 *
 * [days] empty means the alarm rings once, at the next occurrence of [hour]:[minute].
 * [challenges] are solved in order before the alarm can be dismissed.
 */
data class Alarm(
    val id: Long = 0,
    val hour: Int,
    val minute: Int,
    val days: Set<DayOfWeek> = emptySet(),
    val label: String = "",
    val enabled: Boolean = true,
    val challenges: List<ChallengeType> = listOf(ChallengeType.PATTERN),
    val difficulty: Difficulty = Difficulty.MEDIUM,
    val ringtoneUri: String? = null,
    val progressiveVolume: Boolean = true,
    val maxSnoozes: Int = DEFAULT_MAX_SNOOZES,
    val briefingEnabled: Boolean = true,
) {
    init {
        require(hour in 0..MAX_HOUR) { "hour must be in 0..23, was $hour" }
        require(minute in 0..MAX_MINUTE) { "minute must be in 0..59, was $minute" }
        require(maxSnoozes >= 0) { "maxSnoozes must be >= 0, was $maxSnoozes" }
    }

    val repeats: Boolean get() = days.isNotEmpty()

    companion object {
        const val DEFAULT_MAX_SNOOZES = 3
        private const val MAX_HOUR = 23
        private const val MAX_MINUTE = 59
    }
}

enum class ChallengeType { PATTERN, QUESTION, MATH }

enum class Difficulty { EASY, MEDIUM, HARD }
