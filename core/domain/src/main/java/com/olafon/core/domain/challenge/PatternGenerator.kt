package com.olafon.core.domain.challenge

import com.olafon.core.domain.model.Difficulty
import javax.inject.Inject
import kotlin.random.Random

/**
 * Draws a random path on a dot grid with the rules of the Android unlock pattern: a dot is never visited twice,
 * and a segment never jumps over a dot that has not been visited yet (it would be drawn through instead).
 */
class PatternGenerator @Inject constructor() : ChallengeGenerator<PatternChallenge> {
    override fun generate(difficulty: Difficulty, random: Random): PatternChallenge {
        val settings = Settings.of(difficulty)
        val dots = settings.gridSize * settings.gridSize
        val sequence = mutableListOf(random.nextInt(dots))
        while (sequence.size < settings.length) {
            val candidates = (0 until dots).filter {
                it !in sequence && !skipsUnvisited(sequence, it, settings.gridSize)
            }
            sequence += candidates[random.nextInt(candidates.size)]
        }
        return PatternChallenge(difficulty, settings.gridSize, sequence, settings.showMillis)
    }

    private data class Settings(val gridSize: Int, val length: Int, val showMillis: Long) {
        companion object {
            fun of(difficulty: Difficulty): Settings = when (difficulty) {
                Difficulty.EASY -> Settings(gridSize = SMALL_GRID, length = EASY_LENGTH, showMillis = SHOW_MILLIS)
                Difficulty.MEDIUM -> Settings(gridSize = SMALL_GRID, length = MEDIUM_LENGTH, showMillis = SHOW_MILLIS)
                Difficulty.HARD -> Settings(gridSize = LARGE_GRID, length = HARD_LENGTH, showMillis = SHORT_SHOW_MILLIS)
            }
        }
    }

    companion object {
        const val SMALL_GRID = 3
        const val LARGE_GRID = 4
        const val EASY_LENGTH = 4
        const val MEDIUM_LENGTH = 5
        const val HARD_LENGTH = 6
        const val SHOW_MILLIS = 3_000L
        const val SHORT_SHOW_MILLIS = 2_500L

        /**
         * True when going from the last dot of [visited] to [next] passes over a grid dot not in [visited].
         * Every dot on the straight segment between the two is a grid dot exactly when it sits at a multiple
         * of the step (dx / g, dy / g), g being the gcd of the deltas.
         */
        fun skipsUnvisited(visited: List<Int>, next: Int, gridSize: Int): Boolean {
            val from = visited.last()
            val (fromRow, fromCol) = from / gridSize to from % gridSize
            val (toRow, toCol) = next / gridSize to next % gridSize
            val steps = gcd(kotlin.math.abs(toRow - fromRow), kotlin.math.abs(toCol - fromCol))
            if (steps <= 1) return false
            val stepRow = (toRow - fromRow) / steps
            val stepCol = (toCol - fromCol) / steps
            return (1 until steps).any { k ->
                val between = (fromRow + k * stepRow) * gridSize + (fromCol + k * stepCol)
                between !in visited
            }
        }

        private tailrec fun gcd(a: Int, b: Int): Int = if (b == 0) a else gcd(b, a % b)
    }
}
