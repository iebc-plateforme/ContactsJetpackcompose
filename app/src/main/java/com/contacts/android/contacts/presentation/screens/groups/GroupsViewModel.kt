package com.contacts.android.contacts.presentation.screens.groups

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.contacts.android.contacts.domain.model.Group
import com.contacts.android.contacts.domain.usecase.group.DeleteGroupUseCase
import com.contacts.android.contacts.domain.usecase.group.GetAllGroupsUseCase
import com.contacts.android.contacts.domain.usecase.group.SaveGroupUseCase
import com.contacts.android.contacts.util.AnalyticsManager
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import javax.inject.Inject
import java.util.Locale

@HiltViewModel
class GroupsViewModel @Inject constructor(
    private val getAllGroupsUseCase: GetAllGroupsUseCase,
    private val saveGroupUseCase: SaveGroupUseCase,
    private val deleteGroupUseCase: DeleteGroupUseCase,
    private val getGroupWithContactsUseCase: com.contacts.android.contacts.domain.usecase.group.GetGroupWithContactsUseCase,
    private val addContactsToGroupUseCase: com.contacts.android.contacts.domain.usecase.group.AddContactsToGroupUseCase,
    private val removeContactFromGroupUseCase: com.contacts.android.contacts.domain.usecase.group.RemoveContactFromGroupUseCase,
    private val syncGroupsUseCase: com.contacts.android.contacts.domain.usecase.group.SyncGroupsUseCase,
    private val contactRepository: com.contacts.android.contacts.domain.repository.ContactRepository,
    private val analyticsManager: AnalyticsManager
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
                        selectedGroup = null,
                        contactSearchQuery = "",
                        selectedContactIds = emptySet()
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
                        groupNameInput = event.group.name,
                        contactSearchQuery = "",
                        selectedContactIds = emptySet()
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
                _state.update { it.copy(showContactSelectionDialog = true, contactSearchQuery = "") }
            }
            GroupsEvent.HideContactSelectionDialog -> {
                _state.update { it.copy(showContactSelectionDialog = false, contactSearchQuery = "") }
            }
            is GroupsEvent.ContactSearchQueryChanged -> {
                _state.update { it.copy(contactSearchQuery = event.query) }
                filterAvailableContacts(event.query)
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
            GroupsEvent.ClearMessage -> {
                _state.update { it.copy(error = null, successMessage = null) }
            }
            GroupsEvent.RefreshGroups -> {
                syncGroups()
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

    private fun filterAvailableContacts(query: String) {
        val allContacts = _state.value.availableContacts
        val filtered = if (query.isBlank()) {
            allContacts
        } else {
            allContacts.filter { contact ->
                contact.displayName.contains(query, ignoreCase = true) ||
                        (contact.primaryPhone?.number?.contains(query) == true)
            }
        }
        _state.update { it.copy(filteredAvailableContacts = filtered) }
    }

    private fun syncGroups() {
        viewModelScope.launch {
            _state.update { it.copy(isLoading = true) }
            syncGroupsUseCase()
                .onSuccess {
                    loadGroups()
                }
                .onFailure { error ->
                    _state.update {
                        it.copy(
                            isLoading = false,
                            isInitialSyncInProgress = false,
                            error = error.message ?: "Failed to sync groups"
                        )
                    }
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
                            isInitialSyncInProgress = false,
                            error = error.message ?: "Failed to load groups"
                        )
                    }
                }
                .collect { groups ->
                    val contacts = withContext(Dispatchers.IO) {
                        contactRepository.getAllContacts().first()
                    }
                    val contactCountsByGroupName = buildContactCountsByGroupName(contacts)
                    val mergedGroups = mergeGroupsByName(groups, contactCountsByGroupName)
                    _state.update {
                        it.copy(
                            groups = mergedGroups,
                            filteredGroups = mergedGroups, // Reset filtered list
                            isLoading = false,
                            isInitialSyncInProgress = false,
                            error = null
                        )
                    }
                    filterGroups(_state.value.searchQuery)
                }
        }
    }

    private fun buildContactCountsByGroupName(
        contacts: List<com.contacts.android.contacts.domain.model.Contact>
    ): Map<String, Int> {
        val counts = mutableMapOf<String, Int>()
        contacts.forEach { contact ->
            val groupKeys = contact.groups
                .map { it.name.trim().lowercase(Locale.getDefault()) }
                .toSet()
            groupKeys.forEach { key ->
                counts[key] = (counts[key] ?: 0) + 1
            }
        }
        return counts
    }

    private fun mergeGroupsByName(
        groups: List<Group>,
        contactCountsByGroupName: Map<String, Int>
    ): List<Group> {
        if (groups.isEmpty()) return groups
        val grouped = groups.groupBy { it.name.trim().lowercase(Locale.getDefault()) }
        return grouped.values.map { groupList ->
            val key = groupList.first().name.trim().lowercase(Locale.getDefault())
            val totalCount = contactCountsByGroupName[key] ?: groupList.sumOf { it.contactCount }
            val representative = groupList.first()
            representative.copy(
                contactCount = totalCount,
                isSystemGroup = groupList.any { it.isSystemGroup }
            )
        }.sortedBy { it.name.lowercase(Locale.getDefault()) }
    }

    private fun loadAvailableContacts() {
        viewModelScope.launch {
            try {
                contactRepository.getAllContacts().first().let { contacts ->
                    _state.update {
                        it.copy(
                            availableContacts = contacts,
                            filteredAvailableContacts = contacts
                        )
                    }
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
                currentState.selectedGroup.copy(name = groupName)
            } else {
                Group(name = groupName)
            }

            saveGroupUseCase(group)
                .onSuccess { groupId ->
                    if (currentState.selectedGroup == null) {
                        analyticsManager.logGroupCreated()
                    }
                    val newContactIds = currentState.selectedContactIds

                    // Gestion des contacts si nécessaire
                    if (newContactIds.isNotEmpty()) {
                        if (currentState.selectedGroup != null) {
                            // Logique complexe de mise à jour des contacts existants vs nouveaux
                            // (Simplifiée ici pour l'exemple, on pourrait l'affiner comme avant)
                            val groupWithContacts = getGroupWithContactsUseCase(currentState.selectedGroup.id)
                            groupWithContacts?.let {
                                val currentContactIds = it.contacts.map { c -> c.id }.toSet()
                                val toAdd = newContactIds - currentContactIds
                                if (toAdd.isNotEmpty()) {
                                    addContactsToGroupUseCase(toAdd.toList(), currentState.selectedGroup.id)
                                }
                            }
                        } else {
                            // Nouveau groupe
                            addContactsToGroupUseCase(newContactIds.toList(), groupId)
                        }
                    }

                    _state.update {
                        it.copy(
                            showAddGroupDialog = false,
                            showEditGroupDialog = false,
                            showContactSelectionDialog = false,
                            groupNameInput = "",
                            selectedGroup = null,
                            selectedContactIds = emptySet(),
                            successMessage = "Group saved successfully"
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
                .onSuccess {
                    analyticsManager.logGroupDeleted()
                    _state.update { it.copy(successMessage = "Group deleted successfully") }
                }
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
                .onSuccess {
                    analyticsManager.logContactsAddedToGroup(contactIds.size)
                }
                .onFailure { error ->
                    _state.update {
                        it.copy(error = error.message ?: "Failed to add contacts to group")
                    }
                }
        }
    }
}
