package com.contacts.android.contacts.presentation.screens.groups

import com.contacts.android.contacts.domain.model.Group

sealed class GroupsEvent {
    object ShowAddGroupDialog : GroupsEvent()
    object HideAddGroupDialog : GroupsEvent()
    data class ShowEditGroupDialog(val group: Group) : GroupsEvent()
    object HideEditGroupDialog : GroupsEvent()
    data class ShowDeleteDialog(val group: Group) : GroupsEvent()
    object HideDeleteDialog : GroupsEvent()
    data class GroupNameChanged(val name: String) : GroupsEvent()
    data class SearchQueryChanged(val query: String) : GroupsEvent()
    object SaveGroup : GroupsEvent()
    object DeleteGroup : GroupsEvent()
    object ShowContactSelectionDialog : GroupsEvent()
    object HideContactSelectionDialog : GroupsEvent()
    data class ToggleContactSelection(val contactId: Long) : GroupsEvent()
    object ClearContactSelection : GroupsEvent()
    data class AddContactsToGroup(val groupId: Long, val contactIds: List<Long>) : GroupsEvent()
}