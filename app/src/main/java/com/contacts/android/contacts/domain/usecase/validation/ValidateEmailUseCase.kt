package com.contacts.android.contacts.domain.usecase.validation

import android.util.Patterns
import com.contacts.android.contacts.R
import javax.inject.Inject

/**
 * Validates email addresses with specific error messages.
 */
class ValidateEmailUseCase @Inject constructor() {

    /**
     * Validates an email address and returns a detailed result.
     * @param email The email to validate
     * @return ValidationResult with specific error if invalid
     */
    operator fun invoke(email: String): ValidationResult {
        // Empty/blank is valid (optional field)
        if (email.isBlank()) return ValidationResult.Valid

        val trimmed = email.trim()

        // Check for @ symbol
        if (!trimmed.contains("@")) {
            return ValidationResult.Invalid(R.string.error_email_missing_at)
        }

        // Use Android's EMAIL_ADDRESS pattern for full validation
        if (!Patterns.EMAIL_ADDRESS.matcher(trimmed).matches()) {
            return ValidationResult.Invalid(R.string.error_email_invalid_format)
        }

        return ValidationResult.Valid
    }

    /**
     * Legacy method for backward compatibility.
     * @return true if valid, false otherwise
     */
    fun isValid(email: String): Boolean {
        return invoke(email).isValid
    }
}
