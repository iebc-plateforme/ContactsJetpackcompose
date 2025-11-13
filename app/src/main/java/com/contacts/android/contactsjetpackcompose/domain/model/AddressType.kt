package com.contacts.android.contactsjetpackcompose.domain.model

enum class AddressType {
    HOME,
    WORK,
    OTHER,
    CUSTOM;

    fun toDisplayString(): String = when (this) {
        HOME -> "Home"
        WORK -> "Work"
        OTHER -> "Other"
        CUSTOM -> "Custom"
    }
}
