package com.contacts.android.contacts.presentation.screens.contactlist

import com.contacts.android.contacts.domain.model.Contact
import com.contacts.android.contacts.domain.model.SortOrder
import com.contacts.android.contacts.domain.model.ContactFilter

data class ContactListState(
    val contacts: List<Contact> = emptyList(),
    val favorites: List<Contact> = emptyList(),
    val groupedContacts: Map<Char, List<Contact>> = emptyMap(),
    val searchQuery: String = "",
    val sortOrder: SortOrder = SortOrder.DEFAULT,
    val filter: ContactFilter = ContactFilter.DEFAULT,
    val isLoading: Boolean = false,
    val error: String? = null,
    val contactCount: Int = 0,
    // Multi-select mode
    val isSelectionMode: Boolean = false,
    val selectedContactIds: Set<Long> = emptySet(),
    // User preferences (moved from UI to prevent recompositions)
    val showPhoneNumbers: Boolean = true,
    val startNameWithSurname: Boolean = false,
    val formatPhoneNumbers: Boolean = true,
    val swipeDeleteConfirmation: Boolean = true
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
