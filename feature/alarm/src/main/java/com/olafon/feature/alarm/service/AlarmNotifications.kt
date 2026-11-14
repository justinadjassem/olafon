package com.olafon.feature.alarm.service

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import androidx.core.app.NotificationCompat
import com.olafon.core.domain.model.RingingAlarm
import com.olafon.feature.alarm.R
import com.olafon.feature.alarm.ring.RingActivity
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton

/** Builds the ongoing notification shown while an alarm rings. Its full-screen intent opens [RingActivity]. */
@Singleton
class AlarmNotifications @Inject constructor(@ApplicationContext private val context: Context) {
    private val manager: NotificationManager = context.getSystemService(NotificationManager::class.java)

    fun ensureChannel() {
        val channel = NotificationChannel(
            CHANNEL_ALARM,
            context.getString(R.string.alarm_channel_name),
            NotificationManager.IMPORTANCE_HIGH,
        ).apply {
            description = context.getString(R.string.alarm_channel_description)
            // The service plays the sound itself; the notification must stay silent.
            setSound(null, null)
            enableVibration(false)
            lockscreenVisibility = Notification.VISIBILITY_PUBLIC
        }
        manager.createNotificationChannel(channel)
    }

    /** [ringing] is null during the few milliseconds before the alarm is loaded from the database. */
    fun ringing(alarmId: Long, sessionId: Long?, ringing: RingingAlarm?): Notification {
        val open = openRingScreen(alarmId, sessionId)
        val title = ringing?.alarm?.label?.takeIf { it.isNotBlank() } ?: context.getString(R.string.ring_title)
        val builder = NotificationCompat.Builder(context, CHANNEL_ALARM)
            .setSmallIcon(R.drawable.ic_alarm)
            .setContentTitle(title)
            .setContentText(context.getString(R.string.ring_notification_text))
            .setCategory(NotificationCompat.CATEGORY_ALARM)
            .setPriority(NotificationCompat.PRIORITY_MAX)
            .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
            .setOngoing(true)
            // Never setSilent(): NotificationCompat would file it in a "silent" group with GROUP_ALERT_SUMMARY and
            // SystemUI would then neither peek nor launch the full-screen intent. The channel itself is mute.
            .setForegroundServiceBehavior(NotificationCompat.FOREGROUND_SERVICE_IMMEDIATE)
            .setContentIntent(open)
            .setFullScreenIntent(open, true)
        if (ringing?.canSnooze == true) {
            val snooze = PendingIntent.getService(
                context,
                0,
                AlarmService.snoozeIntent(context),
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE,
            )
            builder.addAction(0, context.getString(R.string.action_snooze), snooze)
        }
        return builder.build()
    }

    fun update(alarmId: Long, sessionId: Long?, ringing: RingingAlarm) {
        manager.notify(NOTIFICATION_ID, ringing(alarmId, sessionId, ringing))
    }

    private fun openRingScreen(alarmId: Long, sessionId: Long?): PendingIntent = PendingIntent.getActivity(
        context,
        0,
        RingActivity.intent(context, alarmId, sessionId),
        PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE,
    )

    companion object {
        const val CHANNEL_ALARM = "alarm_ring"
        const val NOTIFICATION_ID = 1001
    }
}
