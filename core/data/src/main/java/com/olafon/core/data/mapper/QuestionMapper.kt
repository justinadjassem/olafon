package com.olafon.core.data.mapper

import com.olafon.core.data.db.entity.QuestionEntity
import com.olafon.core.domain.model.Difficulty
import com.olafon.core.domain.model.Question
import com.olafon.core.domain.model.QuestionCategory
import kotlinx.serialization.json.Json

fun Question.toEntity(json: Json): QuestionEntity = QuestionEntity(
    id = id,
    category = category.name,
    difficulty = difficulty.name,
    prompt = prompt,
    choices = json.encodeToString(choices),
    answerIndex = answerIndex,
)

fun QuestionEntity.toDomain(json: Json): Question = Question(
    id = id,
    category = QuestionCategory.valueOf(category),
    difficulty = Difficulty.valueOf(difficulty),
    prompt = prompt,
    choices = json.decodeFromString<List<String>>(choices),
    answerIndex = answerIndex,
)
