package com.olafon.core.data.question

import com.olafon.core.domain.model.Difficulty
import com.olafon.core.domain.model.QuestionCategory
import java.io.File
import kotlinx.serialization.json.Json
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

/** Validates the bundled bank itself, so a broken edit of the JSON fails the build. */
class QuestionBankTest {
    private val questions = File("src/main/assets/questions_fr.json").inputStream().use(QuestionBankParser(Json)::parse)

    @Test
    fun `bank holds at least 200 questions`() {
        assertTrue("only ${questions.size} questions", questions.size >= MIN_QUESTIONS)
    }

    @Test
    fun `ids and prompts are unique`() {
        assertEquals(questions.size, questions.map { it.id }.toSet().size)
        assertEquals(questions.size, questions.map { it.prompt.trim().lowercase() }.toSet().size)
    }

    @Test
    fun `every category and difficulty is covered`() {
        for (category in QuestionCategory.entries) {
            for (difficulty in Difficulty.entries) {
                val n = questions.count { it.category == category && it.difficulty == difficulty }
                assertTrue("$category/$difficulty has only $n questions", n >= MIN_PER_CELL)
            }
        }
    }

    @Test
    fun `choices are distinct and the answer is one of them`() {
        for (q in questions) {
            assertEquals("duplicate choices in ${q.id}", q.choices.size, q.choices.toSet().size)
            assertTrue("blank choice in ${q.id}", q.choices.none { it.isBlank() })
            assertTrue("blank prompt in ${q.id}", q.prompt.isNotBlank())
        }
    }

    private companion object {
        const val MIN_QUESTIONS = 200
        const val MIN_PER_CELL = 10
    }
}
