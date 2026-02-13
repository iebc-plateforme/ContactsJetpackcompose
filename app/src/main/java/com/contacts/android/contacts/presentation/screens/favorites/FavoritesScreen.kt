package com.contacts.android.contacts.presentation.screens.favorites

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectDragGesturesAfterLongPress
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.StarBorder
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.zIndex
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.contacts.android.contacts.R
import com.contacts.android.contacts.data.preferences.FavoritesViewType
import com.contacts.android.contacts.data.preferences.UserPreferences
import com.contacts.android.contacts.domain.model.Contact
import com.contacts.android.contacts.presentation.components.*
import com.contacts.android.contacts.presentation.screens.contactlist.ContactListEvent
import com.contacts.android.contacts.presentation.screens.contactlist.ContactListViewModel
import kotlinx.coroutines.launch
import kotlin.math.roundToInt

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FavoritesScreen(
    onContactClick: (Long) -> Unit,
    onAddContact: () -> Unit,
    onNavigateToSettings: () -> Unit,
    modifier: Modifier = Modifier,
    hideTopBar: Boolean = false,
    hideFab: Boolean = false,
    disableSwipeGestures: Boolean = false,
    onAddToFavorites: () -> Unit = {},
    viewModel: ContactListViewModel = hiltViewModel()
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    var showMenu by remember { mutableStateOf(false) }
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()
    val context = LocalContext.current

    // Confirmation dialog state for delete
    var contactToDelete by remember { mutableStateOf<Long?>(null) }
    var showDeleteConfirmation by remember { mutableStateOf(false) }

    // Helper function to handle delete
    val handleDelete: (Long) -> Unit = remember {
        { contactId ->
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
                                leadingIcon = { Icon(Icons.Default.Settings, contentDescription = null) }
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
                // FIX: Show loading during initial sync to prevent flash of empty state
                state.isInitialSyncInProgress || (state.isLoading && state.favorites.isEmpty()) -> {
                    ShimmerContactList()
                }
                state.favorites.isEmpty() -> {
                    EmptyState(
                        icon = Icons.Default.StarBorder,
                        title = stringResource(R.string.favorites_empty_title),
                        description = stringResource(R.string.favorites_empty_description),
                        actionLabel = stringResource(R.string.favorites_add),
                        onAction = onAddToFavorites
                    )
                }
                else -> {
                    if (state.favoritesViewType == FavoritesViewType.GRID) {
                        FavoritesGrid(
                            contacts = state.favorites,
                            onContactClick = onContactClick
                        )
                    } else {
                        FavoritesList(
                            contacts = state.favorites,
                            onContactClick = onContactClick,
                            onDelete = handleDelete,
                            onFavoriteToggle = { id, isFav ->
                                viewModel.onEvent(ContactListEvent.ToggleFavorite(id, isFav))
                            },
                            state = state,
                            viewModel = viewModel
                        )
                    }
                }
            }
        }
    }

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

@Composable
@OptIn(ExperimentalFoundationApi::class)
fun FavoritesList(
    contacts: List<Contact>,
    onContactClick: (Long) -> Unit,
    onDelete: (Long) -> Unit,
    onFavoriteToggle: (Long, Boolean) -> Unit,
    state: com.contacts.android.contacts.presentation.screens.contactlist.ContactListState,
    viewModel: ContactListViewModel
) {
    val scope = rememberCoroutineScope()
    val listState = rememberLazyListState()
    var isDragging by remember { mutableStateOf(false) }
    var draggedIndex by remember { mutableStateOf<Int?>(null) }
    var dragOffset by remember { mutableStateOf(0f) }

    val displayContacts = remember { mutableStateListOf<Contact>() }
    LaunchedEffect(contacts, isDragging) {
        if (!isDragging) {
            displayContacts.clear()
            displayContacts.addAll(contacts)
        }
    }

    val canDrag = !state.isSelectionMode && state.searchQuery.isBlank()

    val sectionIndexMap = remember(displayContacts, state.startNameWithSurname) {
        val map = LinkedHashMap<Char, Int>()
        displayContacts.forEachIndexed { index, contact ->
            val keyName = if (state.startNameWithSurname && contact.lastName.isNotBlank()) {
                contact.lastName
            } else {
                contact.firstName
            }.ifBlank { contact.displayName }
            val key = keyName.firstOrNull()?.uppercaseChar() ?: '#'
            if (!map.containsKey(key)) {
                map[key] = index
            }
        }
        map
    }
    val sectionKeys = remember(sectionIndexMap) { sectionIndexMap.keys.toList() }

    fun moveItem(from: Int, to: Int) {
        if (from == to) return
        val item = displayContacts.removeAt(from)
        displayContacts.add(to, item)
    }

    fun commitOrder() {
        viewModel.onEvent(ContactListEvent.UpdateFavoritesOrder(displayContacts.map { it.id }))
    }

    Box(modifier = Modifier.fillMaxSize()) {
        LazyColumn(
            state = listState,
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(end = 48.dp)
        ) {
            itemsIndexed(
                items = displayContacts,
                key = { _, contact -> contact.id }
            ) { index, contact ->
                val isDragged = draggedIndex == index
                val dragHandleModifier = if (canDrag) {
                    Modifier.pointerInput(displayContacts, canDrag) {
                        detectDragGesturesAfterLongPress(
                            onDragStart = {
                                isDragging = true
                                draggedIndex = index
                                dragOffset = 0f
                            },
                            onDragEnd = {
                                isDragging = false
                                draggedIndex = null
                                dragOffset = 0f
                                commitOrder()
                            },
                            onDragCancel = {
                                isDragging = false
                                draggedIndex = null
                                dragOffset = 0f
                            },
                            onDrag = { change, dragAmount ->
                                change.consume()
                                dragOffset += dragAmount.y

                                val currentIndex = draggedIndex ?: return@detectDragGesturesAfterLongPress
                                val itemInfo = listState.layoutInfo.visibleItemsInfo
                                    .firstOrNull { it.index == currentIndex }
                                val itemSize = itemInfo?.size ?: return@detectDragGesturesAfterLongPress

                                if (dragOffset > itemSize && currentIndex < displayContacts.lastIndex) {
                                    moveItem(currentIndex, currentIndex + 1)
                                    draggedIndex = currentIndex + 1
                                    dragOffset -= itemSize
                                } else if (dragOffset < -itemSize && currentIndex > 0) {
                                    moveItem(currentIndex, currentIndex - 1)
                                    draggedIndex = currentIndex - 1
                                    dragOffset += itemSize
                                }
                            }
                        )
                    }
                } else {
                    Modifier
                }

                ContactListItem(
                    contact = contact,
                    onClick = { onContactClick(contact.id) },
                    onDelete = { onDelete(contact.id) },
                    onFavoriteToggle = { onFavoriteToggle(contact.id, !contact.isFavorite) },
                    showPhoneNumber = state.showPhoneNumbers,
                    startNameWithSurname = state.startNameWithSurname,
                    formatPhoneNumbers = state.formatPhoneNumbers,
                    avatarSize = AvatarSize.Large,
                    isSelectionMode = state.isSelectionMode,
                    isSelected = contact.id in state.selectedContactIds,
                    onSelectionToggle = {
                        viewModel.onEvent(ContactListEvent.ToggleContactSelection(contact.id))
                    },
                    onLongClick = {
                        viewModel.onEvent(ContactListEvent.EnterSelectionMode)
                        viewModel.onEvent(ContactListEvent.ToggleContactSelection(contact.id))
                    },
                    enableDrag = canDrag,
                    dragModifier = dragHandleModifier,
                    modifier = Modifier
                        .zIndex(if (isDragged) 1f else 0f)
                        .offset { IntOffset(0, if (isDragged) dragOffset.roundToInt() else 0) }
                )
            }
        }

        if (canDrag && sectionKeys.isNotEmpty()) {
            FastScroller(
                listState = listState,
                sections = sectionKeys,
                onSectionSelected = { sectionIndex ->
                    val key = sectionKeys.getOrNull(sectionIndex) ?: return@FastScroller
                    val targetIndex = sectionIndexMap[key] ?: 0
                    scope.launch {
                        listState.animateScrollToItem(targetIndex)
                    }
                },
                modifier = Modifier.align(Alignment.CenterEnd)
            )
        }
    }
}

@Composable
fun FavoritesGrid(
    contacts: List<Contact>,
    onContactClick: (Long) -> Unit
) {
    LazyVerticalGrid(
        columns = GridCells.Adaptive(minSize = 100.dp),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
        horizontalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        items(contacts, key = { it.id }) { contact ->
            ContactGridItem(
                contact = contact,
                onClick = { onContactClick(contact.id) }
            )
        }
    }
}

@Composable
fun ContactGridItem(
    contact: Contact,
    onClick: () -> Unit
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier
            .clickable(onClick = onClick)
            .padding(8.dp)
    ) {
        ContactAvatar(
            name = contact.displayName,
            photoUri = contact.photoUri,
            size = AvatarSize.ExtraLarge
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = contact.displayName,
            style = MaterialTheme.typography.bodyMedium,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            textAlign = TextAlign.Center
        )
    }
}
