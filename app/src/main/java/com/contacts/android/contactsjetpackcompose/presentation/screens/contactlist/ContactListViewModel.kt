package com.contacts.android.contactsjetpackcompose.presentation.screens.contactlist

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.contacts.android.contactsjetpackcompose.domain.model.Contact
import com.contacts.android.contactsjetpackcompose.domain.usecase.contact.*
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

@OptIn(FlowPreview::class)
@HiltViewModel
class ContactListViewModel @Inject constructor(
    private val getAllContactsUseCase: GetAllContactsUseCase,
    private val getFavoriteContactsUseCase: GetFavoriteContactsUseCase,
    private val searchContactsUseCase: SearchContactsUseCase,
    private val toggleFavoriteUseCase: ToggleFavoriteUseCase,
    private val deleteContactUseCase: DeleteContactUseCase,
    private val deleteMultipleContactsUseCase: DeleteMultipleContactsUseCase,
    private val getContactsCountUseCase: GetContactsCountUseCase,
    private val syncContactsUseCase: SyncContactsUseCase
) : ViewModel() {

    private val _state = MutableStateFlow(ContactListState())
    val state: StateFlow<ContactListState> = _state.asStateFlow()

    private val searchQueryFlow = MutableStateFlow("")

    init {
        syncContacts()
        loadContacts()
        observeContactCount()
    }

    private fun syncContacts() {
        viewModelScope.launch {
            syncContactsUseCase()
                .onFailure { error ->
                    _state.update {
                        it.copy(error = error.message ?: "Failed to sync contacts")
                    }
                }
        }
    }

    fun onEvent(event: ContactListEvent) {
        when (event) {
            is ContactListEvent.SearchQueryChanged -> {
                searchQueryFlow.value = event.query
                _state.update { it.copy(searchQuery = event.query) }
            }
            is ContactListEvent.SortOrderChanged -> {
                _state.update { it.copy(sortOrder = event.sortOrder) }
            }
            is ContactListEvent.FilterChanged -> {
                _state.update { it.copy(filter = event.filter) }
            }
            is ContactListEvent.ToggleFavorite -> {
                toggleFavorite(event.contactId, event.isFavorite)
            }
            is ContactListEvent.DeleteContact -> {
                deleteContact(event.contactId)
            }
            ContactListEvent.RefreshContacts -> {
                loadContacts()
            }
            ContactListEvent.ClearError -> {
                _state.update { it.copy(error = null) }
            }

            // Multi-select mode events
            ContactListEvent.EnterSelectionMode -> {
                _state.update { it.copy(isSelectionMode = true, selectedContactIds = emptySet()) }
            }
            ContactListEvent.ExitSelectionMode -> {
                _state.update { it.copy(isSelectionMode = false, selectedContactIds = emptySet()) }
            }
            is ContactListEvent.ToggleContactSelection -> {
                toggleContactSelection(event.contactId)
            }
            ContactListEvent.SelectAllContacts -> {
                selectAllContacts()
            }
            ContactListEvent.DeselectAllContacts -> {
                _state.update { it.copy(selectedContactIds = emptySet()) }
            }
            ContactListEvent.DeleteSelectedContacts -> {
                deleteSelectedContacts()
            }
            is ContactListEvent.AddSelectedToGroup -> {
                // This will be handled by a separate dialog/screen
                // For now, just a placeholder
            }
            ContactListEvent.ExportSelectedContacts -> {
                // This will be handled by navigation to export screen
                // For now, just a placeholder
            }
        }
    }

    private fun loadContacts() {
        viewModelScope.launch {
            _state.update { it.copy(isLoading = true, error = null) }

            try {
                combine(
                    searchQueryFlow
                        .debounce(300)
                        .flatMapLatest { query ->
                            if (query.isBlank()) {
                                getAllContactsUseCase()
                            } else {
                                searchContactsUseCase(query)
                            }
                        },
                    getFavoriteContactsUseCase()
                ) { contacts, favorites ->
                    // Apply filter
                    val filteredContacts = applyFilter(contacts, _state.value.filter)
                    val filteredFavorites = applyFilter(favorites, _state.value.filter)

                    // Apply sort
                    val sortedContacts = applySorting(filteredContacts, _state.value.sortOrder)
                    val sortedFavorites = applySorting(filteredFavorites, _state.value.sortOrder)

                    val groupedContacts = if (searchQueryFlow.value.isBlank()) {
                        sortedContacts.groupBy {
                            it.firstName.firstOrNull()?.uppercaseChar() ?: '#'
                        }.toSortedMap()
                    } else {
                        emptyMap()
                    }

                    _state.update {
                        it.copy(
                            contacts = sortedContacts,
                            favorites = sortedFavorites,
                            groupedContacts = groupedContacts,
                            isLoading = false,
                            error = null
                        )
                    }
                }.collect()
            } catch (e: Exception) {
                _state.update {
                    it.copy(
                        isLoading = false,
                        error = e.message ?: "An error occurred while loading contacts"
                    )
                }
            }
        }
    }

    private fun observeContactCount() {
        viewModelScope.launch {
            getContactsCountUseCase.getCountFlow()
                .collect { count ->
                    _state.update { it.copy(contactCount = count) }
                }
        }
    }

    private fun toggleFavorite(contactId: Long, isFavorite: Boolean) {
        viewModelScope.launch {
            toggleFavoriteUseCase(contactId, isFavorite)
                .onFailure { error ->
                    _state.update {
                        it.copy(error = error.message ?: "Failed to update favorite status")
                    }
                }
        }
    }

    private fun deleteContact(contactId: Long) {
        viewModelScope.launch {
            deleteContactUseCase(contactId)
                .onFailure { error ->
                    _state.update {
                        it.copy(error = error.message ?: "Failed to delete contact")
                    }
                }
        }
    }

    private fun applySorting(contacts: List<Contact>, sortOrder: SortOrder): List<Contact> {
        return when (sortOrder) {
            SortOrder.FIRST_NAME_ASC -> contacts.sortedBy { it.firstName.lowercase() }
            SortOrder.FIRST_NAME_DESC -> contacts.sortedByDescending { it.firstName.lowercase() }
            SortOrder.LAST_NAME_ASC -> contacts.sortedBy { it.lastName.lowercase() }
            SortOrder.LAST_NAME_DESC -> contacts.sortedByDescending { it.lastName.lowercase() }
            SortOrder.DATE_ADDED -> contacts.sortedByDescending { it.createdAt }
            SortOrder.DATE_MODIFIED -> contacts.sortedByDescending { it.updatedAt }
        }
    }

    private fun applyFilter(contacts: List<Contact>, filter: ContactFilter): List<Contact> {
        return when (filter) {
            ContactFilter.ALL -> contacts
            ContactFilter.WITH_PHONE -> contacts.filter { it.phoneNumbers.isNotEmpty() }
            ContactFilter.WITH_EMAIL -> contacts.filter { it.emails.isNotEmpty() }
            ContactFilter.WITH_ADDRESS -> contacts.filter { it.addresses.isNotEmpty() }
        }
    }

    // Multi-select mode functions
    private fun toggleContactSelection(contactId: Long) {
        _state.update { currentState ->
            val selectedIds = currentState.selectedContactIds.toMutableSet()
            if (contactId in selectedIds) {
                selectedIds.remove(contactId)
            } else {
                selectedIds.add(contactId)
            }
            currentState.copy(selectedContactIds = selectedIds)
        }
    }

    private fun selectAllContacts() {
        val allContactIds = _state.value.contacts.map { it.id }.toSet()
        _state.update { it.copy(selectedContactIds = allContactIds) }
    }

    private fun deleteSelectedContacts() {
        viewModelScope.launch {
            val selectedIds = _state.value.selectedContactIds.toList()
            if (selectedIds.isEmpty()) return@launch

            deleteMultipleContactsUseCase(selectedIds)
                .onSuccess { deletedCount ->
                    _state.update {
                        it.copy(
                            isSelectionMode = false,
                            selectedContactIds = emptySet(),
                            error = null
                        )
                    }
                }
                .onFailure { error ->
                    _state.update {
                        it.copy(error = error.message ?: "Failed to delete contacts")
                    }
                }
        }
    }
}
