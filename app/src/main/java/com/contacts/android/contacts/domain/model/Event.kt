package com.contacts.android.contacts.domain.model

import androidx.annotation.StringRes
import com.contacts.android.contacts.R

data class Event(
    val date: String, // ISO format: YYYY-MM-DD or --MM-DD for recurring
    val type: EventType = EventType.OTHER
)

enum class EventType(@StringRes val displayNameRes: Int) {
    ANNIVERSARY(R.string.event_type_anniversary),
    BIRTHDAY(R.string.birthday), // Alternative to main birthday field
    CUSTOM(R.string.event_type_custom),
    OTHER(R.string.type_other)
}
