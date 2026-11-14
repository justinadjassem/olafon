package com.olafon.core.data.question

import com.olafon.core.domain.model.Difficulty
import com.olafon.core.domain.model.Question
import com.olafon.core.domain.model.QuestionCategory
import java.io.InputStream
import javax.inject.Inject
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.decodeFromStream

/** Turns the bundled JSON bank into domain questions. Fails fast on a malformed file: the bank ships with the app. */
class QuestionBankParser @Inject constructor(private val json: Json) {
    fun parse(stream: InputStream): List<Question> =
        json.decodeFromStream<QuestionBankDto>(stream).questions.map { it.toDomain() }

    private fun QuestionDto.toDomain(): Question = Question(
        id = id,
        category = parseCategory(categorie),
        difficulty = parseDifficulty(niveau),
        prompt = question,
        choices = choix,
        answerIndex = reponse,
    )

    private fun parseCategory(label: String): QuestionCategory = when (label) {
        "culture" -> QuestionCategory.CULTURE
        "logique" -> QuestionCategory.LOGIC
        "vocabulaire" -> QuestionCategory.VOCABULARY
        else -> throw IllegalArgumentException("unknown question category: $label")
    }

    private fun parseDifficulty(label: String): Difficulty = when (label) {
        "facile" -> Difficulty.EASY
        "moyen" -> Difficulty.MEDIUM
        "difficile" -> Difficulty.HARD
        else -> throw IllegalArgumentException("unknown difficulty: $label")
    }
}
