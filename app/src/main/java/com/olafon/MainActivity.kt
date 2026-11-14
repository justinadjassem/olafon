package com.olafon

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.runtime.getValue
import androidx.lifecycle.compose.LifecycleResumeEffect
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.olafon.core.designsystem.theme.OlafonTheme
import com.olafon.feature.alarm.onboarding.OnboardingScreen
import com.olafon.home.TestAlarmScreen
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    private val viewModel: MainViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            OlafonTheme {
                val destination by viewModel.destination.collectAsStateWithLifecycle()
                LifecycleResumeEffect(Unit) {
                    viewModel.refreshPermissions()
                    onPauseOrDispose { }
                }
                when (destination) {
                    MainDestination.LOADING -> Unit
                    MainDestination.ONBOARDING -> OnboardingScreen(onDone = viewModel::refreshPermissions)
                    MainDestination.HOME -> TestAlarmScreen()
                }
            }
        }
    }
}
