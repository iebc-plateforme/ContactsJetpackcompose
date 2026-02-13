package com.contacts.android.contacts.domain.usecase.contact

import android.util.Log
import com.contacts.android.contacts.data.provider.SystemContactsWriter
import com.contacts.android.contacts.domain.model.Contact
import com.contacts.android.contacts.domain.model.ContactSource
import com.contacts.android.contacts.domain.repository.ContactRepository
import javax.inject.Inject

/**
 * Use case for saving contacts to both the local Room database and Android's ContactsContract.
 * This ensures contacts created in the app are visible to other apps.
 */
class SaveContactUseCase @Inject constructor(
    private val repository: ContactRepository,
    private val systemContactsWriter: SystemContactsWriter
) {
    companion object {
        private const val TAG = "SaveContactUseCase"
    }

    /**
     * Save a contact to the local database and optionally to Android's ContactsContract.
     *
     * @param contact The contact to save
     * @param writeToSystem Whether to also write to Android's system contacts (default: true)
     * @return Result containing the local contact ID, or a failure with error
     */
    suspend operator fun invoke(
        contact: Contact,
        writeToSystem: Boolean = true
    ): Result<Long> {
        return try {
            // Validate contact has at least a name
            if (contact.firstName.isBlank() && contact.lastName.isBlank()) {
                return Result.failure(IllegalArgumentException("Contact must have a name"))
            }

            val isNewContact = contact.id == 0L
            val localId: Long

            if (isNewContact) {
                // CRITICAL FIX: Mark new contacts as MANUAL to protect from sync deletion
                // This ensures manually created contacts are never deleted during system sync
                val manualContact = if (contact.source.isEmpty()) {
                    contact.copy(source = ContactSource.MANUAL)
                } else {
                    contact
                }
                localId = repository.insertContact(manualContact)
                Log.d(TAG, "New contact inserted with localId: $localId")
            } else {
                // Preserve source on update - don't change it
                repository.updateContact(contact.copy(updatedAt = System.currentTimeMillis()))
                localId = contact.id
                Log.d(TAG, "Contact updated with localId: $localId")
            }

            // Write to Android's ContactsContract if requested
            if (writeToSystem) {
                try {
                    val contactToSync = contact.copy(id = localId)

                    if (contact.systemRawContactId != null) {
                        // Update existing system contact
                        val success = systemContactsWriter.updateContact(
                            contactToSync,
                            contact.systemRawContactId
                        )
                        if (success) {
                            Log.d(TAG, "System contact updated for rawContactId: ${contact.systemRawContactId}")
                        } else {
                            Log.w(TAG, "Failed to update system contact, but local save succeeded")
                        }
                    } else {
                        // Insert new system contact
                        val systemRawContactId = systemContactsWriter.insertContact(
                            contactToSync,
                            contact.accountName,
                            contact.accountType
                        )

                        if (systemRawContactId != null) {
                            // Update local record with system ID
                            repository.updateSystemRawContactId(localId, systemRawContactId)
                            Log.d(TAG, "System contact created with rawContactId: $systemRawContactId, linked to localId: $localId")
                        } else {
                            Log.w(TAG, "Failed to create system contact, but local save succeeded")
                        }
                    }
                } catch (e: Exception) {
                    // System write failed, but local save succeeded - don't fail the whole operation
                    Log.e(TAG, "Failed to write to system contacts, but local save succeeded", e)
                }
            }

            Result.success(localId)
        } catch (e: Exception) {
            Log.e(TAG, "Failed to save contact", e)
            Result.failure(e)
        }
    }
}
