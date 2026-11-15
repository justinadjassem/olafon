package com.olafon.core.domain.challenge

import com.olafon.core.domain.model.Difficulty
import kotlin.random.Random

/** Produces a fresh challenge of one kind. Deterministic for a given [Random], which keeps tests stable. */
interface ChallengeGenerator<T : Challenge> {
    fun generate(difficulty: Difficulty, random: Random = Random.Default): T
}
