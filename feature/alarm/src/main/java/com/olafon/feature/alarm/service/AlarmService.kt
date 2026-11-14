package com.olafon.feature.alarm.service

import android.app.Service
import android.content.Context
import android.content.Intent
import android.content.pm.ServiceInfo
import android.os.IBinder
import android.util.Log
import androidx.core.app.ServiceCompat
import com.olafon.core.domain.usecase.StartRingingUseCase
import com.olafon.feature.alarm.receiver.AlarmReceiver
import com.olafon.feature.alarm.sound.AlarmPlayer
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.launch

/**
 * Foreground service that owns the ring: sound, vibration and the ongoing notification. The ring screen is only a
 * view on top of it, so closing the app from the recents never silences an alarm.
 *
 * The ring intent is redelivered if the system kills the process mid-ring, which restarts the sound. In that rare
 * case a second wake session may be opened for the same ring; the next occurrence is armed idempotently.
 */
@AndroidEntryPoint
class AlarmService : Service() {
    @Inject lateinit var startRinging: StartRingingUseCase

    @Inject lateinit var controller: RingController

    @Inject lateinit var notifications: AlarmNotifications

    @Inject lateinit var player: AlarmPlayer

    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Main.immediate)

    override fun onCreate() {
        super.onCreate()
        notifications.ensureChannel()
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        val alarmId = intent?.getLongExtra(AlarmReceiver.EXTRA_ALARM_ID, AlarmReceiver.NO_ID) ?: AlarmReceiver.NO_ID
        return when {
            intent?.action == ACTION_SNOOZE -> {
                scope.launch { controller.snooze() }
                START_NOT_STICKY
            }
            intent == null || alarmId == AlarmReceiver.NO_ID -> {
                stopSelf()
                START_NOT_STICKY
            }
            else -> {
                startRing(alarmId, intent)
                START_REDELIVER_INTENT
            }
        }
    }

    private fun startRing(alarmId: Long, intent: Intent) {
        val sessionId = intent.getLongExtra(AlarmReceiver.EXTRA_SESSION_ID, AlarmReceiver.NO_ID)
            .takeIf { it != AlarmReceiver.NO_ID }
        // A second alarm while one rings: drop the current notification first, because SystemUI only launches the
        // full-screen intent of a newly posted notification, never of an update.
        if (controller.ringing.value != null) {
            ServiceCompat.stopForeground(this, ServiceCompat.STOP_FOREGROUND_REMOVE)
        }
        // Go foreground right away: the system grants only a few seconds after startForegroundService().
        ServiceCompat.startForeground(
            this,
            AlarmNotifications.NOTIFICATION_ID,
            notifications.ringing(alarmId, sessionId, ringing = null),
            ServiceInfo.FOREGROUND_SERVICE_TYPE_SYSTEM_EXEMPTED,
        )
        scope.launch { ring(alarmId, sessionId) }
    }

    private suspend fun ring(alarmId: Long, sessionId: Long?) {
        val ringing = startRinging(alarmId, sessionId)
        if (ringing == null) {
            Log.w(TAG, "Alarm $alarmId no longer exists, nothing to ring")
            stopSelf()
            return
        }
        controller.onRingStarted(ringing)
        notifications.update(alarmId, ringing.session.id, ringing)
        player.start(ringing.alarm.ringtoneUri, ringing.alarm.progressiveVolume, scope)
    }

    override fun onDestroy() {
        player.stop()
        controller.onRingStopped()
        ServiceCompat.stopForeground(this, ServiceCompat.STOP_FOREGROUND_REMOVE)
        scope.cancel()
        super.onDestroy()
    }

    override fun onBind(intent: Intent?): IBinder? = null

    companion object {
        private const val TAG = "AlarmService"
        private const val ACTION_SNOOZE = "com.olafon.action.SNOOZE"

        fun ringIntent(context: Context, alarmId: Long, sessionId: Long?): Intent =
            Intent(context, AlarmService::class.java)
                .putExtra(AlarmReceiver.EXTRA_ALARM_ID, alarmId)
                .putExtra(AlarmReceiver.EXTRA_SESSION_ID, sessionId ?: AlarmReceiver.NO_ID)

        fun snoozeIntent(context: Context): Intent = Intent(context, AlarmService::class.java).setAction(ACTION_SNOOZE)

        fun stopIntent(context: Context): Intent = Intent(context, AlarmService::class.java)
    }
}
