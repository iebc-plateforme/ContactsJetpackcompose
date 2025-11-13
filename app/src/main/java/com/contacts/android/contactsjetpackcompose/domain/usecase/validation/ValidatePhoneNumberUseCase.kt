package com.contacts.android.contactsjetpackcompose.domain.usecase.validation

import javax.inject.Inject

class ValidatePhoneNumberUseCase @Inject constructor() {
    operator fun invoke(phoneNumber: String): Boolean {
        if (phoneNumber.isBlank()) return false

        // Remove common formatting characters
        val cleaned = phoneNumber.replace(Regex("[\\s\\-\\(\\)\\.]"), "")

        // Check if it contains only digits and optional + at the start
        val isValidFormat = cleaned.matches(Regex("^\\+?[0-9]{7,15}$"))

        return isValidFormat
    }
}
