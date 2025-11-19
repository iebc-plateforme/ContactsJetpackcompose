package com.contacts.android.contacts.domain.model

/**
 * Filter options for contacts (based on Fossify implementation)
 */
enum class ContactFilterType {
    ALL, // Show all contacts
    FAVORITES_ONLY, // Show only favorites
    WITH_PHONE_ONLY, // Show contacts with phone numbers
    WITH_EMAIL_ONLY, // Show contacts with email addresses
    WITH_ADDRESS_ONLY, // Show contacts with addresses
    GROUPS, // Filter by specific groups
    CUSTOM // Custom filter combination
}

/**
 * Contact filter configuration
 */
data class ContactFilter(
    val type: ContactFilterType = ContactFilterType.ALL,
    val selectedGroupIds: Set<Long> = emptySet(),
    val showPrivateContacts: Boolean = true, // SMT_PRIVATE equivalent
    val ignoredSources: Set<String> = emptySet() // For cloud account filtering
) {
    companion object {
        val DEFAULT = ContactFilter()

        // Encode to string for storage (simplified)
        fun fromString(value: String): ContactFilter {
            if (value.isBlank()) return DEFAULT

            return try {
                val parts = value.split("|")
                val type = ContactFilterType.valueOf(parts.getOrNull(0) ?: "ALL")
                val groupIds = parts.getOrNull(1)?.split(",")
                    ?.filter { it.isNotBlank() }
                    ?.map { it.toLong() }
                    ?.toSet() ?: emptySet()
                val showPrivate = parts.getOrNull(2)?.toBoolean() ?: true
                val ignored = parts.getOrNull(3)?.split(",")?.toSet() ?: emptySet()

                ContactFilter(type, groupIds, showPrivate, ignored)
            } catch (e: Exception) {
                DEFAULT
            }
        }
    }

    // Encode to string for storage
    override fun toString(): String {
        return "${type.name}|${selectedGroupIds.joinToString(",")}|$showPrivateContacts|${ignoredSources.joinToString(",")}"
    }

    fun getDisplayName(): String {
        return when (type) {
            ContactFilterType.ALL -> "All contacts"
            ContactFilterType.FAVORITES_ONLY -> "Favorites only"
            ContactFilterType.WITH_PHONE_ONLY -> "With phone number"
            ContactFilterType.WITH_EMAIL_ONLY -> "With email"
            ContactFilterType.WITH_ADDRESS_ONLY -> "With address"
            ContactFilterType.GROUPS -> "By groups (${selectedGroupIds.size})"
            ContactFilterType.CUSTOM -> "Custom filter"
        }
    }

    fun isActive(): Boolean {
        return type != ContactFilterType.ALL ||
               selectedGroupIds.isNotEmpty() ||
               !showPrivateContacts ||
               ignoredSources.isNotEmpty()
    }
}
