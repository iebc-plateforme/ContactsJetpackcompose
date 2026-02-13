package com.contacts.android.contacts.presentation.screens.groupdetail

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.contacts.android.contacts.domain.model.Contact
import com.contacts.android.contacts.domain.model.Group
import com.contacts.android.contacts.domain.repository.ContactRepository
import com.contacts.android.contacts.domain.repository.GroupRepository
import com.contacts.android.contacts.domain.usecase.group.AddContactsToGroupUseCase
import com.contacts.android.contacts.domain.usecase.group.DeleteGroupUseCase
import com.contacts.android.contacts.domain.usecase.group.GetGroupWithContactsUseCase
import com.contacts.android.contacts.domain.usecase.group.RemoveContactFromGroupUseCase
import com.contacts.android.contacts.domain.usecase.group.SaveGroupUseCase
import com.contacts.android.contacts.util.AnalyticsManager
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject
import java.util.Locale

@HiltViewModel
class GroupDetailViewModel @Inject constructor(
    private val getGroupWithContactsUseCase: GetGroupWithContactsUseCase,
    private val saveGroupUseCase: SaveGroupUseCase,
    private val deleteGroupUseCase: DeleteGroupUseCase,
    private val addContactsToGroupUseCase: AddContactsToGroupUseCase,
    private val removeContactFromGroupUseCase: RemoveContactFromGroupUseCase,
    private val contactRepository: ContactRepository,
    private val groupRepository: GroupRepository,
    private val analyticsManager: AnalyticsManager,
    savedStateHandle: SavedStateHandle
) : ViewModel() {

    private val _state = MutableStateFlow(GroupDetailState())
    val state: StateFlow<GroupDetailState> = _state.asStateFlow()

    private val groupId: Long = checkNotNull(savedStateHandle["groupId"])
    private var sameNameGroups: List<Group> = emptyList()

    private val _navigateBack = MutableSharedFlow<Unit>()
    val navigateBack: SharedFlow<Unit> = _navigateBack.asSharedFlow()

    private val _openSmsWithNumbers = MutableSharedFlow<String>()
    val openSmsWithNumbers: SharedFlow<String> = _openSmsWithNumbers.asSharedFlow()

    private val _openEmailWithAddresses = MutableSharedFlow<String>()
    val openEmailWithAddresses: SharedFlow<String> = _openEmailWithAddresses.asSharedFlow()

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
            GroupDetailEvent.SendGroupSms -> {
                prepareGroupSms()
            }
            GroupDetailEvent.SendGroupEmail -> {
                prepareGroupEmail()
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
                val group = groupWithContacts?.group
                if (group == null) {
                    _state.update {
                        it.copy(
                            error = "Group not found",
                            isLoading = false
                        )
                    }
                    return@launch
                }

                sameNameGroups = groupRepository.getGroupsByName(group.name)
                val groupIds = sameNameGroups.map { it.id }.distinct()
                val mergedContacts = groupIds.flatMap { id ->
                    groupRepository.getContactsByGroupId(id).first()
                }.distinctBy { it.id }
                    .sortedBy { it.displayName.lowercase(Locale.getDefault()) }

                val mergedGroup = group.copy(
                    contactCount = mergedContacts.size,
                    isSystemGroup = sameNameGroups.any { it.isSystemGroup }
                )

                _state.update {
                    it.copy(
                        group = mergedGroup,
                        contacts = mergedContacts,
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
            val contactsById = (_state.value.availableContacts + _state.value.contacts)
                .associateBy { it.id }
            val groupedTargets = contactIds.groupBy { contactId ->
                val contact = contactsById[contactId]
                val matchingGroup = contact?.let { matchGroupForContact(it) }
                matchingGroup?.id ?: groupId
            }

            var anySuccess = false
            groupedTargets.forEach { (targetGroupId, ids) ->
                addContactsToGroupUseCase(ids, targetGroupId)
                    .onSuccess { anySuccess = true }
            }

            if (anySuccess) {
                analyticsManager.logContactsAddedToGroup(contactIds.size)
                _state.update {
                    it.copy(
                        showAddContactsDialog = false,
                        selectedContactIds = emptySet()
                    )
                }
                loadGroupDetails()
            } else {
                _state.update { it.copy(error = "Failed to add contacts to group") }
            }
        }
    }

    private fun removeContactFromGroup(contactId: Long) {
        viewModelScope.launch {
            val groupName = _state.value.group?.name
            val contact = _state.value.contacts.firstOrNull { it.id == contactId }
            val groupIdsToRemove = contact?.groups
                ?.filter { group ->
                    groupName != null && group.name.equals(groupName, ignoreCase = true)
                }
                ?.map { it.id }
                ?.distinct()
                ?.ifEmpty { listOf(groupId) }
                ?: listOf(groupId)

            var anySuccess = false
            groupIdsToRemove.forEach { targetGroupId ->
                removeContactFromGroupUseCase(contactId, targetGroupId)
                    .onSuccess { anySuccess = true }
            }

            if (anySuccess) {
                loadGroupDetails()
            } else {
                _state.update { it.copy(error = "Failed to remove contact from group") }
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
            var anySuccess = false
            val groupsToUpdate = if (sameNameGroups.isEmpty()) listOf(group) else sameNameGroups
            groupsToUpdate.forEach { targetGroup ->
                saveGroupUseCase(targetGroup.copy(name = newName))
                    .onSuccess { anySuccess = true }
            }

            if (anySuccess) {
                _state.update {
                    it.copy(
                        showEditGroupDialog = false,
                        groupNameInput = ""
                    )
                }
                loadGroupDetails()
            } else {
                _state.update { it.copy(error = "Failed to update group name") }
            }
        }
    }

    private fun deleteGroup() {
        val group = _state.value.group ?: return

        viewModelScope.launch {
            var anySuccess = false
            val groupsToDelete = if (sameNameGroups.isEmpty()) listOf(group) else sameNameGroups
            groupsToDelete.forEach { targetGroup ->
                deleteGroupUseCase(targetGroup)
                    .onSuccess { anySuccess = true }
            }

            if (anySuccess) {
                analyticsManager.logGroupDeleted()
                _navigateBack.emit(Unit)
            } else {
                _state.update { it.copy(error = "Failed to delete group") }
            }
        }
    }

    private fun matchGroupForContact(contact: Contact): Group? {
        return sameNameGroups.firstOrNull { group ->
            group.accountName == contact.accountName && group.accountType == contact.accountType
        } ?: sameNameGroups.firstOrNull()
    }

    private fun prepareGroupSms() {
        val contacts = _state.value.contacts
        val numbers = contacts.mapNotNull { contact: Contact -> contact.primaryPhone?.number }
            .filter { it.isNotBlank() }
            .distinct()

        if (numbers.isEmpty()) {
            _state.update { it.copy(error = "No contacts with phone numbers in this group") }
            return
        }

        viewModelScope.launch {
            analyticsManager.logGroupSmsSent(numbers.size)
            _openSmsWithNumbers.emit(numbers.joinToString(";"))
        }
    }

    private fun prepareGroupEmail() {
        val contacts = _state.value.contacts
        val emails = contacts.mapNotNull { contact: Contact -> contact.primaryEmail?.email }
            .filter { it.isNotBlank() }
            .distinct()

        if (emails.isEmpty()) {
            _state.update { it.copy(error = "No contacts with email addresses in this group") }
            return
        }

        viewModelScope.launch {
            analyticsManager.logGroupEmailSent(emails.size)
            _openEmailWithAddresses.emit(emails.joinToString(","))
        }
    }
}
