package com.olafon.feature.alarm.ring

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.olafon.core.domain.model.RingingAlarm
import com.olafon.core.domain.repository.PreferencesRepository
import com.olafon.core.domain.repository.WakeSessionRepository
import com.olafon.core.domain.usecase.ComputeWakeStatsUseCase
import com.olafon.feature.alarm.service.RingController
import dagger.hilt.android.lifecycle.HiltViewModel
import java.time.Clock
import java.time.Duration
import java.time.Instant
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

/** What the success panel shows once the last challenge is passed and the sound has stopped. */
data class WakeSummary(val duration: Duration, val streakDays: Int, val briefingEnabled: Boolean)

data class RingUiState(
    /** Null while the service loads the alarm, or once the ring has ended. */
    val ringing: RingingAlarm? = null,
    val hasEmergencyCode: Boolean = false,
    val emergencyCodeRejected: Boolean = false,
    /** True once the user tapped "start the challenges": the snooze is no longer offered. */
    val challengesStarted: Boolean = false,
    /** Set once the alarm is dismissed through the challenges; the screen stays on the success panel. */
    val summary: WakeSummary? = null,
    /** True once the user ended the ring from this screen: the activity must close. */
    val finished: Boolean = false,
)

@HiltViewModel
class RingViewModel @Inject constructor(
    private val controller: RingController,
    preferences: PreferencesRepository,
    private val sessions: WakeSessionRepository,
    private val computeStats: ComputeWakeStatsUseCase,
    private val clock: Clock,
) : ViewModel() {
    private val local = MutableStateFlow(RingUiState())

    val state: StateFlow<RingUiState> = combine(
        controller.ringing,
        preferences.observe().map { it.hasEmergencyExitCode },
        local,
    ) { ringing, hasCode, local ->
        local.copy(ringing = ringing, hasEmergencyCode = hasCode)
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(STOP_TIMEOUT_MS), RingUiState())

    fun onSnooze() {
        viewModelScope.launch { if (controller.snooze()) finish() }
    }

    fun onStartChallenges() {
        local.value = local.value.copy(challengesStarted = true)
    }

    /** Called by the challenge session once the last challenge is passed: stop the sound, show the summary. */
    fun onChallengesSolved() {
        val ringing = controller.ringing.value ?: return
        viewModelScope.launch {
            controller.dismiss()
            val stats = computeStats(sessions.observeRecent(ComputeWakeStatsUseCase.HISTORY_LIMIT).first())
            val duration = Duration.between(ringing.session.ringAt, Instant.now(clock))
            local.value =
                local.value.copy(summary = WakeSummary(duration, stats.streakDays, ringing.alarm.briefingEnabled))
        }
    }

    fun onFinish() = finish()

    fun onEmergencyCode(code: String) {
        viewModelScope.launch {
            if (controller.emergencyDismiss(code)) {
                finish()
            } else {
                local.value = local.value.copy(emergencyCodeRejected = true)
            }
        }
    }

    fun onEmergencyCodeEdited() {
        local.value = local.value.copy(emergencyCodeRejected = false)
    }

    private fun finish() {
        local.value = local.value.copy(finished = true)
    }

    private companion object {
        const val STOP_TIMEOUT_MS = 5_000L
    }
}
