package com.olafon.core.domain.repository

import com.olafon.core.domain.model.ThemeMode
import com.olafon.core.domain.model.UserPreferences
import kotlinx.coroutines.flow.Flow

interface PreferencesRepository {
    fun observe(): Flow<UserPreferences>

    suspend fun markFirstLaunchDone()

    suspend fun setTheme(theme: ThemeMode)

    suspend fun setEveningSyncHour(hour: Int)

    /** Stores a new emergency exit code, or removes it when [code] is null. Only a hash is persisted. */
    suspend fun setEmergencyExitCode(code: String?)

    /** True when [code] matches the stored emergency exit code. Always false when none is set. */
    suspend fun verifyEmergencyExitCode(code: String): Boolean
}
