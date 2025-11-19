package com.contacts.android.contacts.presentation.screens.favorites

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.contacts.android.contacts.data.preferences.UserPreferences
import com.contacts.android.contacts.presentation.components.*
import com.contacts.android.contacts.presentation.screens.contactlist.ContactListEvent
import com.contacts.android.contacts.presentation.screens.contactlist.ContactListViewModel
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FavoritesScreen(
    onContactClick: (Long) -> Unit,
    onAddContact: () -> Unit,
    onNavigateToSettings: () -> Unit,
    modifier: Modifier = Modifier,
    hideTopBar: Boolean = false,
    hideFab: Boolean = false,
    viewModel: ContactListViewModel = hiltViewModel()
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    var showMenu by remember { mutableStateOf(false) }
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()

    // Confirmation dialog state for swipe-to-delete
    var contactToDelete by remember { mutableStateOf<Long?>(null) }
    var showDeleteConfirmation by remember { mutableStateOf(false) }

    // Get user preferences for display settings (like Fossify)
    val context = LocalContext.current
    val userPreferences = remember { UserPreferences(context) }
    val showPhoneNumbers by userPreferences.showPhoneNumbers.collectAsState(initial = true)
    val startNameWithSurname by userPreferences.startNameWithSurname.collectAsState(initial = false)

    // Helper function to handle delete with optional confirmation
    val handleDelete: (Long) -> Unit = remember(state.swipeDeleteConfirmation) {
        { contactId ->
            if (state.swipeDeleteConfirmation) {
                // Show confirmation dialog
                contactToDelete = contactId
                showDeleteConfirmation = true
            } else {
                // Delete immediately (with snackbar undo)
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
            }
        }
    }

    Scaffold(
        snackbarHost = { SnackbarHost(hostState = snackbarHostState) },
        topBar = {
            if (!hideTopBar) {
                TopAppBar(
                    title = { Text("Favorites") },
                    actions = {
                        IconButton(onClick = { showMenu = true }) {
                            Icon(Icons.Default.MoreVert, contentDescription = "More options")
                        }

                        DropdownMenu(
                            expanded = showMenu,
                            onDismissRequest = { showMenu = false }
                        ) {
                            DropdownMenuItem(
                                text = { Text("Settings") },
                                onClick = {
                                    showMenu = false
                                    onNavigateToSettings()
                                },
                                leadingIcon = {
                                    Icon(Icons.Default.Settings, contentDescription = null)
                                }
                            )
                        }
                    }
                )
            }
        }
    ) { paddingValues ->
        Box(
            modifier = modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            when {
                state.isLoading && state.favorites.isEmpty() -> {
                    LoadingIndicator()
                }
                state.favorites.isEmpty() -> {
                    EmptyState(
                        icon = Icons.Default.StarBorder,
                        title = "No favorite contacts",
                        description = "Mark contacts as favorites to see them here"
                    )
                }
                else -> {
                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        contentPadding = PaddingValues(bottom = 88.dp)
                    ) {
                        items(
                            items = state.favorites,
                            key = { contact -> contact.id }
                        ) { contact ->
                            SafeSwipeableContactListItem(
                                contact = contact,
                                onClick = { onContactClick(contact.id) },
                                onDelete = { handleDelete(contact.id) },
                                onFavoriteToggle = {
                                    viewModel.onEvent(
                                        ContactListEvent.ToggleFavorite(
                                            contact.id,
                                            !contact.isFavorite
                                        )
                                    )
                                },
                                showPhoneNumber = state.showPhoneNumbers,
                                startNameWithSurname = state.startNameWithSurname,
                                formatPhoneNumbers = state.formatPhoneNumbers,
                                isSelectionMode = state.isSelectionMode,
                                isSelected = contact.id in state.selectedContactIds,
                                onSelectionToggle = {
                                    viewModel.onEvent(ContactListEvent.ToggleContactSelection(contact.id))
                                },
                                onLongClick = {
                                    viewModel.onEvent(ContactListEvent.EnterSelectionMode)
                                    viewModel.onEvent(ContactListEvent.ToggleContactSelection(contact.id))
                                },
                                enableSwipeActions = !state.isSelectionMode
                            )
                        }
                    }
                }
            }
        }
    }

    // Delete confirmation dialog
    if (showDeleteConfirmation && contactToDelete != null) {
        val contact = state.favorites.find { it.id == contactToDelete }

        DeleteConfirmationDialog(
            title = "Delete Contact",
            message = "Are you sure you want to delete ${contact?.displayName ?: "this contact"}? This action cannot be undone.",
            onConfirm = {
                contactToDelete?.let { contactId ->
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
                }
                contactToDelete = null
            },
            onDismiss = {
                showDeleteConfirmation = false
                contactToDelete = null
            }
        )
    }
}
