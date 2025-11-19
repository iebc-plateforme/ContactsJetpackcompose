package com.contacts.android.contacts.domain.model

data class Contact(
    val id: Long = 0,
    // Name fields (Fossify-complete)
    val prefix: String? = null, // Mr., Mrs., Dr., etc.
    val firstName: String,
    val middleName: String? = null,
    val lastName: String = "",
    val suffix: String? = null, // Jr., Sr., III, etc.
    val nickname: String? = null,
    // Contact info
    val photoUri: String? = null,
    val phoneNumbers: List<PhoneNumber> = emptyList(),
    val emails: List<Email> = emptyList(),
    val addresses: List<Address> = emptyList(),
    val websites: List<Website> = emptyList(),
    val instantMessages: List<InstantMessage> = emptyList(),
    // Organization
    val organization: String? = null,
    val title: String? = null,
    // Notes and dates
    val notes: String? = null,
    val birthday: String? = null, // ISO format: YYYY-MM-DD
    val events: List<Event> = emptyList(), // Anniversaries, custom events
    // System
    val ringtone: String? = null, // URI to custom ringtone
    val isFavorite: Boolean = false,
    val groups: List<Group> = emptyList(),
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis()
) {
    val displayName: String
        get() {
            val parts = listOfNotNull(
                prefix,
                firstName.takeIf { it.isNotEmpty() },
                middleName,
                lastName.takeIf { it.isNotEmpty() },
                suffix
            )
            return parts.joinToString(" ").ifEmpty { nickname ?: "Unnamed Contact" }
        }

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
