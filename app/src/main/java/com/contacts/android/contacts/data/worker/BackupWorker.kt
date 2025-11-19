package com.contacts.android.contacts.data.worker

import android.content.Context
import androidx.hilt.work.HiltWorker
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.contacts.android.contacts.domain.usecase.backup.CreateBackupUseCase
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject

/**
 * Worker for automatic contact backups
 *
 * Scheduled to run periodically based on user preferences
 */
@HiltWorker
class BackupWorker @AssistedInject constructor(
    @Assisted context: Context,
    @Assisted workerParams: WorkerParameters,
    private val createBackupUseCase: CreateBackupUseCase
) : CoroutineWorker(context, workerParams) {

    override suspend fun doWork(): Result {
        return try {
            // Get backup preferences from input data
            val includePhotos = inputData.getBoolean(KEY_INCLUDE_PHOTOS, false)

            // Create backup
            createBackupUseCase(includePhotos)
                .onSuccess {
                    // Backup successful
                    Result.success()
                }
                .onFailure {
                    // Backup failed
                    Result.failure()
                }

            Result.success()
        } catch (e: Exception) {
            e.printStackTrace()
            Result.failure()
        }
    }

    companion object {
        const val WORK_NAME = "ContactBackupWork"
        const val KEY_INCLUDE_PHOTOS = "include_photos"
    }
}
