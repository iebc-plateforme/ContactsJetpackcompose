package com.contacts.android.contacts.presentation.screens.contactlist

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.contacts.android.contacts.R
import com.contacts.android.contacts.presentation.components.*
import com.contacts.android.contacts.presentation.components.SafeSwipeableContactListItem
import com.google.accompanist.swiperefresh.rememberSwipeRefreshState
import com.google.accompanist.swiperefresh.SwipeRefresh
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ContactListScreen(
    onContactClick: (Long) -> Unit,
    onAddContact: () -> Unit,
    onNavigateToGroups: () -> Unit,
    onNavigateToSettings: () -> Unit,
    modifier: Modifier = Modifier,
    showFavoritesSection: Boolean = true,
    hideTopBar: Boolean = false,
    hideFab: Boolean = false,
    viewModel: ContactListViewModel = hiltViewModel()
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    var showMenu by remember { mutableStateOf(false) }
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()

    // OPTIMIZATION: User preferences now come from ViewModel state to prevent recompositions
    val swipeRefreshState = rememberSwipeRefreshState(
        isRefreshing = state.isLoading
    )

    Scaffold(
        snackbarHost = { SnackbarHost(hostState = snackbarHostState) },
        topBar = {
            if (!hideTopBar) {
                if (state.isSelectionMode) {
                    SelectionModeTopBar(
                        selectedCount = state.selectedCount,
                        onExitSelectionMode = {
                            viewModel.onEvent(ContactListEvent.ExitSelectionMode)
                        },
                        onSelectAll = {
                            viewModel.onEvent(ContactListEvent.SelectAllContacts)
                        },
                        onDeselectAll = {
                            viewModel.onEvent(ContactListEvent.DeselectAllContacts)
                        },
                        onDeleteSelected = {
                            viewModel.onEvent(ContactListEvent.DeleteSelectedContacts)
                        },
                        isAllSelected = state.isAllSelected
                    )
                } else {
                    var showSortDialog by remember { mutableStateOf(false) }
                    var showFilterDialog by remember { mutableStateOf(false) }

                    ContactListTopBar(
                        searchQuery = state.searchQuery,
                        onSearchQueryChange = { query ->
                            viewModel.onEvent(ContactListEvent.SearchQueryChanged(query))
                        },
                        onMenuClick = { showMenu = true },
                        showMenu = showMenu,
                        onDismissMenu = { showMenu = false },
                        onNavigateToGroups = onNavigateToGroups,
                        onNavigateToSettings = onNavigateToSettings,
                        onSortClick = { showSortDialog = true },
                        onFilterClick = { showFilterDialog = true }
                    )

                    // Sort Dialog
                    if (showSortDialog) {
                        SortDialog(
                            currentSort = state.sortOrder,
                            showCustomSort = false,
                            onDismiss = { showSortDialog = false },
                            onSortSelected = { sortOrder ->
                                viewModel.onEvent(ContactListEvent.SortOrderChanged(sortOrder))
                                showSortDialog = false
                            }
                        )
                    }

                    // Enhanced Filter Dialog with Account/Source Filtering (Fossify-style)
                    if (showFilterDialog) {
                        EnhancedFilterDialog(
                            currentFilter = state.filter,
                            totalContactsCount = state.contactCount,
                            favoritesCount = state.favorites.size,
                            withPhoneCount = state.contacts.count { it.phoneNumbers.isNotEmpty() },
                            withEmailCount = state.contacts.count { it.emails.isNotEmpty() },
                            withAddressCount = state.contacts.count { it.addresses.isNotEmpty() },
                            availableGroups = emptyList(), // Groups filtering can be added later if needed
                            availableSources = state.availableSources, // NEW: Account/source filtering
                            onDismiss = { showFilterDialog = false },
                            onFilterSelected = { filter ->
                                viewModel.onEvent(ContactListEvent.FilterChanged(filter))
                                showFilterDialog = false
                            }
                        )
                    }
                }
            }
        },
        floatingActionButton = {
            if (!hideFab) {
                FloatingActionButton(
                    onClick = onAddContact,
                    containerColor = MaterialTheme.colorScheme.primaryContainer,
                    contentColor = MaterialTheme.colorScheme.onPrimaryContainer
                ) {
                    Icon(
                        imageVector = Icons.Default.Add,
                        contentDescription = stringResource(R.string.contact_add)
                    )
                }
            }
        },
        floatingActionButtonPosition = FabPosition.Center
    ) { paddingValues ->
        SwipeRefresh(
            state = swipeRefreshState,
            onRefresh = { viewModel.onEvent(ContactListEvent.RefreshContacts) },
            modifier = modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            when {
                state.isLoading && !state.hasContacts -> {
                    ShimmerContactList()
                }
                !state.hasContacts && state.searchQuery.isBlank() -> {
                    EmptyState(
                        icon = Icons.Default.ContactPage,
                        title = stringResource(R.string.empty_contacts),
                        description = stringResource(R.string.empty_contacts_description)
                    )
                }
                !state.hasContacts && state.searchQuery.isNotBlank() -> {
                    EmptyState(
                        icon = Icons.Default.SearchOff,
                        title = stringResource(R.string.search_no_results),
                        description = stringResource(R.string.try_different_search_term)
                    )
                }
                else -> {
                    ContactListContent(
                        state = state,
                        onContactClick = onContactClick,
                        onToggleFavorite = { contactId, isFavorite ->
                            viewModel.onEvent(
                                ContactListEvent.ToggleFavorite(contactId, isFavorite)
                            )
                        },
                        onToggleSelection = { contactId ->
                            viewModel.onEvent(
                                ContactListEvent.ToggleContactSelection(contactId)
                            )
                        },
                        onLongClick = { contactId ->
                            viewModel.onEvent(ContactListEvent.EnterSelectionMode)
                            viewModel.onEvent(ContactListEvent.ToggleContactSelection(contactId))
                        },
                        onDeleteContact = { contactId ->
                            viewModel.onEvent(ContactListEvent.DeleteContact(contactId))
                            scope.launch {
                                val result = snackbarHostState.showSnackbar(
                                    message = "Contact deleted",
                                    actionLabel = "Undo",
                                    duration = SnackbarDuration.Short
                                )
                                if (result == SnackbarResult.ActionPerformed) {
                                    viewModel.onEvent(ContactListEvent.UndoDeleteContact)
                                }
                            }
                        },
                        showFavoritesSection = showFavoritesSection
                    )
                }
            }

            // Error snackbar
            state.error?.let { errorMessage ->
                LaunchedEffect(errorMessage) {
                    // Show error message
                    // You can implement SnackbarHost here if needed
                    viewModel.onEvent(ContactListEvent.ClearError)
                }
            }
        }
    }
}

@Composable
private fun ContactListContent(
    state: ContactListState,
    onContactClick: (Long) -> Unit,
    onToggleFavorite: (Long, Boolean) -> Unit,
    onToggleSelection: (Long) -> Unit,
    onLongClick: (Long) -> Unit,
    onDeleteContact: (Long) -> Unit,
    showFavoritesSection: Boolean = true
) {
    val listState = rememberLazyListState()
    val scope = rememberCoroutineScope()

    // Confirmation dialog state for swipe-to-delete
    var contactToDelete by remember { mutableStateOf<Long?>(null) }
    var showDeleteConfirmation by remember { mutableStateOf(false) }

    // Helper function to handle delete with optional confirmation
    val handleDelete: (Long) -> Unit = remember(state.swipeDeleteConfirmation) {
        { contactId ->
            if (state.swipeDeleteConfirmation) {
                // Show confirmation dialog
                contactToDelete = contactId
                showDeleteConfirmation = true
            } else {
                // Delete immediately (with snackbar undo)
                onDeleteContact(contactId)
            }
        }
    }

    // OPTIMIZATION: Memoize expensive calculations to prevent recalculation on every recomposition
    val filteredGroupedContacts = remember(
        state.favorites,
        state.groupedContacts,
        showFavoritesSection,
        state.showFavorites
    ) {
        if (showFavoritesSection && state.showFavorites && state.favorites.isNotEmpty()) {
            val favoriteIds = state.favorites.map { it.id }.toSet()
            state.groupedContacts.mapValues { (_, contacts) ->
                contacts.filterNot { it.id in favoriteIds }
            }.filterValues { it.isNotEmpty() }
        } else {
            state.groupedContacts
        }
    }

    val fastScrollerState = rememberFastScrollerState(filteredGroupedContacts)

    // OPTIMIZATION: Calculate favorites offset once
    val favoritesOffset = remember(state.favorites.size, showFavoritesSection, state.showFavorites) {
        if (showFavoritesSection && state.showFavorites && state.favorites.isNotEmpty()) {
            state.favorites.size + 2 // +1 for section header, +1 for divider spacer
        } else {
            0
        }
    }

    Box(modifier = Modifier.fillMaxSize()) {
        LazyColumn(
            state = listState,
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(bottom = 88.dp, end = 48.dp)
        ) {
            // Favorites section
            if (showFavoritesSection && state.showFavorites) {
                item {
                    SectionHeader(text = stringResource(R.string.favorites))
                }
                items(
                    items = state.favorites,
                    key = { contact -> "fav_${contact.id}" }
                ) { contact ->
                    SafeSwipeableContactListItem(
                        contact = contact,
                        onClick = { onContactClick(contact.id) },
                        onDelete = { handleDelete(contact.id) },
                        onFavoriteToggle = { onToggleFavorite(contact.id, !contact.isFavorite) },
                        showPhoneNumber = state.showPhoneNumbers,
                        startNameWithSurname = state.startNameWithSurname,
                        formatPhoneNumbers = state.formatPhoneNumbers,
                        isSelectionMode = state.isSelectionMode,
                        isSelected = contact.id in state.selectedContactIds,
                        onSelectionToggle = { onToggleSelection(contact.id) },
                        onLongClick = { onLongClick(contact.id) },
                        enableSwipeActions = !state.isSelectionMode
                    )
                }

                item {
                    Spacer(modifier = Modifier.height(8.dp))
                    HorizontalDivider()
                    Spacer(modifier = Modifier.height(8.dp))
                }
            }

            // All contacts grouped by first letter
            if (state.searchQuery.isBlank()) {
                filteredGroupedContacts.forEach { (letter, contacts) ->
                    item(key = "header_$letter") {
                        SectionHeader(text = letter.toString())
                    }
                    items(
                        items = contacts,
                        key = { contact -> "contact_${contact.id}" }
                    ) { contact ->
                        SafeSwipeableContactListItem(
                            contact = contact,
                            onClick = { onContactClick(contact.id) },
                            onDelete = { handleDelete(contact.id) },
                            onFavoriteToggle = { onToggleFavorite(contact.id, !contact.isFavorite) },
                            showPhoneNumber = state.showPhoneNumbers,
                            startNameWithSurname = state.startNameWithSurname,
                            formatPhoneNumbers = state.formatPhoneNumbers,
                            isSelectionMode = state.isSelectionMode,
                            isSelected = contact.id in state.selectedContactIds,
                            onSelectionToggle = { onToggleSelection(contact.id) },
                            onLongClick = { onLongClick(contact.id) },
                            enableSwipeActions = !state.isSelectionMode
                        )
                    }
                }
            } else {
                // Search results
                items(
                    items = state.contacts,
                    key = { contact -> "search_${contact.id}" }
                ) { contact ->
                    SafeSwipeableContactListItem(
                        contact = contact,
                        onClick = { onContactClick(contact.id) },
                        onDelete = { handleDelete(contact.id) },
                        onFavoriteToggle = { onToggleFavorite(contact.id, !contact.isFavorite) },
                        showPhoneNumber = state.showPhoneNumbers,
                        startNameWithSurname = state.startNameWithSurname,
                        formatPhoneNumbers = state.formatPhoneNumbers,
                        isSelectionMode = state.isSelectionMode,
                        isSelected = contact.id in state.selectedContactIds,
                        onSelectionToggle = { onToggleSelection(contact.id) },
                        onLongClick = { onLongClick(contact.id) },
                        enableSwipeActions = !state.isSelectionMode
                    )
                }
            }
        }

        // Fast scroller (only show when not searching and has grouped contacts)
        if (state.searchQuery.isBlank() && filteredGroupedContacts.isNotEmpty()) {
            FastScroller(
                listState = listState,
                sections = fastScrollerState.sectionKeys,
                onSectionSelected = { sectionIndex ->
                    scope.launch {
                        val targetIndex = fastScrollerState.getIndexForSection(sectionIndex)
                        listState.animateScrollToItem(targetIndex + favoritesOffset)
                    }
                },
                modifier = Modifier.align(Alignment.CenterEnd)
            )
        }
    }

    // Delete confirmation dialog
    if (showDeleteConfirmation && contactToDelete != null) {
        val contact = state.contacts.find { it.id == contactToDelete }
            ?: state.favorites.find { it.id == contactToDelete }

        DeleteConfirmationDialog(
            title = stringResource(R.string.contact_delete),
            message = stringResource(R.string.delete_contact_confirmation, contact?.displayName ?: "this contact"),
            onConfirm = {
                contactToDelete?.let { onDeleteContact(it) }
                contactToDelete = null
            },
            onDismiss = {
                showDeleteConfirmation = false
                contactToDelete = null
            }
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ContactListTopBar(
    searchQuery: String,
    onSearchQueryChange: (String) -> Unit,
    onMenuClick: () -> Unit,
    showMenu: Boolean,
    onDismissMenu: () -> Unit,
    onNavigateToGroups: () -> Unit,
    onNavigateToSettings: () -> Unit,
    onSortClick: () -> Unit,
    onFilterClick: () -> Unit
) {
    Column {
        TopAppBar(
            title = {
                Text(stringResource(R.string.nav_contacts))
            },
            actions = {
                IconButton(onClick = onSortClick) {
                    Icon(
                        imageVector = Icons.Default.Sort,
                        contentDescription = stringResource(R.string.sort_by)
                    )
                }
                IconButton(onClick = onFilterClick) {
                    Icon(
                        imageVector = Icons.Default.FilterList,
                        contentDescription = stringResource(R.string.action_filter)
                    )
                }
                IconButton(onClick = onMenuClick) {
                    Icon(
                        imageVector = Icons.Default.MoreVert,
                        contentDescription = stringResource(R.string.more_options)
                    )
                }
                DropdownMenu(
                    expanded = showMenu,
                    onDismissRequest = onDismissMenu
                ) {
                    DropdownMenuItem(
                        text = { Text(stringResource(R.string.nav_groups)) },
                        onClick = {
                            onDismissMenu()
                            onNavigateToGroups()
                        },
                        leadingIcon = {
                            Icon(Icons.Default.Group, contentDescription = stringResource(R.string.nav_groups))
                        }
                    )
                    DropdownMenuItem(
                        text = { Text(stringResource(R.string.nav_settings)) },
                        onClick = {
                            onDismissMenu()
                            onNavigateToSettings()
                        },
                        leadingIcon = {
                            Icon(Icons.Default.Settings, contentDescription = stringResource(R.string.nav_settings))
                        }
                    )
                }
            }
        )

        OutlinedTextField(
            value = searchQuery,
            onValueChange = onSearchQueryChange,
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 8.dp),
            placeholder = { Text(stringResource(R.string.search_contacts)) },
            leadingIcon = {
                Icon(Icons.Default.Search, contentDescription = stringResource(R.string.action_search))
            },
            trailingIcon = {
                if (searchQuery.isNotEmpty()) {
                    IconButton(onClick = { onSearchQueryChange("") }) {
                        Icon(Icons.Default.Clear, contentDescription = stringResource(R.string.action_clear_all))
                    }
                }
            },
            singleLine = true
        )
    }
}
