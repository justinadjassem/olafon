package com.olafon

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.olafon.core.domain.repository.PreferencesRepository
import com.olafon.core.domain.usecase.SyncBriefingUseCase
import com.olafon.feature.alarm.onboarding.AlarmPermissions
import com.olafon.feature.briefing.sync.BriefingSyncScheduler
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

enum class MainDestination { LOADING, ONBOARDING, HOME }

/** Sends the user back to the onboarding whenever a required permission is missing, not only on first launch. */
@HiltViewModel
class MainViewModel @Inject constructor(
    preferences: PreferencesRepository,
    private val permissions: AlarmPermissions,
    private val syncBriefing: SyncBriefingUseCase,
    private val syncScheduler: BriefingSyncScheduler,
) : ViewModel() {
    private val requiredGranted = MutableStateFlow(permissions.status().requiredGranted)

    val destination: StateFlow<MainDestination> = combine(preferences.observe(), requiredGranted) { prefs, granted ->
        if (prefs.firstLaunchDone && granted) MainDestination.HOME else MainDestination.ONBOARDING
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(STOP_TIMEOUT_MS), MainDestination.LOADING)

    init {
        viewModelScope.launch { runCatching { syncScheduler.schedule() } }
    }

    /** Called every time the app comes to the front: permissions may have changed, and the agenda too. */
    fun refreshPermissions() {
        requiredGranted.value = permissions.status().requiredGranted
        viewModelScope.launch { runCatching { syncBriefing() } }
    }

    private companion object {
        const val STOP_TIMEOUT_MS = 5_000L
    }
}
