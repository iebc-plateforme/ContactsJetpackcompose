package com.contacts.android.contacts.domain.model

data class Event(
    val date: String, // ISO format: YYYY-MM-DD or --MM-DD for recurring
    val type: EventType = EventType.OTHER
)

enum class EventType(val displayName: String) {
    ANNIVERSARY("Anniversary"),
    BIRTHDAY("Birthday"), // Alternative to main birthday field
    CUSTOM("Custom Event"),
    OTHER("Other")
}
