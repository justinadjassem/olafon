package com.olafon.feature.alarm.ring

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.KeyEvent
import android.view.WindowManager
import androidx.activity.ComponentActivity
import androidx.activity.addCallback
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import com.olafon.core.designsystem.theme.OlafonTheme
import com.olafon.feature.alarm.receiver.AlarmReceiver
import dagger.hilt.android.AndroidEntryPoint

/**
 * Shown over the lock screen by the ring notification's full-screen intent. It is a view on the ringing service:
 * finishing it, pressing back or the volume keys never stops the sound.
 */
@AndroidEntryPoint
class RingActivity : ComponentActivity() {
    private val viewModel: RingViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setShowWhenLocked(true)
        setTurnScreenOn(true)
        window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
        // Back must not leave the ring screen: the only ways out are the challenges, a snooze or the emergency code.
        onBackPressedDispatcher.addCallback(this) { }
        setContent {
            OlafonTheme {
                RingScreen(viewModel = viewModel, onFinished = ::finish)
            }
        }
    }

    /** Anti-cheat: the volume keys are swallowed so the alarm cannot be turned down from the ring screen. */
    override fun onKeyDown(keyCode: Int, event: KeyEvent): Boolean = when (keyCode) {
        KeyEvent.KEYCODE_VOLUME_DOWN, KeyEvent.KEYCODE_VOLUME_UP, KeyEvent.KEYCODE_VOLUME_MUTE -> true
        else -> super.onKeyDown(keyCode, event)
    }

    companion object {
        fun intent(context: Context, alarmId: Long, sessionId: Long?): Intent =
            Intent(context, RingActivity::class.java)
                .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_NO_USER_ACTION)
                .putExtra(AlarmReceiver.EXTRA_ALARM_ID, alarmId)
                .putExtra(AlarmReceiver.EXTRA_SESSION_ID, sessionId ?: AlarmReceiver.NO_ID)
    }
}
