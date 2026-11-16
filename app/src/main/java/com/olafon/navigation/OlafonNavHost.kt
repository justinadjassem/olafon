package com.olafon.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.res.stringResource
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.olafon.R
import com.olafon.feature.briefing.BriefingScreen
import com.olafon.feature.home.edit.EditAlarmScreen
import com.olafon.feature.home.edit.EditAlarmViewModel
import com.olafon.feature.home.home.HomeScreen
import com.olafon.feature.home.settings.SettingsScreen

@Composable
fun OlafonNavHost(showTestActions: Boolean, openRoute: String? = null, onRouteOpened: () -> Unit = {}) {
    val navController = rememberNavController()
    // A launcher shortcut or the widget asks for a screen: open it above home, once.
    LaunchedEffect(openRoute) {
        if (openRoute != null) {
            navController.navigate(openRoute) { launchSingleTop = true }
            onRouteOpened()
        }
    }
    NavHost(navController = navController, startDestination = Routes.HOME) {
        composable(Routes.HOME) {
            HomeScreen(
                onNewAlarm = { navController.navigate(Routes.editAlarm(EditAlarmViewModel.NEW_ALARM_ID)) },
                onEditAlarm = { navController.navigate(Routes.editAlarm(it)) },
                onSettings = { navController.navigate(Routes.SETTINGS) },
                onDay = { navController.navigate(Routes.BRIEFING) },
                showTestActions = showTestActions,
            )
        }
        composable(
            route = Routes.EDIT_ALARM,
            arguments = listOf(navArgument(EditAlarmViewModel.ARG_ALARM_ID) { type = NavType.LongType }),
        ) {
            EditAlarmScreen(onClose = { navController.popBackStack() })
        }
        composable(Routes.BRIEFING) {
            BriefingScreen(closeLabel = stringResource(R.string.action_back), onClose = {
                navController.popBackStack()
            })
        }
        composable(Routes.SETTINGS) {
            SettingsScreen(onBack = { navController.popBackStack() })
        }
    }
}
