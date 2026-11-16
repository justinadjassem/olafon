package com.olafon.core.domain.model

/** A multiple-choice question from the local bank. [answerIndex] points into [choices]. */
data class Question(
    val id: String,
    val category: QuestionCategory,
    val difficulty: Difficulty,
    val prompt: String,
    val choices: List<String>,
    val answerIndex: Int,
) {
    init {
        require(choices.size >= MIN_CHOICES) { "a question needs at least $MIN_CHOICES choices" }
        require(answerIndex in choices.indices) { "answerIndex out of range" }
    }

    val answer: String get() = choices[answerIndex]

    fun isCorrect(choiceIndex: Int): Boolean = choiceIndex == answerIndex

    private companion object {
        const val MIN_CHOICES = 2
    }
}

enum class QuestionCategory { CULTURE, LOGIC, VOCABULARY }
