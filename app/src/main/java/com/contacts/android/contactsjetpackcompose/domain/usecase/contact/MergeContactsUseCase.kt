package com.contacts.android.contactsjetpackcompose.domain.usecase.contact

import com.contacts.android.contactsjetpackcompose.domain.model.*
import com.contacts.android.contactsjetpackcompose.domain.repository.ContactRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import javax.inject.Inject

/**
 * Use case for merging multiple contacts into one
 *
 * Combines all unique information from multiple contacts:
 * - Keeps all unique phone numbers, emails, and addresses
 * - Merges notes
 * - Combines groups
 * - Deletes the source contacts after merging
 */
class MergeContactsUseCase @Inject constructor(
    private val contactRepository: ContactRepository
) {
    /**
     * Merge multiple contacts into a single contact
     *
     * @param contactIds List of contact IDs to merge (minimum 2)
     * @param targetContactId Optional ID of the contact to use as base (uses first if null)
     * @return Result containing the merged contact ID, or error
     */
    suspend operator fun invoke(
        contactIds: List<Long>,
        targetContactId: Long? = null
    ): Result<Long> = withContext(Dispatchers.IO) {
        try {
            if (contactIds.size < 2) {
                return@withContext Result.failure(
                    IllegalArgumentException("Need at least 2 contacts to merge")
                )
            }

            // Get all contacts to merge
            val contacts = mutableListOf<Contact>()
            contactIds.forEach { id ->
                contactRepository.getContactById(id).collect { contact ->
                    contact?.let { contacts.add(it) }
                }
            }

            if (contacts.size != contactIds.size) {
                return@withContext Result.failure(
                    IllegalStateException("Could not load all contacts for merging")
                )
            }

            // Determine target contact (base for merge)
            val targetIndex = if (targetContactId != null) {
                contacts.indexOfFirst { it.id == targetContactId }.takeIf { it >= 0 } ?: 0
            } else {
                0
            }
            val targetContact = contacts[targetIndex]
            val otherContacts = contacts.filterIndexed { index, _ -> index != targetIndex }

            // Merge all data
            val mergedContact = mergeContactData(targetContact, otherContacts)

            // Update the target contact with merged data
            contactRepository.updateContact(mergedContact)

            // Delete other contacts
            otherContacts.forEach { contact ->
                contactRepository.deleteContactById(contact.id)
            }

            Result.success(mergedContact.id)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    private fun mergeContactData(target: Contact, others: List<Contact>): Contact {
        // Use target's basic info, but fill in blanks from others if needed
        var firstName = target.firstName
        var lastName = target.lastName
        var organization = target.organization
        var title = target.title
        var photoUri = target.photoUri

        others.forEach { other ->
            if (firstName.isBlank()) firstName = other.firstName
            if (lastName.isBlank()) lastName = other.lastName
            if (organization == null && other.organization != null) organization = other.organization
            if (title == null && other.title != null) title = other.title
            if (photoUri == null && other.photoUri != null) photoUri = other.photoUri
        }

        // Merge phone numbers (remove duplicates)
        val allPhones = mutableListOf<PhoneNumber>()
        allPhones.addAll(target.phoneNumbers)
        others.forEach { other ->
            other.phoneNumbers.forEach { phone ->
                if (!allPhones.any { it.number == phone.number }) {
                    allPhones.add(phone.copy(id = 0)) // Reset ID for new entry
                }
            }
        }

        // Merge emails (remove duplicates)
        val allEmails = mutableListOf<Email>()
        allEmails.addAll(target.emails)
        others.forEach { other ->
            other.emails.forEach { email ->
                if (!allEmails.any { it.email.equals(email.email, ignoreCase = true) }) {
                    allEmails.add(email.copy(id = 0))
                }
            }
        }

        // Merge addresses (keep all unique addresses)
        val allAddresses = mutableListOf<Address>()
        allAddresses.addAll(target.addresses)
        others.forEach { other ->
            other.addresses.forEach { address ->
                if (!allAddresses.any { it.fullAddress == address.fullAddress }) {
                    allAddresses.add(address.copy(id = 0))
                }
            }
        }

        // Merge notes
        val allNotes = mutableListOf<String>()
        target.notes?.takeIf { it.isNotBlank() }?.let { allNotes.add(it) }
        others.forEach { other ->
            other.notes?.takeIf { it.isNotBlank() && it !in allNotes }?.let { allNotes.add(it) }
        }
        val mergedNotes = allNotes.joinToString("\n\n")

        // Merge groups
        val allGroups = mutableSetOf<Group>()
        allGroups.addAll(target.groups)
        others.forEach { other ->
            allGroups.addAll(other.groups)
        }

        // Keep favorite status if any contact was favorite
        val isFavorite = target.isFavorite || others.any { it.isFavorite }

        return target.copy(
            firstName = firstName,
            lastName = lastName,
            organization = organization,
            title = title,
            photoUri = photoUri,
            phoneNumbers = allPhones,
            emails = allEmails,
            addresses = allAddresses,
            notes = mergedNotes.takeIf { it.isNotBlank() },
            groups = allGroups.toList(),
            isFavorite = isFavorite
        )
    }
}
