package com.olafon.core.data.di

import com.olafon.core.data.repository.AlarmRepositoryImpl
import com.olafon.core.data.repository.PreferencesRepositoryImpl
import com.olafon.core.data.repository.QuestionRepositoryImpl
import com.olafon.core.data.repository.WakeSessionRepositoryImpl
import com.olafon.core.domain.repository.AlarmRepository
import com.olafon.core.domain.repository.PreferencesRepository
import com.olafon.core.domain.repository.QuestionRepository
import com.olafon.core.domain.repository.WakeSessionRepository
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent

@Module
@InstallIn(SingletonComponent::class)
abstract class RepositoryModule {
    @Binds
    abstract fun bindAlarmRepository(impl: AlarmRepositoryImpl): AlarmRepository

    @Binds
    abstract fun bindQuestionRepository(impl: QuestionRepositoryImpl): QuestionRepository

    @Binds
    abstract fun bindWakeSessionRepository(impl: WakeSessionRepositoryImpl): WakeSessionRepository

    @Binds
    abstract fun bindPreferencesRepository(impl: PreferencesRepositoryImpl): PreferencesRepository
}
