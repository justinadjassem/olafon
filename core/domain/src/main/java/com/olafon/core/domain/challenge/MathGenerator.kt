package com.olafon.core.domain.challenge

import com.olafon.core.domain.model.Difficulty
import javax.inject.Inject
import kotlin.random.Random

/**
 * Mental arithmetic sized by difficulty: small sums to start, two-digit sums and differences, then products and
 * three-digit sums. Differences never go negative so the keypad needs no sign.
 */
class MathGenerator @Inject constructor() : ChallengeGenerator<MathChallenge> {
    override fun generate(difficulty: Difficulty, random: Random): MathChallenge = when (difficulty) {
        Difficulty.EASY -> MathChallenge(
            difficulty,
            random.nextInt(EASY_RANGE),
            MathOperation.ADD,
            random.nextInt(EASY_RANGE),
        )
        Difficulty.MEDIUM -> twoDigits(difficulty, random)
        Difficulty.HARD -> if (random.nextBoolean()) product(difficulty, random) else threeDigits(difficulty, random)
    }

    private fun twoDigits(difficulty: Difficulty, random: Random): MathChallenge {
        val a = random.nextInt(TWO_DIGITS)
        val b = random.nextInt(TWO_DIGITS)
        return if (random.nextBoolean()) {
            MathChallenge(difficulty, a, MathOperation.ADD, b)
        } else {
            MathChallenge(difficulty, maxOf(a, b), MathOperation.SUBTRACT, minOf(a, b))
        }
    }

    private fun product(difficulty: Difficulty, random: Random): MathChallenge =
        MathChallenge(difficulty, random.nextInt(PRODUCT_LEFT), MathOperation.MULTIPLY, random.nextInt(PRODUCT_RIGHT))

    private fun threeDigits(difficulty: Difficulty, random: Random): MathChallenge =
        MathChallenge(difficulty, random.nextInt(THREE_DIGITS), MathOperation.ADD, random.nextInt(THREE_DIGITS))

    private fun Random.nextInt(range: IntRange): Int = nextInt(range.first, range.last + 1)

    companion object {
        val EASY_RANGE = 2..20
        val TWO_DIGITS = 10..99
        val PRODUCT_LEFT = 12..25
        val PRODUCT_RIGHT = 3..12
        val THREE_DIGITS = 100..999
    }
}
