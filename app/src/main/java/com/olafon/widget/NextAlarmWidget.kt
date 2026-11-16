package com.olafon.widget

import android.content.Context
import android.content.Intent
import androidx.compose.runtime.Composable
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.glance.GlanceId
import androidx.glance.GlanceModifier
import androidx.glance.ImageProvider
import androidx.glance.action.clickable
import androidx.glance.appwidget.GlanceAppWidget
import androidx.glance.appwidget.action.actionStartActivity
import androidx.glance.appwidget.provideContent
import androidx.glance.background
import androidx.glance.layout.Alignment
import androidx.glance.layout.Column
import androidx.glance.layout.Row
import androidx.glance.layout.Spacer
import androidx.glance.layout.fillMaxSize
import androidx.glance.layout.padding
import androidx.glance.layout.width
import androidx.glance.text.FontWeight
import androidx.glance.text.Text
import androidx.glance.text.TextStyle
import androidx.glance.unit.ColorProvider
import com.olafon.MainActivity
import com.olafon.R
import com.olafon.core.designsystem.theme.OlafonColors
import com.olafon.core.domain.repository.AlarmRepository
import com.olafon.core.domain.usecase.ComputeNextTriggerUseCase
import com.olafon.navigation.Routes
import dagger.hilt.EntryPoint
import dagger.hilt.InstallIn
import dagger.hilt.android.EntryPointAccessors
import dagger.hilt.components.SingletonComponent
import java.time.Clock
import java.time.ZonedDateTime
import java.time.format.DateTimeFormatter
import java.util.Locale

/**
 * Home-screen widget: the time and day of the next enabled alarm. It shows a date, never a countdown, so it stays
 * true between refreshes. A tap opens the app, or the editor when no alarm is set.
 */
class NextAlarmWidget : GlanceAppWidget() {
    @EntryPoint
    @InstallIn(SingletonComponent::class)
    interface Dependencies {
        fun alarms(): AlarmRepository

        fun computeNextTrigger(): ComputeNextTriggerUseCase

        fun clock(): Clock
    }

    override suspend fun provideGlance(context: Context, id: GlanceId) {
        val deps = EntryPointAccessors.fromApplication(context, Dependencies::class.java)
        val now = ZonedDateTime.now(deps.clock())
        val next = runCatching {
            deps.alarms().enabled().map { deps.computeNextTrigger()(it, now).atZone(now.zone) }.minOrNull()
        }.getOrNull()
        provideContent { Content(context, next) }
    }

    @Composable
    private fun Content(context: Context, next: ZonedDateTime?) {
        val open = Intent(context, MainActivity::class.java)
            .setAction(Intent.ACTION_VIEW)
            .apply { if (next == null) putExtra(Routes.EXTRA_ROUTE, "alarm/edit/0") }
        Row(
            modifier = GlanceModifier
                .fillMaxSize()
                .background(ImageProvider(R.drawable.widget_background))
                .clickable(actionStartActivity(open))
                .padding(horizontal = 18.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(
                text = next?.format(DateTimeFormatter.ofPattern("HH:mm")) ?: "--:--",
                style = TextStyle(
                    color = ColorProvider(OlafonColors.Lime),
                    fontSize = 34.sp,
                    fontWeight = FontWeight.Bold,
                ),
            )
            Spacer(GlanceModifier.width(14.dp))
            Column {
                Text(
                    text = context.getString(
                        if (next ==
                            null
                        ) {
                            R.string.widget_no_alarm
                        } else {
                            R.string.widget_next_alarm_title
                        },
                    ),
                    style = TextStyle(
                        color = ColorProvider(OlafonColors.OnSurface),
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Medium,
                    ),
                )
                Text(
                    text = next?.format(DateTimeFormatter.ofPattern("EEE d MMM", Locale.FRENCH))
                        ?: context.getString(R.string.widget_tap_to_add),
                    style = TextStyle(color = ColorProvider(OlafonColors.Muted), fontSize = 12.sp),
                )
            }
        }
    }
}
