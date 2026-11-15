package com.olafon.feature.challenges.session

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.olafon.core.domain.challenge.Challenge
import com.olafon.core.domain.challenge.MathChallenge
import com.olafon.core.domain.challenge.MathGenerator
import com.olafon.core.domain.challenge.NextQuestionChallengeUseCase
import com.olafon.core.domain.challenge.PatternChallenge
import com.olafon.core.domain.challenge.PatternGenerator
import com.olafon.core.domain.challenge.QuestionChallenge
import com.olafon.core.domain.model.ChallengeType
import com.olafon.core.domain.model.Difficulty
import com.olafon.core.domain.usecase.RecordChallengeAttemptUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlin.random.Random
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class ChallengeSessionUiState(
    /** Zero-based index of the current challenge in the alarm's list. */
    val step: Int = 0,
    val total: Int = 0,
    /** Null while the next challenge is being prepared. */
    val challenge: Challenge? = null,
    /** Wrong answers given on the current challenge instance. */
    val failedAttempts: Int = 0,
    /** True right after a wrong answer, until the user acts again. */
    val lastAnswerWrong: Boolean = false,
    /** True once every challenge is solved: the caller dismisses the alarm. */
    val completed: Boolean = false,
)

/**
 * Runs the alarm's challenges in order. Every answer counts as an attempt on the wake session. A wrong answer
 * never moves the user back: a pattern or an operation gets [MAX_ATTEMPTS] tries before a new one is drawn, and
 * a question is always replaced by another.
 */
@HiltViewModel
class ChallengeSessionViewModel @Inject constructor(
    private val patterns: PatternGenerator,
    private val maths: MathGenerator,
    private val nextQuestion: NextQuestionChallengeUseCase,
    private val recordAttempt: RecordChallengeAttemptUseCase,
    private val random: Random,
) : ViewModel() {
    private val _state = MutableStateFlow(ChallengeSessionUiState())
    val state: StateFlow<ChallengeSessionUiState> = _state.asStateFlow()

    private var types: List<ChallengeType> = emptyList()
    private var difficulty = Difficulty.MEDIUM
    private var sessionId = 0L
    private val askedQuestions = mutableListOf<String>()

    /** Idempotent: a recomposition or a rotation never restarts the session. */
    fun start(types: List<ChallengeType>, difficulty: Difficulty, sessionId: Long) {
        if (this.types.isNotEmpty()) return
        this.types = types.ifEmpty { listOf(ChallengeType.PATTERN) }
        this.difficulty = difficulty
        this.sessionId = sessionId
        _state.update { it.copy(total = this.types.size) }
        load(step = 0)
    }

    fun submitPattern(input: List<Int>) {
        val challenge = _state.value.challenge as? PatternChallenge ?: return
        answer(challenge.isSolvedBy(input))
    }

    fun submitChoice(index: Int) {
        val challenge = _state.value.challenge as? QuestionChallenge ?: return
        answer(challenge.isSolvedBy(index))
    }

    fun submitMath(answer: Int?) {
        val challenge = _state.value.challenge as? MathChallenge ?: return
        answer(challenge.isSolvedBy(answer))
    }

    fun onWrongFeedbackShown() {
        _state.update { it.copy(lastAnswerWrong = false) }
    }

    private fun answer(correct: Boolean) {
        viewModelScope.launch { recordAttempt(sessionId) }
        if (correct) {
            advance()
        } else {
            retry()
        }
    }

    private fun advance() {
        val next = _state.value.step + 1
        if (next >= types.size) {
            _state.update { it.copy(challenge = null, lastAnswerWrong = false, completed = true) }
        } else {
            load(next)
        }
    }

    private fun retry() {
        val current = _state.value
        val failed = current.failedAttempts + 1
        val replace = current.challenge is QuestionChallenge || failed >= MAX_ATTEMPTS
        if (replace) {
            load(current.step, wrong = true)
        } else {
            _state.update { it.copy(failedAttempts = failed, lastAnswerWrong = true) }
        }
    }

    private fun load(step: Int, wrong: Boolean = false) {
        _state.update { it.copy(step = step, challenge = null, failedAttempts = 0, lastAnswerWrong = wrong) }
        when (types[step]) {
            ChallengeType.PATTERN -> show(patterns.generate(difficulty, random))
            ChallengeType.MATH -> show(maths.generate(difficulty, random))
            ChallengeType.QUESTION -> viewModelScope.launch {
                val question = nextQuestion(difficulty, askedQuestions)
                if (question == null) {
                    // Empty bank: this challenge cannot be played, move on rather than lock the user out.
                    advance()
                } else {
                    askedQuestions += question.questionId
                    show(question)
                }
            }
        }
    }

    private fun show(challenge: Challenge) {
        _state.update { it.copy(challenge = challenge) }
    }

    companion object {
        /** Tries on one pattern or operation before a new one is drawn. */
        const val MAX_ATTEMPTS = 3
    }
}
