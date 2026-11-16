package com.olafon.core.domain.challenge

import com.olafon.core.domain.model.ChallengeType
import com.olafon.core.domain.model.Difficulty

/** One obstacle between the ringing alarm and its dismissal. Generated and checked by pure Kotlin. */
sealed interface Challenge {
    val type: ChallengeType
    val difficulty: Difficulty
}

/** Memorise a path drawn on a dot grid, then draw it back. */
data class PatternChallenge(
    override val difficulty: Difficulty,
    /** Dots per side: 3 or 4. */
    val gridSize: Int,
    /** Dot indices, row by row from the top left, in drawing order. */
    val sequence: List<Int>,
    /** How long the pattern stays visible before it has to be drawn back. */
    val showMillis: Long,
) : Challenge {
    override val type: ChallengeType get() = ChallengeType.PATTERN

    init {
        require(sequence.isNotEmpty()) { "a pattern needs at least one dot" }
        require(sequence.toSet().size == sequence.size) { "a pattern never visits a dot twice" }
        require(sequence.all { it in 0 until gridSize * gridSize }) { "dot index out of the grid" }
    }

    fun isSolvedBy(input: List<Int>): Boolean = input == sequence
}

/** A multiple-choice question; [answerIndex] points into [choices]. */
data class QuestionChallenge(
    override val difficulty: Difficulty,
    /** Identifier in the bank, used to avoid repeating recent questions. */
    val questionId: String,
    val prompt: String,
    val choices: List<String>,
    val answerIndex: Int,
    val source: QuestionSource,
) : Challenge {
    override val type: ChallengeType get() = ChallengeType.QUESTION

    init {
        require(answerIndex in choices.indices) { "answerIndex out of range" }
    }

    fun isSolvedBy(choiceIndex: Int): Boolean = choiceIndex == answerIndex
}

enum class QuestionSource {
    /** The bundled local bank. */
    BANK,

    /** Generated from the user's agenda (V2). */
    SCHEDULE,
}

/** An arithmetic operation to solve in one's head and type on a keypad. */
data class MathChallenge(
    override val difficulty: Difficulty,
    val left: Int,
    val operation: MathOperation,
    val right: Int,
) : Challenge {
    override val type: ChallengeType get() = ChallengeType.MATH

    val answer: Int get() = operation.apply(left, right)

    fun isSolvedBy(input: Int?): Boolean = input == answer
}

enum class MathOperation(val symbol: String) {
    ADD("+"),
    SUBTRACT("−"),
    MULTIPLY("×"),
    ;

    fun apply(left: Int, right: Int): Int = when (this) {
        ADD -> left + right
        SUBTRACT -> left - right
        MULTIPLY -> left * right
    }
}
