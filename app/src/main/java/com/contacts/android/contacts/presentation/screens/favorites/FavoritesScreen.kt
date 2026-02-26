package com.contacts.android.contacts.presentation.screens.favorites

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.gestures.detectDragGesturesAfterLongPress
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
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

    var contactToDelete by remember { mutableStateOf<Long?>(null) }
    var showDeleteConfirmation by remember { mutableStateOf(false) }

    val handleDelete: (Long) -> Unit = remember {
        { contactId ->
            contactToDelete = contactId
            showDeleteConfirmation = true
        }
    }

    val handleFavoriteToggle: (Long, Boolean) -> Unit = remember(viewModel) {
        { id, isFav -> viewModel.onEvent(ContactListEvent.ToggleFavorite(id, isFav)) }
    }

    Scaffold(
        snackbarHost = { SnackbarHost(hostState = snackbarHostState) },
        contentWindowInsets = if (hideTopBar) WindowInsets(0, 0, 0, 0) else ScaffoldDefaults.contentWindowInsets,
        topBar = {
            if (!hideTopBar) {
                TopAppBar(
                    title = { Text(stringResource(R.string.favorites)) },
                    actions = {
                        // View toggle button
                        if (state.favorites.isNotEmpty()) {
                            IconButton(
                                onClick = {
                                    val newType = if (state.favoritesViewType == FavoritesViewType.LIST)
                                        FavoritesViewType.GRID else FavoritesViewType.LIST
                                    viewModel.onEvent(ContactListEvent.ToggleFavoritesViewType(newType))
                                }
                            ) {
                                Icon(
                                    imageVector = if (state.favoritesViewType == FavoritesViewType.LIST)
                                        Icons.Default.GridView else Icons.Default.ViewList,
                                    contentDescription = if (state.favoritesViewType == FavoritesViewType.LIST)
                                        "Grid view" else "List view"
                                )
                            }
                        }
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
                            onContactClick = onContactClick,
                            onDelete = handleDelete,
                            onFavoriteToggle = handleFavoriteToggle
                        )
                    } else {
                        FavoritesList(
                            contacts = state.favorites,
                            onContactClick = onContactClick,
                            onDelete = handleDelete,
                            onFavoriteToggle = handleFavoriteToggle,
                            onUpdateOrder = { newOrder ->
                                viewModel.onEvent(ContactListEvent.UpdateFavoritesOrder(newOrder))
                            },
                            onEnterSelectionMode = {
                                viewModel.onEvent(ContactListEvent.EnterSelectionMode)
                            },
                            onToggleContactSelection = { id ->
                                viewModel.onEvent(ContactListEvent.ToggleContactSelection(id))
                            },
                            showPhoneNumbers = state.showPhoneNumbers,
                            startNameWithSurname = state.startNameWithSurname,
                            formatPhoneNumbers = state.formatPhoneNumbers,
                            isSelectionMode = state.isSelectionMode,
                            selectedContactIds = state.selectedContactIds,
                            searchQuery = state.searchQuery
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
private fun FavoritesList(
    contacts: List<Contact>,
    onContactClick: (Long) -> Unit,
    onDelete: (Long) -> Unit,
    onFavoriteToggle: (Long, Boolean) -> Unit,
    onUpdateOrder: (List<Long>) -> Unit,
    onEnterSelectionMode: () -> Unit,
    onToggleContactSelection: (Long) -> Unit,
    showPhoneNumbers: Boolean,
    startNameWithSurname: Boolean,
    formatPhoneNumbers: Boolean,
    isSelectionMode: Boolean,
    selectedContactIds: Set<Long>,
    searchQuery: String
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

    val canDrag = !isSelectionMode && searchQuery.isBlank()

    val sectionIndexMap = remember(displayContacts, startNameWithSurname) {
        val map = LinkedHashMap<Char, Int>()
        displayContacts.forEachIndexed { index, contact ->
            val keyName = if (startNameWithSurname && contact.lastName.isNotBlank()) {
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
        onUpdateOrder(displayContacts.map { it.id })
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
                    showPhoneNumber = showPhoneNumbers,
                    startNameWithSurname = startNameWithSurname,
                    formatPhoneNumbers = formatPhoneNumbers,
                    avatarSize = AvatarSize.Large,
                    isSelectionMode = isSelectionMode,
                    isSelected = contact.id in selectedContactIds,
                    onSelectionToggle = { onToggleContactSelection(contact.id) },
                    onLongClick = {
                        onEnterSelectionMode()
                        onToggleContactSelection(contact.id)
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

@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun FavoritesGrid(
    contacts: List<Contact>,
    onContactClick: (Long) -> Unit,
    onDelete: (Long) -> Unit,
    onFavoriteToggle: (Long, Boolean) -> Unit
) {
    LazyVerticalGrid(
        columns = GridCells.Adaptive(minSize = 100.dp),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        items(contacts, key = { it.id }) { contact ->
            ContactGridItem(
                contact = contact,
                onClick = { onContactClick(contact.id) },
                onRemoveFromFavorites = { onFavoriteToggle(contact.id, false) },
                onDelete = { onDelete(contact.id) }
            )
        }
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun ContactGridItem(
    contact: Contact,
    onClick: () -> Unit,
    onRemoveFromFavorites: () -> Unit,
    onDelete: () -> Unit
) {
    var showMenu by remember { mutableStateOf(false) }

    Box {
        Card(
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surfaceContainerLow
            ),
            shape = RoundedCornerShape(16.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier
                    .combinedClickable(
                        onClick = onClick,
                        onLongClick = { showMenu = true }
                    )
                    .padding(12.dp)
                    .fillMaxWidth()
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
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        }

        DropdownMenu(
            expanded = showMenu,
            onDismissRequest = { showMenu = false }
        ) {
            DropdownMenuItem(
                text = { Text(stringResource(R.string.remove_from_favorites)) },
                onClick = {
                    showMenu = false
                    onRemoveFromFavorites()
                },
                leadingIcon = { Icon(Icons.Default.StarBorder, contentDescription = null) }
            )
            DropdownMenuItem(
                text = {
                    Text(
                        stringResource(R.string.contact_delete),
                        color = MaterialTheme.colorScheme.error
                    )
                },
                onClick = {
                    showMenu = false
                    onDelete()
                },
                leadingIcon = {
                    Icon(
                        Icons.Default.Delete,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.error
                    )
                }
            )
        }
    }
}
