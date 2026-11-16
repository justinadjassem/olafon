package com.olafon.core.domain.usecase

import com.olafon.core.domain.model.WakeSession
import com.olafon.core.domain.model.WakeStats
import java.time.Clock
import java.time.Duration
import java.time.Instant
import java.time.ZoneOffset
import org.junit.Assert.assertEquals
import org.junit.Test

class ComputeWakeStatsUseCaseTest {
    // Thursday 19 Nov 2026, 09:00 UTC.
    private val now: Instant = Instant.parse("2026-11-19T09:00:00Z")
    private val stats = ComputeWakeStatsUseCase(Clock.fixed(now, ZoneOffset.UTC))

    private fun session(daysAgo: Long, dismissed: Boolean = true, snoozes: Int = 0, emergency: Boolean = false) =
        WakeSession(
            id = daysAgo,
            alarmId = 1,
            ringAt = now.minus(Duration.ofDays(daysAgo)),
            dismissedAt = if (dismissed) now.minus(Duration.ofDays(daysAgo)).plusSeconds(60) else null,
            snoozes = snoozes,
            emergencyExit = emergency,
        )

    @Test
    fun `counts this week from monday and the streak of clean wakes`() {
        val sessions = listOf(
            session(0),
            session(1),
            session(2, snoozes = 1),
            session(3, emergency = true),
            session(4),
            session(8),
        )

        assertEquals(WakeStats(weekSuccess = 3, weekTotal = 4, streakDays = 2), stats(sessions))
    }

    @Test
    fun `a day without a ring does not break the streak, an open session does`() {
        assertEquals(5, stats(listOf(session(0), session(1), session(3), session(4), session(10))).streakDays)
        assertEquals(0, stats(listOf(session(0, dismissed = false), session(1))).streakDays)
    }

    @Test
    fun `empty history gives zeros`() {
        assertEquals(WakeStats(), stats(emptyList()))
    }
}
