package com.contacts.android.contacts.presentation.screens.contactlist

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.contacts.android.contacts.domain.model.Contact
import com.contacts.android.contacts.domain.usecase.contact.*
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
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
    private val syncContactsUseCase: SyncContactsUseCase,
    private val syncGroupsUseCase: com.contacts.android.contacts.domain.usecase.group.SyncGroupsUseCase,
    private val saveContactUseCase: SaveContactUseCase,
    private val restoreMigrationDataUseCase: RestoreMigrationDataUseCase,
    private val userPreferences: com.contacts.android.contacts.data.preferences.UserPreferences
) : ViewModel() {

    private val _state = MutableStateFlow(ContactListState())
    val state: StateFlow<ContactListState> = _state.asStateFlow()

    private val searchQueryFlow = MutableStateFlow("")

    private var _lastDeletedContact: Contact? = null

    // Sync state tracking to prevent redundant syncs
    private var lastSyncTimestamp: Long = 0
    private val syncCooldownMs = 60_000L // Only sync once per minute

    init {
        // OPTIMIZATION: Lazy load - observe local database first for instant display
        // Only sync from ContentProvider if database is empty or on explicit refresh
        observeContactUpdates()
        observeContactCount()
        observeUserPreferences()

        // Check if initial sync is needed
        viewModelScope.launch {
            val count = getContactsCountUseCase.getCount()
            if (count == 0) {
                // Database is empty, perform initial sync
                syncContacts()
            }
            // Otherwise, use cached data and sync in background if needed
        }
    }

    // OPTIMIZATION: Observe user preferences in ViewModel to prevent UI recompositions
    private fun observeUserPreferences() {
        // Observe sort/filter separately
        viewModelScope.launch {
            userPreferences.sortOrder.collect { sortOrder ->
                _state.update { it.copy(sortOrder = sortOrder) }
            }
        }

        viewModelScope.launch {
            userPreferences.contactFilter.collect { filter ->
                _state.update { it.copy(filter = filter) }
            }
        }

        // Observe other preferences
        viewModelScope.launch {
            combine(
                userPreferences.showPhoneNumbers,
                userPreferences.startNameWithSurname,
                userPreferences.formatPhoneNumbers,
                userPreferences.swipeDeleteConfirmation
            ) { showPhoneNumbers, startNameWithSurname, formatPhoneNumbers, swipeDeleteConfirmation ->
                QuadruplePreferences(showPhoneNumbers, startNameWithSurname, formatPhoneNumbers, swipeDeleteConfirmation)
            }
            .collect { prefs ->
                _state.update {
                    it.copy(
                        showPhoneNumbers = prefs.showPhoneNumbers,
                        startNameWithSurname = prefs.startNameWithSurname,
                        formatPhoneNumbers = prefs.formatPhoneNumbers,
                        swipeDeleteConfirmation = prefs.swipeDeleteConfirmation
                    )
                }
            }
        }
    }

    // Helper data class for combining preferences
    private data class QuadruplePreferences(
        val showPhoneNumbers: Boolean,
        val startNameWithSurname: Boolean,
        val formatPhoneNumbers: Boolean,
        val swipeDeleteConfirmation: Boolean
    )

    // Helper data class for combining contact flow results
    private data class QuadrupleResult(
        val contacts: List<Contact>,
        val favorites: List<Contact>,
        val groupedContacts: Map<Char, List<Contact>>,
        val availableSources: Map<String, Int>
    )

    private fun syncContacts() {
        viewModelScope.launch {
            val currentTime = System.currentTimeMillis()
            // Prevent redundant syncs within cooldown period
            if (currentTime - lastSyncTimestamp < syncCooldownMs) {
                return@launch
            }

            _state.update { it.copy(isLoading = true) }
            lastSyncTimestamp = currentTime

            // CRITICAL FIX: Sync groups FIRST, then contacts
            // This ensures groups exist in the database before contact-group associations are created
            // Following Fossify's pattern of syncing groups before contacts
            syncGroupsUseCase()
                .onSuccess {
                    // After groups are synced, sync contacts with their group associations
                    syncContactsUseCase()
                        .onSuccess {
                            // CRITICAL: Restore favorites and group associations from v136 migration
                            // This matches old contacts to new synced contacts by phone/email
                            restoreMigrationDataUseCase()
                                .onSuccess { restoredCount ->
                                    if (restoredCount > 0) {
                                        android.util.Log.d("ContactListViewModel", "Successfully restored $restoredCount favorites/groups from v136")
                                    }
                                }
                                .onFailure { error ->
                                    android.util.Log.w("ContactListViewModel", "Could not restore migration data: ${error.message}")
                                }
                            _state.update { it.copy(isLoading = false) }
                        }
                        .onFailure { error ->
                            _state.update {
                                it.copy(error = error.message ?: "Failed to sync contacts", isLoading = false)
                            }
                        }
                }
                .onFailure { error ->
                    _state.update {
                        it.copy(error = error.message ?: "Failed to sync groups", isLoading = false)
                    }
                }
        }
    }

    private fun observeContactUpdates() {
        viewModelScope.launch {
            // OPTIMIZATION: Debounce search to reduce excessive database queries
            val contactsFlow = searchQueryFlow
                .debounce(300)
                .distinctUntilChanged() // Prevent duplicate queries
                .flatMapLatest { query ->
                    if (query.isBlank()) {
                        getAllContactsUseCase()
                    } else {
                        searchContactsUseCase(query)
                    }
                }

            // OPTIMIZATION: Separate favorites flow to reduce unnecessary recalculations
            val favoritesFlow = getFavoriteContactsUseCase()

            // OPTIMIZATION: Only combine when necessary and apply filtering/sorting efficiently
            combine(
                contactsFlow,
                favoritesFlow,
                _state.map { it.sortOrder }.distinctUntilChanged(),
                _state.map { it.filter }.distinctUntilChanged(),
                searchQueryFlow
            ) { contacts, favorites, sortOrder, filter, query ->
                // All processing happens on IO dispatcher to avoid blocking main thread
                val filteredContacts = applyFilter(contacts, filter)
                val sortedContacts = applySorting(filteredContacts, sortOrder)

                val filteredFavorites = applyFilter(favorites, filter)
                val sortedFavorites = applySorting(filteredFavorites, sortOrder)

                // OPTIMIZATION: Only group contacts when not searching (expensive operation)
                val groupedContacts = if (query.isBlank()) {
                    sortedContacts
                        .groupBy { it.firstName.firstOrNull()?.uppercaseChar() ?: '#' }
                        .toSortedMap()
                } else {
                    emptyMap()
                }

                // Calculate available sources for filtering (Fossify-style)
                val availableSources = contacts
                    .groupBy { it.source }
                    .mapValues { it.value.size }
                    .filterKeys { it.isNotEmpty() }

                QuadrupleResult(sortedContacts, sortedFavorites, groupedContacts, availableSources)
            }
            .flowOn(Dispatchers.IO) // Process on background thread
            .catch { e ->
                _state.update {
                    it.copy(
                        isLoading = false,
                        error = e.message ?: "An error occurred"
                    )
                }
            }
            .collect { result ->
                _state.update {
                    it.copy(
                        contacts = result.contacts,
                        favorites = result.favorites,
                        groupedContacts = result.groupedContacts,
                        availableSources = result.availableSources,
                        isLoading = false
                    )
                }
            }
        }
    }

    fun onEvent(event: ContactListEvent) {
        when (event) {
            is ContactListEvent.SearchQueryChanged -> {
                _state.update { it.copy(searchQuery = event.query) }
                searchQueryFlow.value = event.query
            }
            is ContactListEvent.SortOrderChanged -> {
                _state.update { it.copy(sortOrder = event.sortOrder) }
                viewModelScope.launch {
                    userPreferences.setSortOrder(event.sortOrder)
                }
            }
            is ContactListEvent.FilterChanged -> {
                _state.update { it.copy(filter = event.filter) }
                viewModelScope.launch {
                    userPreferences.setContactFilter(event.filter)
                }
            }
            is ContactListEvent.ToggleFavorite -> {
                toggleFavorite(event.contactId, event.isFavorite)
            }
            is ContactListEvent.DeleteContact -> {
                deleteContact(event.contactId)
            }
            ContactListEvent.RefreshContacts -> {
                syncContacts()
            }
            ContactListEvent.ClearError -> {
                _state.update { it.copy(error = null) }
            }
            ContactListEvent.UndoDeleteContact -> {
                undoDeleteContact()
            }
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
                // TODO: Implement group assignment dialog
            }
            ContactListEvent.ExportSelectedContacts -> {
                exportSelectedContacts()
            }
            ContactListEvent.ShareSelectedContacts -> {
                shareSelectedContacts()
            }
            ContactListEvent.AddSelectedToFavorites -> {
                addSelectedToFavorites()
            }
            ContactListEvent.RemoveSelectedFromFavorites -> {
                removeSelectedFromFavorites()
            }
            ContactListEvent.MergeSelectedContacts -> {
                mergeSelectedContacts()
            }
        }
    }

    private fun shareSelectedContacts() {
        viewModelScope.launch {
            // Get selected contacts
            val selectedContacts = _state.value.contacts.filter {
                it.id in _state.value.selectedContactIds
            }
            // Share action will be triggered via callback to screen
            _state.update {
                it.copy(shareContacts = selectedContacts)
            }
        }
    }

    private fun addSelectedToFavorites() {
        viewModelScope.launch {
            _state.value.selectedContactIds.forEach { contactId ->
                toggleFavoriteUseCase(contactId, true)
            }
            _state.update { it.copy(isSelectionMode = false, selectedContactIds = emptySet()) }
        }
    }

    private fun removeSelectedFromFavorites() {
        viewModelScope.launch {
            _state.value.selectedContactIds.forEach { contactId ->
                toggleFavoriteUseCase(contactId, false)
            }
            _state.update { it.copy(isSelectionMode = false, selectedContactIds = emptySet()) }
        }
    }

    private fun mergeSelectedContacts() {
        viewModelScope.launch {
            val selectedIds = _state.value.selectedContactIds.toList()
            if (selectedIds.size >= 2) {
                // Merge action will be triggered via callback to screen
                _state.update {
                    it.copy(mergeContactIds = selectedIds)
                }
            }
        }
    }

    private fun exportSelectedContacts() {
        viewModelScope.launch {
            // Get selected contacts
            val selectedContacts = _state.value.contacts.filter {
                it.id in _state.value.selectedContactIds
            }
            // Export action will be triggered via callback to screen
            _state.update {
                it.copy(exportContacts = selectedContacts)
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
            val contactToDelete = _state.value.contacts.find { it.id == contactId }
            contactToDelete?.let { contact ->
                _lastDeletedContact = contact
                deleteContactUseCase(contactId)
                    .onFailure { error ->
                        _state.update {
                            it.copy(error = error.message ?: "Failed to delete contact")
                        }
                    }
            }
        }
    }

    private fun undoDeleteContact() {
        viewModelScope.launch {
            _lastDeletedContact?.let { contact ->
                saveContactUseCase(contact)
                    .onSuccess {
                        _lastDeletedContact = null
                    }
                    .onFailure { error ->
                        _state.update {
                            it.copy(error = error.message ?: "Failed to undo delete")
                        }
                    }
            }
        }
    }

    private fun applySorting(contacts: List<Contact>, sortOrder: com.contacts.android.contacts.domain.model.SortOrder): List<Contact> {
        val sorted = when (sortOrder.type) {
            com.contacts.android.contacts.domain.model.SortType.FIRST_NAME -> contacts.sortedBy { it.firstName.lowercase() }
            com.contacts.android.contacts.domain.model.SortType.MIDDLE_NAME -> contacts.sortedBy { it.middleName?.lowercase() ?: "" }
            com.contacts.android.contacts.domain.model.SortType.SURNAME -> contacts.sortedBy { it.lastName.lowercase() }
            com.contacts.android.contacts.domain.model.SortType.FULL_NAME -> contacts.sortedBy { it.displayName.lowercase() }
            com.contacts.android.contacts.domain.model.SortType.DATE_CREATED -> contacts.sortedBy { it.createdAt }
            com.contacts.android.contacts.domain.model.SortType.DATE_UPDATED -> contacts.sortedBy { it.updatedAt }
            com.contacts.android.contacts.domain.model.SortType.CUSTOM -> contacts // For custom ordering (e.g., in favorites)
        }

        return if (sortOrder.direction == com.contacts.android.contacts.domain.model.SortDirection.DESCENDING) {
            sorted.reversed()
        } else {
            sorted
        }
    }

    private fun applyFilter(contacts: List<Contact>, filter: com.contacts.android.contacts.domain.model.ContactFilter): List<Contact> {
        // First, apply account/source filtering (Fossify-style)
        var filteredContacts = contacts
        if (filter.ignoredSources.isNotEmpty()) {
            filteredContacts = filteredContacts.filter { contact ->
                contact.source !in filter.ignoredSources
            }
        }

        // Then apply type-based filtering
        return when (filter.type) {
            com.contacts.android.contacts.domain.model.ContactFilterType.ALL -> filteredContacts
            com.contacts.android.contacts.domain.model.ContactFilterType.FAVORITES_ONLY -> filteredContacts.filter { it.isFavorite }
            com.contacts.android.contacts.domain.model.ContactFilterType.WITH_PHONE_ONLY -> filteredContacts.filter { it.phoneNumbers.isNotEmpty() }
            com.contacts.android.contacts.domain.model.ContactFilterType.WITH_EMAIL_ONLY -> filteredContacts.filter { it.emails.isNotEmpty() }
            com.contacts.android.contacts.domain.model.ContactFilterType.WITH_ADDRESS_ONLY -> filteredContacts.filter { it.addresses.isNotEmpty() }
            com.contacts.android.contacts.domain.model.ContactFilterType.GROUPS -> {
                if (filter.selectedGroupIds.isEmpty()) {
                    filteredContacts
                } else {
                    filteredContacts.filter { contact ->
                        contact.groups.any { it.id in filter.selectedGroupIds }
                    }
                }
            }
            com.contacts.android.contacts.domain.model.ContactFilterType.CUSTOM -> filteredContacts // Custom filter logic
        }
    }

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
