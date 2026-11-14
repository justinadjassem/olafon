package com.olafon.core.domain.usecase

import com.olafon.core.domain.fake.FakeAlarmRepository
import com.olafon.core.domain.fake.FakeWakeSessionRepository
import com.olafon.core.domain.fake.RecordingScheduler
import com.olafon.core.domain.model.Alarm
import java.time.Clock
import java.time.DayOfWeek
import java.time.Instant
import java.time.ZoneId
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class StartRingingUseCaseTest {
    private val zone: ZoneId = ZoneId.of("Africa/Lome")
    private val ringInstant: Instant = Instant.parse("2026-11-16T06:30:00Z") // lundi 06:30 à Lomé
    private val clock: Clock = Clock.fixed(ringInstant, zone)
    private val sessions = FakeWakeSessionRepository()
    private val scheduler = RecordingScheduler()

    private fun useCase(vararg alarms: Alarm): Pair<StartRingingUseCase, FakeAlarmRepository> {
        val repository = FakeAlarmRepository(alarms.toList())
        return StartRingingUseCase(repository, sessions, scheduler, ComputeNextTriggerUseCase(clock), clock) to
            repository
    }

    @Test
    fun `opens a session and disables a one-shot alarm`() = runTest {
        val (start, alarms) = useCase(Alarm(id = 1, hour = 6, minute = 30))

        val ringing = start(1)

        assertNotNull(ringing)
        assertEquals(1L, ringing!!.session.id)
        assertEquals(ringInstant, ringing.session.ringAt)
        assertFalse(alarms.get(1)!!.enabled)
        assertTrue(scheduler.scheduled.isEmpty())
    }

    @Test
    fun `arms the next occurrence of a repeating alarm for the following day`() = runTest {
        val everyDay = DayOfWeek.entries.toSet()
        val (start, alarms) = useCase(Alarm(id = 2, hour = 6, minute = 30, days = everyDay))

        start(2)

        assertTrue(alarms.get(2)!!.enabled)
        assertEquals(Instant.parse("2026-11-17T06:30:00Z"), scheduler.scheduled[2])
    }

    @Test
    fun `resuming after a snooze reuses the session and does not re-arm`() = runTest {
        val (start, _) = useCase(Alarm(id = 3, hour = 6, minute = 30, days = DayOfWeek.entries.toSet()))
        val first = start(3)!!
        scheduler.scheduled.clear()
        sessions.recordSnooze(first.session.id)

        val resumed = start(3, sessionId = first.session.id)!!

        assertEquals(first.session.id, resumed.session.id)
        assertEquals(1, resumed.session.snoozes)
        assertTrue(scheduler.scheduled.isEmpty())
    }

    @Test
    fun `returns null for a deleted alarm`() = runTest {
        val (start, _) = useCase()

        assertNull(start(99))
        assertTrue(sessions.sessions.isEmpty())
    }
}
