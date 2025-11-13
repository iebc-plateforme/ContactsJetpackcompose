package com.contacts.android.contactsjetpackcompose.presentation.screens.favorites

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
import com.contacts.android.contactsjetpackcompose.data.preferences.UserPreferences
import com.contacts.android.contactsjetpackcompose.presentation.components.*
import com.contacts.android.contactsjetpackcompose.presentation.screens.contactlist.ContactListEvent
import com.contacts.android.contactsjetpackcompose.presentation.screens.contactlist.ContactListViewModel

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
    var showAddToFavoritesDialog by remember { mutableStateOf(false) }

    // Get user preferences for display settings (like Fossify)
    val context = LocalContext.current
    val userPreferences = remember { UserPreferences(context) }
    val showPhoneNumbers by userPreferences.showPhoneNumbers.collectAsState(initial = true)
    val startNameWithSurname by userPreferences.startNameWithSurname.collectAsState(initial = false)

    Scaffold(
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
        },
        floatingActionButton = {
            if (!hideFab) {
                FloatingActionButton(
                    onClick = { showAddToFavoritesDialog = true },
                    containerColor = MaterialTheme.colorScheme.primaryContainer,
                    contentColor = MaterialTheme.colorScheme.onPrimaryContainer
                ) {
                    Icon(
                        imageVector = Icons.Default.Star,
                        contentDescription = "Add to favorites"
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
                            ContactListItem(
                                contact = contact,
                                onClick = { onContactClick(contact.id) },
                                showFavoriteButton = true,
                                onFavoriteClick = {
                                    viewModel.onEvent(
                                        ContactListEvent.ToggleFavorite(
                                            contact.id,
                                            !contact.isFavorite
                                        )
                                    )
                                },
                                showPhoneNumber = showPhoneNumbers,
                                startNameWithSurname = startNameWithSurname
                            )
                        }
                    }
                }
            }
        }
    }

    // Add to Favorites Dialog
    if (showAddToFavoritesDialog) {
        AddToFavoritesDialog(
            allContacts = state.contacts,
            favoriteContacts = state.favorites,
            onAddToFavorites = { contactIds ->
                contactIds.forEach { contactId ->
                    viewModel.onEvent(ContactListEvent.ToggleFavorite(contactId, true))
                }
                showAddToFavoritesDialog = false
            },
            onDismiss = { showAddToFavoritesDialog = false }
        )
    }
}

@Composable
private fun AddToFavoritesDialog(
    allContacts: List<com.contacts.android.contactsjetpackcompose.domain.model.Contact>,
    favoriteContacts: List<com.contacts.android.contactsjetpackcompose.domain.model.Contact>,
    onAddToFavorites: (List<Long>) -> Unit,
    onDismiss: () -> Unit
) {
    val favoriteIds = favoriteContacts.map { it.id }.toSet()
    val availableContacts = allContacts.filter { it.id !in favoriteIds }
    var selectedContactIds by remember { mutableStateOf(setOf<Long>()) }

    AlertDialog(
        onDismissRequest = onDismiss,
        icon = { Icon(Icons.Default.Star, contentDescription = null) },
        title = { Text("Add to favorites") },
        text = {
            if (availableContacts.isEmpty()) {
                Text(
                    text = "All contacts are already in favorites",
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
                                .padding(vertical = 4.dp),
                            verticalAlignment = androidx.compose.ui.Alignment.CenterVertically
                        ) {
                            Checkbox(
                                checked = contact.id in selectedContactIds,
                                onCheckedChange = { checked ->
                                    selectedContactIds = if (checked) {
                                        selectedContactIds + contact.id
                                    } else {
                                        selectedContactIds - contact.id
                                    }
                                }
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
            TextButton(
                onClick = { onAddToFavorites(selectedContactIds.toList()) },
                enabled = selectedContactIds.isNotEmpty()
            ) {
                Text("Add (${selectedContactIds.size})")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}
