package com.contacts.android.contacts.domain.model

/**
 * Sorting options for contacts (based on Fossify implementation)
 */
enum class SortType {
    FIRST_NAME,
    MIDDLE_NAME,
    SURNAME,
    FULL_NAME,
    DATE_CREATED,
    DATE_UPDATED,
    CUSTOM // For favorites custom ordering
}

/**
 * Sort direction
 */
enum class SortDirection {
    ASCENDING,
    DESCENDING
}

/**
 * Complete sort configuration
 */
data class SortOrder(
    val type: SortType = SortType.FIRST_NAME,
    val direction: SortDirection = SortDirection.ASCENDING
) {
    companion object {
        // Default sorting (First Name, Ascending)
        val DEFAULT = SortOrder(SortType.FIRST_NAME, SortDirection.ASCENDING)

        // Encode to Int for storage
        fun fromInt(value: Int): SortOrder {
            val typeValue = value and 0xFF // Lower 8 bits for type
            val directionValue = (value shr 8) and 0xFF // Next 8 bits for direction

            val type = SortType.values().getOrNull(typeValue) ?: SortType.FIRST_NAME
            val direction = SortDirection.values().getOrNull(directionValue) ?: SortDirection.ASCENDING

            return SortOrder(type, direction)
        }
    }

    // Encode to Int for storage
    fun toInt(): Int {
        return type.ordinal or (direction.ordinal shl 8)
    }

    fun getDisplayName(): String {
        val typeName = when (type) {
            SortType.FIRST_NAME -> "First name"
            SortType.MIDDLE_NAME -> "Middle name"
            SortType.SURNAME -> "Surname"
            SortType.FULL_NAME -> "Full name"
            SortType.DATE_CREATED -> "Date created"
            SortType.DATE_UPDATED -> "Date updated"
            SortType.CUSTOM -> "Custom order"
        }
        val directionName = when (direction) {
            SortDirection.ASCENDING -> "Ascending"
            SortDirection.DESCENDING -> "Descending"
        }
        return "$typeName • $directionName"
    }
}
