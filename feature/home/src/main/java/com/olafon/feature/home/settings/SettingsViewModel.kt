package com.olafon.feature.home.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.olafon.core.domain.repository.PreferencesRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

@HiltViewModel
class SettingsViewModel @Inject constructor(private val preferences: PreferencesRepository) : ViewModel() {
    val hasEmergencyCode: StateFlow<Boolean> = preferences.observe()
        .map { it.hasEmergencyExitCode }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(STOP_TIMEOUT_MS), false)

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
