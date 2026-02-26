package com.contacts.android.contacts.domain.usecase.contact

import com.contacts.android.contacts.data.provider.ContactsProvider
import com.contacts.android.contacts.domain.model.*
import com.contacts.android.contacts.domain.repository.ContactRepository
import com.contacts.android.contacts.domain.repository.GroupRepository
import kotlinx.coroutines.flow.first
import javax.inject.Inject

class SyncContactsUseCase @Inject constructor(
    private val contactsProvider: ContactsProvider,
    private val contactRepository: ContactRepository,
    private val groupRepository: GroupRepository
) {
    suspend operator fun invoke(): Result<Unit> {
        return try {
            // Efficiently sync contacts from the device's contact provider with the local database.
            val providerContacts = contactsProvider.getAllContacts()
            val databaseContacts = contactRepository.getAllContactsOnce()

            // CRITICAL FIX: Map system group row IDs to local Group entries
            // This is essential for maintaining contact-group associations
            val allGroups = groupRepository.getAllGroups().first()
            val groupMap = allGroups.mapNotNull { group ->
                group.systemGroupId?.let { systemGroupId -> systemGroupId to group }
            }.toMap()

            val providerContactMap = providerContacts.associateBy { it.id }
            val databaseContactMap = databaseContacts.associateBy { it.id }

            val contactsToInsert = mutableListOf<Contact>()
            val contactsToUpdate = mutableListOf<Contact>()

            // Find contacts to insert or update
            for (providerContact in providerContacts) {
                val databaseContact = databaseContactMap[providerContact.id]

                // CRITICAL FIX: If the ID matches but the local contact is MANUAL/IMPORTED,
                // it's an ID collision, NOT the same contact. Treat provider contact as NEW.
                if (databaseContact == null || ContactSource.isProtectedSource(databaseContact.source)) {
                    // New contact: use real system timestamp as createdAt
                    contactsToInsert.add(providerContact.toDomainModel(
                        groupMap,
                        createdAt = providerContact.lastUpdatedTimestamp
                    ))
                } else if (providerContact.isNewerThan(databaseContact)) {
                    // Update: preserve original createdAt from database
                    contactsToUpdate.add(providerContact.toDomainModel(
                        groupMap,
                        id = databaseContact.id,
                        createdAt = databaseContact.createdAt
                    ))
                }
            }

            // CRITICAL FIX: NEVER delete manually created or imported contacts during sync
            // Only delete system-synced contacts that no longer exist in the ContactsProvider
            val contactsToDelete = databaseContacts.filter { contact ->
                // Contact not in provider AND contact is from system (not manual/imported)
                contact.id !in providerContactMap && !ContactSource.isProtectedSource(contact.source)
            }

            // Perform database operations in a single transaction
            contactRepository.syncContacts(
                contactsToInsert,
                contactsToUpdate,
                contactsToDelete
            )

            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    private fun com.contacts.android.contacts.data.provider.ContactData.toDomainModel(
        groupMap: Map<Long, Group>,
        id: Long = 0,
        createdAt: Long = lastUpdatedTimestamp
    ): Contact {
        // CRITICAL FIX: Map system group IDs to Group objects
        // This ensures contact-group associations are maintained in the database
        val contactGroups = groupIds.mapNotNull { systemGroupId ->
            groupMap[systemGroupId]
        }

        return Contact(
            id = id,
            firstName = extractFirstName(displayName),
            lastName = extractLastName(displayName),
            phoneNumbers = phoneNumbers.map { PhoneNumber(number = it.number, type = it.type) },
            emails = emails.map { Email(email = it.address, type = it.type) },
            addresses = addresses.map { Address(street = it.street, city = it.city, state = it.state, postalCode = it.postalCode, country = it.country, type = it.type) },
            organization = null,
            title = null,
            photoUri = photoUri,
            isFavorite = isFavorite,
            notes = null,
            source = accountType ?: ContactSource.SYSTEM,
            accountName = accountName,
            accountType = accountType,
            createdAt = createdAt,
            groups = contactGroups,
            systemRawContactId = rawContactId
        )
    }

    private fun com.contacts.android.contacts.data.provider.ContactData.isNewerThan(contact: Contact): Boolean {
        // Simple comparison to check for obvious changes
        return displayName != contact.displayName ||
                photoUri != contact.photoUri ||
                isFavorite != contact.isFavorite ||
                (rawContactId != null && contact.systemRawContactId == null) // Backfill missing rawContactId
    }

    private fun extractFirstName(displayName: String): String {
        val parts = displayName.trim().split(" ", limit = 2)
        return parts.firstOrNull() ?: ""
    }

    private fun extractLastName(displayName: String): String {
        val parts = displayName.trim().split(" ", limit = 2)
        return if (parts.size > 1) parts[1] else ""
    }
}
