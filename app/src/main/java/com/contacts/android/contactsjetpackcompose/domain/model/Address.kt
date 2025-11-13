package com.contacts.android.contactsjetpackcompose.domain.model

data class Address(
    val id: Long = 0,
    val street: String? = null,
    val city: String? = null,
    val state: String? = null,
    val postalCode: String? = null,
    val country: String? = null,
    val type: AddressType = AddressType.HOME,
    val label: String? = null
) {
    val displayType: String
        get() = label?.takeIf { it.isNotBlank() } ?: type.toDisplayString()

    val fullAddress: String
        get() = buildString {
            street?.let { append("$it\n") }

            val cityStateLine = listOfNotNull(
                city,
                state?.let { if (postalCode != null) "$it $postalCode" else it }
                    ?: postalCode
            ).joinToString(", ")

            if (cityStateLine.isNotEmpty()) {
                append("$cityStateLine\n")
            }

            country?.let { append(it) }
        }.trim()

    val isNotEmpty: Boolean
        get() = street != null || city != null || state != null ||
                postalCode != null || country != null
}
