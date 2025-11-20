package com.contacts.android.contacts.presentation.screens.favorites
import androidx.compose.ui.res.stringResource
import com.contacts.android.contacts.R

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
    disableSwipeGestures: Boolean = false, // NEW: Disable swipes when in horizontal pager
    viewModel: ContactListViewModel = hiltViewModel()
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    var showMenu by remember { mutableStateOf(false) }
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()

    // Confirmation dialog state for delete
    var contactToDelete by remember { mutableStateOf<Long?>(null) }
    var showDeleteConfirmation by remember { mutableStateOf(false) }

    // Get user preferences for display settings (like Fossify)
    val context = LocalContext.current
    val userPreferences = remember { UserPreferences(context) }
    val showPhoneNumbers by userPreferences.showPhoneNumbers.collectAsState(initial = true)
    val startNameWithSurname by userPreferences.startNameWithSurname.collectAsState(initial = false)

    // Helper function to handle delete with confirmation dialog
    val handleDelete: (Long) -> Unit = remember {
        { contactId ->
            // Always show confirmation dialog for safer UX
            contactToDelete = contactId
            showDeleteConfirmation = true
        }
    }

    Scaffold(
        snackbarHost = { SnackbarHost(hostState = snackbarHostState) },
        contentWindowInsets = if (hideTopBar) WindowInsets(0, 0, 0, 0) else ScaffoldDefaults.contentWindowInsets,
        topBar = {
            if (!hideTopBar) {
                TopAppBar(
                    title = { Text(stringResource(R.string.favorites)) },
                    actions = {
                        IconButton(onClick = { showMenu = true }) {
                            Icon(Icons.Default.MoreVert, contentDescription = stringResource(R.string.more_options))
                        }

                        DropdownMenu(
                            expanded = showMenu,
                            onDismissRequest = { showMenu = false }
                        ) {
                            DropdownMenuItem(
                                text = { Text(stringResource(R.string.nav_settings)) },
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
                        title = stringResource(R.string.favorites_empty_title),
                        description = stringResource(R.string.favorites_empty_description)
                    )
                }
                else -> {
                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        contentPadding = PaddingValues()
                    ) {
                        items(
                            items = state.favorites,
                            key = { contact -> contact.id }
                        ) { contact ->
                            ContactListItem(
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
                                }
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
            title = stringResource(R.string.contact_delete),
            message = stringResource(R.string.delete_contact_confirmation, contact?.displayName ?: ""),
            onConfirm = {
                contactToDelete?.let { contactId ->
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
