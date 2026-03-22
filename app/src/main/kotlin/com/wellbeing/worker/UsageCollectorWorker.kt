package com.wellbeing.worker

import android.content.Context
import androidx.hilt.work.HiltWorker
import androidx.work.*
import com.wellbeing.core.data.repository.UsageRepository
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import java.util.*
import java.util.concurrent.TimeUnit

@HiltWorker
class UsageCollectorWorker @AssistedInject constructor(
    @Assisted context: Context,
    @Assisted params: WorkerParameters,
    private val usageRepository: UsageRepository
) : CoroutineWorker(context, params) {

    override suspend fun doWork(): Result {
        val now = System.currentTimeMillis()

        val isInitialRun = inputData.getBoolean(KEY_INITIAL_RUN, false)
        val startTime = if (isInitialRun) {
            getStartOfDay()
        } else {
            now - TimeUnit.MINUTES.toMillis(15)
        }

        return try {
            usageRepository.collectAndPersistEvents(startTime, now)
            Result.success()
        } catch (e: Exception) {
            Result.retry()
        }
    }

    private fun getStartOfDay(): Long {
        return Calendar.getInstance().apply {
            set(Calendar.HOUR_OF_DAY, 0)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }.timeInMillis
    }

    companion object {
        const val KEY_INITIAL_RUN = "initial_run"
        private const val WORK_NAME = "UsageCollectorWork"

        fun schedule(context: Context, immediate: Boolean = false) {
            val constraints = Constraints.Builder()
                .setRequiresBatteryNotLow(false)
                .build()

            val workRequestBuilder = PeriodicWorkRequestBuilder<UsageCollectorWorker>(
                15, TimeUnit.MINUTES
            )
                .setConstraints(constraints)

            if (immediate) {
                workRequestBuilder.setInputData(workDataOf(KEY_INITIAL_RUN to true))
            }

            WorkManager.getInstance(context).enqueueUniquePeriodicWork(
                WORK_NAME,
                ExistingPeriodicWorkPolicy.UPDATE,
                workRequestBuilder.build()
            )

            if (immediate) {
                val immediateRequest = OneTimeWorkRequestBuilder<UsageCollectorWorker>()
                    .setInputData(workDataOf(KEY_INITIAL_RUN to true))
                    .build()
                WorkManager.getInstance(context).enqueue(immediateRequest)
            }
        }
    }
}
