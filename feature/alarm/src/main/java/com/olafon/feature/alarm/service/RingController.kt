package com.olafon.feature.alarm.service

import android.content.Context
import com.olafon.core.domain.model.RingingAlarm
import com.olafon.core.domain.usecase.DismissAlarmUseCase
import com.olafon.core.domain.usecase.EmergencyDismissUseCase
import com.olafon.core.domain.usecase.SnoozeAlarmUseCase
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

/**
 * Single source of truth for "what is ringing right now", shared by [AlarmService] (which owns the sound) and the
 * ring screen. Every way of ending a ring goes through here so the service is always stopped consistently.
 */
@Singleton
class RingController @Inject constructor(
    @ApplicationContext private val context: Context,
    private val snoozeAlarm: SnoozeAlarmUseCase,
    private val dismissAlarm: DismissAlarmUseCase,
    private val emergencyDismiss: EmergencyDismissUseCase,
) {
    private val _ringing = MutableStateFlow<RingingAlarm?>(null)
    val ringing: StateFlow<RingingAlarm?> = _ringing.asStateFlow()

    internal fun onRingStarted(ringing: RingingAlarm) {
        _ringing.value = ringing
    }

    internal fun onRingStopped() {
        _ringing.value = null
    }

    /** Returns false when nothing rings or the snooze limit is reached. */
    suspend fun snooze(): Boolean {
        val current = _ringing.value ?: return false
        val accepted = snoozeAlarm(current)
        if (accepted) stopService()
        return accepted
    }

    /** Ends the ring once the challenges are solved. */
    suspend fun dismiss() {
        val current = _ringing.value ?: return
        dismissAlarm(current)
        stopService()
    }

    /** Returns false when [code] is wrong; the ring goes on. */
    suspend fun emergencyDismiss(code: String): Boolean {
        val current = _ringing.value ?: return false
        val accepted = emergencyDismiss(current, code)
        if (accepted) stopService()
        return accepted
    }

    private fun stopService() {
        context.stopService(AlarmService.stopIntent(context))
    }
}
