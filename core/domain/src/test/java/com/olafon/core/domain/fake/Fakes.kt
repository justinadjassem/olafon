package com.olafon.core.domain.fake

import com.olafon.core.domain.model.Alarm
import com.olafon.core.domain.model.ThemeMode
import com.olafon.core.domain.model.UserPreferences
import com.olafon.core.domain.model.WakeSession
import com.olafon.core.domain.repository.AlarmRepository
import com.olafon.core.domain.repository.PreferencesRepository
import com.olafon.core.domain.repository.WakeSessionRepository
import com.olafon.core.domain.scheduler.AlarmScheduler
import java.time.Instant
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.map

class FakeAlarmRepository(initial: List<Alarm> = emptyList()) : AlarmRepository {
    private val alarms = MutableStateFlow(initial.associateBy { it.id })
    private var nextId = (initial.maxOfOrNull { it.id } ?: 0L) + 1

    override fun observeAll(): Flow<List<Alarm>> = alarms.map { it.values.toList() }

    override suspend fun get(id: Long): Alarm? = alarms.value[id]

    override suspend fun enabled(): List<Alarm> = alarms.value.values.filter { it.enabled }

    override suspend fun upsert(alarm: Alarm): Long {
        val id = if (alarm.id == 0L) nextId++ else alarm.id
        alarms.value = alarms.value + (id to alarm.copy(id = id))
        return id
    }

    override suspend fun delete(id: Long) {
        alarms.value = alarms.value - id
    }
}

class FakeWakeSessionRepository : WakeSessionRepository {
    val sessions = linkedMapOf<Long, WakeSession>()
    private var nextId = 1L

    override suspend fun start(alarmId: Long, ringAt: Instant): Long {
        val id = nextId++
        sessions[id] = WakeSession(id = id, alarmId = alarmId, ringAt = ringAt)
        return id
    }

    override suspend fun recordAttempt(sessionId: Long) = update(sessionId) { copy(attempts = attempts + 1) }

    override suspend fun recordSnooze(sessionId: Long) = update(sessionId) { copy(snoozes = snoozes + 1) }

    override suspend fun dismiss(sessionId: Long, at: Instant, emergency: Boolean) =
        update(sessionId) { copy(dismissedAt = at, emergencyExit = emergency) }

    override suspend fun get(sessionId: Long): WakeSession? = sessions[sessionId]

    override fun observeRecent(limit: Int): Flow<List<WakeSession>> =
        MutableStateFlow(sessions.values.sortedByDescending { it.ringAt }.take(limit))

    private fun update(id: Long, change: WakeSession.() -> WakeSession) {
        sessions[id] = sessions.getValue(id).change()
    }
}

class RecordingScheduler : AlarmScheduler {
    val scheduled = mutableMapOf<Long, Instant>()
    val snoozes = mutableMapOf<Long, Pair<Long, Instant>>()
    val cancelled = mutableListOf<Long>()
    val cancelledSnoozes = mutableListOf<Long>()

    override fun schedule(alarm: Alarm, triggerAt: Instant) {
        scheduled[alarm.id] = triggerAt
    }

    override fun cancel(alarmId: Long) {
        scheduled.remove(alarmId)
        cancelled += alarmId
    }

    override fun scheduleSnooze(alarm: Alarm, sessionId: Long, triggerAt: Instant) {
        snoozes[alarm.id] = sessionId to triggerAt
    }

    override fun cancelSnooze(alarmId: Long) {
        snoozes.remove(alarmId)
        cancelledSnoozes += alarmId
    }
}

class FakePreferencesRepository(private var emergencyCode: String? = null) : PreferencesRepository {
    private val state = MutableStateFlow(UserPreferences(hasEmergencyExitCode = emergencyCode != null))

    override fun observe(): Flow<UserPreferences> = state

    override suspend fun markFirstLaunchDone() = state.update { copy(firstLaunchDone = true) }

    override suspend fun setTheme(theme: ThemeMode) = state.update { copy(theme = theme) }

    override suspend fun setEveningSyncHour(hour: Int) = state.update { copy(eveningSyncHour = hour) }

    override suspend fun setEmergencyExitCode(code: String?) {
        emergencyCode = code
        state.update { copy(hasEmergencyExitCode = code != null) }
    }

    override suspend fun verifyEmergencyExitCode(code: String): Boolean = emergencyCode != null && emergencyCode == code

    private fun MutableStateFlow<UserPreferences>.update(change: UserPreferences.() -> UserPreferences) {
        value = value.change()
    }
}
