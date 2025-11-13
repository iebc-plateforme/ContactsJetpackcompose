package com.contacts.android.contactsjetpackcompose.presentation.screens.editcontact

import com.contacts.android.contactsjetpackcompose.domain.model.*

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
    val selectedGroups: List<Long> = emptyList(),
    val isLoading: Boolean = false,
    val isSaving: Boolean = false,
    val error: String? = null
) {
    val isValid: Boolean
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
