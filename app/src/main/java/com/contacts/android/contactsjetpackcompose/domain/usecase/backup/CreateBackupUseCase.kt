package com.contacts.android.contactsjetpackcompose.domain.usecase.backup

import android.content.Context
import com.contacts.android.contactsjetpackcompose.data.vcf.VcfBuilder
import com.contacts.android.contactsjetpackcompose.domain.repository.ContactRepository
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.withContext
import java.io.File
import java.text.SimpleDateFormat
import java.util.*
import javax.inject.Inject

/**
 * Use case for creating automatic backups of contacts
 *
 * Creates a VCF backup file in the app's external files directory
 */
class CreateBackupUseCase @Inject constructor(
    @ApplicationContext private val context: Context,
    private val vcfBuilder: VcfBuilder,
    private val contactRepository: ContactRepository
) {
    /**
     * Create a backup of all contacts
     *
     * @param includePhotos Whether to include contact photos in backup
     * @return Result containing the backup file path, or error
     */
    suspend operator fun invoke(includePhotos: Boolean = false): Result<String> = withContext(Dispatchers.IO) {
        try {
            // Get all contacts
            val contacts = contactRepository.getAllContacts().first()

            if (contacts.isEmpty()) {
                return@withContext Result.failure(
                    IllegalStateException("No contacts to backup")
                )
            }

            // Generate VCF content
            val vcfContent = vcfBuilder.buildMultiple(contacts, includePhotos)

            // Create backup directory
            val backupDir = getBackupDirectory()
            if (!backupDir.exists()) {
                backupDir.mkdirs()
            }

            // Create backup file with timestamp
            val timestamp = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.US).format(Date())
            val backupFile = File(backupDir, "contacts_backup_$timestamp.vcf")

            // Write content
            backupFile.writeText(vcfContent, Charsets.UTF_8)

            // Clean up old backups (keep last 10)
            cleanupOldBackups(backupDir)

            Result.success(backupFile.absolutePath)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * Get the backup directory
     */
    fun getBackupDirectory(): File {
        return File(context.getExternalFilesDir(null), "backups")
    }

    /**
     * Get list of all backup files
     */
    fun getBackupFiles(): List<File> {
        val backupDir = getBackupDirectory()
        if (!backupDir.exists()) return emptyList()

        return backupDir.listFiles { file ->
            file.name.startsWith("contacts_backup_") && file.name.endsWith(".vcf")
        }?.sortedByDescending { it.lastModified() } ?: emptyList()
    }

    private fun cleanupOldBackups(backupDir: File, keepCount: Int = 10) {
        val backups = backupDir.listFiles { file ->
            file.name.startsWith("contacts_backup_") && file.name.endsWith(".vcf")
        }?.sortedByDescending { it.lastModified() } ?: return

        // Delete old backups, keeping only the most recent ones
        backups.drop(keepCount).forEach { file ->
            file.delete()
        }
    }
}
