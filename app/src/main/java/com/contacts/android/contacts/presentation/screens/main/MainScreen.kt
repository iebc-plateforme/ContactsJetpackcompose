package com.contacts.android.contacts.presentation.screens.main

import android.Manifest
import android.content.ActivityNotFoundException
import android.content.Intent
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Sort
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.contacts.android.contacts.R
import com.contacts.android.contacts.data.preferences.UserPreferences
import com.contacts.android.contacts.presentation.components.AdMobBanner
import com.contacts.android.contacts.presentation.components.EnhancedFilterDialog
import com.contacts.android.contacts.presentation.components.SortDialog
import com.contacts.android.contacts.presentation.screens.contactlist.*
import com.contacts.android.contacts.presentation.screens.favorites.FavoritesScreen
import com.contacts.android.contacts.presentation.screens.groups.GroupsEvent
import com.contacts.android.contacts.presentation.screens.groups.GroupsScreen
import com.contacts.android.contacts.presentation.screens.groups.GroupsViewModel
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.isGranted
import com.google.accompanist.permissions.rememberPermissionState
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class, ExperimentalPermissionsApi::class)
@Composable
fun MainScreen(
    onContactClick: (Long) -> Unit,
    onAddContact: () -> Unit,
    onNavigateToSettings: () -> Unit,
    onGroupClick: (Long) -> Unit,
    onLaunchDialer: () -> Unit = {},
    onScanQRCode: (() -> Unit)? = null,
    onNavigateToPremium: () -> Unit = {},
    onNavigateToPremiumSupport: () -> Unit = {},
    defaultTab: com.contacts.android.contacts.data.preferences.DefaultTab =
        com.contacts.android.contacts.data.preferences.DefaultTab.CONTACTS,
    contactsViewModel: ContactListViewModel = hiltViewModel(),
    groupsViewModel: GroupsViewModel = hiltViewModel(),
    userPreferences: UserPreferences
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
    val isPremium by userPreferences.isPremium.collectAsStateWithLifecycle(initialValue = false)
    val appOpenCount by userPreferences.appOpenCount.collectAsStateWithLifecycle(initialValue = 0)
    val lastPremiumPromptTime by userPreferences.lastPremiumPromptTime.collectAsStateWithLifecycle(initialValue = 0L)
    val lastPremiumPromptCount by userPreferences.lastPremiumPromptCount.collectAsStateWithLifecycle(initialValue = 0)
    val context = LocalContext.current

    var showMenu by remember { mutableStateOf(false) }
    var showFilterDialog by remember { mutableStateOf(false) }
    var showSortDialog by remember { mutableStateOf(false) }
    var searchQuery by remember { mutableStateOf("") }
    val isSearchActive by remember { derivedStateOf { searchQuery.isNotEmpty() } }
    var showAddToFavoritesDialog by remember { mutableStateOf(false) }
    var showExportOptionsDialog by remember { mutableStateOf(false) }
    var includePhotosInExport by remember { mutableStateOf(false) }
    var showPremiumPrompt by remember { mutableStateOf(false) }

    val readContactsPermission = rememberPermissionState(Manifest.permission.READ_CONTACTS)

    // Trigger initial sync when permission is granted
    LaunchedEffect(readContactsPermission.status.isGranted) {
        if (readContactsPermission.status.isGranted) {
            contactsViewModel.onPermissionGranted()
        }
    }

    // File picker for importing VCF
    val importLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenDocument()
    ) { uri ->
        uri?.let { contactsViewModel.onEvent(ContactListEvent.ImportContacts(it)) }
    }

    // File picker for exporting VCF
    val exportLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.CreateDocument("text/vcard")
    ) { uri ->
        uri?.let { contactsViewModel.onEvent(ContactListEvent.ExportAllContacts(it, includePhotosInExport)) }
    }

    // Show import result toast
    LaunchedEffect(contactsState.importResult) {
        contactsState.importResult?.let { result ->
            val message = if (result.success) {
                if (result.count == 1) context.getString(R.string.import_success_single)
                else context.getString(R.string.import_success, result.count)
            } else {
                result.errorMessage ?: context.getString(R.string.import_failed)
            }
            Toast.makeText(context, message, Toast.LENGTH_LONG).show()
            contactsViewModel.onEvent(ContactListEvent.ClearImportResult)
        }
    }

    // Show export result toast + premium nudge for VCF export
    LaunchedEffect(contactsState.exportResult) {
        contactsState.exportResult?.let { result ->
            val message = if (result.success) {
                val base = if (result.count == 1) context.getString(R.string.export_success_single)
                else context.getString(R.string.export_success, result.count)
                if (!isPremium) "$base\n${context.getString(R.string.premium_nudge_after_export)}"
                else base
            } else {
                result.errorMessage ?: context.getString(R.string.export_failed)
            }
            Toast.makeText(context, message, Toast.LENGTH_LONG).show()
            contactsViewModel.onEvent(ContactListEvent.ClearExportResult)
        }
    }

    // Debounce search queries
    LaunchedEffect(searchQuery, pagerState.currentPage, pagerState.isScrollInProgress) {
        if (pagerState.isScrollInProgress) return@LaunchedEffect
        kotlinx.coroutines.delay(300)
        when (pagerState.currentPage) {
            0, 1 -> contactsViewModel.onEvent(ContactListEvent.SearchQueryChanged(searchQuery))
            2 -> groupsViewModel.onEvent(GroupsEvent.SearchQueryChanged(searchQuery))
        }
    }

    // Premium prompt logic
    LaunchedEffect(isPremium, appOpenCount, lastPremiumPromptTime, lastPremiumPromptCount) {
        if (isPremium) return@LaunchedEffect
        if (appOpenCount < 5) return@LaunchedEffect

        val now = System.currentTimeMillis()
        val enoughOpens = (appOpenCount - lastPremiumPromptCount) >= 15
        val sevenDaysMillis = 7L * 24 * 60 * 60 * 1000
        val enoughTime = (now - lastPremiumPromptTime) >= sevenDaysMillis

        if (enoughOpens && enoughTime && !showPremiumPrompt) {
            kotlinx.coroutines.delay(3000)
            showPremiumPrompt = true
        }
    }

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        containerColor = MaterialTheme.colorScheme.surfaceContainerLowest,
        contentWindowInsets = WindowInsets(0, 0, 0, 0),
        floatingActionButtonPosition = FabPosition.End,
        topBar = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .windowInsetsPadding(WindowInsets.statusBars)
                    .padding(top = 12.dp, bottom = 4.dp)
            ) {
                // Search Bar
                Surface(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp)
                        .heightIn(min = 52.dp),
                    shape = MaterialTheme.shapes.extraLarge,
                    color = MaterialTheme.colorScheme.surfaceContainerHigh,
                    tonalElevation = 2.dp
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        IconButton(onClick = { showMenu = true }) {
                            Icon(
                                imageVector = Icons.Default.Menu,
                                contentDescription = stringResource(R.string.more_options),
                                tint = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }

                        SearchTextField(
                            searchQuery = searchQuery,
                            onSearchQueryChange = { searchQuery = it },
                            currentPage = pagerState.currentPage,
                            modifier = Modifier.weight(1f)
                        )

                        if (isSearchActive) {
                            IconButton(onClick = { searchQuery = "" }) {
                                Icon(
                                    imageVector = Icons.Default.Close,
                                    contentDescription = stringResource(R.string.clear_text)
                                )
                            }
                        }

                        PremiumBadgeButton(
                            isPremium = isPremium,
                            onClick = {
                                if (isPremium) onNavigateToPremiumSupport() else onNavigateToPremium()
                            }
                        )
                    }
                }

                // Quick Actions Row
                QuickActionRow(
                    onLaunchDialer = onLaunchDialer,
                    onFilterClick = { showFilterDialog = true },
                    onSortClick = { showSortDialog = true }
                )

                // Dropdown Menu
                DropdownMenu(
                    expanded = showMenu,
                    onDismissRequest = { showMenu = false },
                    modifier = Modifier.padding(start = 16.dp)
                ) {
                    DropdownMenuItem(
                        text = { Text(stringResource(R.string.nav_settings)) },
                        onClick = {
                            showMenu = false
                            onNavigateToSettings()
                        },
                        leadingIcon = { Icon(Icons.Default.Settings, contentDescription = null) }
                    )
                    DropdownMenuItem(
                        text = { Text(stringResource(R.string.action_share_app)) },
                        onClick = {
                            showMenu = false
                            try {
                                val sendIntent = Intent(Intent.ACTION_SEND).apply {
                                    type = "text/plain"
                                    putExtra(Intent.EXTRA_TEXT, context.getString(R.string.share_app_text))
                                    putExtra(Intent.EXTRA_TITLE, context.getString(R.string.app_name))
                                }
                                context.startActivity(Intent.createChooser(sendIntent, null))
                            } catch (e: ActivityNotFoundException) {
                                Toast.makeText(context, R.string.error_no_share_app, Toast.LENGTH_SHORT).show()
                            }
                        },
                        leadingIcon = { Icon(Icons.Default.Share, contentDescription = null) }
                    )
                    HorizontalDivider()
                    DropdownMenuItem(
                        text = { Text(stringResource(R.string.export_contacts_title)) },
                        onClick = {
                            showMenu = false
                            showExportOptionsDialog = true
                        },
                        leadingIcon = { Icon(Icons.Default.FileUpload, contentDescription = null) }
                    )
                    DropdownMenuItem(
                        text = { Text(stringResource(R.string.import_contacts_title)) },
                        onClick = {
                            showMenu = false
                            importLauncher.launch(arrayOf("text/vcard", "text/x-vcard"))
                        },
                        leadingIcon = { Icon(Icons.Default.FileDownload, contentDescription = null) }
                    )
                }
            }
        },
        bottomBar = {
            NavigationBar(
                modifier = Modifier.windowInsetsPadding(WindowInsets.navigationBars),
                tonalElevation = 1.dp
            ) {
                NavigationBarItem(
                    icon = {
                        Icon(Icons.Default.ContactPage, contentDescription = null)
                    },
                    label = { Text(stringResource(R.string.nav_contacts)) },
                    selected = pagerState.currentPage == 0,
                    onClick = { scope.launch { pagerState.animateScrollToPage(0) } }
                )
                NavigationBarItem(
                    icon = {
                        Icon(Icons.Default.Star, contentDescription = null)
                    },
                    label = { Text(stringResource(R.string.favorites)) },
                    selected = pagerState.currentPage == 1,
                    onClick = { scope.launch { pagerState.animateScrollToPage(1) } }
                )
                NavigationBarItem(
                    icon = {
                        Icon(Icons.Default.Group, contentDescription = null)
                    },
                    label = { Text(stringResource(R.string.nav_groups)) },
                    selected = pagerState.currentPage == 2,
                    onClick = { scope.launch { pagerState.animateScrollToPage(2) } }
                )
            }
        },
        floatingActionButton = {
            if (!isSearchActive) {
                FloatingActionButton(
                    onClick = {
                        when (pagerState.currentPage) {
                            0 -> onAddContact()
                            1 -> showAddToFavoritesDialog = true
                            2 -> groupsViewModel.onEvent(GroupsEvent.ShowAddGroupDialog)
                            else -> onAddContact()
                        }
                    },
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
            // Main content with HorizontalPager
            HorizontalPager(
                state = pagerState,
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
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
                        showFavoritesSection = false,
                        emptyActionLabel = stringResource(R.string.filter_contacts),
                        onEmptyAction = { showFilterDialog = true },
                        modifier = Modifier.fillMaxSize()
                    )
                    1 -> FavoritesScreen(
                        onContactClick = onContactClick,
                        onAddContact = onAddContact,
                        onNavigateToSettings = onNavigateToSettings,
                        hideTopBar = true,
                        disableSwipeGestures = true,
                        onAddToFavorites = { showAddToFavoritesDialog = true },
                        modifier = Modifier.fillMaxSize()
                    )
                    2 -> GroupsScreen(
                        onGroupClick = onGroupClick,
                        onAddGroup = { groupsViewModel.onEvent(GroupsEvent.ShowAddGroupDialog) },
                        onNavigateToSettings = onNavigateToSettings,
                        hideTopBar = true,
                        hideFab = true,
                        modifier = Modifier.fillMaxSize()
                    )
                }
            }

            // AdMob Banner
            AdMobBanner(
                modifier = Modifier.fillMaxWidth(),
                adUnitId = com.contacts.android.contacts.ads.AdMobManager.BANNER_HOME_AD_UNIT_ID,
                onUpgradeToPremium = onNavigateToPremium
            )
        }
    }

    // Filter Dialog
    if (showFilterDialog && pagerState.currentPage != 2) {
        EnhancedFilterDialog(
            currentFilter = contactsState.filter,
            totalContactsCount = contactsState.contactCount,
            favoritesCount = contactsState.favorites.size,
            withPhoneCount = contactsState.contacts.count { it.phoneNumbers.isNotEmpty() },
            withEmailCount = contactsState.contacts.count { it.emails.isNotEmpty() },
            withAddressCount = contactsState.contacts.count { it.addresses.isNotEmpty() },
            availableGroups = emptyList(),
            availableSources = contactsState.availableSources,
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
            title = { Text(stringResource(R.string.export_contacts_title)) },
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
            onDismissRequest = {},
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
            confirmButton = {}
        )
    }

    if (showPremiumPrompt && !isPremium) {
        PremiumUpsellDialog(
            priceText = "$4.99",
            onUpgrade = {
                showPremiumPrompt = false
                scope.launch {
                    userPreferences.setLastPremiumPrompt(System.currentTimeMillis(), appOpenCount)
                }
                onNavigateToPremium()
            },
            onDismiss = {
                showPremiumPrompt = false
                scope.launch {
                    userPreferences.setLastPremiumPrompt(System.currentTimeMillis(), appOpenCount)
                }
            }
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun SearchTextField(
    searchQuery: String,
    onSearchQueryChange: (String) -> Unit,
    currentPage: Int,
    modifier: Modifier = Modifier
) {
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
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
            )
        },
        modifier = modifier,
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

@Composable
private fun PremiumUpsellDialog(
    priceText: String,
    onUpgrade: () -> Unit,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        shape = MaterialTheme.shapes.extraLarge,
        icon = {
            Surface(
                color = MaterialTheme.colorScheme.primaryContainer,
                shape = RoundedCornerShape(20.dp),
                modifier = Modifier.size(72.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.WorkspacePremium,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier
                        .padding(16.dp)
                        .size(40.dp)
                )
            }
        },
        title = {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(
                    text = stringResource(R.string.premium_prompt_title),
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = stringResource(R.string.premium_prompt_message),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                PremiumBenefitRow(
                    icon = Icons.Default.Block,
                    text = stringResource(R.string.premium_prompt_benefit_ads)
                )
                PremiumBenefitRow(
                    icon = Icons.Default.Palette,
                    text = stringResource(R.string.premium_prompt_benefit_themes)
                )
                PremiumBenefitRow(
                    icon = Icons.Default.Lock,
                    text = stringResource(R.string.premium_benefit_private_contacts)
                )
                PremiumBenefitRow(
                    icon = Icons.Default.Label,
                    text = stringResource(R.string.premium_benefit_tags)
                )
                PremiumBenefitRow(
                    icon = Icons.Default.PictureAsPdf,
                    text = stringResource(R.string.premium_benefit_pdf)
                )

                Spacer(modifier = Modifier.height(4.dp))

                // Price highlight
                Surface(
                    color = MaterialTheme.colorScheme.tertiaryContainer,
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = stringResource(R.string.premium_prompt_price, priceText),
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onTertiaryContainer,
                        modifier = Modifier.padding(12.dp),
                        textAlign = androidx.compose.ui.text.style.TextAlign.Center
                    )
                }
            }
        },
        confirmButton = {
            Button(onClick = onUpgrade) {
                Icon(
                    Icons.Default.WorkspacePremium,
                    contentDescription = null,
                    modifier = Modifier.size(18.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(stringResource(R.string.premium_prompt_upgrade))
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(stringResource(R.string.premium_prompt_not_now))
            }
        }
    )
}

@Composable
private fun PremiumBenefitRow(
    icon: ImageVector,
    text: String
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier.fillMaxWidth()
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.primary,
            modifier = Modifier.size(20.dp)
        )
        Spacer(modifier = Modifier.width(12.dp))
        Text(
            text = text,
            style = MaterialTheme.typography.bodyMedium,
            modifier = Modifier.weight(1f)
        )
        Icon(
            imageVector = Icons.Default.CheckCircle,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.primary,
            modifier = Modifier.size(16.dp)
        )
    }
}

/**
 * Quick action chips - only essential actions: Dialer, Filter, Sort
 */
@Composable
private fun QuickActionRow(
    onLaunchDialer: () -> Unit,
    onFilterClick: () -> Unit,
    onSortClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    LazyRow(
        modifier = modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        contentPadding = PaddingValues(horizontal = 16.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        item {
            QuickActionChip(
                icon = Icons.Default.Dialpad,
                label = stringResource(R.string.launch_dialer),
                onClick = onLaunchDialer,
                containerColor = MaterialTheme.colorScheme.primaryContainer,
                contentColor = MaterialTheme.colorScheme.onPrimaryContainer
            )
        }
        item {
            QuickActionChip(
                icon = Icons.Default.FilterList,
                label = stringResource(R.string.filter),
                onClick = onFilterClick
            )
        }
        item {
            QuickActionChip(
                icon = Icons.AutoMirrored.Filled.Sort,
                label = stringResource(R.string.sort),
                onClick = onSortClick
            )
        }
    }
}

@Composable
private fun QuickActionChip(
    icon: ImageVector,
    label: String,
    onClick: () -> Unit,
    containerColor: Color = MaterialTheme.colorScheme.surfaceVariant,
    contentColor: Color = MaterialTheme.colorScheme.onSurfaceVariant
) {
    SuggestionChip(
        onClick = onClick,
        label = {
            Text(
                text = label,
                style = MaterialTheme.typography.labelMedium
            )
        },
        icon = {
            Icon(
                imageVector = icon,
                contentDescription = null,
                modifier = Modifier.size(18.dp)
            )
        },
        colors = SuggestionChipDefaults.suggestionChipColors(
            containerColor = containerColor,
            labelColor = contentColor,
            iconContentColor = contentColor
        ),
        border = null,
        shape = RoundedCornerShape(12.dp)
    )
}

/**
 * Premium badge button - static icon, no distracting animations
 */
@Composable
private fun PremiumBadgeButton(
    isPremium: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    IconButton(
        onClick = onClick,
        modifier = modifier
    ) {
        Icon(
            imageVector = Icons.Default.WorkspacePremium,
            contentDescription = if (isPremium) {
                stringResource(R.string.premium_prompt_title)
            } else {
                stringResource(R.string.premium_prompt_upgrade)
            },
            modifier = Modifier.size(24.dp),
            tint = if (isPremium) {
                MaterialTheme.colorScheme.tertiary
            } else {
                MaterialTheme.colorScheme.onSurfaceVariant
            }
        )
    }
}
