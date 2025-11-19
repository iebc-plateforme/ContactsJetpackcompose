package com.contacts.android.contacts.domain.usecase.validation

import com.contacts.android.contacts.domain.model.Contact
import javax.inject.Inject

data class ContactValidationResult(
    val isValid: Boolean,
    val errorMessage: String? = null
)

class ValidateContactUseCase @Inject constructor() {
    operator fun invoke(contact: Contact): ContactValidationResult {
        // Contact must have at least a first name or last name
        if (contact.firstName.isBlank() && contact.lastName.isBlank()) {
            return ContactValidationResult(
                isValid = false,
                errorMessage = "Contact must have at least a first or last name"
            )
        }

        // If there are phone numbers, at least one should be valid
        if (contact.phoneNumbers.isNotEmpty()) {
            val hasValidPhone = contact.phoneNumbers.any { it.number.isNotBlank() }
            if (!hasValidPhone) {
                return ContactValidationResult(
                    isValid = false,
                    errorMessage = "At least one phone number must be valid"
                )
            }
        }

        // If there are emails, at least one should be valid
        if (contact.emails.isNotEmpty()) {
            val hasValidEmail = contact.emails.any { it.email.isNotBlank() }
            if (!hasValidEmail) {
                return ContactValidationResult(
                    isValid = false,
                    errorMessage = "At least one email must be valid"
                )
            }
        }

        return ContactValidationResult(isValid = true)
    }
}
