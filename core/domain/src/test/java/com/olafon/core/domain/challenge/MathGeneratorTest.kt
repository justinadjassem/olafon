package com.olafon.core.domain.challenge

import com.olafon.core.domain.model.Difficulty
import kotlin.random.Random
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class MathGeneratorTest {
    private val generator = MathGenerator()

    @Test
    fun `easy is a small sum, medium a two-digit sum or difference, hard a product or a three-digit sum`() {
        repeat(300) { seed ->
            val easy = generator.generate(Difficulty.EASY, Random(seed))
            assertEquals(MathOperation.ADD, easy.operation)
            assertTrue(easy.left in MathGenerator.EASY_RANGE && easy.right in MathGenerator.EASY_RANGE)

            val medium = generator.generate(Difficulty.MEDIUM, Random(seed))
            assertTrue(medium.operation in setOf(MathOperation.ADD, MathOperation.SUBTRACT))
            assertTrue(medium.left in MathGenerator.TWO_DIGITS && medium.right in MathGenerator.TWO_DIGITS)
            assertTrue("no negative answers", medium.answer >= 0)

            val hard = generator.generate(Difficulty.HARD, Random(seed))
            when (hard.operation) {
                MathOperation.MULTIPLY ->
                    assertTrue(hard.left in MathGenerator.PRODUCT_LEFT && hard.right in MathGenerator.PRODUCT_RIGHT)
                MathOperation.ADD ->
                    assertTrue(hard.left in MathGenerator.THREE_DIGITS && hard.right in MathGenerator.THREE_DIGITS)
                MathOperation.SUBTRACT -> error("hard never subtracts")
            }
        }
    }

    @Test
    fun `answer is checked against the typed number`() {
        val challenge = MathChallenge(Difficulty.HARD, 17, MathOperation.MULTIPLY, 6)

        assertEquals(102, challenge.answer)
        assertTrue(challenge.isSolvedBy(102))
        assertFalse(challenge.isSolvedBy(101))
        assertFalse(challenge.isSolvedBy(null))
    }
}
