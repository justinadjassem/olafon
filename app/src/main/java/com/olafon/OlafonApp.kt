package com.olafon

import android.app.Application
import androidx.hilt.work.HiltWorkerFactory
import androidx.work.Configuration
import com.olafon.widget.NextAlarmWidgetUpdater
import dagger.hilt.android.HiltAndroidApp
import javax.inject.Inject

/** WorkManager is initialised on demand with Hilt's factory, so workers can inject use cases. */
@HiltAndroidApp
class OlafonApp :
    Application(),
    Configuration.Provider {
    @Inject lateinit var workerFactory: HiltWorkerFactory

    @Inject lateinit var widgetUpdater: NextAlarmWidgetUpdater

    override fun onCreate() {
        super.onCreate()
        widgetUpdater.start()
    }

    override val workManagerConfiguration: Configuration
        get() = Configuration.Builder().setWorkerFactory(workerFactory).build()
}
