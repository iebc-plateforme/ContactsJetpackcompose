package com.contacts.android.contacts.domain.usecase.contact

import com.contacts.android.contacts.data.provider.ContactsProvider
import com.contacts.android.contacts.domain.model.*
import com.contacts.android.contacts.domain.repository.ContactRepository
import javax.inject.Inject

class SyncContactsUseCase @Inject constructor(
    private val contactsProvider: ContactsProvider,
    private val contactRepository: ContactRepository
) {
    suspend operator fun invoke(): Result<Unit> {
        return try {
            // Efficiently sync contacts from the device's contact provider with the local database.
            val providerContacts = contactsProvider.getAllContacts()
            val databaseContacts = contactRepository.getAllContactsOnce()

            val providerContactMap = providerContacts.associateBy { it.id }
            val databaseContactMap = databaseContacts.associateBy { it.id }

            val contactsToInsert = mutableListOf<Contact>()
            val contactsToUpdate = mutableListOf<Contact>()

            // Find contacts to insert or update
            for (providerContact in providerContacts) {
                val databaseContact = databaseContactMap[providerContact.id]
                if (databaseContact == null) {
                    contactsToInsert.add(providerContact.toDomainModel())
                } else if (providerContact.isNewerThan(databaseContact)) {
                    contactsToUpdate.add(providerContact.toDomainModel(databaseContact.id))
                }
            }

            // Find contacts to delete
            val contactsToDelete = databaseContacts.filter { it.id !in providerContactMap }

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

    private fun com.contacts.android.contacts.data.provider.ContactData.toDomainModel(id: Long = 0): Contact {
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
            notes = null
        )
    }

    private fun com.contacts.android.contacts.data.provider.ContactData.isNewerThan(contact: Contact): Boolean {
        // This is a simplified comparison. A more robust implementation would compare all fields.
        return displayName != "${contact.firstName} ${contact.lastName}".trim() ||
                photoUri != contact.photoUri ||
                isFavorite != contact.isFavorite
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
