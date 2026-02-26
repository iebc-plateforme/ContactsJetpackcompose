package com.contacts.android.contacts.presentation.screens.editcontact

import com.contacts.android.contacts.domain.model.*

data class EditContactState(
    val contactId: Long = 0L,
    val firstName: String = "",
    val lastName: String = "",
    val photoUri: String? = null,
    val phoneNumbers: List<PhoneNumberInput> = listOf(PhoneNumberInput()),
    val emails: List<EmailInput> = listOf(EmailInput()),
    val addresses: List<AddressInput> = listOf(AddressInput()),
    val organization: String = "",
    val title: String = "",
    val notes: String = "",
    val birthday: String = "",
    val isFavorite: Boolean = false, // Favorite toggle state
    val selectedGroups: List<Long> = emptyList(),
    val tags: List<String> = emptyList(),
    val availableTags: List<String> = emptyList(),
    val isLoading: Boolean = false,
    val isSaving: Boolean = false,
    val error: String? = null,
    // Validation error maps (index -> error message resource ID or null if valid)
    val phoneValidationErrors: Map<Int, Int?> = emptyMap(),
    val emailValidationErrors: Map<Int, Int?> = emptyMap(),
    // Track if user has attempted to save (show all errors after first save attempt)
    val hasAttemptedSave: Boolean = false
) {
    /**
     * Check if the form is valid for saving.
     * Name is required, and any non-empty phone/email must be valid.
     */
    val isValid: Boolean
        get() {
            val hasName = firstName.isNotBlank() || lastName.isNotBlank()
            val hasPhoneErrors = phoneValidationErrors.values.any { it != null }
            val hasEmailErrors = emailValidationErrors.values.any { it != null }
            return hasName && !hasPhoneErrors && !hasEmailErrors
        }

    /**
     * Check if only the name requirement is met (for basic validation before save)
     */
    val hasRequiredName: Boolean
        get() = firstName.isNotBlank() || lastName.isNotBlank()

    val isEditMode: Boolean
        get() = contactId != 0L
}

data class PhoneNumberInput(
    val id: Long = 0,
    val number: String = "",
    val type: PhoneType = PhoneType.MOBILE,
    val label: String? = null
)

data class EmailInput(
    val id: Long = 0,
    val email: String = "",
    val type: EmailType = EmailType.HOME,
    val label: String? = null
)

data class AddressInput(
    val id: Long = 0,
    val street: String = "",
    val city: String = "",
    val state: String = "",
    val postalCode: String = "",
    val country: String = "",
    val type: AddressType = AddressType.HOME,
    val label: String? = null
)
