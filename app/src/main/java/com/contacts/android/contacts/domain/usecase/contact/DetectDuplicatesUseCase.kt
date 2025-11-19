package com.contacts.android.contacts.domain.usecase.contact

import com.contacts.android.contacts.domain.model.Contact
import com.contacts.android.contacts.domain.repository.ContactRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.withContext
import javax.inject.Inject

/**
 * Use case for detecting duplicate contacts
 *
 * Duplicates are identified by:
 * 1. Exact name match (first + last name)
 * 2. Same phone number
 * 3. Fuzzy name matching (optional)
 */
class DetectDuplicatesUseCase @Inject constructor(
    private val contactRepository: ContactRepository
) {
    suspend operator fun invoke(): Result<List<DuplicateGroup>> = withContext(Dispatchers.IO) {
        try {
            val allContacts = contactRepository.getAllContacts().first()

            if (allContacts.size < 2) {
                return@withContext Result.success(emptyList())
            }

            val duplicateGroups = mutableListOf<DuplicateGroup>()
            val processed = mutableSetOf<Long>()

            // Find duplicates by name
            allContacts.forEach { contact ->
                if (contact.id in processed) return@forEach

                val duplicates = findDuplicatesByName(contact, allContacts, processed)
                if (duplicates.isNotEmpty()) {
                    duplicateGroups.add(
                        DuplicateGroup(
                            contacts = listOf(contact) + duplicates,
                            reason = DuplicateReason.SAME_NAME
                        )
                    )
                    processed.add(contact.id)
                    processed.addAll(duplicates.map { it.id })
                }
            }

            // Find duplicates by phone number
            allContacts.forEach { contact ->
                if (contact.id in processed) return@forEach

                val duplicates = findDuplicatesByPhone(contact, allContacts, processed)
                if (duplicates.isNotEmpty()) {
                    duplicateGroups.add(
                        DuplicateGroup(
                            contacts = listOf(contact) + duplicates,
                            reason = DuplicateReason.SAME_PHONE
                        )
                    )
                    processed.add(contact.id)
                    processed.addAll(duplicates.map { it.id })
                }
            }

            Result.success(duplicateGroups)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    private fun findDuplicatesByName(
        contact: Contact,
        allContacts: List<Contact>,
        processed: Set<Long>
    ): List<Contact> {
        if (contact.firstName.isBlank() && contact.lastName.isBlank()) {
            return emptyList()
        }

        return allContacts.filter { other ->
            other.id != contact.id &&
            other.id !in processed &&
            contact.firstName.equals(other.firstName, ignoreCase = true) &&
            contact.lastName.equals(other.lastName, ignoreCase = true)
        }
    }

    private fun findDuplicatesByPhone(
        contact: Contact,
        allContacts: List<Contact>,
        processed: Set<Long>
    ): List<Contact> {
        if (contact.phoneNumbers.isEmpty()) {
            return emptyList()
        }

        val contactPhones = contact.phoneNumbers.map { normalizePhone(it.number) }.toSet()

        return allContacts.filter { other ->
            other.id != contact.id &&
            other.id !in processed &&
            other.phoneNumbers.isNotEmpty() &&
            other.phoneNumbers.any { phone ->
                normalizePhone(phone.number) in contactPhones
            }
        }
    }

    private fun normalizePhone(phone: String): String {
        // Remove all non-digit characters for comparison
        return phone.replace(Regex("[^0-9]"), "")
    }
}

/**
 * Group of duplicate contacts
 */
data class DuplicateGroup(
    val contacts: List<Contact>,
    val reason: DuplicateReason
)

/**
 * Reason why contacts are considered duplicates
 */
enum class DuplicateReason {
    SAME_NAME,
    SAME_PHONE,
    FUZZY_NAME
}
