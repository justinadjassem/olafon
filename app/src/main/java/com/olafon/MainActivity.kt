package com.olafon

import android.content.pm.ApplicationInfo
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
import com.olafon.navigation.OlafonNavHost
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    private val viewModel: MainViewModel by viewModels()

    /** Debug installs get a one-tap test alarm on the home screen for the manual checklist. */
    private val isDebuggable: Boolean
        get() = applicationInfo.flags and ApplicationInfo.FLAG_DEBUGGABLE != 0

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
                    MainDestination.HOME -> OlafonNavHost(showTestActions = isDebuggable)
                }
            }
        }
    }
}
