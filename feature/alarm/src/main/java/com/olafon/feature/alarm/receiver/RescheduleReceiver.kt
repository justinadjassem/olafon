package com.olafon.feature.alarm.receiver

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log
import androidx.core.content.ContextCompat
import com.olafon.core.domain.usecase.FindInterruptedRingUseCase
import com.olafon.core.domain.usecase.RescheduleAlarmsUseCase
import com.olafon.feature.alarm.service.AlarmService
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

/**
 * Re-arms every enabled alarm after a reboot (including the locked, direct-boot phase), an app update, a manual
 * time change, a time zone change, or a change of the exact-alarm permission.
 *
 * After a reboot it also resumes a ring that was cut short: powering the phone off is the one escape Android
 * cannot block, so the alarm rings again as soon as the system is up, on the same wake session.
 */
@AndroidEntryPoint
class RescheduleReceiver : BroadcastReceiver() {
    @Inject lateinit var rescheduleAlarms: RescheduleAlarmsUseCase

    @Inject lateinit var findInterruptedRing: FindInterruptedRingUseCase

    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action !in HANDLED_ACTIONS) return
        val pending = goAsync()
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val count = rescheduleAlarms()
                Log.i(TAG, "Rescheduled $count alarm(s) after ${intent.action}")
                if (intent.action in BOOT_ACTIONS) resumeInterruptedRing(context)
            } finally {
                pending.finish()
            }
        }
    }

    private suspend fun resumeInterruptedRing(context: Context) {
        val session = findInterruptedRing() ?: return
        Log.i(TAG, "Resuming ring of alarm ${session.alarmId} interrupted by the reboot (session ${session.id})")
        ContextCompat.startForegroundService(context, AlarmService.ringIntent(context, session.alarmId, session.id))
    }

    private companion object {
        const val TAG = "RescheduleReceiver"
        val BOOT_ACTIONS = setOf(Intent.ACTION_BOOT_COMPLETED, Intent.ACTION_LOCKED_BOOT_COMPLETED)
        val HANDLED_ACTIONS = setOf(
            Intent.ACTION_BOOT_COMPLETED,
            Intent.ACTION_LOCKED_BOOT_COMPLETED,
            Intent.ACTION_MY_PACKAGE_REPLACED,
            Intent.ACTION_TIME_CHANGED,
            Intent.ACTION_TIMEZONE_CHANGED,
            "android.app.action.SCHEDULE_EXACT_ALARM_PERMISSION_STATE_CHANGED",
        )
    }
}
