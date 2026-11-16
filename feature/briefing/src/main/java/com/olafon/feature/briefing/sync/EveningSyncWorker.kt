package com.olafon.feature.briefing.sync

import android.content.Context
import androidx.hilt.work.HiltWorker
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.olafon.core.domain.usecase.SyncBriefingUseCase
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject

/** Fills the briefing cache in the evening so the morning briefing needs neither network nor a source. */
@HiltWorker
class EveningSyncWorker @AssistedInject constructor(
    @Assisted context: Context,
    @Assisted params: WorkerParameters,
    private val syncBriefing: SyncBriefingUseCase,
) : CoroutineWorker(context, params) {
    override suspend fun doWork(): Result = runCatching {
        syncBriefing()
    }.fold({ Result.success() }, { Result.retry() })
}
