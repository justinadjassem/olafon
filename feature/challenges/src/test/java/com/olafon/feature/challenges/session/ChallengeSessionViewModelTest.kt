package com.olafon.feature.challenges.session

import com.olafon.core.domain.challenge.MathChallenge
import com.olafon.core.domain.challenge.MathGenerator
import com.olafon.core.domain.challenge.NextQuestionChallengeUseCase
import com.olafon.core.domain.challenge.PatternChallenge
import com.olafon.core.domain.challenge.PatternGenerator
import com.olafon.core.domain.challenge.QuestionChallenge
import com.olafon.core.domain.model.ChallengeType
import com.olafon.core.domain.model.Difficulty
import com.olafon.core.domain.model.Question
import com.olafon.core.domain.model.QuestionCategory
import com.olafon.core.domain.model.RingInterruption
import com.olafon.core.domain.model.WakeSession
import com.olafon.core.domain.repository.QuestionRepository
import com.olafon.core.domain.repository.WakeSessionRepository
import com.olafon.core.domain.usecase.RecordChallengeAttemptUseCase
import java.time.Instant
import kotlin.random.Random
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class ChallengeSessionViewModelTest {
    private class AttemptCounter : WakeSessionRepository {
        var attempts = 0

        override suspend fun recordAttempt(sessionId: Long) {
            attempts++
        }

        override suspend fun start(alarmId: Long, ringAt: Instant): Long = error("unused")

        override suspend fun recordSnooze(sessionId: Long) = error("unused")

        override suspend fun dismiss(sessionId: Long, at: Instant, emergency: Boolean) = error("unused")

        override suspend fun recordInterruption(sessionId: Long, interruption: RingInterruption) = error("unused")

        override suspend fun get(sessionId: Long): WakeSession? = error("unused")

        override suspend fun latestOpenSince(since: Instant): WakeSession? = error("unused")

        override fun observeRecent(limit: Int): Flow<List<WakeSession>> = error("unused")
    }

    private class Bank(ids: List<String>) : QuestionRepository {
        private val questions = ids.map {
            Question(it, QuestionCategory.LOGIC, Difficulty.MEDIUM, "Q$it", listOf("non", "oui"), answerIndex = 1)
        }

        override suspend fun ensureSeeded() = Unit

        override suspend fun count(): Int = questions.size

        override suspend fun random(
            difficulty: Difficulty,
            category: QuestionCategory?,
            excludeIds: Set<String>,
        ): Question? = questions.firstOrNull { it.id !in excludeIds }
    }

    private val attempts = AttemptCounter()

    private fun viewModel(bank: List<String> = listOf("a", "b", "c")) = ChallengeSessionViewModel(
        patterns = PatternGenerator(),
        maths = MathGenerator(),
        nextQuestion = NextQuestionChallengeUseCase(Bank(bank)),
        recordAttempt = RecordChallengeAttemptUseCase(attempts),
        random = Random(7),
    )

    @Before
    fun setUp() = Dispatchers.setMain(UnconfinedTestDispatcher())

    @After
    fun tearDown() = Dispatchers.resetMain()

    @Test
    fun `plays the alarm's challenges in order and completes after the last one`() = runTest {
        val vm = viewModel()
        vm.start(
            listOf(ChallengeType.PATTERN, ChallengeType.MATH, ChallengeType.QUESTION),
            Difficulty.EASY,
            sessionId = 1,
        )

        val pattern = vm.state.value.challenge as PatternChallenge
        assertEquals(0 to 3, vm.state.value.step to vm.state.value.total)
        vm.submitPattern(pattern.sequence)

        val math = vm.state.value.challenge as MathChallenge
        assertEquals(1, vm.state.value.step)
        vm.submitMath(math.answer)

        val question = vm.state.value.challenge as QuestionChallenge
        assertEquals(2, vm.state.value.step)
        vm.submitChoice(question.answerIndex)

        assertTrue(vm.state.value.completed)
        assertNull(vm.state.value.challenge)
        assertEquals(3, attempts.attempts)
    }

    @Test
    fun `a wrong pattern keeps the same pattern twice, then draws a new one`() = runTest {
        val vm = viewModel()
        vm.start(listOf(ChallengeType.PATTERN), Difficulty.MEDIUM, sessionId = 1)
        val first = vm.state.value.challenge as PatternChallenge

        vm.submitPattern(first.sequence.reversed())
        assertEquals(first, vm.state.value.challenge)
        assertEquals(1 to true, vm.state.value.failedAttempts to vm.state.value.lastAnswerWrong)

        vm.submitPattern(emptyList())
        assertEquals(first, vm.state.value.challenge)
        assertEquals(2, vm.state.value.failedAttempts)

        vm.submitPattern(emptyList())
        assertNotEquals(first, vm.state.value.challenge)
        assertEquals(0 to true, vm.state.value.failedAttempts to vm.state.value.lastAnswerWrong)
        assertEquals(0, vm.state.value.step)
        assertEquals(3, attempts.attempts)
    }

    @Test
    fun `a wrong answer replaces the question without repeating it and never moves back`() = runTest {
        val vm = viewModel()
        vm.start(listOf(ChallengeType.MATH, ChallengeType.QUESTION), Difficulty.EASY, sessionId = 1)
        vm.submitMath((vm.state.value.challenge as MathChallenge).answer)
        val first = vm.state.value.challenge as QuestionChallenge

        vm.submitChoice(0)

        val second = vm.state.value.challenge as QuestionChallenge
        assertNotEquals(first.questionId, second.questionId)
        assertEquals(1, vm.state.value.step)
        assertTrue(vm.state.value.lastAnswerWrong)
        assertFalse(vm.state.value.completed)
    }

    @Test
    fun `an empty bank skips the question rather than locking the user out`() = runTest {
        val vm = viewModel(bank = emptyList())

        vm.start(listOf(ChallengeType.QUESTION), Difficulty.EASY, sessionId = 1)

        assertTrue(vm.state.value.completed)
    }

    @Test
    fun `start is idempotent and an empty list falls back on a pattern`() = runTest {
        val vm = viewModel()
        vm.start(emptyList(), Difficulty.HARD, sessionId = 1)
        val challenge = vm.state.value.challenge

        vm.start(listOf(ChallengeType.MATH), Difficulty.EASY, sessionId = 2)

        assertTrue(challenge is PatternChallenge)
        assertEquals(challenge, vm.state.value.challenge)
        assertEquals(1, vm.state.value.total)
    }
}
