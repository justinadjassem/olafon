package com.olafon.feature.alarm.sound

import android.content.Context
import android.media.AudioManager
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.math.roundToInt
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch

/**
 * Anti-cheat: keeps the alarm stream at its target level while ringing. The ring screen swallows the volume keys,
 * but the user can still reach the system volume panel; every two seconds the level is put back.
 *
 * The target is the alarm volume set before the ring, raised to at least [MIN_LEVEL_RATIO] of the maximum so a
 * phone left on "silent alarm" still wakes its owner.
 */
@Singleton
class VolumeGuard @Inject constructor(@ApplicationContext context: Context) {
    private val audioManager: AudioManager = context.getSystemService(AudioManager::class.java)
    private var job: Job? = null
    private var originalVolume: Int? = null

    fun start(scope: CoroutineScope) {
        stop()
        val max = audioManager.getStreamMaxVolume(AudioManager.STREAM_ALARM)
        val current = audioManager.getStreamVolume(AudioManager.STREAM_ALARM)
        originalVolume = current
        val target = maxOf(current, (max * MIN_LEVEL_RATIO).roundToInt())
        job = scope.launch {
            while (isActive) {
                if (audioManager.getStreamVolume(AudioManager.STREAM_ALARM) != target) {
                    audioManager.setStreamVolume(AudioManager.STREAM_ALARM, target, 0)
                }
                delay(CHECK_INTERVAL_MS)
            }
        }
    }

    /** Stops guarding and restores the level the user had before the ring. */
    fun stop() {
        job?.cancel()
        job = null
        originalVolume?.let { audioManager.setStreamVolume(AudioManager.STREAM_ALARM, it, 0) }
        originalVolume = null
    }

    private companion object {
        const val CHECK_INTERVAL_MS = 2_000L
        const val MIN_LEVEL_RATIO = 0.6
    }
}
