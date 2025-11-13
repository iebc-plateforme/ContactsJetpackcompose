package com.contacts.android.contactsjetpackcompose.presentation.screens.contactlist

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.CheckCircleOutline
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.contacts.android.contactsjetpackcompose.data.preferences.UserPreferences
import com.contacts.android.contactsjetpackcompose.presentation.components.*
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

    // Get user preferences for display settings (like Fossify)
    val context = LocalContext.current
    val userPreferences = remember { UserPreferences(context) }
    val showPhoneNumbers by userPreferences.showPhoneNumbers.collectAsState(initial = true)
    val startNameWithSurname by userPreferences.startNameWithSurname.collectAsState(initial = false)
    val formatPhoneNumbers by userPreferences.formatPhoneNumbers.collectAsState(initial = true)

    Scaffold(
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
                        onDeleteSelected = {
                            viewModel.onEvent(ContactListEvent.DeleteSelectedContacts)
                        },
                        isAllSelected = state.isAllSelected
                    )
                } else {
                    ContactListTopBar(
                        searchQuery = state.searchQuery,
                        onSearchQueryChange = { query ->
                            viewModel.onEvent(ContactListEvent.SearchQueryChanged(query))
                        },
                        onMenuClick = { showMenu = true },
                        showMenu = showMenu,
                        onDismissMenu = { showMenu = false },
                        onNavigateToGroups = onNavigateToGroups,
                        onNavigateToSettings = onNavigateToSettings
                    )
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
                        contentDescription = "Add contact"
                    )
                }
            }
        },
        floatingActionButtonPosition = FabPosition.Center
    ) { paddingValues ->
        Box(
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
                        title = "No contacts yet",
                        description = "Tap the + button to add your first contact"
                    )
                }
                !state.hasContacts && state.searchQuery.isNotBlank() -> {
                    EmptyState(
                        icon = Icons.Default.SearchOff,
                        title = "No results found",
                        description = "Try a different search term"
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
                        showFavoritesSection = showFavoritesSection,
                        showPhoneNumbers = showPhoneNumbers,
                        startNameWithSurname = startNameWithSurname,
                        formatPhoneNumbers = formatPhoneNumbers
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
    showFavoritesSection: Boolean = true,
    showPhoneNumbers: Boolean = true,
    startNameWithSurname: Boolean = false,
    formatPhoneNumbers: Boolean = true
) {
    val listState = rememberLazyListState()
    val scope = rememberCoroutineScope()

    // Filter out favorites from grouped contacts to avoid duplicates
    val favoriteIds = if (showFavoritesSection && state.showFavorites) {
        state.favorites.map { it.id }.toSet()
    } else {
        emptySet()
    }

    val filteredGroupedContacts = if (favoriteIds.isNotEmpty()) {
        state.groupedContacts.mapValues { (_, contacts) ->
            contacts.filterNot { it.id in favoriteIds }
        }.filterValues { it.isNotEmpty() }
    } else {
        state.groupedContacts
    }

    val fastScrollerState = rememberFastScrollerState(filteredGroupedContacts)

    Box(modifier = Modifier.fillMaxSize()) {
        LazyColumn(
            state = listState,
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(bottom = 88.dp, end = 48.dp)
        ) {
            // Favorites section
            if (showFavoritesSection && state.showFavorites) {
                item {
                    SectionHeader(text = "Favorites")
                }
                items(
                    items = state.favorites,
                    key = { contact -> "fav_${contact.id}" }
                ) { contact ->
                    ContactListItem(
                        contact = contact,
                        onClick = { onContactClick(contact.id) },
                        showPhoneNumber = showPhoneNumbers,
                        startNameWithSurname = startNameWithSurname,
                        formatPhoneNumbers = formatPhoneNumbers,
                        isSelectionMode = state.isSelectionMode,
                        isSelected = contact.id in state.selectedContactIds,
                        onSelectionToggle = { onToggleSelection(contact.id) },
                        onLongClick = { onLongClick(contact.id) }
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
                        ContactListItem(
                            contact = contact,
                            onClick = { onContactClick(contact.id) },
                            showPhoneNumber = showPhoneNumbers,
                            startNameWithSurname = startNameWithSurname,
                            formatPhoneNumbers = formatPhoneNumbers,
                            isSelectionMode = state.isSelectionMode,
                            isSelected = contact.id in state.selectedContactIds,
                            onSelectionToggle = { onToggleSelection(contact.id) },
                            onLongClick = { onLongClick(contact.id) }
                        )
                    }
                }
            } else {
                // Search results
                items(
                    items = state.contacts,
                    key = { contact -> "search_${contact.id}" }
                ) { contact ->
                    ContactListItem(
                        contact = contact,
                        onClick = { onContactClick(contact.id) },
                        showPhoneNumber = showPhoneNumbers,
                        startNameWithSurname = startNameWithSurname,
                        formatPhoneNumbers = formatPhoneNumbers,
                        isSelectionMode = state.isSelectionMode,
                        isSelected = contact.id in state.selectedContactIds,
                        onSelectionToggle = { onToggleSelection(contact.id) },
                        onLongClick = { onLongClick(contact.id) }
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
                        // Add offset for favorites section if shown
                        val offset = if (showFavoritesSection && state.showFavorites) {
                            state.favorites.size + 2 // +2 for header and divider
                        } else {
                            0
                        }
                        listState.animateScrollToItem(targetIndex + offset)
                    }
                },
                modifier = Modifier.align(Alignment.CenterEnd)
            )
        }
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
    onNavigateToSettings: () -> Unit
) {
    Column {
        TopAppBar(
            title = {
                Text("Contacts")
            },
            actions = {
                IconButton(onClick = onMenuClick) {
                    Icon(
                        imageVector = Icons.Default.MoreVert,
                        contentDescription = "More options"
                    )
                }

                DropdownMenu(
                    expanded = showMenu,
                    onDismissRequest = onDismissMenu
                ) {
                    DropdownMenuItem(
                        text = { Text("Groups") },
                        onClick = {
                            onDismissMenu()
                            onNavigateToGroups()
                        },
                        leadingIcon = {
                            Icon(Icons.Default.Group, contentDescription = null)
                        }
                    )
                    DropdownMenuItem(
                        text = { Text("Settings") },
                        onClick = {
                            onDismissMenu()
                            onNavigateToSettings()
                        },
                        leadingIcon = {
                            Icon(Icons.Default.Settings, contentDescription = null)
                        }
                    )
                }
            }
        )

        // Search bar
        OutlinedTextField(
            value = searchQuery,
            onValueChange = onSearchQueryChange,
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 8.dp),
            placeholder = { Text("Search contacts") },
            leadingIcon = {
                Icon(Icons.Default.Search, contentDescription = null)
            },
            trailingIcon = {
                if (searchQuery.isNotEmpty()) {
                    IconButton(onClick = { onSearchQueryChange("") }) {
                        Icon(Icons.Default.Clear, contentDescription = "Clear search")
                    }
                }
            },
            singleLine = true,
            colors = OutlinedTextFieldDefaults.colors()
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun SelectionModeTopBar(
    selectedCount: Int,
    onExitSelectionMode: () -> Unit,
    onSelectAll: () -> Unit,
    onDeleteSelected: () -> Unit,
    isAllSelected: Boolean
) {
    TopAppBar(
        title = {
            Text("$selectedCount selected")
        },
        navigationIcon = {
            IconButton(onClick = onExitSelectionMode) {
                Icon(
                    imageVector = Icons.Default.Close,
                    contentDescription = "Exit selection mode"
                )
            }
        },
        actions = {
            // Select all / Deselect all
            IconButton(onClick = onSelectAll) {
                Icon(
                    imageVector = if (isAllSelected) Icons.Default.CheckCircle else Icons.Default.CheckCircleOutline,
                    contentDescription = if (isAllSelected) "Deselect all" else "Select all"
                )
            }

            // Delete selected
            IconButton(
                onClick = onDeleteSelected,
                enabled = selectedCount > 0
            ) {
                Icon(
                    imageVector = Icons.Default.Delete,
                    contentDescription = "Delete selected contacts"
                )
            }
        },
        colors = TopAppBarDefaults.topAppBarColors(
            containerColor = MaterialTheme.colorScheme.secondaryContainer,
            titleContentColor = MaterialTheme.colorScheme.onSecondaryContainer,
            navigationIconContentColor = MaterialTheme.colorScheme.onSecondaryContainer,
            actionIconContentColor = MaterialTheme.colorScheme.onSecondaryContainer
        )
    )
}
