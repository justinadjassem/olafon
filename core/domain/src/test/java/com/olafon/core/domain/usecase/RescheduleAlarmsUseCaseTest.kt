package com.olafon.core.domain.usecase

import com.olafon.core.domain.fake.FakeAlarmRepository
import com.olafon.core.domain.fake.RecordingScheduler
import com.olafon.core.domain.model.Alarm
import java.time.Clock
import java.time.Instant
import java.time.ZoneId
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class RescheduleAlarmsUseCaseTest {
    private val clock: Clock = Clock.fixed(Instant.parse("2026-11-16T05:00:00Z"), ZoneId.of("Africa/Lome"))
    private val scheduler = RecordingScheduler()

    @Test
    fun `arms every enabled alarm and skips disabled ones`() = runTest {
        val alarms = FakeAlarmRepository(
            listOf(
                Alarm(id = 1, hour = 6, minute = 0),
                Alarm(id = 2, hour = 7, minute = 0, enabled = false),
                Alarm(id = 3, hour = 4, minute = 0),
            ),
        )

        val count = RescheduleAlarmsUseCase(alarms, scheduler, ComputeNextTriggerUseCase(clock))()

        assertEquals(2, count)
        assertEquals(Instant.parse("2026-11-16T06:00:00Z"), scheduler.scheduled[1])
        assertNull(scheduler.scheduled[2])
        assertEquals(Instant.parse("2026-11-17T04:00:00Z"), scheduler.scheduled[3])
    }
}
