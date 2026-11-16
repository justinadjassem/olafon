package com.olafon.core.domain.usecase

import com.olafon.core.domain.model.WakeSession
import com.olafon.core.domain.model.WakeStats
import java.time.Clock
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.ZonedDateTime
import java.time.temporal.TemporalAdjusters
import javax.inject.Inject

/**
 * Turns the recent wake sessions into the home figures. A day without any ring neither breaks nor extends the
 * streak: only days the alarm actually rang count, and the streak stops at the first of them that needed a snooze,
 * the emergency code, or is still open.
 */
class ComputeWakeStatsUseCase @Inject constructor(private val clock: Clock) {
    operator fun invoke(sessions: List<WakeSession>): WakeStats {
        val today = ZonedDateTime.now(clock).toLocalDate()
        val monday = today.with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY))
        val byDay = sessions.groupBy { it.ringAt.atZone(clock.zone).toLocalDate() }

        val thisWeek = byDay.filterKeys { it >= monday }.values.flatten()
        val streak = byDay.keys.sortedDescending().takeWhile { day -> byDay.getValue(day).all(::cleanWake) }.size

        return WakeStats(
            weekSuccess = thisWeek.count { it.dismissed && !it.emergencyExit },
            weekTotal = thisWeek.size,
            streakDays = streak,
        )
    }

    private fun cleanWake(session: WakeSession): Boolean =
        session.dismissed && !session.emergencyExit && session.snoozes == 0

    /** Days of history worth loading for these figures. */
    companion object {
        const val HISTORY_LIMIT = 60

        fun sinceDate(today: LocalDate): LocalDate = today.minusDays(HISTORY_LIMIT.toLong())
    }
}
