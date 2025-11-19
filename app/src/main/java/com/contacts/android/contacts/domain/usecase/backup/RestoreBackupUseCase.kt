package com.contacts.android.contacts.domain.usecase.backup

import android.net.Uri
import androidx.core.net.toUri
import com.contacts.android.contacts.domain.usecase.vcf.ImportContactsFromVcfUseCase
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import javax.inject.Inject

/**
 * Use case for restoring contacts from a backup file
 */
class RestoreBackupUseCase @Inject constructor(
    private val importContactsUseCase: ImportContactsFromVcfUseCase
) {
    /**
     * Restore contacts from a backup file
     *
     * @param backupFile The backup file to restore from
     * @return Result containing the number of restored contacts, or error
     */
    suspend operator fun invoke(backupFile: File): Result<Int> = withContext(Dispatchers.IO) {
        try {
            if (!backupFile.exists()) {
                return@withContext Result.failure(
                    IllegalArgumentException("Backup file does not exist")
                )
            }

            // Use the import use case to restore contacts
            importContactsUseCase(backupFile.toUri())
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * Restore contacts from a URI
     *
     * @param backupUri The backup file URI
     * @return Result containing the number of restored contacts, or error
     */
    suspend fun restoreFromUri(backupUri: Uri): Result<Int> {
        return importContactsUseCase(backupUri)
    }
}
