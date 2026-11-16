package com.olafon.feature.briefing

import androidx.lifecycle.ViewModel
import com.olafon.core.domain.briefing.Briefing
import com.olafon.core.domain.repository.BriefingRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import java.time.Clock
import java.time.LocalDate
import javax.inject.Inject
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

sealed interface BriefingUiState {
    data object Loading : BriefingUiState

    /** Nothing was ever synchronised for that day. */
    data object NoData : BriefingUiState

    data class Ready(val briefing: Briefing) : BriefingUiState
}

/** Reads the cache only: the briefing must show even with no network and no source reachable. */
@HiltViewModel
class BriefingViewModel @Inject constructor(private val repository: BriefingRepository, private val clock: Clock) :
    ViewModel() {
    val today: LocalDate get() = LocalDate.now(clock)

    fun state(day: LocalDate): Flow<BriefingUiState> =
        repository.observe(day).map { it?.let(BriefingUiState::Ready) ?: BriefingUiState.NoData }
}
