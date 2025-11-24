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
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.contacts.android.contacts.R
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
                            contentDescription = stringResource(R.string.action_back)
                        )
                    }
                },
                actions = {
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
                            text = { Text(stringResource(R.string.groups_edit)) },
                            onClick = {
                                showMenu = false
                                viewModel.onEvent(GroupDetailEvent.ShowEditGroupDialog)
                            },
                            leadingIcon = {
                                Icon(Icons.Default.Edit, contentDescription = null)
                            }
                        )
                        DropdownMenuItem(
                            text = { Text(stringResource(R.string.groups_delete)) },
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
                    contentDescription = stringResource(R.string.contact_add)
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
                        title = stringResource(R.string.empty_contacts),
                        description = stringResource(R.string.empty_contacts_description)
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
            title = { Text(stringResource(R.string.groups_delete)) },
            text = {
                Text(stringResource(R.string.groups_delete_confirmation, state.group?.name ?: ""))
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        viewModel.onEvent(GroupDetailEvent.DeleteGroup)
                    }
                ) {
                    Text(stringResource(R.string.action_delete), color = MaterialTheme.colorScheme.error)
                }
            },
            dismissButton = {
                TextButton(
                    onClick = {
                        viewModel.onEvent(GroupDetailEvent.HideDeleteGroupDialog)
                    }
                ) {
                    Text(stringResource(R.string.action_cancel))
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
                contentDescription = stringResource(R.string.more_options)
            )
        }

        DropdownMenu(
            expanded = showRemoveMenu,
            onDismissRequest = { showRemoveMenu = false }
        ) {
            DropdownMenuItem(
                text = { Text(stringResource(R.string.remove)) },
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
    var searchQuery by remember { mutableStateOf("") }
    // Filter contacts based on search query (name or phone)
    val filteredContacts = remember(availableContacts, searchQuery) {
        if (searchQuery.isBlank()) {
            availableContacts
        } else {
            availableContacts.filter { contact ->
                contact.displayName.contains(searchQuery, ignoreCase = true) ||
                        (contact.primaryPhone?.number?.contains(searchQuery, ignoreCase = true) ?: false)
            }
        }
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        icon = { Icon(Icons.Default.PersonAdd, contentDescription = null) },
        title = { Text(stringResource(R.string.add_to_group)) },
        text = {
            Column {
                // Search field
                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = { searchQuery = it },
                    label = { Text(stringResource(R.string.search_hint)) },
                    leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
                    singleLine = true,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 8.dp)
                )

                if (filteredContacts.isEmpty()) {
                    Text(
                        text = stringResource(R.string.all_contacts_in_group),
                        style = MaterialTheme.typography.bodyMedium,
                        modifier = Modifier.padding(16.dp)
                    )
                } else {
                    LazyColumn(
                        modifier = Modifier
                            .fillMaxWidth()
                            .heightIn(max = 300.dp) // limit height for scrollable area
                    ) {
                        items(filteredContacts) { contact ->
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable { onToggleContact(contact.id) }
                                    .padding(vertical = 4.dp, horizontal = 8.dp),
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
            }
        },
        confirmButton = {
            TextButton(
                onClick = onConfirm,
                enabled = selectedContactIds.isNotEmpty()
            ) {
                Text(stringResource(R.string.action_add))
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
private fun EditGroupNameDialog(
    groupName: String,
    onGroupNameChange: (String) -> Unit,
    onConfirm: () -> Unit,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        icon = { Icon(Icons.Default.Edit, contentDescription = null) },
        title = { Text(stringResource(R.string.groups_edit)) },
        text = {
            OutlinedTextField(
                value = groupName,
                onValueChange = onGroupNameChange,
                label = { Text(stringResource(R.string.groups_name)) },
                singleLine = true,
                modifier = Modifier.fillMaxWidth()
            )
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