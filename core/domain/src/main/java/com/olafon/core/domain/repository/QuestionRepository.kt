package com.olafon.core.domain.repository

import com.olafon.core.domain.model.Difficulty
import com.olafon.core.domain.model.Question
import com.olafon.core.domain.model.QuestionCategory

interface QuestionRepository {
    /** Loads the bundled bank into local storage if it is not there yet. Safe to call repeatedly. */
    suspend fun ensureSeeded()

    suspend fun count(): Int

    /** A random question, optionally restricted to [category], never one of [excludeIds]. */
    suspend fun random(
        difficulty: Difficulty,
        category: QuestionCategory? = null,
        excludeIds: Set<String> = emptySet(),
    ): Question?
}
