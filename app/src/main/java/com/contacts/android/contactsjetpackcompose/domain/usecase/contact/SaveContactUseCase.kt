package com.contacts.android.contactsjetpackcompose.domain.usecase.contact

import com.contacts.android.contactsjetpackcompose.domain.model.Contact
import com.contacts.android.contactsjetpackcompose.domain.repository.ContactRepository
import javax.inject.Inject

class SaveContactUseCase @Inject constructor(
    private val repository: ContactRepository
) {
    suspend operator fun invoke(contact: Contact): Result<Long> {
        return try {
            // Validate contact has at least a name
            if (contact.firstName.isBlank() && contact.lastName.isBlank()) {
                return Result.failure(IllegalArgumentException("Contact must have a name"))
            }

            val id = if (contact.id == 0L) {
                repository.insertContact(contact)
            } else {
                repository.updateContact(contact.copy(updatedAt = System.currentTimeMillis()))
                contact.id
            }
            Result.success(id)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
