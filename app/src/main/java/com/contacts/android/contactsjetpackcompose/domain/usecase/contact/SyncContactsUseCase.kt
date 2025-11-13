package com.contacts.android.contactsjetpackcompose.domain.usecase.contact

import com.contacts.android.contactsjetpackcompose.data.provider.ContactsProvider
import com.contacts.android.contactsjetpackcompose.domain.model.*
import com.contacts.android.contactsjetpackcompose.domain.repository.ContactRepository
import javax.inject.Inject

class SyncContactsUseCase @Inject constructor(
    private val contactsProvider: ContactsProvider,
    private val contactRepository: ContactRepository
) {
    suspend operator fun invoke(): Result<Unit> {
        return try {
            // Clear existing contacts to avoid duplicates
            contactRepository.deleteAllContacts()

            // Read contacts from system provider
            val providerContacts = contactsProvider.getAllContacts()

            // Convert to domain models and save to local database
            providerContacts.forEach { contactData ->
                val contact = Contact(
                    id = 0, // Let Room auto-generate IDs
                    firstName = extractFirstName(contactData.displayName),
                    lastName = extractLastName(contactData.displayName),
                    phoneNumbers = contactData.phoneNumbers.map { phoneData ->
                        PhoneNumber(
                            id = 0,
                            number = phoneData.number,
                            type = phoneData.type
                        )
                    },
                    emails = contactData.emails.map { emailData ->
                        Email(
                            id = 0,
                            email = emailData.address,
                            type = emailData.type
                        )
                    },
                    addresses = contactData.addresses.map { addressData ->
                        Address(
                            id = 0,
                            street = addressData.street,
                            city = addressData.city,
                            state = addressData.state,
                            postalCode = addressData.postalCode,
                            country = addressData.country,
                            type = addressData.type
                        )
                    },
                    organization = null,
                    title = null,
                    photoUri = contactData.photoUri,
                    isFavorite = contactData.isFavorite,
                    notes = null
                )

                contactRepository.insertContact(contact)
            }

            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
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
