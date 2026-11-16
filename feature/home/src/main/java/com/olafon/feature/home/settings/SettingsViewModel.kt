package com.olafon.feature.home.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.olafon.core.domain.model.DeviceCalendar
import com.olafon.core.domain.repository.CalendarRepository
import com.olafon.core.domain.repository.PreferencesRepository
import com.olafon.core.domain.usecase.SyncBriefingUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class SettingsUiState(
    val hasEmergencyCode: Boolean = false,
    val calendarPermission: Boolean = false,
    val calendars: List<DeviceCalendar> = emptyList(),
    val excludedCalendarIds: Set<Long> = emptySet(),
)

@HiltViewModel
class SettingsViewModel @Inject constructor(
    private val preferences: PreferencesRepository,
    private val calendarRepository: CalendarRepository,
    private val syncBriefing: SyncBriefingUseCase,
) : ViewModel() {
    private val device = MutableStateFlow(SettingsUiState())

    val state: StateFlow<SettingsUiState> = combine(preferences.observe(), device) { prefs, device ->
        device.copy(hasEmergencyCode = prefs.hasEmergencyExitCode, excludedCalendarIds = prefs.excludedCalendarIds)
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(STOP_TIMEOUT_MS), SettingsUiState())

    init {
        refreshCalendars()
    }

    /** Called at start and after the permission dialog: lists the calendars and refreshes the briefing cache. */
    fun refreshCalendars() {
        viewModelScope.launch {
            val granted = calendarRepository.hasPermission()
            val calendars = runCatching { calendarRepository.calendars() }.getOrDefault(emptyList())
            device.update { it.copy(calendarPermission = granted, calendars = calendars) }
            if (granted) runCatching { syncBriefing() }
        }
    }

    fun onCalendarToggle(calendarId: Long, included: Boolean) {
        viewModelScope.launch {
            preferences.setCalendarIncluded(calendarId, included)
            runCatching { syncBriefing() }
        }
    }

    /** Returns false when [code] is not exactly [CODE_LENGTH] digits. */
    fun onSetEmergencyCode(code: String): Boolean {
        if (code.length != CODE_LENGTH || !code.all(Char::isDigit)) return false
        viewModelScope.launch { preferences.setEmergencyExitCode(code) }
        return true
    }

    fun onClearEmergencyCode() {
        viewModelScope.launch { preferences.setEmergencyExitCode(null) }
    }

    companion object {
        const val CODE_LENGTH = 6
        private const val STOP_TIMEOUT_MS = 5_000L
    }
}
