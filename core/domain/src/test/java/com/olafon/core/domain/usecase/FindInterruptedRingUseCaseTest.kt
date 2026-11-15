package com.olafon.core.domain.usecase

import com.olafon.core.domain.fake.FakeAlarmRepository
import com.olafon.core.domain.fake.FakeWakeSessionRepository
import com.olafon.core.domain.model.Alarm
import java.time.Clock
import java.time.Duration
import java.time.Instant
import java.time.ZoneOffset
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class FindInterruptedRingUseCaseTest {
    private val now: Instant = Instant.parse("2026-11-16T06:30:00Z")
    private val clock: Clock = Clock.fixed(now, ZoneOffset.UTC)
    private val sessions = FakeWakeSessionRepository()
    private val alarms = FakeAlarmRepository(listOf(Alarm(id = 7, hour = 6, minute = 0)))
    private val find = FindInterruptedRingUseCase(sessions, alarms, clock)

    @Test
    fun `returns the open session of a ring cut short a few minutes ago`() = runTest {
        val id = sessions.start(alarmId = 7, ringAt = now.minus(Duration.ofMinutes(12)))

        assertEquals(id, find()?.id)
    }

    @Test
    fun `ignores a session that was dismissed`() = runTest {
        val id = sessions.start(alarmId = 7, ringAt = now.minus(Duration.ofMinutes(12)))
        sessions.dismiss(id, now.minus(Duration.ofMinutes(5)))

        assertNull(find())
    }

    @Test
    fun `ignores a session older than the resume window`() = runTest {
        sessions.start(alarmId = 7, ringAt = now.minus(FindInterruptedRingUseCase.MAX_AGE).minusSeconds(1))

        assertNull(find())
    }

    @Test
    fun `ignores a session whose alarm was deleted`() = runTest {
        sessions.start(alarmId = 99, ringAt = now.minus(Duration.ofMinutes(1)))

        assertNull(find())
    }

    @Test
    fun `picks the most recent open session when several are left`() = runTest {
        sessions.start(alarmId = 7, ringAt = now.minus(Duration.ofMinutes(40)))
        val latest = sessions.start(alarmId = 7, ringAt = now.minus(Duration.ofMinutes(3)))

        assertEquals(latest, find()?.id)
    }
}
