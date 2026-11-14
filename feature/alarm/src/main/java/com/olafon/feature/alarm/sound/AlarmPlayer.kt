package com.olafon.feature.alarm.sound

import android.content.Context
import android.media.RingtoneManager
import android.net.Uri
import android.provider.Settings
import android.util.Log
import androidx.core.net.toUri
import androidx.media3.common.AudioAttributes
import androidx.media3.common.C
import androidx.media3.common.MediaItem
import androidx.media3.common.PlaybackException
import androidx.media3.common.Player
import androidx.media3.exoplayer.ExoPlayer
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

/**
 * Plays the ringtone in a loop on the alarm stream, with an optional volume ramp, and vibrates. Must be driven
 * from the main thread, like ExoPlayer itself.
 */
@Singleton
class AlarmPlayer @Inject constructor(
    @ApplicationContext private val context: Context,
    private val vibration: AlarmVibration,
    private val volumeGuard: VolumeGuard,
) {
    private var player: ExoPlayer? = null
    private var rampJob: Job? = null

    fun start(ringtoneUri: String?, progressive: Boolean, scope: CoroutineScope) {
        stop()
        val candidates = ringtoneCandidates(ringtoneUri)
        val exo = ExoPlayer.Builder(context)
            .setAudioAttributes(ALARM_ATTRIBUTES, HANDLE_AUDIO_FOCUS)
            .setWakeMode(C.WAKE_MODE_LOCAL)
            .build()
        exo.repeatMode = Player.REPEAT_MODE_ONE
        exo.addListener(FallbackOnError(exo, candidates))
        exo.setMediaItem(MediaItem.fromUri(candidates.first()))
        exo.volume = if (progressive) RAMP_START_VOLUME else 1f
        exo.prepare()
        exo.play()
        player = exo
        if (progressive) rampJob = scope.launch { ramp(exo) }
        volumeGuard.start(scope)
        vibration.start()
    }

    fun stop() {
        rampJob?.cancel()
        rampJob = null
        volumeGuard.stop()
        vibration.stop()
        player?.release()
        player = null
    }

    private suspend fun ramp(exo: ExoPlayer) {
        val steps = (RAMP_DURATION_MS / RAMP_STEP_MS).toInt()
        repeat(steps) { step ->
            delay(RAMP_STEP_MS)
            exo.volume = RAMP_START_VOLUME + (1f - RAMP_START_VOLUME) * (step + 1) / steps
        }
    }

    /** The user's choice first, then the system alarm sound, then the system ringtone as a last resort. */
    private fun ringtoneCandidates(ringtoneUri: String?): List<Uri> = listOfNotNull(
        ringtoneUri?.toUri(),
        RingtoneManager.getActualDefaultRingtoneUri(context, RingtoneManager.TYPE_ALARM),
        Settings.System.DEFAULT_ALARM_ALERT_URI,
        Settings.System.DEFAULT_RINGTONE_URI,
    ).distinct()

    /** A custom ringtone may be unreadable (deleted file, locked storage before first unlock): try the next one. */
    private class FallbackOnError(private val exo: ExoPlayer, private val candidates: List<Uri>) : Player.Listener {
        private var index = 0

        override fun onPlayerError(error: PlaybackException) {
            index++
            if (index >= candidates.size) {
                Log.e(TAG, "No playable ringtone", error)
                return
            }
            Log.w(TAG, "Ringtone ${candidates[index - 1]} failed, trying ${candidates[index]}", error)
            exo.setMediaItem(MediaItem.fromUri(candidates[index]))
            exo.prepare()
            exo.play()
        }
    }

    private companion object {
        const val TAG = "AlarmPlayer"

        /** An alarm must play over anything else, so it never asks for audio focus. */
        const val HANDLE_AUDIO_FOCUS = false
        const val RAMP_START_VOLUME = 0.1f
        const val RAMP_DURATION_MS = 45_000L
        const val RAMP_STEP_MS = 1_000L
        val ALARM_ATTRIBUTES: AudioAttributes = AudioAttributes.Builder()
            .setUsage(C.USAGE_ALARM)
            .setContentType(C.AUDIO_CONTENT_TYPE_SONIFICATION)
            .build()
    }
}
