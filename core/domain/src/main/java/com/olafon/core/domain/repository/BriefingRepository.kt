package com.olafon.core.domain.repository

import com.olafon.core.domain.briefing.Briefing
import java.time.Instant
import java.time.LocalDate
import kotlinx.coroutines.flow.Flow

/** Local cache of briefings. The briefing screen reads only from here, never from a source. */
interface BriefingRepository {
    /** Replaces everything cached for the day of [briefing] and stamps it with [syncedAt]. */
    suspend fun save(briefing: Briefing, syncedAt: Instant)

    /** The cached briefing of [day], or null when that day was never synchronised. */
    fun observe(day: LocalDate): Flow<Briefing?>

    /** Drops cached days before [day]. */
    suspend fun deleteBefore(day: LocalDate)
}
