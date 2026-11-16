package com.olafon.feature.home.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.olafon.core.domain.briefing.Briefing
import com.olafon.core.domain.model.Alarm
import com.olafon.core.domain.model.ChallengeType
import com.olafon.core.domain.model.WakeStats
import com.olafon.core.domain.repository.AlarmRepository
import com.olafon.core.domain.repository.BriefingRepository
import com.olafon.core.domain.repository.WakeSessionRepository
import com.olafon.core.domain.usecase.ComputeNextTriggerUseCase
import com.olafon.core.domain.usecase.ComputeWakeStatsUseCase
import com.olafon.core.domain.usecase.SaveAlarmUseCase
import com.olafon.core.domain.usecase.ToggleAlarmUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import java.time.Clock
import java.time.LocalDate
import java.time.ZonedDateTime
import javax.inject.Inject
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

/** The alarm that rings first among the enabled ones, with its instant. */
data class NextAlarm(val alarm: Alarm, val at: ZonedDateTime)

data class HomeUiState(
    val loaded: Boolean = false,
    val nextAlarm: NextAlarm? = null,
    val alarms: List<Alarm> = emptyList(),
    val stats: WakeStats = WakeStats(),
    /** Tomorrow as cached by the last sync; null when the agenda was never read. */
    val tomorrow: Briefing? = null,
)

@HiltViewModel
class HomeViewModel @Inject constructor(
    alarms: AlarmRepository,
    sessions: WakeSessionRepository,
    briefings: BriefingRepository,
    private val computeNextTrigger: ComputeNextTriggerUseCase,
    computeStats: ComputeWakeStatsUseCase,
    private val toggleAlarm: ToggleAlarmUseCase,
    private val saveAlarm: SaveAlarmUseCase,
    private val clock: Clock,
) : ViewModel() {
    val state: StateFlow<HomeUiState> = combine(
        alarms.observeAll(),
        sessions.observeRecent(ComputeWakeStatsUseCase.HISTORY_LIMIT),
        briefings.observe(LocalDate.now(clock).plusDays(1)),
    ) { all, recent, tomorrow ->
        HomeUiState(
            loaded = true,
            nextAlarm = nextAlarm(all),
            alarms = all.sortedWith(compareBy({ it.hour }, { it.minute })),
            stats = computeStats(recent),
            tomorrow = tomorrow,
        )
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(STOP_TIMEOUT_MS), HomeUiState())

    private fun nextAlarm(all: List<Alarm>): NextAlarm? {
        val now = ZonedDateTime.now(clock)
        return all.filter { it.enabled }
            .map { NextAlarm(it, computeNextTrigger(it, now).atZone(now.zone)) }
            .minByOrNull { it.at }
    }

    fun onToggle(alarm: Alarm, enabled: Boolean) {
        viewModelScope.launch { toggleAlarm(alarm.id, enabled) }
    }

    /** Debug builds only: an alarm one minute from now that chains every challenge, for the manual checklist. */
    fun onAddTestAlarm() {
        viewModelScope.launch {
            val at = ZonedDateTime.now(clock).plusMinutes(1)
            saveAlarm(
                Alarm(
                    hour = at.hour,
                    minute = at.minute,
                    label = "Test",
                    challenges = listOf(ChallengeType.PATTERN, ChallengeType.QUESTION, ChallengeType.MATH),
                ),
            )
        }
    }

    private companion object {
        const val STOP_TIMEOUT_MS = 5_000L
    }
}
