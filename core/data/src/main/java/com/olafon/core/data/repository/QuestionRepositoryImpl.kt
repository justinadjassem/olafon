package com.olafon.core.data.repository

import android.content.Context
import com.olafon.core.data.db.dao.QuestionDao
import com.olafon.core.data.mapper.toDomain
import com.olafon.core.data.mapper.toEntity
import com.olafon.core.data.question.QuestionBankParser
import com.olafon.core.domain.model.Difficulty
import com.olafon.core.domain.model.Question
import com.olafon.core.domain.model.QuestionCategory
import com.olafon.core.domain.repository.QuestionRepository
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.withContext
import kotlinx.serialization.json.Json

@Singleton
class QuestionRepositoryImpl @Inject constructor(
    @ApplicationContext private val context: Context,
    private val dao: QuestionDao,
    private val parser: QuestionBankParser,
    private val json: Json,
) : QuestionRepository {
    private val seedLock = Mutex()

    override suspend fun ensureSeeded() = seedLock.withLock {
        if (dao.count() > 0) return
        val questions = withContext(Dispatchers.IO) {
            context.assets.open(ASSET_NAME).use(parser::parse)
        }
        dao.insertAll(questions.map { it.toEntity(json) })
    }

    override suspend fun count(): Int = dao.count()

    override suspend fun random(
        difficulty: Difficulty,
        category: QuestionCategory?,
        excludeIds: Set<String>,
    ): Question? {
        ensureSeeded()
        val entity = dao.random(difficulty.name, category?.name, excludeIds)
            ?: dao.randomAnyDifficulty(category?.name, excludeIds)
        return entity?.toDomain(json)
    }

    companion object {
        const val ASSET_NAME = "questions_fr.json"
    }
}
