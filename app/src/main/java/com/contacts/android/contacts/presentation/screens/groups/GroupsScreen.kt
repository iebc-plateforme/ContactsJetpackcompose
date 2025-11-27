package com.contacts.android.contacts.presentation.screens.groups

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.contacts.android.contacts.R
import com.contacts.android.contacts.domain.model.Contact
import com.contacts.android.contacts.domain.model.Group
import com.contacts.android.contacts.presentation.components.EmptyState
import com.contacts.android.contacts.presentation.components.LoadingIndicator
import com.google.accompanist.swiperefresh.SwipeRefresh
import com.google.accompanist.swiperefresh.rememberSwipeRefreshState

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GroupsScreen(
    onGroupClick: (Long) -> Unit,
    onAddGroup: () -> Unit,
    onNavigateToSettings: () -> Unit,
    modifier: Modifier = Modifier,
    hideTopBar: Boolean = false,
    hideFab: Boolean = false,
    viewModel: GroupsViewModel = hiltViewModel()
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }
    var showMenu by remember { mutableStateOf(false) }

    // Pull-to-refresh state
    val swipeRefreshState = rememberSwipeRefreshState(
        isRefreshing = state.isLoading
    )

    // Gestion des messages (Erreurs et Succès)
    LaunchedEffect(state.error) {
        state.error?.let { error ->
            snackbarHostState.showSnackbar(
                message = error,
                withDismissAction = true
            )
            viewModel.onEvent(GroupsEvent.ClearMessage)
        }
    }

    LaunchedEffect(state.successMessage) {
        state.successMessage?.let { message ->
            snackbarHostState.showSnackbar(message)
            viewModel.onEvent(GroupsEvent.ClearMessage)
        }
    }

    Scaffold(
        contentWindowInsets = if (hideTopBar) WindowInsets(0, 0, 0, 0) else ScaffoldDefaults.contentWindowInsets,
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            if (!hideTopBar) {
                TopAppBar(
                    title = {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Text(stringResource(R.string.groups_title))
                            if (state.groups.isNotEmpty()) {
                                Surface(
                                    shape = CircleShape,
                                    color = MaterialTheme.colorScheme.primaryContainer,
                                    modifier = Modifier.size(24.dp)
                                ) {
                                    Box(contentAlignment = Alignment.Center) {
                                        Text(
                                            text = state.groups.size.toString(),
                                            style = MaterialTheme.typography.labelSmall,
                                            color = MaterialTheme.colorScheme.onPrimaryContainer,
                                            fontWeight = FontWeight.Bold
                                        )
                                    }
                                }
                            }
                        }
                    },
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
        },
        floatingActionButton = {
            if (!hideFab) {
                FloatingActionButton(
                    onClick = {
                        viewModel.onEvent(GroupsEvent.ShowAddGroupDialog)
                    },
                    containerColor = MaterialTheme.colorScheme.primaryContainer,
                    contentColor = MaterialTheme.colorScheme.onPrimaryContainer
                ) {
                    Icon(
                        imageVector = Icons.Default.Add,
                        contentDescription = stringResource(R.string.groups_add)
                    )
                }
            }
        }
    ) { paddingValues ->
        SwipeRefresh(
            state = swipeRefreshState,
            onRefresh = { viewModel.onEvent(GroupsEvent.RefreshGroups) },
            modifier = modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            Box(
                modifier = Modifier.fillMaxSize()
            ) {
                when {
                    state.isLoading && state.groups.isEmpty() -> {
                        LoadingIndicator()
                    }
                state.filteredGroups.isEmpty() -> {
                    EmptyState(
                        icon = Icons.Default.Group,
                        title = if (state.searchQuery.isNotEmpty()) stringResource(R.string.empty_groups_search) else stringResource(R.string.groups_empty_title),
                        description = if (state.searchQuery.isNotEmpty()) stringResource(R.string.try_different_search_term) else stringResource(R.string.groups_empty_description)
                    )
                }
                else -> {
                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        contentPadding = PaddingValues(bottom = 88.dp)
                    ) {
                        // Recherche de groupes
                        if (state.groups.isNotEmpty()) {
                            item {
                                OutlinedTextField(
                                    value = state.searchQuery,
                                    onValueChange = { viewModel.onEvent(GroupsEvent.SearchQueryChanged(it)) },
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(16.dp),
                                    placeholder = { Text(stringResource(R.string.search_hint)) },
                                    leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
                                    singleLine = true,
                                    shape = RoundedCornerShape(12.dp)
                                )
                            }
                        }

                        items(
                            items = state.filteredGroups,
                            key = { group -> group.id }
                        ) { group ->
                            GroupListItem(
                                group = group,
                                onClick = { onGroupClick(group.id) },
                                onEdit = {
                                    viewModel.onEvent(GroupsEvent.ShowEditGroupDialog(group))
                                },
                                onDelete = {
                                    viewModel.onEvent(GroupsEvent.ShowDeleteDialog(group))
                                }
                            )
                            HorizontalDivider(
                                modifier = Modifier.padding(horizontal = 16.dp),
                                color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)
                            )
                        }
                    }
                }
            }
        }
        }
    }

    // Add/Edit Group Dialog
    if (state.showAddGroupDialog || state.showEditGroupDialog) {
        GroupInputDialog(
            title = if (state.showEditGroupDialog) stringResource(R.string.groups_edit) else stringResource(R.string.groups_new),
            groupName = state.groupNameInput,
            selectedContactsCount = state.selectedContactIds.size,
            showContactSelection = !state.showEditGroupDialog,
            onGroupNameChange = {
                viewModel.onEvent(GroupsEvent.GroupNameChanged(it))
            },
            onSelectContacts = {
                viewModel.onEvent(GroupsEvent.ShowContactSelectionDialog)
            },
            onConfirm = {
                viewModel.onEvent(GroupsEvent.SaveGroup)
            },
            onDismiss = {
                viewModel.onEvent(GroupsEvent.ClearContactSelection)
                if (state.showEditGroupDialog) {
                    viewModel.onEvent(GroupsEvent.HideEditGroupDialog)
                } else {
                    viewModel.onEvent(GroupsEvent.HideAddGroupDialog)
                }
            }
        )
    }

    // Contact Selection Dialog with Search
    if (state.showContactSelectionDialog) {
        ContactSelectionDialog(
            availableContacts = state.filteredAvailableContacts,
            searchQuery = state.contactSearchQuery,
            selectedContactIds = state.selectedContactIds,
            onSearchQueryChange = {
                viewModel.onEvent(GroupsEvent.ContactSearchQueryChanged(it))
            },
            onToggleContact = { contactId ->
                viewModel.onEvent(GroupsEvent.ToggleContactSelection(contactId))
            },
            onDismiss = {
                viewModel.onEvent(GroupsEvent.HideContactSelectionDialog)
            }
        )
    }

    // Delete Group Dialog
    if (state.showDeleteDialog) {
        AlertDialog(
            onDismissRequest = {
                viewModel.onEvent(GroupsEvent.HideDeleteDialog)
            },
            icon = {
                Icon(Icons.Default.Warning, contentDescription = null, tint = MaterialTheme.colorScheme.error)
            },
            title = { Text(stringResource(R.string.groups_delete)) },
            text = { Text(stringResource(R.string.groups_delete_confirmation, state.selectedGroup?.name ?: "")) },
            confirmButton = {
                TextButton(
                    onClick = {
                        viewModel.onEvent(GroupsEvent.DeleteGroup)
                    },
                    colors = ButtonDefaults.textButtonColors(contentColor = MaterialTheme.colorScheme.error)
                ) {
                    Text(stringResource(R.string.action_delete))
                }
            },
            dismissButton = {
                TextButton(
                    onClick = {
                        viewModel.onEvent(GroupsEvent.HideDeleteDialog)
                    }
                ) {
                    Text(stringResource(R.string.action_cancel))
                }
            }
        )
    }
}

@Composable
private fun GroupListItem(
    group: Group,
    onClick: () -> Unit,
    onEdit: () -> Unit,
    onDelete: () -> Unit,
    modifier: Modifier = Modifier
) {
    var showMenu by remember { mutableStateOf(false) }

    Row(
        modifier = modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Colored group icon
        GroupIcon(groupName = group.name)

        Spacer(modifier = Modifier.width(16.dp))

        Column(
            modifier = Modifier.weight(1f)
        ) {
            Text(
                text = group.name,
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurface
            )
            Text(
                text = stringResource(R.string.contacts_count, group.contactCount, if (group.contactCount != 1) "s" else ""),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }

        // Only show menu for non-system groups
        if (!group.isSystemGroup) {
            IconButton(onClick = { showMenu = true }) {
                Icon(
                    imageVector = Icons.Default.MoreVert,
                    contentDescription = stringResource(R.string.more_options)
                )
            }

            DropdownMenu(
                expanded = showMenu,
                onDismissRequest = { showMenu = false }
            ) {
                DropdownMenuItem(
                    text = { Text(stringResource(R.string.action_edit)) },
                    onClick = {
                        showMenu = false
                        onEdit()
                    },
                    leadingIcon = {
                        Icon(Icons.Default.Edit, contentDescription = null)
                    }
                )
                DropdownMenuItem(
                    text = { Text(stringResource(R.string.action_delete)) },
                    onClick = {
                        showMenu = false
                        onDelete()
                    },
                    leadingIcon = {
                        Icon(Icons.Default.Delete, contentDescription = null)
                    }
                )
            }
        }
    }
}

@Composable
private fun GroupInputDialog(
    title: String,
    groupName: String,
    selectedContactsCount: Int,
    showContactSelection: Boolean,
    onGroupNameChange: (String) -> Unit,
    onSelectContacts: () -> Unit,
    onConfirm: () -> Unit,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(title) },
        text = {
            Column(modifier = Modifier.fillMaxWidth()) {
                OutlinedTextField(
                    value = groupName,
                    onValueChange = onGroupNameChange,
                    label = { Text(stringResource(R.string.groups_name)) },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                if (showContactSelection) {
                    Spacer(modifier = Modifier.height(16.dp))
                    OutlinedButton(
                        onClick = onSelectContacts,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Icon(Icons.Default.PersonAdd, contentDescription = null)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = if (selectedContactsCount > 0) {
                                stringResource(R.string.groups_selected) + ": " + stringResource(R.string.contacts_count, selectedContactsCount, if (selectedContactsCount != 1) "s" else "")
                            } else {
                                stringResource(R.string.groups_select_contacts)
                            }
                        )
                    }

                    // Hint: can create empty group
                    if (selectedContactsCount == 0) {
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = stringResource(R.string.groups_empty_hint),
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        },
        confirmButton = {
            TextButton(
                onClick = onConfirm,
                enabled = groupName.isNotBlank()
            ) {
                Text(stringResource(R.string.action_save))
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(stringResource(R.string.action_cancel))
            }
        }
    )
}

@Composable
private fun ContactSelectionDialog(
    availableContacts: List<Contact>,
    searchQuery: String,
    selectedContactIds: Set<Long>,
    onSearchQueryChange: (String) -> Unit,
    onToggleContact: (Long) -> Unit,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(stringResource(R.string.groups_select_contacts)) },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(max = 400.dp)
            ) {
                // Barre de recherche dans le dialogue
                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = onSearchQueryChange,
                    placeholder = { Text(stringResource(R.string.search_hint)) },
                    leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 8.dp),
                    singleLine = true,
                    shape = RoundedCornerShape(8.dp)
                )

                if (availableContacts.isEmpty()) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = stringResource(R.string.empty_no_contacts_available),
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                } else {
                    LazyColumn(
                        modifier = Modifier.weight(1f)
                    ) {
                        items(
                            items = availableContacts,
                            key = { it.id }
                        ) { contact ->
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable { onToggleContact(contact.id) }
                                    .padding(vertical = 6.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Checkbox(
                                    checked = contact.id in selectedContactIds,
                                    onCheckedChange = { onToggleContact(contact.id) }
                                )
                                Spacer(modifier = Modifier.width(12.dp))
                                Column {
                                    Text(
                                        text = contact.displayName,
                                        style = MaterialTheme.typography.bodyLarge
                                    )
                                    contact.primaryPhone?.let { phone ->
                                        Text(
                                            text = phone.number,
                                            style = MaterialTheme.typography.bodySmall,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text(stringResource(R.string.action_done))
            }
        }
    )
}

/**
 * Colored group icon
 */
@Composable
private fun GroupIcon(
    groupName: String,
    modifier: Modifier = Modifier
) {
    val color = remember(groupName) {
        val hash = groupName.hashCode()
        val hue = (hash and 0xFF).toFloat() / 255f * 360f
        Color.hsv(hue, 0.6f, 0.9f)
    }

    val initial = remember(groupName) {
        groupName.firstOrNull()?.uppercaseChar()?.toString() ?: "G"
    }

    Box(
        modifier = modifier
            .size(40.dp)
            .clip(CircleShape)
            .background(color),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = initial,
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            color = Color.White
        )
    }
}