package com.olafon.core.domain.challenge

import com.olafon.core.domain.model.Difficulty
import kotlin.random.Random
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class PatternGeneratorTest {
    private val generator = PatternGenerator()

    @Test
    fun `grid size, length and display time follow the difficulty`() {
        val easy = generator.generate(Difficulty.EASY, Random(1))
        val medium = generator.generate(Difficulty.MEDIUM, Random(1))
        val hard = generator.generate(Difficulty.HARD, Random(1))

        assertEquals(Triple(3, 4, 3_000L), Triple(easy.gridSize, easy.sequence.size, easy.showMillis))
        assertEquals(Triple(3, 5, 3_000L), Triple(medium.gridSize, medium.sequence.size, medium.showMillis))
        assertEquals(Triple(4, 6, 2_500L), Triple(hard.gridSize, hard.sequence.size, hard.showMillis))
    }

    @Test
    fun `never visits a dot twice nor jumps over an unvisited dot, on both grids`() {
        repeat(500) { seed ->
            for (difficulty in Difficulty.entries) {
                val pattern = generator.generate(difficulty, Random(seed))
                val seen = mutableListOf<Int>()
                for (dot in pattern.sequence) {
                    assertFalse("seed $seed repeats dot $dot", dot in seen)
                    if (seen.isNotEmpty()) {
                        assertFalse(
                            "seed $seed jumps from ${seen.last()} to $dot over an unvisited dot",
                            PatternGenerator.skipsUnvisited(seen, dot, pattern.gridSize),
                        )
                    }
                    seen += dot
                }
            }
        }
    }

    @Test
    fun `same seed gives the same pattern`() {
        assertEquals(generator.generate(Difficulty.HARD, Random(42)), generator.generate(Difficulty.HARD, Random(42)))
    }

    @Test
    fun `skip rule matches the unlock pattern on a 3x3 grid`() {
        // Corner to opposite corner crosses the centre.
        assertTrue(PatternGenerator.skipsUnvisited(listOf(0), 8, gridSize = 3))
        assertFalse(PatternGenerator.skipsUnvisited(listOf(4, 0), 8, gridSize = 3))
        // Left to right on the top row crosses the top middle.
        assertTrue(PatternGenerator.skipsUnvisited(listOf(0), 2, gridSize = 3))
        // A knight's move crosses nothing.
        assertFalse(PatternGenerator.skipsUnvisited(listOf(0), 5, gridSize = 3))
    }

    @Test
    fun `skip rule handles the long edges of a 4x4 grid`() {
        // 0 → 3 passes over 1 and 2.
        assertTrue(PatternGenerator.skipsUnvisited(listOf(0), 3, gridSize = 4))
        assertTrue(PatternGenerator.skipsUnvisited(listOf(1, 0), 3, gridSize = 4))
        assertFalse(PatternGenerator.skipsUnvisited(listOf(2, 1, 0), 3, gridSize = 4))
        // 0 → 15 is the long diagonal through 5 and 10; 0 → 6 (one row, two columns) crosses nothing.
        assertTrue(PatternGenerator.skipsUnvisited(listOf(0), 15, gridSize = 4))
        assertFalse(PatternGenerator.skipsUnvisited(listOf(0), 6, gridSize = 4))
    }

    @Test
    fun `a pattern is solved only by the exact same sequence`() {
        val pattern = PatternChallenge(Difficulty.EASY, gridSize = 3, sequence = listOf(0, 4, 8, 5), showMillis = 1)

        assertTrue(pattern.isSolvedBy(listOf(0, 4, 8, 5)))
        assertFalse(pattern.isSolvedBy(listOf(5, 8, 4, 0)))
        assertFalse(pattern.isSolvedBy(listOf(0, 4, 8)))
    }
}
