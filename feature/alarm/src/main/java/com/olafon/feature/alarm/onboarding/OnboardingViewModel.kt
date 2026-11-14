package com.olafon.feature.alarm.onboarding

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.olafon.core.domain.repository.PreferencesRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

@HiltViewModel
class OnboardingViewModel @Inject constructor(
    val permissions: AlarmPermissions,
    private val preferences: PreferencesRepository,
) : ViewModel() {
    private val _status = MutableStateFlow(permissions.status())
    val status: StateFlow<AlarmPermissions.Status> = _status.asStateFlow()

    /** Called whenever the screen resumes: the user may have changed something in the system settings. */
    fun refresh() {
        _status.value = permissions.status()
    }

    fun onContinue(onDone: () -> Unit) {
        viewModelScope.launch {
            preferences.markFirstLaunchDone()
            onDone()
        }
    }
}
