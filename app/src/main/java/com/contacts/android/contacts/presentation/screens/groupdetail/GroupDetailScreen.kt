package com.contacts.android.contacts.presentation.screens.groupdetail

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.contacts.android.contacts.domain.model.Contact
import com.contacts.android.contacts.presentation.components.ContactListItem
import com.contacts.android.contacts.presentation.components.EmptyState
import com.contacts.android.contacts.presentation.components.LoadingIndicator
import com.contacts.android.contacts.presentation.components.SwipeableGroupContactItem

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GroupDetailScreen(
    onBackClick: () -> Unit,
    onContactClick: (Long) -> Unit,
    modifier: Modifier = Modifier,
    viewModel: GroupDetailViewModel = hiltViewModel()
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    var showMenu by remember { mutableStateOf(false) }

    // Handle navigation back on delete
    LaunchedEffect(Unit) {
        viewModel.navigateBack.collect {
            onBackClick()
        }
    }

    // Show error snackbar
    state.error?.let { error ->
        LaunchedEffect(error) {
            // Error will be shown in a snackbar
            viewModel.onEvent(GroupDetailEvent.ClearError)
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = state.group?.name ?: "",
                        fontWeight = FontWeight.Bold
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(
                            imageVector = Icons.Default.ArrowBack,
                            contentDescription = "Back"
                        )
                    }
                },
                actions = {
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
                            text = { Text("Edit group name") },
                            onClick = {
                                showMenu = false
                                viewModel.onEvent(GroupDetailEvent.ShowEditGroupDialog)
                            },
                            leadingIcon = {
                                Icon(Icons.Default.Edit, contentDescription = null)
                            }
                        )
                        DropdownMenuItem(
                            text = { Text("Delete group") },
                            onClick = {
                                showMenu = false
                                viewModel.onEvent(GroupDetailEvent.ShowDeleteGroupDialog)
                            },
                            leadingIcon = {
                                Icon(Icons.Default.Delete, contentDescription = null)
                            }
                        )
                    }
                }
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = {
                    viewModel.onEvent(GroupDetailEvent.ShowAddContactsDialog)
                },
                containerColor = MaterialTheme.colorScheme.primaryContainer,
                contentColor = MaterialTheme.colorScheme.onPrimaryContainer
            ) {
                Icon(
                    imageVector = Icons.Default.PersonAdd,
                    contentDescription = "Add contacts"
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
                state.isLoading -> {
                    LoadingIndicator()
                }
                state.contacts.isEmpty() -> {
                    EmptyState(
                        icon = Icons.Default.PersonOff,
                        title = "No contacts in this group",
                        description = "Tap the + button to add contacts"
                    )
                }
                else -> {
                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        contentPadding = PaddingValues(bottom = 88.dp)
                    ) {
                        items(
                            items = state.contacts,
                            key = { contact -> contact.id }
                        ) { contact ->
                            SwipeableGroupContactItem(
                                contact = contact,
                                onClick = { onContactClick(contact.id) },
                                onRemoveFromGroup = {
                                    viewModel.onEvent(
                                        GroupDetailEvent.RemoveContactFromGroup(contact.id)
                                    )
                                },
                                showPhoneNumber = true,
                                startNameWithSurname = false,
                                formatPhoneNumbers = true
                            )
                        }
                    }
                }
            }
        }
    }

    // Add Contacts Dialog
    if (state.showAddContactsDialog) {
        AddContactsDialog(
            availableContacts = state.availableContacts,
            selectedContactIds = state.selectedContactIds,
            onToggleContact = { contactId ->
                viewModel.toggleContactSelection(contactId)
            },
            onConfirm = {
                viewModel.onEvent(
                    GroupDetailEvent.AddContactsToGroup(
                        state.selectedContactIds.toList()
                    )
                )
            },
            onDismiss = {
                viewModel.onEvent(GroupDetailEvent.HideAddContactsDialog)
            }
        )
    }

    // Edit Group Name Dialog
    if (state.showEditGroupDialog) {
        EditGroupNameDialog(
            groupName = state.groupNameInput,
            onGroupNameChange = { viewModel.updateGroupNameInput(it) },
            onConfirm = {
                viewModel.onEvent(GroupDetailEvent.UpdateGroupName(state.groupNameInput))
            },
            onDismiss = {
                viewModel.onEvent(GroupDetailEvent.HideEditGroupDialog)
            }
        )
    }

    // Delete Group Confirmation Dialog
    if (state.showDeleteGroupDialog) {
        AlertDialog(
            onDismissRequest = {
                viewModel.onEvent(GroupDetailEvent.HideDeleteGroupDialog)
            },
            icon = {
                Icon(
                    Icons.Default.Warning,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.error
                )
            },
            title = { Text("Delete Group") },
            text = {
                Text("Are you sure you want to delete \"${state.group?.name}\"? This will not delete the contacts, only the group.")
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        viewModel.onEvent(GroupDetailEvent.DeleteGroup)
                    }
                ) {
                    Text("Delete", color = MaterialTheme.colorScheme.error)
                }
            },
            dismissButton = {
                TextButton(
                    onClick = {
                        viewModel.onEvent(GroupDetailEvent.HideDeleteGroupDialog)
                    }
                ) {
                    Text("Cancel")
                }
            }
        )
    }
}

@Composable
private fun ContactListItemWithRemove(
    contact: Contact,
    onClick: () -> Unit,
    onRemove: () -> Unit,
    modifier: Modifier = Modifier
) {
    var showRemoveMenu by remember { mutableStateOf(false) }

    Row(
        modifier = modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        ContactListItem(
            contact = contact,
            onClick = onClick,
            modifier = Modifier.weight(1f)
        )

        IconButton(onClick = { showRemoveMenu = true }) {
            Icon(
                imageVector = Icons.Default.MoreVert,
                contentDescription = "More options"
            )
        }

        DropdownMenu(
            expanded = showRemoveMenu,
            onDismissRequest = { showRemoveMenu = false }
        ) {
            DropdownMenuItem(
                text = { Text("Remove from group") },
                onClick = {
                    showRemoveMenu = false
                    onRemove()
                },
                leadingIcon = {
                    Icon(Icons.Default.PersonRemove, contentDescription = null)
                }
            )
        }
    }
}

@Composable
private fun AddContactsDialog(
    availableContacts: List<Contact>,
    selectedContactIds: Set<Long>,
    onToggleContact: (Long) -> Unit,
    onConfirm: () -> Unit,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        icon = { Icon(Icons.Default.PersonAdd, contentDescription = null) },
        title = { Text("Add contacts to group") },
        text = {
            if (availableContacts.isEmpty()) {
                Text(
                    text = "All contacts are already in this group",
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
            if (availableContacts.isNotEmpty()) {
                TextButton(
                    onClick = onConfirm,
                    enabled = selectedContactIds.isNotEmpty()
                ) {
                    Text("Add")
                }
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(if (availableContacts.isEmpty()) "Close" else "Cancel")
            }
        }
    )
}

@Composable
private fun EditGroupNameDialog(
    groupName: String,
    onGroupNameChange: (String) -> Unit,
    onConfirm: () -> Unit,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        icon = { Icon(Icons.Default.Edit, contentDescription = null) },
        title = { Text("Edit group name") },
        text = {
            OutlinedTextField(
                value = groupName,
                onValueChange = onGroupNameChange,
                label = { Text("Group name") },
                singleLine = true,
                modifier = Modifier.fillMaxWidth()
            )
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