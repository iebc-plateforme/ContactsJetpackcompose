package com.contacts.android.contactsjetpackcompose.domain.model

data class Contact(
    val id: Long = 0,
    val firstName: String,
    val lastName: String = "",
    val photoUri: String? = null,
    val phoneNumbers: List<PhoneNumber> = emptyList(),
    val emails: List<Email> = emptyList(),
    val addresses: List<Address> = emptyList(),
    val organization: String? = null,
    val title: String? = null,
    val notes: String? = null,
    val isFavorite: Boolean = false,
    val groups: List<Group> = emptyList(),
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis()
) {
    val displayName: String
        get() = "$firstName $lastName".trim().ifEmpty { "Unnamed Contact" }

    val initials: String
        get() = buildString {
            firstName.firstOrNull()?.let { append(it.uppercaseChar()) }
            lastName.firstOrNull()?.let { append(it.uppercaseChar()) }
        }.ifEmpty { "?" }

    val primaryPhone: PhoneNumber?
        get() = phoneNumbers.firstOrNull()

    val primaryEmail: Email?
        get() = emails.firstOrNull()
}
