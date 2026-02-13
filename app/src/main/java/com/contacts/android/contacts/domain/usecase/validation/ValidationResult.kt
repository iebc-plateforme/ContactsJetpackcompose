package com.contacts.android.contacts.domain.usecase.validation

import androidx.annotation.StringRes

/**
 * Result of field validation with detailed error information.
 */
sealed class ValidationResult {
    /**
     * Field is valid
     */
    object Valid : ValidationResult()

    /**
     * Field is invalid with a specific error
     * @param errorResId String resource ID for the error message
     */
    data class Invalid(@StringRes val errorResId: Int) : ValidationResult()

    val isValid: Boolean
        get() = this is Valid
}
