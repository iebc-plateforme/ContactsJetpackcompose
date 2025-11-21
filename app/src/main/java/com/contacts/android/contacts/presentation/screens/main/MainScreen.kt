package com.contacts.android.contacts.presentation.screens.main
import androidx.compose.ui.res.stringResource
import com.contacts.android.contacts.R

import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
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
import com.contacts.android.contacts.presentation.components.AdMobBanner
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
    var showExportOptionsDialog by remember { mutableStateOf(false) }
    var includePhotosInExport by remember { mutableStateOf(false) }

    // File picker for importing VCF
    val importLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenDocument()
    ) { uri ->
        uri?.let {
            contactsViewModel.onEvent(ContactListEvent.ImportContacts(it))
        }
    }

    // File picker for exporting VCF
    val exportLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.CreateDocument("text/vcard")
    ) { uri ->
        uri?.let {
            contactsViewModel.onEvent(ContactListEvent.ExportAllContacts(it, includePhotosInExport))
        }
    }

    // Show import result toast/dialog
    LaunchedEffect(contactsState.importResult) {
        contactsState.importResult?.let { result ->
            val message = if (result.success) {
                if (result.count == 1) {
                    context.getString(R.string.import_success_single)
                } else {
                    context.getString(R.string.import_success, result.count)
                }
            } else {
                result.errorMessage ?: context.getString(R.string.import_failed)
            }
            Toast.makeText(context, message, Toast.LENGTH_LONG).show()
            contactsViewModel.onEvent(ContactListEvent.ClearImportResult)
        }
    }

    // Show export result toast/dialog
    LaunchedEffect(contactsState.exportResult) {
        contactsState.exportResult?.let { result ->
            val message = if (result.success) {
                if (result.count == 1) {
                    context.getString(R.string.export_success_single)
                } else {
                    context.getString(R.string.export_success, result.count)
                }
            } else {
                result.errorMessage ?: context.getString(R.string.export_failed)
            }
            Toast.makeText(context, message, Toast.LENGTH_LONG).show()
            contactsViewModel.onEvent(ContactListEvent.ClearExportResult)
        }
    }

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
            // Modern compact top bar
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .windowInsetsPadding(WindowInsets.statusBars),
                color = MaterialTheme.colorScheme.surface,
                tonalElevation = 1.dp
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp)
                        .padding(horizontal = 4.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Back button when searching
                    if (isSearchActive) {
                        IconButton(
                            onClick = {
                                haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                                isSearchActive = false
                                searchQuery = ""
                            },
                            modifier = Modifier.size(40.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.ArrowBack,
                                contentDescription = stringResource(R.string.close_search),
                                modifier = Modifier.size(20.dp)
                            )
                        }
                    }

                    // Title or Search field
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .padding(horizontal = if (isSearchActive) 4.dp else 12.dp),
                        contentAlignment = Alignment.CenterStart
                    ) {
                        if (isSearchActive) {
                            SearchTextField(
                                searchQuery = searchQuery,
                                onSearchQueryChange = { searchQuery = it },
                                currentPage = pagerState.currentPage
                            )
                        } else {
                            Text(
                                text = when (pagerState.currentPage) {
                                    0 -> stringResource(R.string.nav_contacts)
                                    1 -> stringResource(R.string.favorites)
                                    else -> stringResource(R.string.nav_groups)
                                },
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.SemiBold
                            )
                        }
                    }

                    // Action icons - compact sizing
                    if (isSearchActive) {
                        if (searchQuery.isNotEmpty()) {
                            IconButton(
                                onClick = {
                                    haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                                    searchQuery = ""
                                },
                                modifier = Modifier.size(40.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Close,
                                    contentDescription = stringResource(R.string.clear_text),
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                        }
                    } else {
                        // Search
                        IconButton(
                            onClick = {
                                haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                                isSearchActive = true
                            },
                            modifier = Modifier.size(40.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Search,
                                contentDescription = stringResource(R.string.action_search),
                                modifier = Modifier.size(20.dp)
                            )
                        }

                        // Filter (not for Groups)
                        if (pagerState.currentPage != 2) {
                            IconButton(
                                onClick = {
                                    haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                                    showFilterDialog = true
                                },
                                modifier = Modifier.size(40.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.FilterList,
                                    contentDescription = stringResource(R.string.filter),
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                        }

                        // Sort
                        IconButton(
                            onClick = {
                                haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                                showSortDialog = true
                            },
                            modifier = Modifier.size(40.dp)
                        ) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.Sort,
                                contentDescription = stringResource(R.string.sort),
                                modifier = Modifier.size(20.dp)
                            )
                        }

                        // More menu
                        Box {
                            IconButton(
                                onClick = {
                                    haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                                    showMenu = true
                                },
                                modifier = Modifier.size(40.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.MoreVert,
                                    contentDescription = stringResource(R.string.more_options),
                                    modifier = Modifier.size(20.dp)
                                )
                            }

                            DropdownMenu(
                                expanded = showMenu,
                                onDismissRequest = { showMenu = false }
                            ) {
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
                                DropdownMenuItem(
                                    text = { Text(stringResource(R.string.export_contacts_title)) },
                                    onClick = {
                                        haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                                        showMenu = false
                                        showExportOptionsDialog = true
                                    },
                                    leadingIcon = {
                                        Icon(Icons.Default.FileUpload, contentDescription = null)
                                    }
                                )
                                DropdownMenuItem(
                                    text = { Text(stringResource(R.string.import_contacts_title)) },
                                    onClick = {
                                        haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                                        showMenu = false
                                        importLauncher.launch(arrayOf("text/vcard", "text/x-vcard"))
                                    },
                                    leadingIcon = {
                                        Icon(Icons.Default.FileDownload, contentDescription = null)
                                    }
                                )
                                HorizontalDivider()
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
                    }
                }
            }
        },
        bottomBar = {
            // Modern minimal bottom navigation
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .windowInsetsPadding(WindowInsets.navigationBars),
                color = MaterialTheme.colorScheme.surface,
                tonalElevation = 2.dp
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(56.dp)
                        .padding(horizontal = 16.dp),
                    horizontalArrangement = Arrangement.SpaceEvenly,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Contacts tab
                    NavigationBarItem(
                        icon = {
                            Icon(
                                Icons.Default.ContactPage,
                                contentDescription = stringResource(R.string.nav_contacts),
                                modifier = Modifier.size(24.dp)
                            )
                        },
                        label = {
                            Text(
                                stringResource(R.string.nav_contacts),
                                style = MaterialTheme.typography.labelSmall
                            )
                        },
                        selected = pagerState.currentPage == 0,
                        onClick = {
                            haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                            scope.launch { pagerState.animateScrollToPage(0) }
                        },
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = MaterialTheme.colorScheme.primary,
                            selectedTextColor = MaterialTheme.colorScheme.primary,
                            unselectedIconColor = MaterialTheme.colorScheme.onSurfaceVariant,
                            unselectedTextColor = MaterialTheme.colorScheme.onSurfaceVariant,
                            indicatorColor = MaterialTheme.colorScheme.primaryContainer
                        ),
                        modifier = Modifier.weight(1f)
                    )

                    // Favorites tab
                    NavigationBarItem(
                        icon = {
                            Icon(
                                Icons.Default.Star,
                                contentDescription = stringResource(R.string.favorites),
                                modifier = Modifier.size(24.dp)
                            )
                        },
                        label = {
                            Text(
                                stringResource(R.string.favorites),
                                style = MaterialTheme.typography.labelSmall
                            )
                        },
                        selected = pagerState.currentPage == 1,
                        onClick = {
                            haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                            scope.launch { pagerState.animateScrollToPage(1) }
                        },
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = MaterialTheme.colorScheme.primary,
                            selectedTextColor = MaterialTheme.colorScheme.primary,
                            unselectedIconColor = MaterialTheme.colorScheme.onSurfaceVariant,
                            unselectedTextColor = MaterialTheme.colorScheme.onSurfaceVariant,
                            indicatorColor = MaterialTheme.colorScheme.primaryContainer
                        ),
                        modifier = Modifier.weight(1f)
                    )

                    // Groups tab
                    NavigationBarItem(
                        icon = {
                            Icon(
                                Icons.Default.Group,
                                contentDescription = stringResource(R.string.nav_groups),
                                modifier = Modifier.size(24.dp)
                            )
                        },
                        label = {
                            Text(
                                stringResource(R.string.nav_groups),
                                style = MaterialTheme.typography.labelSmall
                            )
                        },
                        selected = pagerState.currentPage == 2,
                        onClick = {
                            haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                            scope.launch { pagerState.animateScrollToPage(2) }
                        },
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = MaterialTheme.colorScheme.primary,
                            selectedTextColor = MaterialTheme.colorScheme.primary,
                            unselectedIconColor = MaterialTheme.colorScheme.onSurfaceVariant,
                            unselectedTextColor = MaterialTheme.colorScheme.onSurfaceVariant,
                            indicatorColor = MaterialTheme.colorScheme.primaryContainer
                        ),
                        modifier = Modifier.weight(1f)
                    )
                }
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
                    modifier = Modifier.padding(bottom = 60.dp), // Move FAB above the banner
                    containerColor = MaterialTheme.colorScheme.primary,
                    contentColor = MaterialTheme.colorScheme.onPrimary
                ) {
                    Icon(
                        imageVector = Icons.Default.Add,
                        contentDescription = when (pagerState.currentPage) {
                            0 -> stringResource(R.string.add_contact_desc)
                            1 -> stringResource(R.string.add_to_favorites_action)
                            2 -> stringResource(R.string.add_group_action)
                            else -> stringResource(R.string.action_add)
                        }
                    )
                }
            }
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            // Main content area with HorizontalPager
            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
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
                        containerColor = MaterialTheme.colorScheme.primary,
                        contentColor = MaterialTheme.colorScheme.onPrimary,
                        modifier = Modifier
                            .align(Alignment.BottomCenter)
                            .padding(bottom = 16.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Dialpad,
                            contentDescription = stringResource(R.string.launch_dialer)
                        )
                    }
                }
            }

            // Fixed AdMob Banner above the bottom navigation bar
            AdMobBanner(
                modifier = Modifier.fillMaxWidth(),
                adUnitId = com.contacts.android.contacts.ads.AdMobManager.BANNER_HOME_AD_UNIT_ID
            )
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

    // Export Options Dialog
    if (showExportOptionsDialog) {
        AlertDialog(
            onDismissRequest = { showExportOptionsDialog = false },
            icon = {
                Icon(
                    imageVector = Icons.Default.FileUpload,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary
                )
            },
            title = {
                Text(stringResource(R.string.export_contacts_title))
            },
            text = {
                Column {
                    Text(
                        text = stringResource(R.string.export_to_vcard),
                        style = MaterialTheme.typography.bodyMedium
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Checkbox(
                            checked = includePhotosInExport,
                            onCheckedChange = { includePhotosInExport = it }
                        )
                        Column(modifier = Modifier.padding(start = 8.dp)) {
                            Text(
                                text = stringResource(R.string.include_photos),
                                style = MaterialTheme.typography.bodyMedium
                            )
                            Text(
                                text = stringResource(R.string.include_photos_warning),
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        showExportOptionsDialog = false
                        val filename = contactsViewModel.getExportFilename()
                        exportLauncher.launch(filename)
                    }
                ) {
                    Text(stringResource(R.string.export_contacts_title))
                }
            },
            dismissButton = {
                TextButton(onClick = { showExportOptionsDialog = false }) {
                    Text(stringResource(R.string.action_cancel))
                }
            }
        )
    }

    // Loading indicator for import/export
    if (contactsState.isImporting || contactsState.isExporting) {
        AlertDialog(
            onDismissRequest = { /* Cannot dismiss while loading */ },
            icon = {
                CircularProgressIndicator(
                    modifier = Modifier.size(48.dp),
                    color = MaterialTheme.colorScheme.primary
                )
            },
            title = {
                Text(
                    text = if (contactsState.isImporting) {
                        stringResource(R.string.importing_contacts)
                    } else {
                        stringResource(R.string.exporting_contacts)
                    }
                )
            },
            text = null,
            confirmButton = { /* No buttons while loading */ }
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
    // Get placeholder text based on current page
    val placeholderText = when (currentPage) {
        0 -> stringResource(R.string.search_contacts_placeholder)
        1 -> stringResource(R.string.search_favorites_placeholder)
        else -> stringResource(R.string.search_groups_placeholder)
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

