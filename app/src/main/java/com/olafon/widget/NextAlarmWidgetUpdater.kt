package com.olafon.widget

import android.content.Context
import android.os.UserManager
import androidx.glance.appwidget.updateAll
import com.olafon.core.domain.repository.AlarmRepository
import com.olafon.core.domain.repository.WakeSessionRepository
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.launch

/**
 * Redraws the widget whenever an alarm is added, edited, toggled or deleted, and whenever one rings (a repeating
 * alarm then moves to its next day without any change in the alarm table).
 */
@Singleton
class NextAlarmWidgetUpdater @Inject constructor(
    @ApplicationContext private val context: Context,
    private val alarms: AlarmRepository,
    private val sessions: WakeSessionRepository,
) {
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Default)

    fun start() {
        scope.launch {
            combine(alarms.observeAll(), sessions.observeRecent(1)) { _, _ -> }.collect {
                // Glance keeps its state in credential-protected storage: wait for the first unlock after a reboot.
                val unlocked = context.getSystemService(UserManager::class.java)?.isUserUnlocked == true
                if (unlocked) runCatching { NextAlarmWidget().updateAll(context) }
            }
        }
    }
}
