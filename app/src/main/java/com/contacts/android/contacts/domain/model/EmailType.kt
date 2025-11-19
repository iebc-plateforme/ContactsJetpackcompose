package com.contacts.android.contacts.domain.model

enum class EmailType {
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
