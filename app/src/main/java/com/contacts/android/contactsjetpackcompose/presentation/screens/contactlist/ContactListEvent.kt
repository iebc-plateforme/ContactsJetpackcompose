package com.contacts.android.contactsjetpackcompose.presentation.screens.contactlist

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
    data class AddSelectedToGroup(val groupId: Long) : ContactListEvent()
    object ExportSelectedContacts : ContactListEvent()
}
