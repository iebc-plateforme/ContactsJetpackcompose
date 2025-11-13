package com.contacts.android.contactsjetpackcompose.data.worker

import android.content.Context
import androidx.work.*
import dagger.hilt.android.qualifiers.ApplicationContext
import java.util.concurrent.TimeUnit
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Scheduler for automatic contact backups
 */
@Singleton
class BackupScheduler @Inject constructor(
    @ApplicationContext private val context: Context
) {
    private val workManager = WorkManager.getInstance(context)

    /**
     * Schedule automatic backups
     *
     * @param frequency Backup frequency (DAILY, WEEKLY, MONTHLY)
     * @param includePhotos Whether to include photos in backups
     */
    fun scheduleBackup(frequency: BackupFrequency, includePhotos: Boolean = false) {
        val constraints = Constraints.Builder()
            .setRequiresBatteryNotLow(true) // Only backup when battery is not low
            .setRequiredNetworkType(NetworkType.NOT_REQUIRED) // No network needed
            .build()

        val repeatInterval = when (frequency) {
            BackupFrequency.DAILY -> 1L to TimeUnit.DAYS
            BackupFrequency.WEEKLY -> 7L to TimeUnit.DAYS
            BackupFrequency.MONTHLY -> 30L to TimeUnit.DAYS
        }

        val inputData = Data.Builder()
            .putBoolean(BackupWorker.KEY_INCLUDE_PHOTOS, includePhotos)
            .build()

        val backupRequest = PeriodicWorkRequestBuilder<BackupWorker>(
            repeatInterval.first,
            repeatInterval.second
        )
            .setConstraints(constraints)
            .setInputData(inputData)
            .setInitialDelay(1, TimeUnit.HOURS) // First backup after 1 hour
            .build()

        workManager.enqueueUniquePeriodicWork(
            BackupWorker.WORK_NAME,
            ExistingPeriodicWorkPolicy.UPDATE,
            backupRequest
        )
    }

    /**
     * Cancel automatic backups
     */
    fun cancelBackup() {
        workManager.cancelUniqueWork(BackupWorker.WORK_NAME)
    }

    /**
     * Check if automatic backup is scheduled
     */
    fun isBackupScheduled(): Boolean {
        val workInfos = workManager.getWorkInfosForUniqueWork(BackupWorker.WORK_NAME).get()
        return workInfos.any { !it.state.isFinished }
    }

    /**
     * Trigger immediate backup (one-time)
     */
    fun triggerImmediateBackup(includePhotos: Boolean = false) {
        val inputData = Data.Builder()
            .putBoolean(BackupWorker.KEY_INCLUDE_PHOTOS, includePhotos)
            .build()

        val backupRequest = OneTimeWorkRequestBuilder<BackupWorker>()
            .setInputData(inputData)
            .build()

        workManager.enqueue(backupRequest)
    }
}

enum class BackupFrequency {
    DAILY,
    WEEKLY,
    MONTHLY
}
