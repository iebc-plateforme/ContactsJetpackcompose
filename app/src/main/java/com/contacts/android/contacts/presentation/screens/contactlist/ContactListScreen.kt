package com.contacts.android.contacts.presentation.screens.contactlist

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.contacts.android.contacts.R
import com.contacts.android.contacts.presentation.components.*
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
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
    emptyActionLabel: String? = null,
    onEmptyAction: (() -> Unit)? = null,
    viewModel: ContactListViewModel = hiltViewModel()
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    var showMenu by remember { mutableStateOf(false) }
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()
    val context = LocalContext.current

    // Note: Initial sync is handled in ViewModel.init() - no need for LaunchedEffect here

    // OPTIMIZATION: User preferences now come from ViewModel state to prevent recompositions
    val swipeRefreshState = rememberSwipeRefreshState(
        isRefreshing = state.isLoading
    )

    // Import/Export Launchers
    val importLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenDocument(),
        onResult = { uri ->
            uri?.let { viewModel.onEvent(ContactListEvent.ImportContacts(it)) }
        }
    )

    val exportLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.CreateDocument("text/vcard"),
        onResult = { uri ->
            uri?.let { viewModel.onEvent(ContactListEvent.ExportAllContacts(it)) }
        }
    )

    Scaffold(
        snackbarHost = { SnackbarHost(hostState = snackbarHostState) },
        contentWindowInsets = if (hideTopBar) WindowInsets(0, 0, 0, 0) else ScaffoldDefaults.contentWindowInsets,
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
                        onFilterClick = { showFilterDialog = true },
                        onShareClick = {
                            val sendIntent = android.content.Intent().apply {
                                action = android.content.Intent.ACTION_SEND
                                putExtra(android.content.Intent.EXTRA_TEXT, "Check out this Contacts app!")
                                type = "text/plain"
                            }
                            val shareIntent = android.content.Intent.createChooser(sendIntent, null)
                            context.startActivity(shareIntent)
                        },
                        onImportClick = {
                            importLauncher.launch(arrayOf("text/vcard", "text/x-vcard"))
                        },
                        onExportClick = {
                            exportLauncher.launch(viewModel.getExportFilename())
                        }
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
                // FIX: Show shimmer during initial sync OR when loading with no contacts
                state.isInitialSyncInProgress || ((state.isLoading || !state.hasLoadedContacts) && !state.hasContacts) -> {
                    ShimmerContactList()
                }
                !state.hasContacts && state.searchQuery.isBlank() -> {
                    EmptyState(
                        icon = Icons.Default.ContactPage,
                        title = stringResource(R.string.empty_contacts),
                        description = stringResource(R.string.empty_contacts_description),
                        actionLabel = emptyActionLabel,
                        onAction = onEmptyAction
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
                                    message = context.getString(R.string.contact_deleted),
                                    actionLabel = context.getString(R.string.undo),
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

@OptIn(ExperimentalFoundationApi::class)
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

    // Confirmation dialog state for delete
    var contactToDelete by remember { mutableStateOf<Long?>(null) }
    var showDeleteConfirmation by remember { mutableStateOf(false) }

    // Recently added bottom sheet state
    var showRecentlyAddedSheet by remember { mutableStateOf(false) }

    // Helper function to handle delete with confirmation dialog
    val handleDelete: (Long) -> Unit = remember {
        { contactId ->
            // Always show confirmation dialog for safer UX
            contactToDelete = contactId
            showDeleteConfirmation = true
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
            contentPadding = PaddingValues(end = 48.dp)
        ) {
            // Recently Added Section (Smart Section)
            if (state.searchQuery.isBlank() && state.recentlyAdded.isNotEmpty() && !showFavoritesSection) {
                item {
                    SectionHeader(
                        text = stringResource(R.string.sort_date_added),
                        trailing = {
                            TextButton(onClick = { showRecentlyAddedSheet = true }) {
                                Text(stringResource(R.string.view_details), style = MaterialTheme.typography.labelSmall)
                            }
                        }
                    )
                }
                item {
                    androidx.compose.foundation.lazy.LazyRow(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 8.dp),
                        contentPadding = PaddingValues(horizontal = 16.dp),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        items(state.recentlyAdded) { contact ->
                            RecentContactCard(
                                contact = contact,
                                onClick = { onContactClick(contact.id) }
                            )
                        }
                    }
                }
                item { Spacer(modifier = Modifier.height(16.dp)) }
            }

            // Favorites section
            if (showFavoritesSection && state.showFavorites) {
                item {
                    SectionHeader(text = stringResource(R.string.favorites))
                }
                items(
                    items = state.favorites,
                    key = { contact -> "fav_${contact.id}" }
                ) { contact ->
                    ContactListItem(
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
                    stickyHeader(key = "header_$letter") {
                        SectionHeader(text = letter.toString())
                    }
                    items(
                        items = contacts,
                        key = { contact -> "contact_${contact.id}" }
                    ) { contact ->
                        ContactListItem(
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
                        onDelete = { handleDelete(contact.id) },
                        onFavoriteToggle = { onToggleFavorite(contact.id, !contact.isFavorite) },
                        showPhoneNumber = state.showPhoneNumbers,
                        startNameWithSurname = state.startNameWithSurname,
                        formatPhoneNumbers = state.formatPhoneNumbers,
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

    // Recently added bottom sheet
    if (showRecentlyAddedSheet) {
        RecentlyAddedBottomSheet(
            contacts = state.allRecentlyAdded,
            onContactClick = { contactId ->
                showRecentlyAddedSheet = false
                onContactClick(contactId)
            },
            onDismiss = { showRecentlyAddedSheet = false }
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
    onFilterClick: () -> Unit,
    onShareClick: () -> Unit,
    onImportClick: () -> Unit,
    onExportClick: () -> Unit
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
                    DropdownMenuItem(
                        text = { Text(stringResource(R.string.action_share_app)) },
                        onClick = {
                            onDismissMenu()
                            onShareClick()
                        },
                        leadingIcon = {
                            Icon(Icons.Default.Share, contentDescription = stringResource(R.string.action_share_app))
                        }
                    )
                    HorizontalDivider()
                    DropdownMenuItem(
                        text = { Text(stringResource(R.string.import_contacts)) },
                        onClick = {
                            onDismissMenu()
                            onImportClick()
                        },
                        leadingIcon = {
                            Icon(Icons.Default.Upload, contentDescription = stringResource(R.string.import_contacts))
                        }
                    )
                    DropdownMenuItem(
                        text = { Text(stringResource(R.string.export_contacts)) },
                        onClick = {
                            onDismissMenu()
                            onExportClick()
                        },
                        leadingIcon = {
                            Icon(Icons.Default.Download, contentDescription = stringResource(R.string.export_contacts))
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

/**
 * Card for recently added contacts shown in the Smart Section
 */
@Composable
private fun RecentContactCard(
    contact: com.contacts.android.contacts.domain.model.Contact,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        onClick = onClick,
        modifier = modifier.width(100.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceContainerHigh
        ),
        shape = RoundedCornerShape(16.dp)
    ) {
        Column(
            modifier = Modifier
                .padding(12.dp)
                .fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            ContactAvatar(
                name = contact.displayName,
                photoUri = contact.photoUri,
                size = AvatarSize.Medium
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = contact.firstName.ifBlank { contact.displayName },
                style = MaterialTheme.typography.labelMedium,
                maxLines = 1,
                overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis,
                textAlign = androidx.compose.ui.text.style.TextAlign.Center
            )
        }
    }
}

/**
 * Bottom sheet showing all recently added contacts with details
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun RecentlyAddedBottomSheet(
    contacts: List<com.contacts.android.contacts.domain.model.Contact>,
    onContactClick: (Long) -> Unit,
    onDismiss: () -> Unit
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 24.dp)
        ) {
            Text(
                text = stringResource(R.string.recently_added_title),
                style = MaterialTheme.typography.titleMedium,
                fontWeight = androidx.compose.ui.text.font.FontWeight.Bold,
                modifier = Modifier.padding(horizontal = 24.dp, vertical = 8.dp)
            )

            if (contacts.isEmpty()) {
                Text(
                    text = stringResource(R.string.no_recently_added),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(horizontal = 24.dp, vertical = 16.dp)
                )
            } else {
                androidx.compose.foundation.lazy.LazyColumn(
                    modifier = Modifier.heightIn(max = 400.dp)
                ) {
                    items(contacts) { contact ->
                        RecentlyAddedItem(
                            contact = contact,
                            onClick = { onContactClick(contact.id) }
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun RecentlyAddedItem(
    contact: com.contacts.android.contacts.domain.model.Contact,
    onClick: () -> Unit
) {
    val dateText = remember(contact.createdAt) {
        formatRelativeDate(contact.createdAt)
    }

    ListItem(
        headlineContent = {
            Text(
                text = contact.displayName,
                fontWeight = androidx.compose.ui.text.font.FontWeight.Medium
            )
        },
        supportingContent = {
            val info = buildString {
                if (contact.phoneNumbers.isNotEmpty()) {
                    append(contact.phoneNumbers.first().number)
                } else if (contact.emails.isNotEmpty()) {
                    append(contact.emails.first().email)
                }
                if (isNotEmpty()) append(" · ")
                append(dateText)
            }
            Text(text = info)
        },
        leadingContent = {
            ContactAvatar(
                name = contact.displayName,
                photoUri = contact.photoUri,
                size = AvatarSize.Small
            )
        },
        trailingContent = {
            Icon(
                imageVector = Icons.Default.ChevronRight,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onSurfaceVariant
            )
        },
        modifier = Modifier.clickable(onClick = onClick)
    )
}

private fun formatRelativeDate(timestamp: Long): String {
    val now = System.currentTimeMillis()
    val diff = now - timestamp
    val minutes = diff / (1000 * 60)
    val hours = minutes / 60
    val days = hours / 24

    return when {
        minutes < 1 -> "Just now"
        minutes < 60 -> "${minutes}m ago"
        hours < 24 -> "${hours}h ago"
        days < 7 -> "${days}d ago"
        days < 30 -> "${days / 7}w ago"
        else -> {
            val sdf = java.text.SimpleDateFormat("MMM d", java.util.Locale.getDefault())
            sdf.format(java.util.Date(timestamp))
        }
    }
}
