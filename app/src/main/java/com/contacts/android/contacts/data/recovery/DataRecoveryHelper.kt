package com.contacts.android.contacts.data.recovery

import android.content.Context
import android.net.Uri
import com.contacts.android.contacts.data.local.dao.ContactDao
import com.contacts.android.contacts.data.local.dao.EmailDao
import com.contacts.android.contacts.data.local.dao.PhoneNumberDao
import com.contacts.android.contacts.data.local.dao.AddressDao
import com.contacts.android.contacts.data.local.entity.ContactEntity
import com.contacts.android.contacts.data.mapper.toDomain
import com.contacts.android.contacts.domain.model.Contact
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileWriter
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Helper class for recovering deleted contact data.
 * This can recover contacts from the database before sync operations.
 */
@Singleton
class DataRecoveryHelper @Inject constructor(
    @ApplicationContext private val context: Context,
    private val contactDao: ContactDao,
    private val phoneNumberDao: PhoneNumberDao,
    private val emailDao: EmailDao,
    private val addressDao: AddressDao
) {

    /**
     * Export all current contacts to a VCF file.
     * This should be called BEFORE granting permissions or syncing.
     */
    suspend fun exportContactsToVcf(outputUri: Uri): Result<Int> = withContext(Dispatchers.IO) {
        try {
            val contacts = contactDao.getAllContacts().map { it.toDomain() }

            context.contentResolver.openOutputStream(outputUri)?.use { outputStream ->
                val writer = outputStream.bufferedWriter()

                contacts.forEach { contact ->
                    writer.write(contactToVcf(contact))
                }

                writer.flush()
            }

            Result.success(contacts.size)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * Export contacts to a safe backup location before any destructive operation.
     */
    suspend fun createSafetyBackup(): Result<File> = withContext(Dispatchers.IO) {
        try {
            val backupDir = File(context.filesDir, "backups")
            if (!backupDir.exists()) {
                backupDir.mkdirs()
            }

            val timestamp = System.currentTimeMillis()
            val backupFile = File(backupDir, "contacts_backup_$timestamp.vcf")

            val contacts = contactDao.getAllContacts().map { it.toDomain() }

            FileWriter(backupFile).use { writer ->
                contacts.forEach { contact ->
                    writer.write(contactToVcf(contact))
                }
            }

            Result.success(backupFile)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * Get all backup files created by the app.
     */
    suspend fun getAvailableBackups(): List<BackupInfo> = withContext(Dispatchers.IO) {
        val backupDir = File(context.filesDir, "backups")
        if (!backupDir.exists()) {
            return@withContext emptyList()
        }

        backupDir.listFiles()
            ?.filter { it.extension == "vcf" }
            ?.map { file ->
                BackupInfo(
                    file = file,
                    name = file.name,
                    timestamp = file.lastModified(),
                    size = file.length()
                )
            }
            ?.sortedByDescending { it.timestamp }
            ?: emptyList()
    }

    /**
     * Check if there's unsaved local data that would be lost during sync.
     */
    suspend fun checkForLocalOnlyContacts(): Result<LocalDataReport> = withContext(Dispatchers.IO) {
        try {
            val allContacts = contactDao.getAllContacts().map { it.toDomain() }

            // Contacts without a system source are likely local-only
            val localOnlyContacts = allContacts.filter { contact ->
                contact.source.isEmpty() || contact.source == "MANUAL" || contact.accountName.isNullOrEmpty()
            }

            val report = LocalDataReport(
                totalContacts = allContacts.size,
                localOnlyContacts = localOnlyContacts.size,
                contacts = localOnlyContacts,
                hasLocalData = localOnlyContacts.isNotEmpty()
            )

            Result.success(report)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * Create a recovery point before performing sync.
     */
    suspend fun createRecoveryPoint(): Result<RecoveryPoint> = withContext(Dispatchers.IO) {
        try {
            val backupFile = createSafetyBackup().getOrThrow()
            val report = checkForLocalOnlyContacts().getOrThrow()

            val recoveryPoint = RecoveryPoint(
                timestamp = System.currentTimeMillis(),
                backupFile = backupFile,
                totalContacts = report.totalContacts,
                localOnlyContacts = report.localOnlyContacts
            )

            Result.success(recoveryPoint)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * Log all current database state for debugging.
     * This helps diagnose what happened during data loss.
     */
    suspend fun generateDatabaseDiagnostics(): String = withContext(Dispatchers.IO) {
        val sb = StringBuilder()
        sb.appendLine("=== Database Diagnostics Report ===")
        sb.appendLine("Timestamp: ${System.currentTimeMillis()}")
        sb.appendLine()

        try {
            val contacts = contactDao.getAllContacts()
            sb.appendLine("Total Contacts: ${contacts.size}")
            sb.appendLine()

            // Group by source
            val bySource = contacts.groupBy { it.contact.source }
            sb.appendLine("Contacts by Source:")
            bySource.forEach { (source, contactList) ->
                sb.appendLine("  ${source.ifEmpty { "EMPTY/MANUAL" }}: ${contactList.size}")
            }
            sb.appendLine()

            // Group by account
            val byAccount = contacts.groupBy { it.contact.accountName.orEmpty() }
            sb.appendLine("Contacts by Account:")
            byAccount.forEach { (account, contactList) ->
                sb.appendLine("  ${account.ifEmpty { "NO_ACCOUNT" }}: ${contactList.size}")
            }
            sb.appendLine()

            val phoneCount = phoneNumberDao.getAllPhoneNumbers().size
            val emailCount = emailDao.getAllEmails().size
            val addressCount = addressDao.getAllAddresses().size

            sb.appendLine("Related Data:")
            sb.appendLine("  Phone Numbers: $phoneCount")
            sb.appendLine("  Emails: $emailCount")
            sb.appendLine("  Addresses: $addressCount")
            sb.appendLine()

            // Sample data (first 5 contacts, anonymized)
            sb.appendLine("Sample Contacts (first 5, anonymized):")
            contacts.take(5).forEach { item ->
                val contact = item.contact
                sb.appendLine("  ID: ${contact.id}")
                sb.appendLine("    Name: [${contact.firstName.take(1).ifEmpty { "?" }}***]")
                sb.appendLine("    Source: ${contact.source}")
                sb.appendLine("    Account: ${contact.accountName?.take(3) ?: "NO_ACCOUNT"}***")
                sb.appendLine("    Favorite: ${contact.isFavorite}")
                sb.appendLine()
            }

        } catch (e: Exception) {
            sb.appendLine("ERROR generating diagnostics: ${e.message}")
            sb.appendLine(e.stackTraceToString())
        }

        sb.toString()
    }

    private fun contactToVcf(contact: Contact): String {
        val vcf = StringBuilder()
        vcf.appendLine("BEGIN:VCARD")
        vcf.appendLine("VERSION:3.0")

        val fullName = "${contact.firstName} ${contact.middleName.orEmpty()} ${contact.lastName}".trim()
        vcf.appendLine("FN:$fullName")
        vcf.appendLine("N:${contact.lastName};${contact.firstName};${contact.middleName.orEmpty()};;")

        contact.phoneNumbers.forEach { phone ->
            vcf.appendLine("TEL;TYPE=${phone.type}:${phone.number}")
        }

        contact.emails.forEach { email ->
            vcf.appendLine("EMAIL;TYPE=${email.type}:${email.email}")
        }

        contact.addresses.forEach { address ->
            vcf.appendLine("ADR;TYPE=${address.type}:;;${address.street};${address.city};${address.state};${address.postalCode};${address.country}")
        }

        contact.organization?.let {
            vcf.appendLine("ORG:$it")
        }

        contact.title?.let {
            vcf.appendLine("TITLE:$it")
        }

        contact.notes?.let {
            vcf.appendLine("NOTE:$it")
        }

        vcf.appendLine("END:VCARD")
        vcf.appendLine()

        return vcf.toString()
    }
}

data class BackupInfo(
    val file: File,
    val name: String,
    val timestamp: Long,
    val size: Long
)

data class LocalDataReport(
    val totalContacts: Int,
    val localOnlyContacts: Int,
    val contacts: List<Contact>,
    val hasLocalData: Boolean
)

data class RecoveryPoint(
    val timestamp: Long,
    val backupFile: File,
    val totalContacts: Int,
    val localOnlyContacts: Int
)
