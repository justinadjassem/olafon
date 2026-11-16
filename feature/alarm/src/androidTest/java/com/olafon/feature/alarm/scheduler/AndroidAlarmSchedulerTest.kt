package com.olafon.feature.alarm.scheduler

import android.app.AlarmManager
import android.content.Context
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.olafon.core.domain.model.Alarm
import java.time.Instant
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotEquals
import org.junit.Assume.assumeTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

/**
 * Runs against the real AlarmManager. `nextAlarmClock` is system-wide, so the test arms alarms a few seconds ahead
 * to be the earliest alarm clock on the device.
 */
@RunWith(AndroidJUnit4::class)
class AndroidAlarmSchedulerTest {
    private val context: Context = ApplicationProvider.getApplicationContext()
    private val alarmManager: AlarmManager = context.getSystemService(AlarmManager::class.java)
    private val scheduler = AndroidAlarmScheduler(context)
    private val alarm = Alarm(id = TEST_ALARM_ID, hour = 7, minute = 0)

    @Before
    fun requireExactAlarms() {
        assumeTrue("exact alarms must be allowed on the test device", alarmManager.canScheduleExactAlarms())
    }

    @After
    fun cleanUp() {
        scheduler.cancel(TEST_ALARM_ID)
        scheduler.cancelSnooze(TEST_ALARM_ID)
    }

    @Test
    fun scheduleRegistersAnAlarmClockAtTheRequestedInstant() {
        val triggerAt = soon()

        scheduler.schedule(alarm, triggerAt)

        assertEquals(triggerAt.toEpochMilli(), alarmManager.nextAlarmClock?.triggerTime)
    }

    @Test
    fun rescheduleReplacesThePreviousOccurrence() {
        val first = soon()
        val second = first.plusSeconds(5)
        scheduler.schedule(alarm, first)

        scheduler.schedule(alarm, second)

        assertEquals(second.toEpochMilli(), alarmManager.nextAlarmClock?.triggerTime)
    }

    @Test
    fun cancelRemovesTheAlarmClock() {
        val triggerAt = soon()
        scheduler.schedule(alarm, triggerAt)

        scheduler.cancel(TEST_ALARM_ID)

        assertNotEquals(triggerAt.toEpochMilli(), alarmManager.nextAlarmClock?.triggerTime)
    }

    @Test
    fun snoozeIsIndependentFromTheRegularOccurrence() {
        val regular = soon().plusSeconds(60)
        val snooze = soon()
        scheduler.schedule(alarm, regular)

        scheduler.scheduleSnooze(alarm, sessionId = 1, triggerAt = snooze)
        assertEquals(snooze.toEpochMilli(), alarmManager.nextAlarmClock?.triggerTime)

        scheduler.cancelSnooze(TEST_ALARM_ID)
        assertEquals(regular.toEpochMilli(), alarmManager.nextAlarmClock?.triggerTime)
    }

    private fun soon(): Instant = Instant.now().plusSeconds(30)

    private companion object {
        const val TEST_ALARM_ID = 424_242L
    }
}
