package com.olafon.core.domain.challenge

import com.olafon.core.domain.model.Difficulty
import com.olafon.core.domain.repository.QuestionRepository
import javax.inject.Inject

/**
 * Draws a question from the local bank, avoiding the ones asked most recently so a wrong answer never brings the
 * same question straight back. Returns null only when the bank is empty.
 */
class NextQuestionChallengeUseCase @Inject constructor(private val questions: QuestionRepository) {
    suspend operator fun invoke(difficulty: Difficulty, recentIds: List<String>): QuestionChallenge? {
        val question = questions.random(difficulty, excludeIds = recentIds.takeLast(RECENT_WINDOW).toSet())
            ?: questions.random(difficulty)
            ?: return null
        return QuestionChallenge(
            difficulty = question.difficulty,
            questionId = question.id,
            prompt = question.prompt,
            choices = question.choices,
            answerIndex = question.answerIndex,
            source = QuestionSource.BANK,
        )
    }

    companion object {
        /** How many of the last questions asked are kept out of the draw. */
        const val RECENT_WINDOW = 30
    }
}
