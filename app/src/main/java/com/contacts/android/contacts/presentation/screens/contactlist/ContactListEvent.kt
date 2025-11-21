package com.contacts.android.contacts.presentation.screens.contactlist

import android.net.Uri
import com.contacts.android.contacts.domain.model.SortOrder
import com.contacts.android.contacts.domain.model.ContactFilter

sealed class ContactListEvent {
    data class SearchQueryChanged(val query: String) : ContactListEvent()
    data class SortOrderChanged(val sortOrder: SortOrder) : ContactListEvent()
    data class FilterChanged(val filter: ContactFilter) : ContactListEvent()
    data class ToggleFavorite(val contactId: Long, val isFavorite: Boolean) : ContactListEvent()
    data class DeleteContact(val contactId: Long) : ContactListEvent()
    object RefreshContacts : ContactListEvent()
    object ClearError : ContactListEvent()

    // Multi-select mode events
    object EnterSelectionMode : ContactListEvent()
    object ExitSelectionMode : ContactListEvent()
    data class ToggleContactSelection(val contactId: Long) : ContactListEvent()
    object SelectAllContacts : ContactListEvent()
    object DeselectAllContacts : ContactListEvent()
    object DeleteSelectedContacts : ContactListEvent()
    object UndoDeleteContact : ContactListEvent()
    data class AddSelectedToGroup(val groupId: Long) : ContactListEvent()
    object ExportSelectedContacts : ContactListEvent()
    object ShareSelectedContacts : ContactListEvent() // NEW: Share as VCF
    object AddSelectedToFavorites : ContactListEvent() // NEW: Bulk add to favorites
    object RemoveSelectedFromFavorites : ContactListEvent() // NEW: Bulk remove from favorites
    object MergeSelectedContacts : ContactListEvent() // NEW: Merge duplicates

    // Import/Export events
    data class ImportContacts(val uri: Uri) : ContactListEvent()
    data class ExportAllContacts(val uri: Uri, val includePhotos: Boolean = false) : ContactListEvent()
    object ClearImportResult : ContactListEvent()
    object ClearExportResult : ContactListEvent()
}
