package com.contacts.android.contacts.domain.usecase.contact

import android.util.Log
import com.contacts.android.contacts.data.local.dao.ContactDao
import com.contacts.android.contacts.data.local.dao.ContactGroupDao
import com.contacts.android.contacts.data.local.dao.PhoneNumberDao
import com.contacts.android.contacts.data.local.dao.EmailDao
import com.contacts.android.contacts.data.local.database.ContactsDatabase
import com.contacts.android.contacts.data.local.entity.ContactGroupCrossRef
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import javax.inject.Inject

/**
 * Restores favorites and group associations from migration temporary data
 * This runs after the initial sync to match old favorites/groups to new contacts
 */
class RestoreMigrationDataUseCase @Inject constructor(
    private val database: ContactsDatabase,
    private val contactDao: ContactDao,
    private val phoneNumberDao: PhoneNumberDao,
    private val emailDao: EmailDao,
    private val contactGroupDao: ContactGroupDao
) {
    suspend operator fun invoke(): Result<Int> = withContext(Dispatchers.IO) {
        try {
            // Check if migration temp table exists
            val hasMigrationData = checkMigrationTableExists()
            if (!hasMigrationData) {
                Log.d("RestoreMigrationData", "No migration data to restore")
                return@withContext Result.success(0)
            }

            var restoredCount = 0

            // Read migration temp data
            val migrationData = readMigrationData()
            Log.d("RestoreMigrationData", "Found ${migrationData.size} items in migration temp data")

            // For each migration entry, try to find matching contact and restore data
            for (entry in migrationData) {
                val matchedContactId = findMatchingContact(entry)

                if (matchedContactId != null) {
                    // Restore favorite status
                    if (entry.isFavorite) {
                        contactDao.toggleFavorite(matchedContactId, true)
                        Log.d("RestoreMigrationData", "Restored favorite for contact $matchedContactId (${entry.firstName} ${entry.lastName})")
                    }

                    // Restore group associations
                    if (entry.groupIds.isNotEmpty()) {
                        for (groupId in entry.groupIds) {
                            try {
                                contactGroupDao.insertContactGroup(
                                    ContactGroupCrossRef(contactId = matchedContactId, groupId = groupId)
                                )
                            } catch (e: Exception) {
                                Log.w("RestoreMigrationData", "Could not restore group association: ${e.message}")
                            }
                        }
                        Log.d("RestoreMigrationData", "Restored ${entry.groupIds.size} group associations for contact $matchedContactId")
                    }

                    restoredCount++
                } else {
                    Log.w("RestoreMigrationData", "Could not find matching contact for ${entry.firstName} ${entry.lastName} (phone: ${entry.phoneNumber}, email: ${entry.email})")
                }
            }

            // Clean up migration temp table
            dropMigrationTable()

            Log.d("RestoreMigrationData", "Restoration complete: $restoredCount contacts restored")
            Result.success(restoredCount)
        } catch (e: Exception) {
            Log.e("RestoreMigrationData", "Error restoring migration data", e)
            Result.failure(e)
        }
    }

    private fun checkMigrationTableExists(): Boolean {
        return try {
            database.openHelper.readableDatabase.query(
                "SELECT name FROM sqlite_master WHERE type='table' AND name='migration_temp_data'"
            ).use { cursor ->
                cursor.count > 0
            }
        } catch (e: Exception) {
            false
        }
    }

    private data class MigrationEntry(
        val phoneNumber: String,
        val email: String,
        val firstName: String,
        val lastName: String,
        val isFavorite: Boolean,
        val groupIds: List<Long>
    )

    private fun readMigrationData(): List<MigrationEntry> {
        val entries = mutableListOf<MigrationEntry>()

        try {
            database.openHelper.readableDatabase.query(
                "SELECT phoneNumber, email, firstName, lastName, isFavorite, groupIds FROM migration_temp_data"
            ).use { cursor ->
                while (cursor.moveToNext()) {
                    val phoneNumber = cursor.getString(0) ?: ""
                    val email = cursor.getString(1) ?: ""
                    val firstName = cursor.getString(2) ?: ""
                    val lastName = cursor.getString(3) ?: ""
                    val isFavorite = cursor.getInt(4) == 1
                    val groupIdsStr = cursor.getString(5) ?: ""
                    val groupIds = if (groupIdsStr.isNotEmpty()) {
                        groupIdsStr.split(",").mapNotNull { it.toLongOrNull() }
                    } else {
                        emptyList()
                    }

                    entries.add(MigrationEntry(phoneNumber, email, firstName, lastName, isFavorite, groupIds))
                }
            }
        } catch (e: Exception) {
            Log.e("RestoreMigrationData", "Error reading migration data", e)
        }

        return entries
    }

    private suspend fun findMatchingContact(entry: MigrationEntry): Long? {
        // Strategy 1: Try to match by phone number (most reliable)
        if (entry.phoneNumber.isNotEmpty()) {
            val phoneMatch = phoneNumberDao.findContactByPhoneNumber(entry.phoneNumber)
            if (phoneMatch != null) {
                return phoneMatch
            }
        }

        // Strategy 2: Try to match by email
        if (entry.email.isNotEmpty()) {
            val emailMatch = emailDao.findContactByEmail(entry.email)
            if (emailMatch != null) {
                return emailMatch
            }
        }

        // Strategy 3: Try to match by full name (less reliable but better than nothing)
        if (entry.firstName.isNotEmpty() || entry.lastName.isNotEmpty()) {
            val nameMatch = contactDao.findContactByName(entry.firstName, entry.lastName)
            if (nameMatch != null) {
                return nameMatch
            }
        }

        return null
    }

    private fun dropMigrationTable() {
        try {
            database.openHelper.writableDatabase.execSQL("DROP TABLE IF EXISTS migration_temp_data")
            Log.d("RestoreMigrationData", "Dropped migration_temp_data table")
        } catch (e: Exception) {
            Log.e("RestoreMigrationData", "Error dropping migration table", e)
        }
    }
}
