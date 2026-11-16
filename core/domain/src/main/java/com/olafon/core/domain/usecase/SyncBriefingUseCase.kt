package com.olafon.core.domain.usecase

import com.olafon.core.domain.briefing.BriefingAggregator
import com.olafon.core.domain.repository.BriefingRepository
import java.time.Clock
import java.time.Instant
import java.time.LocalDate
import javax.inject.Inject

/**
 * Fills the cache for today and tomorrow. Runs in the evening, at boot and when the app opens, so the morning
 * briefing never needs a source: it reads what was cached, even offline.
 */
class SyncBriefingUseCase @Inject constructor(
    private val aggregator: BriefingAggregator,
    private val repository: BriefingRepository,
    private val clock: Clock,
) {
    /** Returns false when no source could be read; the cache is then left untouched. */
    suspend operator fun invoke(): Boolean {
        val today = LocalDate.now(clock)
        val days = listOf(today, today.plusDays(1)).mapNotNull { aggregator.build(it) }
        days.forEach { repository.save(it, Instant.now(clock)) }
        repository.deleteBefore(today)
        return days.isNotEmpty()
    }
}
