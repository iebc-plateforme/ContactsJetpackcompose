package com.contacts.android.contacts.presentation.screens.groups

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.contacts.android.contacts.domain.model.Group
import com.contacts.android.contacts.domain.usecase.group.DeleteGroupUseCase
import com.contacts.android.contacts.domain.usecase.group.GetAllGroupsUseCase
import com.contacts.android.contacts.domain.usecase.group.SaveGroupUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class GroupsViewModel @Inject constructor(
    private val getAllGroupsUseCase: GetAllGroupsUseCase,
    private val saveGroupUseCase: SaveGroupUseCase,
    private val deleteGroupUseCase: DeleteGroupUseCase,
    private val getGroupWithContactsUseCase: com.contacts.android.contacts.domain.usecase.group.GetGroupWithContactsUseCase,
    private val addContactsToGroupUseCase: com.contacts.android.contacts.domain.usecase.group.AddContactsToGroupUseCase,
    private val removeContactFromGroupUseCase: com.contacts.android.contacts.domain.usecase.group.RemoveContactFromGroupUseCase,
    private val syncGroupsUseCase: com.contacts.android.contacts.domain.usecase.group.SyncGroupsUseCase,
    private val contactRepository: com.contacts.android.contacts.domain.repository.ContactRepository
) : ViewModel() {

    private val _state = MutableStateFlow(GroupsState())
    val state: StateFlow<GroupsState> = _state.asStateFlow()

    init {
        // Sync groups from Android ContactsContract first
        syncGroups()
    }

    fun onEvent(event: GroupsEvent) {
        when (event) {
            GroupsEvent.ShowAddGroupDialog -> {
                _state.update {
                    it.copy(
                        showAddGroupDialog = true,
                        groupNameInput = "",
                        selectedGroup = null
                    )
                }
            }
            GroupsEvent.HideAddGroupDialog -> {
                _state.update { it.copy(showAddGroupDialog = false, groupNameInput = "") }
            }
            is GroupsEvent.ShowEditGroupDialog -> {
                _state.update {
                    it.copy(
                        showEditGroupDialog = true,
                        selectedGroup = event.group,
                        groupNameInput = event.group.name
                    )
                }
            }
            GroupsEvent.HideEditGroupDialog -> {
                _state.update {
                    it.copy(
                        showEditGroupDialog = false,
                        selectedGroup = null,
                        groupNameInput = ""
                    )
                }
            }
            is GroupsEvent.ShowDeleteDialog -> {
                _state.update {
                    it.copy(
                        showDeleteDialog = true,
                        selectedGroup = event.group
                    )
                }
            }
            GroupsEvent.HideDeleteDialog -> {
                _state.update {
                    it.copy(
                        showDeleteDialog = false,
                        selectedGroup = null
                    )
                }
            }
            is GroupsEvent.GroupNameChanged -> {
                _state.update { it.copy(groupNameInput = event.name) }
            }
            is GroupsEvent.SearchQueryChanged -> {
                _state.update { it.copy(searchQuery = event.query) }
                filterGroups(event.query)
            }
            GroupsEvent.SaveGroup -> {
                saveGroup()
            }
            GroupsEvent.DeleteGroup -> {
                deleteGroup()
            }
            GroupsEvent.ShowContactSelectionDialog -> {
                loadAvailableContacts()
                _state.update { it.copy(showContactSelectionDialog = true) }
            }
            GroupsEvent.HideContactSelectionDialog -> {
                _state.update { it.copy(showContactSelectionDialog = false) }
            }
            is GroupsEvent.ToggleContactSelection -> {
                val currentSelection = _state.value.selectedContactIds
                val newSelection = if (event.contactId in currentSelection) {
                    currentSelection - event.contactId
                } else {
                    currentSelection + event.contactId
                }
                _state.update { it.copy(selectedContactIds = newSelection) }
            }
            GroupsEvent.ClearContactSelection -> {
                _state.update { it.copy(selectedContactIds = emptySet()) }
            }
            is GroupsEvent.AddContactsToGroup -> {
                addContactsToGroup(event.groupId, event.contactIds)
            }
        }
    }

    private fun filterGroups(query: String) {
        val currentGroups = _state.value.groups
        val filtered = if (query.isBlank()) {
            currentGroups
        } else {
            currentGroups.filter { group ->
                group.name.contains(query, ignoreCase = true)
            }
        }
        _state.update { it.copy(filteredGroups = filtered) }
    }

    /**
     * Sync groups from Android ContactsContract to local database
     * Following Fossify's pattern of loading system groups
     */
    private fun syncGroups() {
        viewModelScope.launch {
            _state.update { it.copy(isLoading = true) }
            syncGroupsUseCase()
                .onSuccess {
                    // After successful sync, load groups from database
                    loadGroups()
                }
                .onFailure { error ->
                    _state.update {
                        it.copy(
                            isLoading = false,
                            error = error.message ?: "Failed to sync groups"
                        )
                    }
                    // Try to load whatever groups are in the database
                    loadGroups()
                }
        }
    }

    private fun loadGroups() {
        viewModelScope.launch {
            getAllGroupsUseCase()
                .catch { error ->
                    _state.update {
                        it.copy(
                            isLoading = false,
                            error = error.message ?: "Failed to load groups"
                        )
                    }
                }
                .collect { groups ->
                    _state.update {
                        it.copy(
                            groups = groups,
                            filteredGroups = groups,
                            isLoading = false,
                            error = null
                        )
                    }
                    filterGroups(_state.value.searchQuery)
                }
        }
    }

    private fun loadAvailableContacts() {
        viewModelScope.launch {
            try {
                contactRepository.getAllContacts().first().let { contacts ->
                    _state.update { it.copy(availableContacts = contacts) }
                }
            } catch (e: Exception) {
                _state.update { it.copy(error = "Failed to load contacts: ${e.message}") }
            }
        }
    }

    private fun saveGroup() {
        val currentState = _state.value
        val groupName = currentState.groupNameInput.trim()

        if (groupName.isBlank()) {
            _state.update { it.copy(error = "Group name cannot be empty") }
            return
        }

        viewModelScope.launch {
            val group = if (currentState.selectedGroup != null) {
                // Update existing group
                currentState.selectedGroup.copy(name = groupName)
            } else {
                // Create new group
                Group(name = groupName)
            }

            saveGroupUseCase(group)
                .onSuccess { groupId ->
                    val newContactIds = currentState.selectedContactIds
                    if (currentState.selectedGroup != null) {
                        val groupId = currentState.selectedGroup.id
                        // Get current contacts in the group
                        val groupWithContacts = getGroupWithContactsUseCase(groupId)
                        groupWithContacts?.let {
                            val currentContactIds = it.contacts.map { it.id }.toSet()
                            val newContactIdsSet = newContactIds.toSet()

                            val contactsToAdd = newContactIdsSet - currentContactIds
                            val contactsToRemove = currentContactIds - newContactIdsSet

                            addContactsToGroup(groupId, contactsToAdd.toList())
                            contactsToRemove.forEach { contactId ->
                                viewModelScope.launch {
                                    removeContactFromGroupUseCase(contactId, groupId)
                                }
                            }
                        }
                    } else {
                        // Add contacts to new group
                        addContactsToGroup(groupId, newContactIds.toList())
                    }

                    _state.update {
                        it.copy(
                            showAddGroupDialog = false,
                            showEditGroupDialog = false,
                            showContactSelectionDialog = false,
                            groupNameInput = "",
                            selectedGroup = null,
                            selectedContactIds = emptySet()
                        )
                    }
                }
                .onFailure { error ->
                    _state.update {
                        it.copy(error = error.message ?: "Failed to save group")
                    }
                }
        }
    }

    private fun deleteGroup() {
        val group = _state.value.selectedGroup ?: return

        viewModelScope.launch {
            _state.update { it.copy(showDeleteDialog = false) }
            deleteGroupUseCase(group)
                .onFailure { error ->
                    _state.update {
                        it.copy(error = error.message ?: "Failed to delete group")
                    }
                }
        }
    }

    private fun addContactsToGroup(groupId: Long, contactIds: List<Long>) {
        viewModelScope.launch {
            addContactsToGroupUseCase(contactIds, groupId)
                .onFailure { error ->
                    _state.update {
                        it.copy(error = error.message ?: "Failed to add contacts to group")
                    }
                }
        }
    }
}
