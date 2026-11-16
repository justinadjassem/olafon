package com.olafon.navigation

import com.olafon.feature.home.edit.EditAlarmViewModel

/** Routes of the main app. The ring and its challenges live in their own activity, started by the alarm engine. */
object Routes {
    const val HOME = "home"
    const val SETTINGS = "settings"
    const val BRIEFING = "briefing"
    const val EDIT_ALARM = "alarm/edit/{${EditAlarmViewModel.ARG_ALARM_ID}}"

    /** Intent extra carrying a route to open, set by the launcher shortcuts and the widget. */
    const val EXTRA_ROUTE = "com.olafon.extra.ROUTE"

    /** Only these may be opened from outside the app. */
    val EXTERNAL = setOf("alarm/edit/0", BRIEFING)

    fun editAlarm(id: Long): String = "alarm/edit/$id"
}
