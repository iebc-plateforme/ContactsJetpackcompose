package com.contacts.android.contacts.presentation.screens.contactlist

import com.contacts.android.contacts.domain.model.Contact
import com.contacts.android.contacts.domain.model.SortOrder
import com.contacts.android.contacts.domain.model.ContactFilter

/**
 * Result of an import or export operation
 */
data class ImportExportResult(
    val success: Boolean,
    val count: Int = 0,
    val errorMessage: String? = null
)

data class ContactListState(
    val contacts: List<Contact> = emptyList(),
    val favorites: List<Contact> = emptyList(),
    val groupedContacts: Map<Char, List<Contact>> = emptyMap(),
    val searchQuery: String = "",
    val sortOrder: SortOrder = SortOrder.DEFAULT,
    val filter: ContactFilter = ContactFilter.DEFAULT,
    val isLoading: Boolean = false,
    val hasLoadedContacts: Boolean = false,
    val isInitialSyncInProgress: Boolean = true, // Track initial sync state for first launch
    val error: String? = null,
    val contactCount: Int = 0,
    // Multi-select mode
    val isSelectionMode: Boolean = false,
    val selectedContactIds: Set<Long> = emptySet(),
    // Available sources/accounts for filtering (Fossify-style)
    val availableSources: Map<String, Int> = emptyMap(), // Map of source name to contact count
    // Multi-select action callbacks
    val shareContacts: List<Contact>? = null,
    val exportContacts: List<Contact>? = null,
    val mergeContactIds: List<Long>? = null,
    // Import/Export state
    val isImporting: Boolean = false,
    val isExporting: Boolean = false,
    val importResult: ImportExportResult? = null,
    val exportResult: ImportExportResult? = null,
    // User preferences (moved from UI to prevent recompositions)
    val showPhoneNumbers: Boolean = true,
    val startNameWithSurname: Boolean = false,
    val formatPhoneNumbers: Boolean = true,
    // Sync dialog state (CRITICAL FIX for data loss prevention)
    val showSyncSuggestion: Boolean = false, // Show suggestion to sync when DB is empty
    val showSyncDialog: Boolean = false, // Show sync confirmation dialog
    val localContactsCount: Int = 0, // Number of manual contacts that will be preserved
    val showSyncCompleteDialog: Boolean = false, // Show after successful sync
    val syncedContactsCount: Int = 0, // Number of contacts synced from system
    // Favorites specific state
    val favoritesViewType: com.contacts.android.contacts.data.preferences.FavoritesViewType = com.contacts.android.contacts.data.preferences.FavoritesViewType.LIST
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
