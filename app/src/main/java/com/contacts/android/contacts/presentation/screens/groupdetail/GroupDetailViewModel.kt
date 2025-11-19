package com.contacts.android.contacts.presentation.screens.groupdetail

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.contacts.android.contacts.domain.repository.ContactRepository
import com.contacts.android.contacts.domain.usecase.group.AddContactsToGroupUseCase
import com.contacts.android.contacts.domain.usecase.group.DeleteGroupUseCase
import com.contacts.android.contacts.domain.usecase.group.GetGroupWithContactsUseCase
import com.contacts.android.contacts.domain.usecase.group.RemoveContactFromGroupUseCase
import com.contacts.android.contacts.domain.usecase.group.SaveGroupUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class GroupDetailViewModel @Inject constructor(
    private val getGroupWithContactsUseCase: GetGroupWithContactsUseCase,
    private val saveGroupUseCase: SaveGroupUseCase,
    private val deleteGroupUseCase: DeleteGroupUseCase,
    private val addContactsToGroupUseCase: AddContactsToGroupUseCase,
    private val removeContactFromGroupUseCase: RemoveContactFromGroupUseCase,
    private val contactRepository: ContactRepository,
    savedStateHandle: SavedStateHandle
) : ViewModel() {

    private val _state = MutableStateFlow(GroupDetailState())
    val state: StateFlow<GroupDetailState> = _state.asStateFlow()

    private val groupId: Long = checkNotNull(savedStateHandle["groupId"])

    private val _navigateBack = MutableSharedFlow<Unit>()
    val navigateBack: SharedFlow<Unit> = _navigateBack.asSharedFlow()

    init {
        loadGroupDetails()
    }

    fun onEvent(event: GroupDetailEvent) {
        when (event) {
            is GroupDetailEvent.LoadGroup -> loadGroupDetails()
            GroupDetailEvent.ShowAddContactsDialog -> {
                loadAvailableContacts()
                _state.update { it.copy(showAddContactsDialog = true) }
            }
            GroupDetailEvent.HideAddContactsDialog -> {
                _state.update {
                    it.copy(
                        showAddContactsDialog = false,
                        selectedContactIds = emptySet()
                    )
                }
            }
            GroupDetailEvent.ShowEditGroupDialog -> {
                _state.update {
                    it.copy(
                        showEditGroupDialog = true,
                        groupNameInput = it.group?.name ?: ""
                    )
                }
            }
            GroupDetailEvent.HideEditGroupDialog -> {
                _state.update {
                    it.copy(
                        showEditGroupDialog = false,
                        groupNameInput = ""
                    )
                }
            }
            GroupDetailEvent.ShowDeleteGroupDialog -> {
                _state.update { it.copy(showDeleteGroupDialog = true) }
            }
            GroupDetailEvent.HideDeleteGroupDialog -> {
                _state.update { it.copy(showDeleteGroupDialog = false) }
            }
            is GroupDetailEvent.AddContactsToGroup -> {
                addContactsToGroup(event.contactIds)
            }
            is GroupDetailEvent.RemoveContactFromGroup -> {
                removeContactFromGroup(event.contactId)
            }
            is GroupDetailEvent.UpdateGroupName -> {
                updateGroupName(event.newName)
            }
            GroupDetailEvent.DeleteGroup -> {
                deleteGroup()
            }
            GroupDetailEvent.ClearError -> {
                _state.update { it.copy(error = null) }
            }
        }
    }

    fun toggleContactSelection(contactId: Long) {
        val currentSelection = _state.value.selectedContactIds
        val newSelection = if (contactId in currentSelection) {
            currentSelection - contactId
        } else {
            currentSelection + contactId
        }
        _state.update { it.copy(selectedContactIds = newSelection) }
    }

    fun updateGroupNameInput(name: String) {
        _state.update { it.copy(groupNameInput = name) }
    }

    private fun loadGroupDetails() {
        viewModelScope.launch {
            _state.update { it.copy(isLoading = true) }
            try {
                val groupWithContacts = getGroupWithContactsUseCase(groupId)
                _state.update {
                    it.copy(
                        group = groupWithContacts?.group,
                        contacts = groupWithContacts?.contacts ?: emptyList(),
                        isLoading = false
                    )
                }
            } catch (e: Exception) {
                _state.update {
                    it.copy(
                        error = e.message ?: "An error occurred",
                        isLoading = false
                    )
                }
            }
        }
    }

    private fun loadAvailableContacts() {
        viewModelScope.launch {
            try {
                contactRepository.getAllContacts().first().let { allContacts ->
                    // Filter out contacts already in the group
                    val currentContactIds = _state.value.contacts.map { it.id }.toSet()
                    val available = allContacts.filterNot { it.id in currentContactIds }
                    _state.update { it.copy(availableContacts = available) }
                }
            } catch (e: Exception) {
                _state.update { it.copy(error = "Failed to load contacts: ${e.message}") }
            }
        }
    }

    private fun addContactsToGroup(contactIds: List<Long>) {
        viewModelScope.launch {
            addContactsToGroupUseCase(contactIds, groupId)
                .onSuccess {
                    _state.update {
                        it.copy(
                            showAddContactsDialog = false,
                            selectedContactIds = emptySet()
                        )
                    }
                    loadGroupDetails()
                }
                .onFailure { error ->
                    _state.update {
                        it.copy(error = error.message ?: "Failed to add contacts to group")
                    }
                }
        }
    }

    private fun removeContactFromGroup(contactId: Long) {
        viewModelScope.launch {
            removeContactFromGroupUseCase(contactId, groupId)
                .onSuccess {
                    loadGroupDetails()
                }
                .onFailure { error ->
                    _state.update {
                        it.copy(error = error.message ?: "Failed to remove contact from group")
                    }
                }
        }
    }

    private fun updateGroupName(newName: String) {
        val group = _state.value.group ?: return

        if (newName.isBlank()) {
            _state.update { it.copy(error = "Group name cannot be empty") }
            return
        }

        viewModelScope.launch {
            saveGroupUseCase(group.copy(name = newName))
                .onSuccess {
                    _state.update {
                        it.copy(
                            showEditGroupDialog = false,
                            groupNameInput = ""
                        )
                    }
                    loadGroupDetails()
                }
                .onFailure { error ->
                    _state.update {
                        it.copy(error = error.message ?: "Failed to update group name")
                    }
                }
        }
    }

    private fun deleteGroup() {
        val group = _state.value.group ?: return

        viewModelScope.launch {
            deleteGroupUseCase(group)
                .onSuccess {
                    _navigateBack.emit(Unit)
                }
                .onFailure { error ->
                    _state.update {
                        it.copy(error = error.message ?: "Failed to delete group")
                    }
                }
        }
    }
}