package com.contacts.android.contacts.presentation.screens.main
import androidx.compose.ui.res.stringResource
import com.contacts.android.contacts.R

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Sort
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.contacts.android.contacts.presentation.components.FilterDialog
import com.contacts.android.contacts.presentation.components.SortDialog
import com.contacts.android.contacts.presentation.screens.contactlist.*
import com.contacts.android.contacts.presentation.screens.favorites.FavoritesScreen
import com.contacts.android.contacts.presentation.screens.groups.GroupsEvent
import com.contacts.android.contacts.presentation.screens.groups.GroupsScreen
import com.contacts.android.contacts.presentation.screens.groups.GroupsViewModel
import kotlinx.coroutines.launch

/**
 * Simplified MainScreen following Fossify Contacts architecture
 * - Simple top bar with menu
 * - Bottom navigation tabs
 * - No complex animations or counters
 * - Filter and Sort in menu
 * - Centralized FAB system in main activity
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainScreen(
    onContactClick: (Long) -> Unit,
    onAddContact: () -> Unit,
    onNavigateToSettings: () -> Unit,
    onGroupClick: (Long) -> Unit,
    onLaunchDialer: () -> Unit = {}, // Launch system dialer
    defaultTab: com.contacts.android.contacts.data.preferences.DefaultTab =
        com.contacts.android.contacts.data.preferences.DefaultTab.CONTACTS,
    contactsViewModel: ContactListViewModel = hiltViewModel(),
    groupsViewModel: GroupsViewModel = hiltViewModel()
) {
    val initialPage = when (defaultTab) {
        com.contacts.android.contacts.data.preferences.DefaultTab.CONTACTS -> 0
        com.contacts.android.contacts.data.preferences.DefaultTab.FAVORITES -> 1
        com.contacts.android.contacts.data.preferences.DefaultTab.GROUPS -> 2
    }

    val pagerState = rememberPagerState(
        initialPage = initialPage,
        pageCount = { 3 }
    )
    val scope = rememberCoroutineScope()
    val contactsState by contactsViewModel.state.collectAsStateWithLifecycle()
    val haptic = LocalHapticFeedback.current
    val context = androidx.compose.ui.platform.LocalContext.current

    var showMenu by remember { mutableStateOf(false) }
    var showFilterDialog by remember { mutableStateOf(false) }
    var showSortDialog by remember { mutableStateOf(false) }
    var searchQuery by remember { mutableStateOf("") }
    var isSearchActive by remember { mutableStateOf(false) }
    var showAddToFavoritesDialog by remember { mutableStateOf(false) }

    // OPTIMIZED: Debounce search to reduce excessive queries and prevent searching during page transitions
    LaunchedEffect(searchQuery, pagerState.currentPage, pagerState.isScrollInProgress) {
        // Don't search while swiping between pages
        if (pagerState.isScrollInProgress) return@LaunchedEffect

        // Debounce search queries to reduce load
        kotlinx.coroutines.delay(300)

        when (pagerState.currentPage) {
            0, 1 -> contactsViewModel.onEvent(ContactListEvent.SearchQueryChanged(searchQuery))
            2 -> groupsViewModel.onEvent(GroupsEvent.SearchQueryChanged(searchQuery))
        }
    }

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        containerColor = MaterialTheme.colorScheme.surfaceContainerLowest,
        contentWindowInsets = WindowInsets(0, 0, 0, 0),
        floatingActionButtonPosition = FabPosition.End,
        topBar = {
            TopAppBar(
                title = {
                    if (isSearchActive) {
                        // OPTIMIZED: Use extracted SearchTextField to prevent full-screen recomposition
                        SearchTextField(
                            searchQuery = searchQuery,
                            onSearchQueryChange = { searchQuery = it },
                            currentPage = pagerState.currentPage
                        )
                    } else {
                        Text(
                            text = when (pagerState.currentPage) {
                                0 -> "Contacts"
                                1 -> "Favorites"
                                else -> "Groups"
                            },
                            fontWeight = FontWeight.Bold
                        )
                    }
                },
                navigationIcon = {
                    if (isSearchActive) {
                        IconButton(onClick = {
                            haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                            isSearchActive = false
                            searchQuery = ""
                        }) {
                            Icon(
                                imageVector = Icons.Default.ArrowBack,
                                contentDescription = "Close search"
                            )
                        }
                    }
                },
                actions = {
                    if (isSearchActive) {
                        // Clear search
                        if (searchQuery.isNotEmpty()) {
                            IconButton(onClick = {
                                haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                                searchQuery = ""
                            }) {
                                Icon(
                                    imageVector = Icons.Default.Close,
                                    contentDescription = "Clear"
                                )
                            }
                        }
                    } else {
                        // Search icon
                        IconButton(onClick = {
                            haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                            isSearchActive = true
                        }) {
                            Icon(
                                imageVector = Icons.Default.Search,
                                contentDescription = "Search"
                            )
                        }

                        // Filter icon (not for Groups tab)
                        if (pagerState.currentPage != 2) {
                            IconButton(onClick = {
                                haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                                showFilterDialog = true
                            }) {
                                Icon(
                                    imageVector = Icons.Default.FilterList,
                                    contentDescription = "Filter"
                                )
                            }
                        }

                        // Sort icon
                        IconButton(onClick = {
                            haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                            showSortDialog = true
                        }) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.Sort,
                                contentDescription = "Sort"
                            )
                        }

                        // More menu
                        IconButton(onClick = {
                            haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                            showMenu = true
                        }) {
                            Icon(
                                imageVector = Icons.Default.MoreVert,
                                contentDescription = "More options"
                            )
                        }

                        DropdownMenu(
                            expanded = showMenu,
                            onDismissRequest = { showMenu = false }
                        ) {
                            // Settings
                            DropdownMenuItem(
                                text = { Text(stringResource(R.string.nav_settings)) },
                                onClick = {
                                    haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                                    showMenu = false
                                    onNavigateToSettings()
                                },
                                leadingIcon = {
                                    Icon(Icons.Default.Settings, contentDescription = null)
                                }
                            )

                            HorizontalDivider()

                            // Export Contacts (vCard)
                            DropdownMenuItem(
                                text = { Text(stringResource(R.string.export_contacts_title)) },
                                onClick = {
                                    haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                                    showMenu = false
                                    // TODO: Trigger export dialog/functionality
                                    // This will be implemented via ViewModel event
                                },
                                leadingIcon = {
                                    Icon(Icons.Default.FileUpload, contentDescription = null)
                                }
                            )

                            // Import Contacts (vCard)
                            DropdownMenuItem(
                                text = { Text(stringResource(R.string.import_contacts_title)) },
                                onClick = {
                                    haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                                    showMenu = false
                                    // TODO: Trigger import file picker
                                },
                                leadingIcon = {
                                    Icon(Icons.Default.FileDownload, contentDescription = null)
                                }
                            )

                            HorizontalDivider()

                            // Privacy Policy
                            DropdownMenuItem(
                                text = { Text(stringResource(R.string.privacy_policy)) },
                                onClick = {
                                    haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                                    showMenu = false
                                    val intent = android.content.Intent(
                                        android.content.Intent.ACTION_VIEW,
                                        android.net.Uri.parse("https://myapps-505cf.web.app/contacts_privacy/privacy.html")
                                    )
                                    context.startActivity(intent)
                                },
                                leadingIcon = {
                                    Icon(Icons.Default.Policy, contentDescription = null)
                                }
                            )
                        }
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surfaceContainerLowest
                ),
                modifier = Modifier.windowInsetsPadding(WindowInsets.statusBars)
            )
        },
        bottomBar = {
            NavigationBar(
                modifier = Modifier.windowInsetsPadding(WindowInsets.navigationBars),
                containerColor = MaterialTheme.colorScheme.surfaceContainerLowest
            ) {
                NavigationBarItem(
                    icon = { Icon(Icons.Default.ContactPage, contentDescription = null) },
                    label = { Text(stringResource(R.string.nav_contacts)) },
                    selected = pagerState.currentPage == 0,
                    onClick = {
                        haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                        scope.launch { pagerState.animateScrollToPage(0) }
                    }
                )
                NavigationBarItem(
                    icon = { Icon(Icons.Default.Star, contentDescription = null) },
                    label = { Text(stringResource(R.string.favorites)) },
                    selected = pagerState.currentPage == 1,
                    onClick = {
                        haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                        scope.launch { pagerState.animateScrollToPage(1) }
                    }
                )
                NavigationBarItem(
                    icon = { Icon(Icons.Default.Group, contentDescription = null) },
                    label = { Text(stringResource(R.string.nav_groups)) },
                    selected = pagerState.currentPage == 2,
                    onClick = {
                        haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                        scope.launch { pagerState.animateScrollToPage(2) }
                    }
                )
            }
        },
        floatingActionButton = {
            // Dynamic context-aware Add FAB - positioned in main activity
            if (!isSearchActive) {
                FloatingActionButton(
                    onClick = {
                        haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                        when (pagerState.currentPage) {
                            0 -> onAddContact() // Contacts fragment: Launch add/edit contact screen
                            1 -> showAddToFavoritesDialog = true // Favorites fragment: Show add to favorites dialog
                            2 -> groupsViewModel.onEvent(GroupsEvent.ShowAddGroupDialog) // Groups fragment: Show new group dialog
                            else -> onAddContact()
                        }
                    },
                    containerColor = MaterialTheme.colorScheme.primaryContainer,
                    contentColor = MaterialTheme.colorScheme.onPrimaryContainer
                ) {
                    Icon(
                        imageVector = Icons.Default.Add,
                        contentDescription = when (pagerState.currentPage) {
                            0 -> "Add contact"
                            1 -> "Add to favorites"
                            2 -> "Add group"
                            else -> "Add"
                        }
                    )
                }
            }
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            HorizontalPager(
                state = pagerState,
                modifier = Modifier
                    .fillMaxSize()
                    .imePadding()
            ) { page ->
                when (page) {
                    0 -> ContactListScreen(
                        onContactClick = onContactClick,
                        onAddContact = onAddContact,
                        onNavigateToGroups = {},
                        onNavigateToSettings = onNavigateToSettings,
                        hideTopBar = true,
                        hideFab = true,
                        showFavoritesSection = false, // Don't show favorites in Contacts tab
                        modifier = Modifier.fillMaxSize()
                    )
                    1 -> FavoritesScreen(
                        onContactClick = onContactClick,
                        onAddContact = onAddContact,
                        onNavigateToSettings = onNavigateToSettings,
                        hideTopBar = true,
                        disableSwipeGestures = true, // IMPORTANT: Prevent swipe conflicts with horizontal pager
                        modifier = Modifier.fillMaxSize()
                    )
                    2 -> GroupsScreen(
                        onGroupClick = onGroupClick,
                        onAddGroup = { groupsViewModel.onEvent(GroupsEvent.ShowAddGroupDialog) },
                        onNavigateToSettings = onNavigateToSettings,
                        hideTopBar = true,
                        hideFab = true, // Hide GroupsScreen's own FAB since we're using centralized FAB
                        modifier = Modifier.fillMaxSize()
                    )
                }
            }

            // Dialpad FAB - Statically centered at the bottom, persistent across all tabs
            // Launches system dialer
            if (!isSearchActive) {
                SmallFloatingActionButton(
                    onClick = {
                        haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                        onLaunchDialer() // Launch system dialer
                    },
                    containerColor = MaterialTheme.colorScheme.secondaryContainer,
                    contentColor = MaterialTheme.colorScheme.onSecondaryContainer,
                    modifier = Modifier
                        .align(Alignment.BottomCenter)
                        .padding(bottom = 16.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Dialpad,
                        contentDescription = "Launch dialer"
                    )
                }
            }
        }
    }

    // Filter Dialog
    if (showFilterDialog && pagerState.currentPage != 2) {
        FilterDialog(
            currentFilter = contactsState.filter,
            totalContactsCount = contactsState.contactCount,
            favoritesCount = contactsState.favorites.size,
            withPhoneCount = contactsState.contacts.count { it.phoneNumbers.isNotEmpty() },
            withEmailCount = contactsState.contacts.count { it.emails.isNotEmpty() },
            withAddressCount = contactsState.contacts.count { it.addresses.isNotEmpty() },
            onDismiss = { showFilterDialog = false },
            onFilterSelected = { filter ->
                contactsViewModel.onEvent(ContactListEvent.FilterChanged(filter))
                showFilterDialog = false
            }
        )
    }

    // Sort Dialog
    if (showSortDialog) {
        SortDialog(
            currentSort = contactsState.sortOrder,
            showCustomSort = false,
            onDismiss = { showSortDialog = false },
            onSortSelected = { sort ->
                contactsViewModel.onEvent(ContactListEvent.SortOrderChanged(sort))
                showSortDialog = false
            }
        )
    }

    // Add to Favorites Dialog
    if (showAddToFavoritesDialog) {
        AddToFavoritesDialog(
            contacts = contactsState.contacts.filterNot { it.isFavorite },
            onContactSelected = { contactId ->
                contactsViewModel.onEvent(ContactListEvent.ToggleFavorite(contactId, true))
                showAddToFavoritesDialog = false
            },
            onDismiss = { showAddToFavoritesDialog = false }
        )
    }
}

/**
 * OPTIMIZATION: Extracted SearchTextField to prevent MainScreen recomposition on every keystroke
 * This significantly improves performance by isolating text field state updates
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun SearchTextField(
    searchQuery: String,
    onSearchQueryChange: (String) -> Unit,
    currentPage: Int,
    modifier: Modifier = Modifier
) {
    // Memoize placeholder text to avoid recalculation
    val placeholderText = remember(currentPage) {
        when (currentPage) {
            0 -> "Search contacts..."
            1 -> "Search favorites..."
            else -> "Search groups..."
        }
    }

    TextField(
        value = searchQuery,
        onValueChange = onSearchQueryChange,
        placeholder = {
            Text(
                text = placeholderText,
                style = MaterialTheme.typography.bodyLarge
            )
        },
        modifier = modifier.fillMaxWidth(),
        singleLine = true,
        colors = TextFieldDefaults.colors(
            focusedContainerColor = Color.Transparent,
            unfocusedContainerColor = Color.Transparent,
            disabledContainerColor = Color.Transparent,
            focusedIndicatorColor = Color.Transparent,
            unfocusedIndicatorColor = Color.Transparent,
            disabledIndicatorColor = Color.Transparent
        ),
        textStyle = MaterialTheme.typography.bodyLarge
    )
}

// Note: Replaced with the comprehensive FilterDialog from presentation.components
// This function is kept for reference but no longer used
/*
@Composable
private fun SimplifiedFilterDialog(
    currentFilter: ContactFilter,
    onFilterSelected: (ContactFilter) -> Unit,
    onDismiss: () -> Unit
) {
    FilterDialog(
        currentFilter = currentFilter,
        totalContactsCount = 0,
        favoritesCount = 0,
        withPhoneCount = 0,
        withEmailCount = 0,
        withAddressCount = 0,
        onDismiss = onDismiss,
        onFilterSelected = onFilterSelected
    )
}
*/

// Note: Replaced with the comprehensive SortDialog from presentation.components
// This function is kept for reference but no longer used
/*
@Composable
private fun SimplifiedSortDialog(
    currentSort: com.contacts.android.contacts.domain.model.SortOrder,
    onSortSelected: (com.contacts.android.contacts.domain.model.SortOrder) -> Unit,
    onDismiss: () -> Unit
) {
    SortDialog(
        currentSort = currentSort,
        showCustomSort = false,
        onDismiss = onDismiss,
        onSortSelected = onSortSelected
    )
}
*/

