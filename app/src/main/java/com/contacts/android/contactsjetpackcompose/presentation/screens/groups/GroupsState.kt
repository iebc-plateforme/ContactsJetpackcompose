package com.contacts.android.contactsjetpackcompose.presentation.screens.groups

import com.contacts.android.contactsjetpackcompose.domain.model.Contact
import com.contacts.android.contactsjetpackcompose.domain.model.Group

data class GroupsState(
    val groups: List<Group> = emptyList(),
    val filteredGroups: List<Group> = emptyList(),
    val searchQuery: String = "",
    val isLoading: Boolean = false,
    val error: String? = null,
    val showAddGroupDialog: Boolean = false,
    val showEditGroupDialog: Boolean = false,
    val showDeleteDialog: Boolean = false,
    val selectedGroup: Group? = null,
    val groupNameInput: String = "",
    val availableContacts: List<Contact> = emptyList(),
    val selectedContactIds: Set<Long> = emptySet(),
    val showContactSelectionDialog: Boolean = false
)
