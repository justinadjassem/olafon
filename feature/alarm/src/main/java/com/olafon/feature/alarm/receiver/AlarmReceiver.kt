package com.olafon.feature.alarm.receiver

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import androidx.core.content.ContextCompat
import com.olafon.feature.alarm.service.AlarmService

/** Target of the AlarmManager broadcast. Hands over to the foreground service as fast as possible. */
class AlarmReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        val alarmId = intent.getLongExtra(EXTRA_ALARM_ID, NO_ID)
        if (alarmId == NO_ID) return
        val sessionId = intent.getLongExtra(EXTRA_SESSION_ID, NO_ID).takeIf { it != NO_ID }
        ContextCompat.startForegroundService(context, AlarmService.ringIntent(context, alarmId, sessionId))
    }

    companion object {
        const val EXTRA_ALARM_ID = "com.olafon.extra.ALARM_ID"
        const val EXTRA_SESSION_ID = "com.olafon.extra.SESSION_ID"
        const val NO_ID = -1L

        /** [sessionId] is set for a snooze, so the ring resumes the same wake session. */
        fun ringIntent(context: Context, alarmId: Long, sessionId: Long? = null): Intent =
            Intent(context, AlarmReceiver::class.java)
                .setAction(ACTION_RING)
                .putExtra(EXTRA_ALARM_ID, alarmId)
                .putExtra(EXTRA_SESSION_ID, sessionId ?: NO_ID)

        private const val ACTION_RING = "com.olafon.action.RING"
    }
}
