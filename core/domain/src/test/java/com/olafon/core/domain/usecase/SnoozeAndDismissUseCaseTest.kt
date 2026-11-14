package com.olafon.core.domain.usecase

import com.olafon.core.domain.fake.FakePreferencesRepository
import com.olafon.core.domain.fake.FakeWakeSessionRepository
import com.olafon.core.domain.fake.RecordingScheduler
import com.olafon.core.domain.model.Alarm
import com.olafon.core.domain.model.RingingAlarm
import com.olafon.core.domain.model.WakeSession
import java.time.Clock
import java.time.Instant
import java.time.ZoneOffset
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class SnoozeAndDismissUseCaseTest {
    private val now: Instant = Instant.parse("2026-11-16T06:30:00Z")
    private val clock: Clock = Clock.fixed(now, ZoneOffset.UTC)
    private val sessions = FakeWakeSessionRepository()
    private val scheduler = RecordingScheduler()
    private val alarm = Alarm(id = 7, hour = 6, minute = 30, maxSnoozes = 2)

    private suspend fun ringing(snoozes: Int = 0): RingingAlarm {
        val id = sessions.start(alarm.id, now)
        repeat(snoozes) { sessions.recordSnooze(id) }
        return RingingAlarm(alarm, sessions.get(id)!!)
    }

    @Test
    fun `snooze records it and arms a snooze five minutes later`() = runTest {
        val ringing = ringing()

        val accepted = SnoozeAlarmUseCase(sessions, scheduler, clock)(ringing)

        assertTrue(accepted)
        assertEquals(1, sessions.get(ringing.session.id)!!.snoozes)
        assertEquals(ringing.session.id to now.plus(SnoozeAlarmUseCase.SNOOZE_DURATION), scheduler.snoozes[alarm.id])
    }

    @Test
    fun `snooze is refused once the per-alarm limit is reached`() = runTest {
        val ringing = ringing(snoozes = 2)

        val accepted = SnoozeAlarmUseCase(sessions, scheduler, clock)(ringing)

        assertFalse(accepted)
        assertFalse(ringing.canSnooze)
        assertNull(scheduler.snoozes[alarm.id])
    }

    @Test
    fun `dismiss closes the session and drops any pending snooze`() = runTest {
        val ringing = ringing()
        scheduler.scheduleSnooze(alarm, ringing.session.id, now)

        DismissAlarmUseCase(sessions, scheduler, clock)(ringing)

        val session: WakeSession = sessions.get(ringing.session.id)!!
        assertEquals(now, session.dismissedAt)
        assertFalse(session.emergencyExit)
        assertNull(scheduler.snoozes[alarm.id])
    }

    @Test
    fun `emergency dismiss requires the right code and marks the session`() = runTest {
        val ringing = ringing()
        val prefs = FakePreferencesRepository(emergencyCode = "123456")
        val emergency = EmergencyDismissUseCase(prefs, sessions, scheduler, clock)

        assertFalse(emergency(ringing, "000000"))
        assertFalse(sessions.get(ringing.session.id)!!.dismissed)

        assertTrue(emergency(ringing, "123456"))
        assertTrue(sessions.get(ringing.session.id)!!.emergencyExit)
    }

    @Test
    fun `emergency dismiss is impossible when no code is set`() = runTest {
        val ringing = ringing()
        val emergency = EmergencyDismissUseCase(FakePreferencesRepository(), sessions, scheduler, clock)

        assertFalse(emergency(ringing, ""))
    }
}
