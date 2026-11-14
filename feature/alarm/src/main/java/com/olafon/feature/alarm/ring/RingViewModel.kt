package com.olafon.feature.alarm.ring

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.olafon.core.domain.model.RingingAlarm
import com.olafon.core.domain.repository.PreferencesRepository
import com.olafon.feature.alarm.service.RingController
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

data class RingUiState(
    /** Null while the service loads the alarm, or once the ring has ended. */
    val ringing: RingingAlarm? = null,
    val hasEmergencyCode: Boolean = false,
    val emergencyCodeRejected: Boolean = false,
    /** True once the user ended the ring from this screen: the activity must close. */
    val finished: Boolean = false,
)

@HiltViewModel
class RingViewModel @Inject constructor(private val controller: RingController, preferences: PreferencesRepository) :
    ViewModel() {
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

    /** Temporary until the challenges of step 3 call it on success. */
    fun onChallengesSolved() {
        viewModelScope.launch {
            controller.dismiss()
            finish()
        }
    }

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
