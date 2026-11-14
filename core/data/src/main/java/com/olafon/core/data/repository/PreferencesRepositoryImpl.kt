package com.olafon.core.data.repository

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import com.olafon.core.data.security.SecretHasher
import com.olafon.core.domain.model.ThemeMode
import com.olafon.core.domain.model.UserPreferences
import com.olafon.core.domain.repository.PreferencesRepository
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map

@Singleton
class PreferencesRepositoryImpl @Inject constructor(
    private val dataStore: DataStore<Preferences>,
    private val hasher: SecretHasher,
) : PreferencesRepository {
    override fun observe(): Flow<UserPreferences> = dataStore.data.map { prefs ->
        UserPreferences(
            firstLaunchDone = prefs[Keys.FIRST_LAUNCH_DONE] ?: false,
            theme = prefs[Keys.THEME]?.let { runCatching { ThemeMode.valueOf(it) }.getOrNull() } ?: ThemeMode.SYSTEM,
            hasEmergencyExitCode = prefs[Keys.EMERGENCY_CODE_HASH] != null,
            eveningSyncHour = prefs[Keys.EVENING_SYNC_HOUR] ?: UserPreferences.DEFAULT_EVENING_SYNC_HOUR,
        )
    }

    override suspend fun markFirstLaunchDone() {
        dataStore.edit { it[Keys.FIRST_LAUNCH_DONE] = true }
    }

    override suspend fun setTheme(theme: ThemeMode) {
        dataStore.edit { it[Keys.THEME] = theme.name }
    }

    override suspend fun setEveningSyncHour(hour: Int) {
        require(hour in 0..MAX_HOUR) { "hour must be in 0..23, was $hour" }
        dataStore.edit { it[Keys.EVENING_SYNC_HOUR] = hour }
    }

    override suspend fun setEmergencyExitCode(code: String?) {
        dataStore.edit { prefs ->
            if (code.isNullOrBlank()) {
                prefs.remove(Keys.EMERGENCY_CODE_HASH)
            } else {
                prefs[Keys.EMERGENCY_CODE_HASH] = hasher.hash(code)
            }
        }
    }

    override suspend fun verifyEmergencyExitCode(code: String): Boolean {
        val stored = dataStore.data.first()[Keys.EMERGENCY_CODE_HASH] ?: return false
        return hasher.matches(code, stored)
    }

    private object Keys {
        val FIRST_LAUNCH_DONE = booleanPreferencesKey("first_launch_done")
        val THEME = stringPreferencesKey("theme")
        val EMERGENCY_CODE_HASH = stringPreferencesKey("emergency_code_hash")
        val EVENING_SYNC_HOUR = intPreferencesKey("evening_sync_hour")
    }

    private companion object {
        const val MAX_HOUR = 23
    }
}
