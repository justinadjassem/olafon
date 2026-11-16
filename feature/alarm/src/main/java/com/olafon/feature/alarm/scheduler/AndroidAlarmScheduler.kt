package com.olafon.feature.alarm.scheduler

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.util.Log
import com.olafon.core.domain.model.Alarm
import com.olafon.core.domain.scheduler.AlarmScheduler
import com.olafon.feature.alarm.receiver.AlarmReceiver
import dagger.hilt.android.qualifiers.ApplicationContext
import java.time.Instant
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Arms alarms with [AlarmManager.setAlarmClock], the only API guaranteed to fire through Doze. It also shows the
 * alarm icon in the status bar and exposes the next alarm to other apps.
 *
 * Each alarm owns two PendingIntents, told apart by their request code: the regular occurrence and the snooze.
 */
@Singleton
class AndroidAlarmScheduler @Inject constructor(@ApplicationContext private val context: Context) : AlarmScheduler {
    private val alarmManager: AlarmManager = context.getSystemService(AlarmManager::class.java)

    override fun schedule(alarm: Alarm, triggerAt: Instant) {
        arm(ringRequestCode(alarm.id), AlarmReceiver.ringIntent(context, alarm.id), triggerAt)
    }

    override fun cancel(alarmId: Long) {
        disarm(ringRequestCode(alarmId), AlarmReceiver.ringIntent(context, alarmId))
    }

    override fun scheduleSnooze(alarm: Alarm, sessionId: Long, triggerAt: Instant) {
        arm(snoozeRequestCode(alarm.id), AlarmReceiver.ringIntent(context, alarm.id, sessionId), triggerAt)
    }

    override fun cancelSnooze(alarmId: Long) {
        disarm(snoozeRequestCode(alarmId), AlarmReceiver.ringIntent(context, alarmId))
    }

    private fun arm(requestCode: Int, intent: Intent, triggerAt: Instant) {
        val operation = PendingIntent.getBroadcast(
            context,
            requestCode,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE,
        )
        val triggerAtMillis = triggerAt.toEpochMilli()
        if (alarmManager.canScheduleExactAlarms()) {
            alarmManager.setAlarmClock(AlarmManager.AlarmClockInfo(triggerAtMillis, showIntent()), operation)
        } else {
            // Android 12 with SCHEDULE_EXACT_ALARM revoked: degrade to an inexact alarm rather than none at all.
            Log.w(TAG, "Exact alarms not allowed, falling back to an inexact alarm")
            alarmManager.setAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, triggerAtMillis, operation)
        }
    }

    private fun disarm(requestCode: Int, intent: Intent) {
        val existing = PendingIntent.getBroadcast(
            context,
            requestCode,
            intent,
            PendingIntent.FLAG_NO_CREATE or PendingIntent.FLAG_IMMUTABLE,
        ) ?: return
        alarmManager.cancel(existing)
        existing.cancel()
    }

    /** Opened when the user taps the alarm icon in the status bar or on the lock screen. */
    private fun showIntent(): PendingIntent? {
        val launch = context.packageManager.getLaunchIntentForPackage(context.packageName) ?: return null
        return PendingIntent.getActivity(context, 0, launch, PendingIntent.FLAG_IMMUTABLE)
    }

    private companion object {
        const val TAG = "AndroidAlarmScheduler"

        fun ringRequestCode(alarmId: Long): Int = (alarmId * 2).toInt()

        fun snoozeRequestCode(alarmId: Long): Int = (alarmId * 2 + 1).toInt()
    }
}
