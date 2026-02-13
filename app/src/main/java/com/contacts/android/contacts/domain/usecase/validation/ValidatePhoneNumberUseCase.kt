package com.contacts.android.contacts.domain.usecase.validation

import com.contacts.android.contacts.R
import javax.inject.Inject

/**
 * Validates phone numbers with specific error messages.
 */
class ValidatePhoneNumberUseCase @Inject constructor() {

    /**
     * Validates a phone number and returns a detailed result.
     * @param phoneNumber The phone number to validate
     * @return ValidationResult with specific error if invalid
     */
    operator fun invoke(phoneNumber: String): ValidationResult {
        // Empty/blank is valid (optional field)
        if (phoneNumber.isBlank()) return ValidationResult.Valid

        // Remove common formatting characters for validation
        val cleaned = phoneNumber.replace(Regex("[\\s\\-\\(\\)\\.\\+]"), "")

        // Check for non-digit characters after cleaning
        if (!cleaned.all { it.isDigit() }) {
            return ValidationResult.Invalid(R.string.error_phone_invalid_characters)
        }

        // Check minimum length (7 digits minimum for local numbers)
        if (cleaned.length < 7) {
            return ValidationResult.Invalid(R.string.error_phone_too_short)
        }

        // Check maximum length (15 digits maximum per E.164)
        if (cleaned.length > 15) {
            return ValidationResult.Invalid(R.string.error_phone_too_long)
        }

        return ValidationResult.Valid
    }

    /**
     * Legacy method for backward compatibility.
     * @return true if valid, false otherwise
     */
    fun isValid(phoneNumber: String): Boolean {
        return invoke(phoneNumber).isValid
    }
}
