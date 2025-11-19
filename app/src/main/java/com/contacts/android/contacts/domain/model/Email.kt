package com.contacts.android.contacts.domain.model

data class Email(
    val id: Long = 0,
    val email: String,
    val type: EmailType = EmailType.HOME,
    val label: String? = null
) {
    val displayType: String
        get() = label?.takeIf { it.isNotBlank() } ?: type.toDisplayString()
}
