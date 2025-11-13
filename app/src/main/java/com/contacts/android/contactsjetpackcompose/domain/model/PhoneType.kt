package com.contacts.android.contactsjetpackcompose.domain.model

enum class PhoneType {
    MOBILE,
    HOME,
    WORK,
    FAX,
    PAGER,
    OTHER,
    CUSTOM;

    fun toDisplayString(): String = when (this) {
        MOBILE -> "Mobile"
        HOME -> "Home"
        WORK -> "Work"
        FAX -> "Fax"
        PAGER -> "Pager"
        OTHER -> "Other"
        CUSTOM -> "Custom"
    }
}
