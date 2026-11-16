package com.olafon.core.domain.briefing

import java.time.LocalDate
import javax.inject.Inject
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope

/** Merges every available source into one day. A source that fails contributes nothing and never breaks the rest. */
class BriefingAggregator @Inject constructor(private val sources: Set<@JvmSuppressWildcards ScheduleSource>) {
    /** Returns null when no source is available at all: there is nothing to say about [day]. */
    suspend fun build(day: LocalDate): Briefing? = coroutineScope {
        val available = sources.filter { runCatching { it.isAvailable() }.getOrDefault(false) }
        if (available.isEmpty()) return@coroutineScope null
        val results = available.map { source ->
            async {
                runCatching { source.fetchEvents(day) to source.fetchTasks(day) }
                    .getOrDefault(emptyList<ScheduleItem>() to emptyList())
            }
        }.awaitAll()
        Briefing(
            day = day,
            events = results.flatMap { it.first }.sortedWith(compareBy({ !it.allDay }, { it.start })),
            tasks = results.flatMap { it.second }.sortedWith(compareBy({ !it.overdue }, { it.priority })),
        )
    }
}
