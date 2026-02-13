package com.contacts.android.contacts.presentation.screens.contactlist

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.contacts.android.contacts.domain.model.Contact
import com.contacts.android.contacts.domain.usecase.contact.*
import com.contacts.android.contacts.domain.usecase.vcf.ExportContactsToVcfUseCase
import com.contacts.android.contacts.domain.usecase.vcf.ImportContactsFromVcfUseCase
import com.contacts.android.contacts.util.AnalyticsManager
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
    private val importContactsFromVcfUseCase: ImportContactsFromVcfUseCase,
    private val exportContactsToVcfUseCase: ExportContactsToVcfUseCase,
    private val userPreferences: com.contacts.android.contacts.data.preferences.UserPreferences,
    private val contactRepository: com.contacts.android.contacts.domain.repository.ContactRepository,
    private val dataRecoveryHelper: com.contacts.android.contacts.data.recovery.DataRecoveryHelper,
    private val analyticsManager: AnalyticsManager
) : ViewModel() {

    private val _state = MutableStateFlow(ContactListState())
    val state: StateFlow<ContactListState> = _state.asStateFlow()

    private val searchQueryFlow = MutableStateFlow("")

    private var _lastDeletedContact: Contact? = null

    // Sync state tracking to prevent redundant syncs
    private var lastSyncTimestamp: Long = 0
    private val syncCooldownMs = 60_000L // Only sync once per minute

    init {
        // Start observing database immediately for instant display if data exists
        observeContactUpdates()
        observeContactCount()
        observeUserPreferences()

        // Check if we have contacts - if yes, no need to wait for permission
        viewModelScope.launch(Dispatchers.IO) {
            val count = getContactsCountUseCase.getCount()
            if (count > 0) {
                // DB has contacts - no initial sync needed
                _state.update { it.copy(isInitialSyncInProgress = false) }
            }
            // If count == 0, keep isInitialSyncInProgress = true
            // The sync will be triggered from UI when permission is granted
        }
    }

    /**
     * Called from UI when READ_CONTACTS permission is granted
     * This triggers the initial sync if needed
     */
    fun onPermissionGranted() {
        android.util.Log.d("ContactListViewModel", "onPermissionGranted() called")
        viewModelScope.launch(Dispatchers.IO) {
            val count = getContactsCountUseCase.getCount()
            android.util.Log.d("ContactListViewModel", "Current contact count: $count, isInitialSyncInProgress: ${_state.value.isInitialSyncInProgress}")

            if (count == 0 && _state.value.isInitialSyncInProgress) {
                android.util.Log.d("ContactListViewModel", "Starting initial sync...")
                _state.update { it.copy(isLoading = true) }
                performInitialSync()
            } else if (count > 0) {
                android.util.Log.d("ContactListViewModel", "Contacts already exist, skipping sync")
                _state.update { it.copy(isInitialSyncInProgress = false) }
            } else {
                android.util.Log.d("ContactListViewModel", "Sync already completed or in progress")
            }
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

        // Observe favorites view type
        viewModelScope.launch {
            userPreferences.favoritesViewType.collect { viewType ->
                _state.update { it.copy(favoritesViewType = viewType) }
            }
        }

        // Observe other preferences
        viewModelScope.launch {
            combine(
                userPreferences.showPhoneNumbers,
                userPreferences.startNameWithSurname,
                userPreferences.formatPhoneNumbers
            ) { showPhoneNumbers, startNameWithSurname, formatPhoneNumbers ->
                TriplePreferences(showPhoneNumbers, startNameWithSurname, formatPhoneNumbers)
            }
            .collect { prefs ->
                _state.update {
                    it.copy(
                        showPhoneNumbers = prefs.showPhoneNumbers,
                        startNameWithSurname = prefs.startNameWithSurname,
                        formatPhoneNumbers = prefs.formatPhoneNumbers
                    )
                }
            }
        }
    }

    // Helper data class for combining preferences
    private data class TriplePreferences(
        val showPhoneNumbers: Boolean,
        val startNameWithSurname: Boolean,
        val formatPhoneNumbers: Boolean
    )

    // Helper data class for contact list preferences
    private data class QuadruplePreferences(
        val sortOrder: com.contacts.android.contacts.domain.model.SortOrder,
        val filter: com.contacts.android.contacts.domain.model.ContactFilter,
        val customOrder: List<Long>,
        val startNameWithSurname: Boolean
    )

    // Helper data class for combining contact flow results
    private data class QuadrupleResult(
        val contacts: List<Contact>,
        val favorites: List<Contact>,
        val groupedContacts: Map<Char, List<Contact>>,
        val availableSources: Map<String, Int>
    )

    /**
     * Initial sync for first app launch - runs immediately without cooldown
     * This is called when the database is empty to fetch contacts from the device
     */
    private suspend fun performInitialSync() {
        lastSyncTimestamp = System.currentTimeMillis()

        syncGroupsUseCase()
            .onSuccess {
                syncContactsUseCase()
                    .onSuccess {
                        restoreMigrationDataUseCase()
                            .onSuccess { restoredCount ->
                                if (restoredCount > 0) {
                                    android.util.Log.d("ContactListViewModel", "Successfully restored $restoredCount favorites/groups from v136")
                                }
                            }
                            .onFailure { error ->
                                android.util.Log.w("ContactListViewModel", "Could not restore migration data: ${error.message}")
                            }

                        _state.update {
                            it.copy(
                                isLoading = false,
                                isInitialSyncInProgress = false
                            )
                        }
                        android.util.Log.d("ContactListViewModel", "Initial sync completed successfully")
                    }
                    .onFailure { error ->
                        _state.update {
                            it.copy(
                                error = error.message ?: "Failed to sync contacts",
                                isLoading = false,
                                isInitialSyncInProgress = false
                            )
                        }
                        android.util.Log.e("ContactListViewModel", "Initial sync failed: ${error.message}")
                    }
            }
            .onFailure { error ->
                _state.update {
                    it.copy(
                        error = error.message ?: "Failed to sync groups",
                        isLoading = false,
                        isInitialSyncInProgress = false
                    )
                }
                android.util.Log.e("ContactListViewModel", "Initial group sync failed: ${error.message}")
            }
    }

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
            // Count contacts before sync to show in completion dialog
            val contactsBeforeSync = getContactsCountUseCase.getCount()

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

                            // Count new synced contacts and show completion dialog
                            val contactsAfterSync = getContactsCountUseCase.getCount()
                            val syncedCount = contactsAfterSync - contactsBeforeSync

                            _state.update {
                                it.copy(
                                    isLoading = false,
                                    showSyncCompleteDialog = syncedCount > 0,
                                    syncedContactsCount = syncedCount
                                )
                            }
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
            val contactsFlow = getAllContactsUseCase()

            // OPTIMIZATION: Separate favorites flow to reduce unnecessary recalculations
            val favoritesFlow = getFavoriteContactsUseCase()

            // Group preferences to avoid combine argument limit (max 5)
            val preferencesFlow = combine(
                _state.map { it.sortOrder }.distinctUntilChanged(),
                _state.map { it.filter }.distinctUntilChanged(),
                userPreferences.favoritesCustomOrder.onStart { emit(emptyList()) },
                _state.map { it.startNameWithSurname }.distinctUntilChanged()
            ) { sortOrder, filter, customOrder, startNameWithSurname ->
                QuadruplePreferences(sortOrder, filter, customOrder, startNameWithSurname)
            }

            // OPTIMIZATION: Only combine when necessary and apply filtering/sorting efficiently
            combine(
                contactsFlow,
                favoritesFlow,
                preferencesFlow,
                searchQueryFlow.debounce(300).distinctUntilChanged()
            ) { contacts, favorites, prefs, query ->
                val (sortOrder, filter, customOrder, startNameWithSurname) = prefs
                val normalizedQuery = query.trim()
                
                // All processing happens on IO dispatcher to avoid blocking main thread
                val filteredContacts = applyFilter(contacts, filter)
                val filteredFavorites = applyFilter(favorites, filter)

                val searchedContacts = if (normalizedQuery.isBlank()) {
                    filteredContacts
                } else {
                    filterByQuery(filteredContacts, normalizedQuery)
                }

                val searchedFavorites = if (normalizedQuery.isBlank()) {
                    filteredFavorites
                } else {
                    filterByQuery(filteredFavorites, normalizedQuery)
                }

                val sortedContacts = applySorting(searchedContacts, sortOrder)
                // Use custom order for favorites if selected, or if implicit default for favorites tab
                val sortedFavorites = applySorting(searchedFavorites, sortOrder, customOrder)

                // OPTIMIZATION: Only group contacts when not searching (expensive operation)
                val groupedContacts = if (normalizedQuery.isBlank()) {
                    sortedContacts
                        .groupBy { getSectionKey(it, sortOrder, startNameWithSurname) }
                        .toSortedMap(sectionComparator())
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
                        hasLoadedContacts = true
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
                if (event.query.isNotBlank()) {
                    analyticsManager.logSearch(event.query)
                }
            }
            is ContactListEvent.SortOrderChanged -> {
                _state.update { it.copy(sortOrder = event.sortOrder) }
                analyticsManager.logSortOrderChanged(event.sortOrder.type.name)
                viewModelScope.launch {
                    userPreferences.setSortOrder(event.sortOrder)
                }
            }
            is ContactListEvent.FilterChanged -> {
                _state.update { it.copy(filter = event.filter) }
                analyticsManager.logFilterChanged(event.filter.type.name)
                viewModelScope.launch {
                    userPreferences.setContactFilter(event.filter)
                }
            }
            is ContactListEvent.ToggleFavorite -> {
                analyticsManager.logFavoriteToggled(event.isFavorite)
                toggleFavorite(event.contactId, event.isFavorite)
            }
            is ContactListEvent.DeleteContact -> {
                analyticsManager.logContactDeleted()
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
            is ContactListEvent.ImportContacts -> {
                importContacts(event.uri)
            }
            is ContactListEvent.ExportAllContacts -> {
                exportAllContacts(event.uri, event.includePhotos)
            }
            ContactListEvent.ClearImportResult -> {
                _state.update { it.copy(importResult = null) }
            }
            ContactListEvent.ClearExportResult -> {
                _state.update { it.copy(exportResult = null) }
            }
            ContactListEvent.ShowSyncDialog -> {
                syncContacts()
            }
            ContactListEvent.DismissSyncDialog -> {
                _state.update { it.copy(showSyncDialog = false) }
            }
            ContactListEvent.ConfirmSync -> {
                syncContacts()
            }
            ContactListEvent.DismissSyncSuggestion -> {
                _state.update { it.copy(showSyncSuggestion = false) }
            }
            ContactListEvent.DismissSyncCompleteDialog -> {
                _state.update { it.copy(showSyncCompleteDialog = false) }
            }
            // Favorites events
            is ContactListEvent.UpdateFavoritesOrder -> {
                viewModelScope.launch {
                    userPreferences.setFavoritesCustomOrder(event.newOrder)
                    // If not already in custom sort mode, switch to it
                    if (_state.value.sortOrder.type != com.contacts.android.contacts.domain.model.SortType.CUSTOM) {
                        val newSort = com.contacts.android.contacts.domain.model.SortOrder(
                            com.contacts.android.contacts.domain.model.SortType.CUSTOM,
                            com.contacts.android.contacts.domain.model.SortDirection.ASCENDING
                        )
                        userPreferences.setSortOrder(newSort)
                    }
                }
            }
            is ContactListEvent.ToggleFavoritesViewType -> {
                viewModelScope.launch {
                    userPreferences.setFavoritesViewType(event.viewType)
                }
            }
        }
    }

    private fun shareSelectedContacts() {
        viewModelScope.launch {
            val selectedContacts = _state.value.contacts.filter {
                it.id in _state.value.selectedContactIds
            }
            analyticsManager.logContactShared()
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
                analyticsManager.logContactsMerged(selectedIds.size)
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

    private fun applySorting(
        contacts: List<Contact>, 
        sortOrder: com.contacts.android.contacts.domain.model.SortOrder,
        customOrder: List<Long> = emptyList()
    ): List<Contact> {
        if (sortOrder.type == com.contacts.android.contacts.domain.model.SortType.CUSTOM) {
            if (customOrder.isEmpty()) return contacts
            val orderMap = customOrder.withIndex().associate { it.value to it.index }
            // Sort by index in custom order, unknown items go to end
            return contacts.sortedBy { orderMap[it.id] ?: Int.MAX_VALUE }
        }

        val selector: (Contact) -> String = when (sortOrder.type) {
            com.contacts.android.contacts.domain.model.SortType.FIRST_NAME -> { it -> it.firstName }
            com.contacts.android.contacts.domain.model.SortType.MIDDLE_NAME -> { it -> it.middleName ?: "" }
            com.contacts.android.contacts.domain.model.SortType.SURNAME -> { it -> it.lastName }
            com.contacts.android.contacts.domain.model.SortType.FULL_NAME -> { it -> it.displayName }
            else -> { it -> "" } // For date sorting, we handle separately below
        }

        val sorted = if (sortOrder.type == com.contacts.android.contacts.domain.model.SortType.DATE_CREATED) {
            contacts.sortedBy { it.createdAt }
        } else if (sortOrder.type == com.contacts.android.contacts.domain.model.SortType.DATE_UPDATED) {
            contacts.sortedBy { it.updatedAt }
        } else if (sortOrder.type == com.contacts.android.contacts.domain.model.SortType.CUSTOM) {
            contacts
        } else {
            // Custom comparator for Latin-first sorting (Fossify style)
            contacts.sortedWith(Comparator { c1, c2 ->
                val s1 = selector(c1)
                val s2 = selector(c2)

                val s1Blank = s1.isBlank() || isNumericOnlyName(s1)
                val s2Blank = s2.isBlank() || isNumericOnlyName(s2)

                if (s1Blank && !s2Blank) return@Comparator 1
                if (!s1Blank && s2Blank) return@Comparator -1
                if (s1Blank && s2Blank) return@Comparator 0

                val s1Latin = isLatin(s1)
                val s2Latin = isLatin(s2)

                when {
                    s1Latin && !s2Latin -> -1
                    !s1Latin && s2Latin -> 1
                    else -> s1.compareTo(s2, ignoreCase = true)
                }
            })
        }

        return if (sortOrder.direction == com.contacts.android.contacts.domain.model.SortDirection.DESCENDING) {
            sorted.reversed()
        } else {
            sorted
        }
    }

    private fun isLatin(text: String): Boolean {
        if (text.isBlank()) return false
        val firstChar = text.trim().first()
        // Check if character is in Basic Latin block (ASCII letters) or Latin-1 Supplement (accented letters)
        // Simple check for A-Z, a-z and common accented characters
        return (firstChar in 'A'..'Z') || (firstChar in 'a'..'z') || 
               (firstChar in '\u00C0'..'\u00FF') // Latin-1 Supplement (Accented chars)
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

    private fun filterByQuery(contacts: List<Contact>, query: String): List<Contact> {
        val normalizedQuery = query.trim()
        if (normalizedQuery.isBlank()) return contacts

        val digitsQuery = normalizePhone(normalizedQuery)
        return contacts.filter { contact ->
            contactMatchesQuery(contact, normalizedQuery, digitsQuery)
        }
    }

    private fun contactMatchesQuery(contact: Contact, query: String, digitsQuery: String): Boolean {
        if (contact.displayName.contains(query, ignoreCase = true)) return true
        if (contact.nickname?.contains(query, ignoreCase = true) == true) return true
        if (contact.organization?.contains(query, ignoreCase = true) == true) return true
        if (contact.title?.contains(query, ignoreCase = true) == true) return true
        if (contact.notes?.contains(query, ignoreCase = true) == true) return true

        if (contact.phoneNumbers.any { phone ->
                phone.number.contains(query, ignoreCase = true) ||
                    (digitsQuery.isNotEmpty() && normalizePhone(phone.number).contains(digitsQuery))
            }) {
            return true
        }

        if (contact.emails.any { it.email.contains(query, ignoreCase = true) }) return true

        if (contact.addresses.any { address ->
                listOfNotNull(
                    address.street,
                    address.city,
                    address.state,
                    address.postalCode,
                    address.country,
                    address.fullAddress
                ).any { it.contains(query, ignoreCase = true) }
            }) {
            return true
        }

        if (contact.instantMessages.any { it.handle.contains(query, ignoreCase = true) }) return true
        if (contact.websites.any { it.url.contains(query, ignoreCase = true) }) return true

        return false
    }

    private fun normalizePhone(phone: String): String {
        return phone.replace(Regex("[^0-9]"), "")
    }

    private fun getSectionKey(
        contact: Contact,
        sortOrder: com.contacts.android.contacts.domain.model.SortOrder,
        startNameWithSurname: Boolean
    ): Char {
        val name = getSortName(contact, sortOrder, startNameWithSurname).trim()
        if (name.isBlank() || isNumericOnlyName(name)) return '#'
        return name.firstOrNull()?.uppercaseChar() ?: '#'
    }

    private fun getSortName(
        contact: Contact,
        sortOrder: com.contacts.android.contacts.domain.model.SortOrder,
        startNameWithSurname: Boolean
    ): String {
        return when (sortOrder.type) {
            com.contacts.android.contacts.domain.model.SortType.FIRST_NAME -> contact.firstName
            com.contacts.android.contacts.domain.model.SortType.MIDDLE_NAME -> contact.middleName ?: ""
            com.contacts.android.contacts.domain.model.SortType.SURNAME -> contact.lastName
            com.contacts.android.contacts.domain.model.SortType.FULL_NAME -> contact.displayName
            else -> {
                if (startNameWithSurname && contact.lastName.isNotBlank()) {
                    contact.lastName
                } else {
                    contact.firstName
                }
            }
        }
    }

    private fun isNumericOnlyName(text: String): Boolean {
        val trimmed = text.trim()
        if (trimmed.isBlank()) return true
        if (trimmed.any { it.isLetter() }) return false
        return trimmed.any { it.isDigit() }
    }

    private fun sectionComparator(): Comparator<Char> {
        return Comparator { a, b ->
            if (a == b) return@Comparator 0
            if (a == '#') return@Comparator 1
            if (b == '#') return@Comparator -1

            val aLatin = a in 'A'..'Z'
            val bLatin = b in 'A'..'Z'

            when {
                aLatin && !bLatin -> -1
                !aLatin && bLatin -> 1
                else -> a.compareTo(b)
            }
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

    private fun importContacts(uri: android.net.Uri) {
        viewModelScope.launch {
            _state.update { it.copy(isImporting = true, importResult = null) }

            importContactsFromVcfUseCase(uri)
                .onSuccess { count ->
                    analyticsManager.logContactsImported(count)
                    _state.update {
                        it.copy(
                            isImporting = false,
                            importResult = ImportExportResult(
                                success = true,
                                count = count
                            )
                        )
                    }
                }
                .onFailure { error ->
                    _state.update {
                        it.copy(
                            isImporting = false,
                            importResult = ImportExportResult(
                                success = false,
                                errorMessage = error.message ?: "Failed to import contacts"
                            )
                        )
                    }
                }
        }
    }

    private fun exportAllContacts(uri: android.net.Uri, includePhotos: Boolean) {
        viewModelScope.launch {
            _state.update { it.copy(isExporting = true, exportResult = null) }

            exportContactsToVcfUseCase.exportAll(uri, includePhotos)
                .onSuccess { count ->
                    analyticsManager.logContactsExported(count)
                    _state.update {
                        it.copy(
                            isExporting = false,
                            exportResult = ImportExportResult(
                                success = true,
                                count = count
                            )
                        )
                    }
                }
                .onFailure { error ->
                    _state.update {
                        it.copy(
                            isExporting = false,
                            exportResult = ImportExportResult(
                                success = false,
                                errorMessage = error.message ?: "Failed to export contacts"
                            )
                        )
                    }
                }
        }
    }

    /**
     * Get the default filename for VCF export
     */
    fun getExportFilename(): String = exportContactsToVcfUseCase.getDefaultFilename()
}
