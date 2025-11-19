package com.contacts.android.contacts.presentation.screens.groupdetail

import com.contacts.android.contacts.domain.model.Contact
import com.contacts.android.contacts.domain.model.Group

data class GroupDetailState(
    val group: Group? = null,
    val contacts: List<Contact> = emptyList(),
    val isLoading: Boolean = false,
    val error: String? = null,
    val showAddContactsDialog: Boolean = false,
    val showEditGroupDialog: Boolean = false,
    val showDeleteGroupDialog: Boolean = false,
    val availableContacts: List<Contact> = emptyList(),
    val selectedContactIds: Set<Long> = emptySet(),
    val groupNameInput: String = ""
)