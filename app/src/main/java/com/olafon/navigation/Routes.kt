package com.olafon.navigation

import com.olafon.feature.home.edit.EditAlarmViewModel

/** Routes of the main app. The ring and its challenges live in their own activity, started by the alarm engine. */
object Routes {
    const val HOME = "home"
    const val SETTINGS = "settings"
    const val EDIT_ALARM = "alarm/edit/{${EditAlarmViewModel.ARG_ALARM_ID}}"

    fun editAlarm(id: Long): String = "alarm/edit/$id"
}
