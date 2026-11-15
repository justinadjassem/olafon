package com.olafon.feature.challenges.di

import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import kotlin.random.Random

@Module
@InstallIn(SingletonComponent::class)
object ChallengeModule {
    @Provides
    fun provideRandom(): Random = Random.Default
}
