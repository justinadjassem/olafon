package com.olafon.core.domain.challenge

import com.olafon.core.domain.model.Difficulty
import com.olafon.core.domain.model.Question
import com.olafon.core.domain.model.QuestionCategory
import com.olafon.core.domain.repository.QuestionRepository
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class NextQuestionChallengeUseCaseTest {
    private class FakeQuestionRepository(private val bank: List<Question>) : QuestionRepository {
        override suspend fun ensureSeeded() = Unit

        override suspend fun count(): Int = bank.size

        override suspend fun random(
            difficulty: Difficulty,
            category: QuestionCategory?,
            excludeIds: Set<String>,
        ): Question? = bank.firstOrNull { it.difficulty == difficulty && it.id !in excludeIds }
            ?: bank.firstOrNull { it.id !in excludeIds }
    }

    private fun question(id: String, difficulty: Difficulty = Difficulty.MEDIUM) =
        Question(id, QuestionCategory.CULTURE, difficulty, "Q$id", listOf("a", "b", "c"), answerIndex = 1)

    @Test
    fun `draws a question of the difficulty and maps it to a bank challenge`() = runTest {
        val next =
            NextQuestionChallengeUseCase(FakeQuestionRepository(listOf(question("1"), question("2", Difficulty.HARD))))

        val challenge = next(Difficulty.HARD, recentIds = emptyList())

        assertEquals("2", challenge?.questionId)
        assertEquals("Q2", challenge?.prompt)
        assertEquals(1, challenge?.answerIndex)
        assertEquals(QuestionSource.BANK, challenge?.source)
        assertTrue(challenge!!.isSolvedBy(1))
    }

    @Test
    fun `skips the questions asked recently`() = runTest {
        val next =
            NextQuestionChallengeUseCase(FakeQuestionRepository(listOf(question("1"), question("2"), question("3"))))

        assertEquals("3", next(Difficulty.MEDIUM, recentIds = listOf("1", "2"))?.questionId)
    }

    @Test
    fun `falls back on a recent question rather than nothing when the bank is exhausted`() = runTest {
        val next = NextQuestionChallengeUseCase(FakeQuestionRepository(listOf(question("1"))))

        assertEquals("1", next(Difficulty.MEDIUM, recentIds = listOf("1"))?.questionId)
    }

    @Test
    fun `returns null on an empty bank`() = runTest {
        assertNull(NextQuestionChallengeUseCase(FakeQuestionRepository(emptyList()))(Difficulty.EASY, emptyList()))
    }
}
