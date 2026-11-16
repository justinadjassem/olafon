package com.olafon

import android.content.Intent
import android.content.pm.ApplicationInfo
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.compose.LifecycleResumeEffect
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.olafon.core.designsystem.theme.OlafonTheme
import com.olafon.feature.alarm.onboarding.OnboardingScreen
import com.olafon.navigation.OlafonNavHost
import com.olafon.navigation.Routes
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    private val viewModel: MainViewModel by viewModels()

    /** Debug installs get a one-tap test alarm on the home screen for the manual checklist. */
    private val isDebuggable: Boolean
        get() = applicationInfo.flags and ApplicationInfo.FLAG_DEBUGGABLE != 0

    /** Route asked by a launcher shortcut or the widget, consumed once the main graph is on screen. */
    private var pendingRoute by mutableStateOf<String?>(null)

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        pendingRoute = intent.externalRoute()
    }

    private fun Intent.externalRoute(): String? = getStringExtra(Routes.EXTRA_ROUTE)?.takeIf { it in Routes.EXTERNAL }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        if (savedInstanceState == null) pendingRoute = intent.externalRoute()
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
                    MainDestination.HOME -> OlafonNavHost(
                        showTestActions = isDebuggable,
                        openRoute = pendingRoute,
                        onRouteOpened = { pendingRoute = null },
                    )
                }
            }
        }
    }
}
