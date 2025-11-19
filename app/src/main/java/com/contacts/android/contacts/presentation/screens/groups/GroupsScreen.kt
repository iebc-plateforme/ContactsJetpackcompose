package com.contacts.android.contacts.presentation.screens.groups

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.contacts.android.contacts.domain.model.Group
import com.contacts.android.contacts.presentation.components.EmptyState
import com.contacts.android.contacts.presentation.components.LoadingIndicator

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
    var showMenu by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            if (!hideTopBar) {
                TopAppBar(
                    title = { Text("Groups") },
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
                        contentDescription = "Add group"
                    )
                }
            }
        }
    ) { paddingValues ->
        Box(
            modifier = modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            when {
                state.isLoading && state.filteredGroups.isEmpty() -> {
                    LoadingIndicator()
                }
                state.filteredGroups.isEmpty() -> {
                    EmptyState(
                        icon = Icons.Default.Group,
                        title = if (state.searchQuery.isNotEmpty()) "No groups found" else "No groups yet",
                        description = if (state.searchQuery.isNotEmpty()) "Try a different search term" else "Create groups to organize your contacts"
                    )
                }
                else -> {
                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        contentPadding = PaddingValues(bottom = 88.dp)
                    ) {
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
                        }
                    }
                }
            }
        }
    }

    // Add Group Dialog
    if (state.showAddGroupDialog || state.showEditGroupDialog) {
        GroupInputDialog(
            title = if (state.showEditGroupDialog) "Edit Group" else "New Group",
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

    // Contact Selection Dialog
    if (state.showContactSelectionDialog) {
        ContactSelectionDialog(
            availableContacts = state.availableContacts,
            selectedContactIds = state.selectedContactIds,
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
            title = { Text("Delete Group") },
            text = { Text("Are you sure you want to delete ${state.selectedGroup?.name}?") },
            confirmButton = {
                TextButton(
                    onClick = {
                        viewModel.onEvent(GroupsEvent.DeleteGroup)
                    }
                ) {
                    Text("Delete", color = MaterialTheme.colorScheme.error)
                }
            },
            dismissButton = {
                TextButton(
                    onClick = {
                        viewModel.onEvent(GroupsEvent.HideDeleteDialog)
                    }
                ) {
                    Text("Cancel")
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
        // Colored group icon (like Fossify Contacts)
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
                text = "${group.contactCount} contact${if (group.contactCount != 1) "s" else ""}",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }

        // Only show menu for non-system groups
        if (!group.isSystemGroup) {
            IconButton(onClick = { showMenu = true }) {
                Icon(
                    imageVector = Icons.Default.MoreVert,
                    contentDescription = "More options"
                )
            }

            DropdownMenu(
                expanded = showMenu,
                onDismissRequest = { showMenu = false }
            ) {
                DropdownMenuItem(
                    text = { Text("Edit") },
                    onClick = {
                        showMenu = false
                        onEdit()
                    },
                    leadingIcon = {
                        Icon(Icons.Default.Edit, contentDescription = null)
                    }
                )
                DropdownMenuItem(
                    text = { Text("Delete") },
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
                    label = { Text("Group name") },
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
                                "Selected: $selectedContactsCount contact${if (selectedContactsCount != 1) "s" else ""}"
                            } else {
                                "Select contacts (optional)"
                            }
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
                Text("Save")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}

@Composable
private fun ContactSelectionDialog(
    availableContacts: List<com.contacts.android.contacts.domain.model.Contact>,
    selectedContactIds: Set<Long>,
    onToggleContact: (Long) -> Unit,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        icon = { Icon(Icons.Default.PersonAdd, contentDescription = null) },
        title = { Text("Select contacts") },
        text = {
            if (availableContacts.isEmpty()) {
                Text(
                    text = "No contacts available",
                    style = MaterialTheme.typography.bodyMedium,
                    modifier = Modifier.padding(16.dp)
                )
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxWidth()
                ) {
                    items(availableContacts) { contact ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { onToggleContact(contact.id) }
                                .padding(vertical = 4.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Checkbox(
                                checked = contact.id in selectedContactIds,
                                onCheckedChange = { onToggleContact(contact.id) }
                            )
                            Spacer(modifier = Modifier.width(8.dp))
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
        },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text("Done")
            }
        }
    )
}

/**
 * Colored group icon like Fossify Contacts
 * Generates a unique color from the group name hash
 */
@Composable
private fun GroupIcon(
    groupName: String,
    modifier: Modifier = Modifier
) {
    // Generate color from hash of group name (like Fossify)
    val color = remember(groupName) {
        val hash = groupName.hashCode()
        // Generate a vibrant color using the hash
        val hue = (hash and 0xFF).toFloat() / 255f * 360f
        Color.hsv(hue, 0.6f, 0.9f)
    }

    // Get first letter of group name
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
