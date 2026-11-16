package com.olafon.feature.alarm.onboarding

import android.Manifest
import android.app.AlarmManager
import android.app.NotificationManager
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.PowerManager
import android.provider.Settings
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat
import com.olafon.feature.alarm.R
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton

/** Reads the permissions a reliable alarm needs and builds the settings intents that grant the missing ones. */
@Singleton
class AlarmPermissions @Inject constructor(@ApplicationContext private val context: Context) {
    data class Status(
        val notifications: Boolean,
        val fullScreenIntent: Boolean,
        val exactAlarms: Boolean,
        val batteryUnrestricted: Boolean,
    ) {
        /** Battery is a recommendation, the other three are required to ring on the lock screen. */
        val requiredGranted: Boolean get() = notifications && fullScreenIntent && exactAlarms
    }

    private val notificationManager = context.getSystemService(NotificationManager::class.java)
    private val alarmManager = context.getSystemService(AlarmManager::class.java)
    private val powerManager = context.getSystemService(PowerManager::class.java)

    fun status(): Status = Status(
        notifications = notificationsEnabled(),
        fullScreenIntent = Build.VERSION.SDK_INT < Build.VERSION_CODES.UPSIDE_DOWN_CAKE ||
            notificationManager.canUseFullScreenIntent(),
        exactAlarms = alarmManager.canScheduleExactAlarms(),
        batteryUnrestricted = powerManager.isIgnoringBatteryOptimizations(context.packageName),
    )

    /** True on Android 13+, where POST_NOTIFICATIONS is a runtime permission to request in-app. */
    val notificationsNeedRuntimeRequest: Boolean = Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU

    /** Null when the version grants full-screen intents without asking (before Android 14). */
    fun fullScreenSettingsIntent(): Intent? = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE) {
        Intent(Settings.ACTION_MANAGE_APP_USE_FULL_SCREEN_INTENT, packageUri())
    } else {
        null
    }

    /** Android 12 only: later versions grant USE_EXACT_ALARM to alarm clock apps at install. */
    fun exactAlarmSettingsIntent(): Intent? =
        if (Build.VERSION.SDK_INT in Build.VERSION_CODES.S..Build.VERSION_CODES.S_V2) {
            Intent(Settings.ACTION_REQUEST_SCHEDULE_EXACT_ALARM, packageUri())
        } else {
            null
        }

    fun notificationSettingsIntent(): Intent = Intent(Settings.ACTION_APP_NOTIFICATION_SETTINGS)
        .putExtra(Settings.EXTRA_APP_PACKAGE, context.packageName)

    fun batterySettingsIntent(): Intent = Intent(Settings.ACTION_IGNORE_BATTERY_OPTIMIZATION_SETTINGS)

    /** Brand-specific hint, because each manufacturer hides its own app-killing switch. */
    fun batteryTipRes(): Int {
        val manufacturer = Build.MANUFACTURER.lowercase()
        return when {
            "xiaomi" in manufacturer || "redmi" in manufacturer || "poco" in manufacturer ->
                R.string.onboarding_battery_tip_xiaomi
            "tecno" in manufacturer || "infinix" in manufacturer || "itel" in manufacturer ->
                R.string.onboarding_battery_tip_transsion
            "samsung" in manufacturer -> R.string.onboarding_battery_tip_samsung
            else -> R.string.onboarding_battery_tip_generic
        }
    }

    private fun notificationsEnabled(): Boolean = if (notificationsNeedRuntimeRequest) {
        ContextCompat.checkSelfPermission(context, Manifest.permission.POST_NOTIFICATIONS) ==
            PackageManager.PERMISSION_GRANTED
    } else {
        NotificationManagerCompat.from(context).areNotificationsEnabled()
    }

    private fun packageUri(): Uri = Uri.fromParts("package", context.packageName, null)
}
