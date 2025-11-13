package com.contacts.android.contactsjetpackcompose.presentation.screens.contactlist

import com.contacts.android.contactsjetpackcompose.domain.model.Contact

enum class SortOrder {
    FIRST_NAME_ASC,
    FIRST_NAME_DESC,
    LAST_NAME_ASC,
    LAST_NAME_DESC,
    DATE_ADDED,
    DATE_MODIFIED
}

enum class ContactFilter {
    ALL,
    WITH_PHONE,
    WITH_EMAIL,
    WITH_ADDRESS
}

data class ContactListState(
    val contacts: List<Contact> = emptyList(),
    val favorites: List<Contact> = emptyList(),
    val groupedContacts: Map<Char, List<Contact>> = emptyMap(),
    val searchQuery: String = "",
    val sortOrder: SortOrder = SortOrder.FIRST_NAME_ASC,
    val filter: ContactFilter = ContactFilter.ALL,
    val isLoading: Boolean = false,
    val error: String? = null,
    val contactCount: Int = 0,
    // Multi-select mode
    val isSelectionMode: Boolean = false,
    val selectedContactIds: Set<Long> = emptySet()
) {
    val hasContacts: Boolean
        get() = contacts.isNotEmpty()

    val showFavorites: Boolean
        get() = favorites.isNotEmpty() && searchQuery.isBlank()

    val selectedCount: Int
        get() = selectedContactIds.size

    val isAllSelected: Boolean
        get() = contacts.isNotEmpty() && selectedContactIds.size == contacts.size
}
