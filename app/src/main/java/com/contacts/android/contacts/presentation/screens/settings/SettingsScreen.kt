package com.contacts.android.contacts.presentation.screens.settings

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.contacts.android.contacts.R
import com.contacts.android.contacts.data.preferences.ColorTheme
import com.contacts.android.contacts.data.preferences.ThemeMode
import com.contacts.android.contacts.presentation.components.AdMobBanner
import com.contacts.android.contacts.presentation.theme.getThemePreviewColor

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    onNavigateBack: () -> Unit,
    viewModel: SettingsViewModel = hiltViewModel()
) {
    val context = LocalContext.current
    val snackbarHostState = remember { SnackbarHostState() }

    var showAboutDialog by remember { mutableStateOf(false) }
    var showThemeModeDialog by remember { mutableStateOf(false) }
    var showColorThemeDialog by remember { mutableStateOf(false) }
    var showLanguageDialog by remember { mutableStateOf(false) }
    var showFontSizeDialog by remember { mutableStateOf(false) }
    var showDefaultTabDialog by remember { mutableStateOf(false) }
    var showContactClickActionDialog by remember { mutableStateOf(false) }
    var showVisibleFieldsDialog by remember { mutableStateOf(false) }
    var showVisibleTabsDialog by remember { mutableStateOf(false) }
    var showMergeDuplicatesDialog by remember { mutableStateOf(false) }
    var showBackupConfigDialog by remember { mutableStateOf(false) }
    var showPrivacyPolicyDialog by remember { mutableStateOf(false) }
    var showLicensesDialog by remember { mutableStateOf(false) }

    // Observe settings from ViewModel
    val themeMode by viewModel.themeMode.collectAsStateWithLifecycle()
    val colorTheme by viewModel.colorTheme.collectAsStateWithLifecycle()
    val appLanguage by viewModel.appLanguage.collectAsStateWithLifecycle()
    val fontScale by viewModel.fontScale.collectAsStateWithLifecycle()
    val defaultTab by viewModel.defaultTab.collectAsStateWithLifecycle()
    val contactClickAction by viewModel.contactClickAction.collectAsStateWithLifecycle()
    val edgeToEdgeDisplay by viewModel.edgeToEdgeDisplay.collectAsStateWithLifecycle()
    val showContactThumbnails by viewModel.showContactThumbnails.collectAsStateWithLifecycle()
    val showPhoneNumbers by viewModel.showPhoneNumbers.collectAsStateWithLifecycle()
    val showOnlyWithPhone by viewModel.showOnlyWithPhone.collectAsStateWithLifecycle()
    val showDuplicates by viewModel.showDuplicates.collectAsStateWithLifecycle()
    val isLanguageChanging by viewModel.isLanguageChanging.collectAsStateWithLifecycle()
    val importExportState by viewModel.importExportState.collectAsStateWithLifecycle()
    val duplicatesState by viewModel.duplicatesState.collectAsStateWithLifecycle()
    val backupState by viewModel.backupState.collectAsStateWithLifecycle()

    // Additional Fossify-like settings
    val startNameWithSurname by viewModel.startNameWithSurname.collectAsStateWithLifecycle()
    val showPrivateContacts by viewModel.showPrivateContacts.collectAsStateWithLifecycle()
    val showDialpadButton by viewModel.showDialpadButton.collectAsStateWithLifecycle()
    val formatPhoneNumbers by viewModel.formatPhoneNumbers.collectAsStateWithLifecycle()
    val callConfirmation by viewModel.callConfirmation.collectAsStateWithLifecycle()

    // Convert font scale to FontSize enum
    val currentFontSize = com.contacts.android.contacts.data.preferences.FontSize.values()
        .find { it.scale == fontScale } ?: com.contacts.android.contacts.data.preferences.FontSize.MEDIUM

    // File picker launchers for import/export
    val importLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri ->
        uri?.let { viewModel.importContacts(it) }
    }

    val exportLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.CreateDocument("text/x-vcard")
    ) { uri ->
        uri?.let { viewModel.exportContacts(it, includePhotos = false) }
    }

    // Handle import/export state
    LaunchedEffect(importExportState) {
        when (val state = importExportState) {
            is ImportExportState.Success -> {
                snackbarHostState.showSnackbar(
                    message = state.message,
                    duration = SnackbarDuration.Short
                )
                viewModel.clearImportExportState()
            }
            is ImportExportState.Error -> {
                snackbarHostState.showSnackbar(
                    message = state.message,
                    duration = SnackbarDuration.Long
                )
                viewModel.clearImportExportState()
            }
            else -> {}
        }
    }

    // Handle duplicates state
    LaunchedEffect(duplicatesState) {
        when (val state = duplicatesState) {
            is DuplicatesState.NoDuplicates -> {
                snackbarHostState.showSnackbar(
                    message = context.getString(R.string.no_duplicate_contacts_found),
                    duration = SnackbarDuration.Short
                )
                viewModel.clearDuplicatesState()
            }
            is DuplicatesState.Found -> {
                // Show the merge duplicates dialog instead of just a snackbar
                showMergeDuplicatesDialog = true
            }
            is DuplicatesState.MergeSuccess -> {
                snackbarHostState.showSnackbar(
                    message = context.getString(R.string.contacts_merged_successfully),
                    duration = SnackbarDuration.Short
                )
                // Close dialog and clear state
                showMergeDuplicatesDialog = false
                viewModel.clearDuplicatesState()
            }
            is DuplicatesState.Error -> {
                snackbarHostState.showSnackbar(
                    message = state.message,
                    duration = SnackbarDuration.Long
                )
                viewModel.clearDuplicatesState()
            }
            else -> {}
        }
    }

    // Handle backup state
    LaunchedEffect(backupState) {
        when (val state = backupState) {
            is BackupState.Success -> {
                snackbarHostState.showSnackbar(
                    message = state.message,
                    duration = SnackbarDuration.Short
                )
                viewModel.clearBackupState()
            }
            is BackupState.Error -> {
                snackbarHostState.showSnackbar(
                    message = state.message,
                    duration = SnackbarDuration.Long
                )
                viewModel.clearBackupState()
            }
            else -> {}
        }
    }

    // Black flash overlay animation for language change
    Box(modifier = Modifier.fillMaxSize()) {
        Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            TopAppBar(
                title = { Text(stringResource(id = R.string.settings_title)) },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(
                            Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = stringResource(id = R.string.action_back)
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    titleContentColor = MaterialTheme.colorScheme.onPrimary,
                    navigationIconContentColor = MaterialTheme.colorScheme.onPrimary
                )
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            // Scrollable settings content
            Column(
                modifier = Modifier
                    .weight(1f)
                    .verticalScroll(rememberScrollState())
            ) {
            // Appearance Section
            SettingsSectionHeader(text = stringResource(id = R.string.settings_appearance))

            SettingsItem(
                icon = Icons.Default.Palette,
                title = stringResource(R.string.color_theme),
                subtitle = colorTheme.name.lowercase().replaceFirstChar { it.uppercase() },
                onClick = { showColorThemeDialog = true }
            )

            SettingsItem(
                icon = Icons.Default.DarkMode,
                title = stringResource(R.string.settings_theme_mode),
                subtitle = when (themeMode) {
                    ThemeMode.LIGHT -> stringResource(R.string.theme_light)
                    ThemeMode.DARK -> stringResource(R.string.theme_dark)
                    ThemeMode.SYSTEM -> stringResource(R.string.theme_system)
                },
                onClick = { showThemeModeDialog = true }
            )

            SettingsItem(
                icon = Icons.Default.Language,
                title = stringResource(R.string.language),
                subtitle = appLanguage.displayName,
                onClick = { showLanguageDialog = true }
            )

            SettingsItem(
                icon = Icons.Default.FormatSize,
                title = stringResource(R.string.font_size),
                subtitle = stringResource(currentFontSize.displayNameRes),
                onClick = { showFontSizeDialog = true }
            )

            HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))

            // Display Section
            SettingsSectionHeader(text = stringResource(id = R.string.settings_display))

            SettingsItem(
                icon = Icons.Default.Checklist,
                title = stringResource(R.string.manage_visible_contact_fields),
                subtitle = stringResource(R.string.choose_which_fields_display),
                onClick = { showVisibleFieldsDialog = true }
            )

            SettingsItem(
                icon = Icons.Default.Tab,
                title = stringResource(R.string.manage_visible_tabs),
                subtitle = stringResource(R.string.select_which_tabs_show),
                onClick = { showVisibleTabsDialog = true }
            )

            SettingsSwitchItem(
                icon = Icons.Default.PhotoCamera,
                title = stringResource(R.string.show_contact_thumbnails_title),
                subtitle = stringResource(R.string.display_contact_photos_lists),
                checked = showContactThumbnails,
                onCheckedChange = { viewModel.setShowContactThumbnails(it) }
            )

            SettingsSwitchItem(
                icon = Icons.Default.Phone,
                title = stringResource(R.string.show_phone_numbers_title),
                subtitle = stringResource(R.string.display_phone_numbers_below),
                checked = showPhoneNumbers,
                onCheckedChange = { viewModel.setShowPhoneNumbers(it) }
            )

            SettingsSwitchItem(
                icon = Icons.Default.Person,
                title = stringResource(R.string.start_name_with_surname),
                subtitle = stringResource(R.string.display_last_name_first),
                checked = startNameWithSurname,
                onCheckedChange = { viewModel.setStartNameWithSurname(it) }
            )

            SettingsSwitchItem(
                icon = Icons.Default.Visibility,
                title = stringResource(R.string.show_private_contacts_title),
                subtitle = stringResource(R.string.display_contacts_marked_private),
                checked = showPrivateContacts,
                onCheckedChange = { viewModel.setShowPrivateContacts(it) }
            )

            SettingsSwitchItem(
                icon = Icons.Default.Fullscreen,
                title = stringResource(R.string.edge_to_edge_display),
                subtitle = stringResource(R.string.extend_content_screen_edges),
                checked = edgeToEdgeDisplay,
                onCheckedChange = { viewModel.setEdgeToEdgeDisplay(it) }
            )

            HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))

            // Behavior Section
            SettingsSectionHeader(text = stringResource(id = R.string.behavior))

            SettingsItem(
                icon = Icons.Default.Tab,
                title = stringResource(R.string.default_tab),
                subtitle = stringResource(defaultTab.displayNameRes),
                onClick = { showDefaultTabDialog = true }
            )

            SettingsItem(
                icon = Icons.Default.TouchApp,
                title = stringResource(R.string.on_contact_clicked),
                subtitle = stringResource(contactClickAction.displayNameRes),
                onClick = { showContactClickActionDialog = true }
            )

            SettingsSwitchItem(
                icon = Icons.Default.Dialpad,
                title = stringResource(R.string.show_dialpad_button_title),
                subtitle = stringResource(R.string.display_quick_dial_button),
                checked = showDialpadButton,
                onCheckedChange = { viewModel.setShowDialpadButton(it) }
            )

            SettingsSwitchItem(
                icon = Icons.Default.Call,
                title = stringResource(R.string.call_confirmation),
                subtitle = stringResource(R.string.ask_before_making_call),
                checked = callConfirmation,
                onCheckedChange = { viewModel.setCallConfirmation(it) }
            )

            SettingsSwitchItem(
                icon = Icons.Default.FormatListNumbered,
                title = stringResource(R.string.format_phone_numbers_title),
                subtitle = stringResource(R.string.automatically_format_phone),
                checked = formatPhoneNumbers,
                onCheckedChange = { viewModel.setFormatPhoneNumbers(it) }
            )

            HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))

            // Contact Management Section
            SettingsSectionHeader(text = stringResource(id = R.string.settings_contact_management))

            SettingsSwitchItem(
                icon = Icons.Default.FilterList,
                title = stringResource(R.string.show_only_contacts_with_phone_title),
                subtitle = stringResource(R.string.hide_contacts_without_phone),
                checked = showOnlyWithPhone,
                onCheckedChange = { viewModel.setShowOnlyWithPhone(it) }
            )

            SettingsSwitchItem(
                icon = Icons.Default.CopyAll,
                title = stringResource(R.string.show_duplicates_title),
                subtitle = stringResource(R.string.display_potentially_duplicate),
                checked = showDuplicates,
                onCheckedChange = { viewModel.setShowDuplicates(it) }
            )

            SettingsItem(
                icon = Icons.Default.CloudUpload,
                title = stringResource(R.string.import_contacts_title),
                subtitle = stringResource(R.string.import_from_vcard),
                onClick = { importLauncher.launch("text/x-vcard") },
                showProgress = importExportState is ImportExportState.Loading
            )

            SettingsItem(
                icon = Icons.Default.CloudDownload,
                title = stringResource(R.string.export_contacts_title),
                subtitle = stringResource(R.string.export_to_vcard),
                onClick = { exportLauncher.launch(viewModel.getDefaultExportFilename()) },
                showProgress = importExportState is ImportExportState.Loading
            )

            SettingsItem(
                icon = Icons.Default.MergeType,
                title = stringResource(R.string.merge_duplicate_contacts_title),
                subtitle = stringResource(R.string.find_merge_duplicate_entries),
                onClick = { viewModel.detectDuplicates() }
            )

            SettingsItem(
                icon = Icons.Default.Backup,
                title = stringResource(R.string.automatic_backups),
                subtitle = stringResource(R.string.schedule_regular_backups),
                onClick = { showBackupConfigDialog = true }
            )

            HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))

            // Privacy Section
            SettingsSectionHeader(text = stringResource(id = R.string.settings_privacy))

            SettingsItem(
                icon = Icons.Default.Lock,
                title = stringResource(R.string.privacy_policy),
                subtitle = stringResource(R.string.view_our_privacy_policy),
                onClick = { showPrivacyPolicyDialog = true }
            )

            HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))

            // About Section
            SettingsSectionHeader(text = stringResource(id = R.string.settings_about))

            SettingsItem(
                icon = Icons.Default.Info,
                title = stringResource(R.string.about),
                subtitle = stringResource(R.string.version),
                onClick = { showAboutDialog = true }
            )

            SettingsItem(
                icon = Icons.Default.Code,
                title = stringResource(R.string.open_source_licenses),
                subtitle = stringResource(R.string.view_third_party_licenses),
                onClick = { showLicensesDialog = true }
            )

            Spacer(modifier = Modifier.height(16.dp))
            }

            // Fixed AdMob Banner at the bottom with edge-to-edge support
            AdMobBanner(
                modifier = Modifier
                    .fillMaxWidth()
                    .windowInsetsPadding(WindowInsets.navigationBars)
            )
        }
    }

    // Language change animation removed - feature disabled
}

    // Color Theme Dialog
    if (showColorThemeDialog) {
        AlertDialog(
            onDismissRequest = { showColorThemeDialog = false },
            icon = {
                Icon(
                    Icons.Default.Palette,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary
                )
            },
            title = {
                Text(stringResource(R.string.select_color_theme), style = MaterialTheme.typography.headlineSmall)
            },
            text = {
                Column {
                    ColorTheme.values().forEach { theme ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable {
                                    viewModel.setColorTheme(theme)
                                    showColorThemeDialog = false
                                }
                                .padding(vertical = 12.dp, horizontal = 8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            // Color preview circle
                            Box(
                                modifier = Modifier
                                    .size(40.dp)
                                    .clip(CircleShape)
                                    .background(getThemePreviewColor(theme))
                            )

                            Spacer(modifier = Modifier.width(16.dp))

                            Text(
                                text = theme.name.lowercase().replaceFirstChar { it.uppercase() },
                                style = MaterialTheme.typography.bodyLarge,
                                modifier = Modifier.weight(1f)
                            )

                            if (theme == colorTheme) {
                                Icon(
                                    Icons.Default.Check,
                                    contentDescription = stringResource(R.string.selected),
                                    tint = MaterialTheme.colorScheme.primary
                                )
                            }
                        }
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = { showColorThemeDialog = false }) {
                    Text(stringResource(id = R.string.action_cancel))
                }
            },
            shape = MaterialTheme.shapes.large
        )
    }

    // Theme Mode Dialog
    if (showThemeModeDialog) {
        AlertDialog(
            onDismissRequest = { showThemeModeDialog = false },
            icon = {
                Icon(
                    Icons.Default.DarkMode,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary
                )
            },
            title = {
                Text(stringResource(R.string.select_theme_mode), style = MaterialTheme.typography.headlineSmall)
            },
            text = {
                Column {
                    ThemeMode.values().forEach { mode ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable {
                                    viewModel.setThemeMode(mode)
                                    showThemeModeDialog = false
                                }
                                .padding(vertical = 12.dp, horizontal = 8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                when (mode) {
                                    ThemeMode.LIGHT -> Icons.Default.LightMode
                                    ThemeMode.DARK -> Icons.Default.DarkMode
                                    ThemeMode.SYSTEM -> Icons.Default.SettingsBrightness
                                },
                                contentDescription = null,
                                modifier = Modifier.size(24.dp)
                            )

                            Spacer(modifier = Modifier.width(16.dp))

                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = when (mode) {
                                        ThemeMode.LIGHT -> "Light"
                                        ThemeMode.DARK -> "Dark"
                                        ThemeMode.SYSTEM -> "System default"
                                    },
                                    style = MaterialTheme.typography.bodyLarge
                                )
                                Text(
                                    text = when (mode) {
                                        ThemeMode.LIGHT -> "Use light theme"
                                        ThemeMode.DARK -> "Use dark theme"
                                        ThemeMode.SYSTEM -> "Follow system settings"
                                    },
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }

                            if (mode == themeMode) {
                                Icon(
                                    Icons.Default.Check,
                                    contentDescription = stringResource(R.string.selected),
                                    tint = MaterialTheme.colorScheme.primary
                                )
                            }
                        }
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = { showThemeModeDialog = false }) {
                    Text(stringResource(id = R.string.action_cancel))
                }
            },
            shape = MaterialTheme.shapes.large
        )
    }

    // Language Dialog - Using modern App Locale API
    if (showLanguageDialog) {
        AlertDialog(
            onDismissRequest = { showLanguageDialog = false },
            icon = {
                Icon(
                    Icons.Default.Language,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary
                )
            },
            title = {
                Text(stringResource(R.string.select_language), style = MaterialTheme.typography.headlineSmall)
            },
            text = {
                Column(
                    modifier = Modifier.verticalScroll(rememberScrollState())
                ) {
                    com.contacts.android.contacts.data.preferences.AppLanguage.values().forEach { language ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable {
                                    viewModel.setAppLanguage(language)
                                    showLanguageDialog = false
                                }
                                .padding(vertical = 12.dp, horizontal = 8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = language.displayName,
                                style = MaterialTheme.typography.bodyLarge,
                                modifier = Modifier.weight(1f)
                            )

                            if (language == appLanguage) {
                                Icon(
                                    Icons.Default.Check,
                                    contentDescription = stringResource(R.string.selected),
                                    tint = MaterialTheme.colorScheme.primary
                                )
                            }
                        }
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = { showLanguageDialog = false }) {
                    Text(stringResource(id = R.string.action_cancel))
                }
            },
            shape = MaterialTheme.shapes.large
        )
    }

    // Font Size Dialog
    if (showFontSizeDialog) {
        AlertDialog(
            onDismissRequest = { showFontSizeDialog = false },
            icon = {
                Icon(
                    Icons.Default.FormatSize,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary
                )
            },
            title = {
                Text(stringResource(R.string.select_font_size), style = MaterialTheme.typography.headlineSmall)
            },
            text = {
                Column {
                    com.contacts.android.contacts.data.preferences.FontSize.values().forEach { fontSize ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable {
                                    viewModel.setFontScale(fontSize.scale)
                                    showFontSizeDialog = false
                                }
                                .padding(vertical = 12.dp, horizontal = 8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = stringResource(fontSize.displayNameRes),
                                style = MaterialTheme.typography.bodyLarge,
                                modifier = Modifier.weight(1f)
                            )

                            if (fontSize == currentFontSize) {
                                Icon(
                                    Icons.Default.Check,
                                    contentDescription = stringResource(R.string.selected),
                                    tint = MaterialTheme.colorScheme.primary
                                )
                            }
                        }
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = { showFontSizeDialog = false }) {
                    Text(stringResource(id = R.string.action_cancel))
                }
            },
            shape = MaterialTheme.shapes.large
        )
    }

    // Default Tab Dialog
    if (showDefaultTabDialog) {
        AlertDialog(
            onDismissRequest = { showDefaultTabDialog = false },
            icon = {
                Icon(
                    Icons.Default.Tab,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary
                )
            },
            title = {
                Text(stringResource(R.string.default_tab_title), style = MaterialTheme.typography.headlineSmall)
            },
            text = {
                Column {
                    com.contacts.android.contacts.data.preferences.DefaultTab.values().forEach { tab ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable {
                                    viewModel.setDefaultTab(tab)
                                    showDefaultTabDialog = false
                                }
                                .padding(vertical = 12.dp, horizontal = 8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = stringResource(tab.displayNameRes),
                                style = MaterialTheme.typography.bodyLarge,
                                modifier = Modifier.weight(1f)
                            )

                            if (tab == defaultTab) {
                                Icon(
                                    Icons.Default.Check,
                                    contentDescription = stringResource(R.string.selected),
                                    tint = MaterialTheme.colorScheme.primary
                                )
                            }
                        }
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = { showDefaultTabDialog = false }) {
                    Text(stringResource(id = R.string.action_cancel))
                }
            },
            shape = MaterialTheme.shapes.large
        )
    }

    // Contact Click Action Dialog
    if (showContactClickActionDialog) {
        AlertDialog(
            onDismissRequest = { showContactClickActionDialog = false },
            icon = {
                Icon(
                    Icons.Default.TouchApp,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary
                )
            },
            title = {
                Text(stringResource(R.string.on_contact_clicked_title), style = MaterialTheme.typography.headlineSmall)
            },
            text = {
                Column {
                    com.contacts.android.contacts.data.preferences.ContactClickAction.values().forEach { action ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable {
                                    viewModel.setContactClickAction(action)
                                    showContactClickActionDialog = false
                                }
                                .padding(vertical = 12.dp, horizontal = 8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = stringResource(action.displayNameRes),
                                style = MaterialTheme.typography.bodyLarge,
                                modifier = Modifier.weight(1f)
                            )

                            if (action == contactClickAction) {
                                Icon(
                                    Icons.Default.Check,
                                    contentDescription = stringResource(R.string.selected),
                                    tint = MaterialTheme.colorScheme.primary
                                )
                            }
                        }
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = { showContactClickActionDialog = false }) {
                    Text(stringResource(id = R.string.action_cancel))
                }
            },
            shape = MaterialTheme.shapes.large
        )
    }

    // About Dialog
    if (showAboutDialog) {
        AlertDialog(
            onDismissRequest = { showAboutDialog = false },
            icon = {
                Icon(
                    Icons.Default.Info,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary
                )
            },
            title = {
                Text(
                    stringResource(R.string.app_name),
                    style = MaterialTheme.typography.headlineSmall
                )
            },
            text = {
                Column {
                    Text(stringResource(R.string.version))
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        stringResource(R.string.app_description),
                        style = MaterialTheme.typography.bodyMedium
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        stringResource(R.string.built_with),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            },
            confirmButton = {
                TextButton(onClick = { showAboutDialog = false }) {
                    Text(stringResource(id = R.string.action_close))
                }
            },
            shape = MaterialTheme.shapes.large
        )
    }

    // Merge Duplicates Dialog
    if (showMergeDuplicatesDialog && duplicatesState is DuplicatesState.Found) {
        MergeDuplicatesDialog(
            duplicateGroups = (duplicatesState as DuplicatesState.Found).duplicateGroups,
            onMerge = { contactIds, targetContactId ->
                viewModel.mergeContacts(contactIds, targetContactId)
            },
            onDismiss = {
                showMergeDuplicatesDialog = false
                viewModel.clearDuplicatesState()
            }
        )
    }

    // Backup Configuration Dialog
    if (showBackupConfigDialog) {
        BackupConfigDialog(
            isBackupScheduled = viewModel.isBackupScheduled(),
            onScheduleBackup = { frequency, includePhotos ->
                viewModel.scheduleAutomaticBackup(frequency, includePhotos)
            },
            onCancelBackup = {
                viewModel.cancelAutomaticBackup()
            },
            onCreateManualBackup = { includePhotos ->
                viewModel.createBackup(includePhotos)
            },
            onDismiss = {
                showBackupConfigDialog = false
            }
        )
    }

    // Privacy Policy Dialog
    if (showPrivacyPolicyDialog) {
        AlertDialog(
            onDismissRequest = { showPrivacyPolicyDialog = false },
            icon = {
                Icon(
                    Icons.Default.Lock,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary
                )
            },
            title = {
                Text(stringResource(R.string.privacy_policy_title), style = MaterialTheme.typography.headlineSmall)
            },
            text = {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .verticalScroll(rememberScrollState())
                ) {
                    Text(
                        text = buildString {
                            appendLine("Last updated: ${java.time.LocalDate.now()}")
                            appendLine()
                            appendLine("Data Collection and Storage")
                            appendLine("This app stores all contact information locally on your device. We do not collect, transmit, or share any of your personal data with third parties.")
                            appendLine()
                            appendLine("Permissions")
                            appendLine("• Contacts: Required to read and manage your contacts")
                            appendLine("• Storage: Required to import/export contacts and create backups")
                            appendLine("• Camera: Optional, for capturing contact photos")
                            appendLine()
                            appendLine("Your Data")
                            appendLine("• All data is stored locally on your device")
                            appendLine("• You have full control over your data")
                            appendLine("• Backups are stored in the app's private storage")
                            appendLine("• No data is transmitted to external servers")
                            appendLine()
                            appendLine("Security")
                            appendLine("Your contact data is protected by your device's security features. We recommend using device encryption and screen lock for additional security.")
                        },
                        style = MaterialTheme.typography.bodyMedium
                    )
                }
            },
            confirmButton = {
                TextButton(onClick = { showPrivacyPolicyDialog = false }) {
                    Text(stringResource(id = R.string.action_close))
                }
            },
            shape = MaterialTheme.shapes.large,
            modifier = Modifier.fillMaxWidth(0.95f)
        )
    }

    // Open Source Licenses Dialog
    if (showLicensesDialog) {
        AlertDialog(
            onDismissRequest = { showLicensesDialog = false },
            icon = {
                Icon(
                    Icons.Default.Code,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary
                )
            },
            title = {
                Text(stringResource(R.string.open_source_licenses), style = MaterialTheme.typography.headlineSmall)
            },
            text = {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxWidth()
                        .heightIn(max = 400.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    item {
                        LicenseItem(
                            libraryName = "Jetpack Compose",
                            license = "Apache License 2.0",
                            copyright = "Copyright 2021 The Android Open Source Project"
                        )
                    }
                    item {
                        LicenseItem(
                            libraryName = "Material3",
                            license = "Apache License 2.0",
                            copyright = "Copyright 2021 The Android Open Source Project"
                        )
                    }
                    item {
                        LicenseItem(
                            libraryName = "Hilt",
                            license = "Apache License 2.0",
                            copyright = "Copyright 2020 The Dagger Authors"
                        )
                    }
                    item {
                        LicenseItem(
                            libraryName = "Room",
                            license = "Apache License 2.0",
                            copyright = "Copyright 2018 The Android Open Source Project"
                        )
                    }
                    item {
                        LicenseItem(
                            libraryName = "Kotlin Coroutines",
                            license = "Apache License 2.0",
                            copyright = "Copyright 2016-2021 JetBrains s.r.o."
                        )
                    }
                    item {
                        LicenseItem(
                            libraryName = "Coil",
                            license = "Apache License 2.0",
                            copyright = "Copyright 2023 Coil Contributors"
                        )
                    }
                    item {
                        LicenseItem(
                            libraryName = "DataStore",
                            license = "Apache License 2.0",
                            copyright = "Copyright 2020 The Android Open Source Project"
                        )
                    }
                    item {
                        LicenseItem(
                            libraryName = "WorkManager",
                            license = "Apache License 2.0",
                            copyright = "Copyright 2018 The Android Open Source Project"
                        )
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = { showLicensesDialog = false }) {
                    Text(stringResource(id = R.string.action_close))
                }
            },
            shape = MaterialTheme.shapes.large,
            modifier = Modifier.fillMaxWidth(0.95f)
        )
    }

    // Visible Fields Management Dialog
    if (showVisibleFieldsDialog) {
        VisibleFieldsDialog(
            showContactThumbnails = showContactThumbnails,
            showPhoneNumbers = showPhoneNumbers,
            showPrivateContacts = showPrivateContacts,
            startNameWithSurname = startNameWithSurname,
            onShowContactThumbnailsChange = { viewModel.setShowContactThumbnails(it) },
            onShowPhoneNumbersChange = { viewModel.setShowPhoneNumbers(it) },
            onShowPrivateContactsChange = { viewModel.setShowPrivateContacts(it) },
            onStartNameWithSurnameChange = { viewModel.setStartNameWithSurname(it) },
            onDismiss = { showVisibleFieldsDialog = false }
        )
    }

    // Visible Tabs Management Dialog
    if (showVisibleTabsDialog) {
        VisibleTabsDialog(
            showDialpadButton = showDialpadButton,
            onShowDialpadButtonChange = { viewModel.setShowDialpadButton(it) },
            onDismiss = { showVisibleTabsDialog = false }
        )
    }
}

@Composable
private fun SettingsSectionHeader(text: String) {
    Text(
        text = text,
        style = MaterialTheme.typography.titleSmall,
        color = MaterialTheme.colorScheme.primary,
        modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
    )
}

@Composable
private fun SettingsItem(
    icon: ImageVector,
    title: String,
    subtitle: String,
    onClick: () -> Unit,
    showProgress: Boolean = false
) {
    Surface(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.size(24.dp)
            )

            Spacer(modifier = Modifier.width(16.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = subtitle,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            if (showProgress) {
                CircularProgressIndicator(
                    modifier = Modifier.size(24.dp),
                    strokeWidth = 2.dp
                )
            } else {
                Icon(
                    imageVector = Icons.Default.ChevronRight,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

@Composable
private fun SettingsSwitchItem(
    icon: ImageVector,
    title: String,
    subtitle: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit
) {
    Surface(
        onClick = { onCheckedChange(!checked) },
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.size(24.dp)
            )

            Spacer(modifier = Modifier.width(16.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = subtitle,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            Switch(
                checked = checked,
                onCheckedChange = onCheckedChange
            )
        }
    }
}

@Composable
private fun MergeDuplicatesDialog(
    duplicateGroups: List<com.contacts.android.contacts.domain.usecase.contact.DuplicateGroup>,
    onMerge: (contactIds: List<Long>, targetContactId: Long?) -> Unit,
    onDismiss: () -> Unit
) {
    var currentGroupIndex by remember { mutableStateOf(0) }
    val currentGroup = duplicateGroups.getOrNull(currentGroupIndex)
    var selectedTargetContactId by remember(currentGroupIndex) {
        mutableStateOf(currentGroup?.contacts?.firstOrNull()?.id)
    }

    if (currentGroup == null) {
        onDismiss()
        return
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        icon = {
            Icon(
                Icons.Default.MergeType,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary
            )
        },
        title = {
            Column {
                Text(
                    "Duplicate Contacts Found",
                    style = MaterialTheme.typography.headlineSmall
                )
                if (duplicateGroups.size > 1) {
                    Text(
                        "Group ${currentGroupIndex + 1} of ${duplicateGroups.size}",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(max = 400.dp)
            ) {
                // Show duplicate reason
                Text(
                    text = when (currentGroup.reason) {
                        com.contacts.android.contacts.domain.usecase.contact.DuplicateReason.SAME_NAME ->
                            "These contacts have the same name"
                        com.contacts.android.contacts.domain.usecase.contact.DuplicateReason.SAME_PHONE ->
                            "These contacts have the same phone number"
                        com.contacts.android.contacts.domain.usecase.contact.DuplicateReason.FUZZY_NAME ->
                            "These contacts have similar names"
                    },
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(bottom = 16.dp)
                )

                Text(
                    "Select the contact to keep as master:",
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = androidx.compose.ui.text.font.FontWeight.Medium,
                    modifier = Modifier.padding(bottom = 8.dp)
                )

                // List of duplicate contacts with radio buttons
                LazyColumn(
                    modifier = Modifier.weight(1f, fill = false),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(currentGroup.contacts) { contact ->
                        Surface(
                            modifier = Modifier.fillMaxWidth(),
                            shape = MaterialTheme.shapes.medium,
                            color = if (selectedTargetContactId == contact.id) {
                                MaterialTheme.colorScheme.primaryContainer
                            } else {
                                MaterialTheme.colorScheme.surfaceVariant
                            },
                            onClick = { selectedTargetContactId = contact.id }
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(12.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                RadioButton(
                                    selected = selectedTargetContactId == contact.id,
                                    onClick = { selectedTargetContactId = contact.id }
                                )

                                Spacer(modifier = Modifier.width(12.dp))

                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = contact.displayName,
                                        style = MaterialTheme.typography.bodyLarge,
                                        fontWeight = androidx.compose.ui.text.font.FontWeight.Medium
                                    )

                                    // Show phone numbers
                                    if (contact.phoneNumbers.isNotEmpty()) {
                                        Text(
                                            text = contact.phoneNumbers.joinToString(", ") { it.number },
                                            style = MaterialTheme.typography.bodySmall,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    }

                                    // Show emails
                                    if (contact.emails.isNotEmpty()) {
                                        Text(
                                            text = contact.emails.joinToString(", ") { it.email },
                                            style = MaterialTheme.typography.bodySmall,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    }

                                    // Show organization
                                    contact.organization?.let { org ->
                                        if (org.isNotBlank()) {
                                            Text(
                                                text = org,
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

                Spacer(modifier = Modifier.height(8.dp))

                // Info about merge
                Text(
                    text = stringResource(id = R.string.merge_contacts_description),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(top = 8.dp)
                )
            }
        },
        confirmButton = {
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                // Skip button (if multiple groups)
                if (duplicateGroups.size > 1 && currentGroupIndex < duplicateGroups.size - 1) {
                    TextButton(
                        onClick = {
                            currentGroupIndex++
                        }
                    ) {
                        Text(stringResource(R.string.skip))
                    }
                }

                // Merge button
                Button(
                    onClick = {
                        selectedTargetContactId?.let { targetId ->
                            val contactIds = currentGroup.contacts.map { it.id }
                            onMerge(contactIds, targetId)

                            // Move to next group or close
                            if (currentGroupIndex < duplicateGroups.size - 1) {
                                currentGroupIndex++
                            }
                        }
                    },
                    enabled = selectedTargetContactId != null
                ) {
                    Text(stringResource(R.string.merge))
                }
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(stringResource(id = R.string.action_cancel))
            }
        },
        shape = MaterialTheme.shapes.large,
        modifier = Modifier.fillMaxWidth(0.95f)
    )
}

@Composable
private fun BackupConfigDialog(
    isBackupScheduled: Boolean,
    onScheduleBackup: (frequency: com.contacts.android.contacts.data.worker.BackupFrequency, includePhotos: Boolean) -> Unit,
    onCancelBackup: () -> Unit,
    onCreateManualBackup: (includePhotos: Boolean) -> Unit,
    onDismiss: () -> Unit
) {
    var selectedFrequency by remember {
        mutableStateOf(com.contacts.android.contacts.data.worker.BackupFrequency.WEEKLY)
    }
    var includePhotos by remember { mutableStateOf(false) }

    AlertDialog(
        onDismissRequest = onDismiss,
        icon = {
            Icon(
                Icons.Default.Backup,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary
            )
        },
        title = {
            Text(
                "Backup Configuration",
                style = MaterialTheme.typography.headlineSmall
            )
        },
        text = {
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Current status
                Surface(
                    shape = MaterialTheme.shapes.medium,
                    color = if (isBackupScheduled) {
                        MaterialTheme.colorScheme.primaryContainer
                    } else {
                        MaterialTheme.colorScheme.surfaceVariant
                    }
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = if (isBackupScheduled) {
                                Icons.Default.CheckCircle
                            } else {
                                Icons.Default.Schedule
                            },
                            contentDescription = null,
                            tint = if (isBackupScheduled) {
                                MaterialTheme.colorScheme.primary
                            } else {
                                MaterialTheme.colorScheme.onSurfaceVariant
                            }
                        )
                        Spacer(modifier = Modifier.width(12.dp))
                        Text(
                            text = if (isBackupScheduled) {
                                "Automatic backup is enabled"
                            } else {
                                "Automatic backup is disabled"
                            },
                            style = MaterialTheme.typography.bodyMedium
                        )
                    }
                }

                HorizontalDivider()

                // Frequency selection
                Text(
                    text = stringResource(id = R.string.backup_frequency),
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = androidx.compose.ui.text.font.FontWeight.Medium
                )

                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    com.contacts.android.contacts.data.worker.BackupFrequency.values().forEach { frequency ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { selectedFrequency = frequency }
                                .padding(vertical = 8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            RadioButton(
                                selected = selectedFrequency == frequency,
                                onClick = { selectedFrequency = frequency }
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Column {
                                Text(
                                    text = frequency.name.lowercase().replaceFirstChar { it.uppercase() },
                                    style = MaterialTheme.typography.bodyLarge
                                )
                                Text(
                                    text = when (frequency) {
                                        com.contacts.android.contacts.data.worker.BackupFrequency.DAILY ->
                                            "Backup every day"
                                        com.contacts.android.contacts.data.worker.BackupFrequency.WEEKLY ->
                                            "Backup every 7 days"
                                        com.contacts.android.contacts.data.worker.BackupFrequency.MONTHLY ->
                                            "Backup every 30 days"
                                    },
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }
                }

                HorizontalDivider()

                // Include photos option
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { includePhotos = !includePhotos }
                        .padding(vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Checkbox(
                        checked = includePhotos,
                        onCheckedChange = { includePhotos = it }
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = stringResource(id = R.string.include_contact_photos),
                            style = MaterialTheme.typography.bodyLarge
                        )
                        Text(
                            text = stringResource(id = R.string.include_contact_photos_description),
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                HorizontalDivider()

                // Manual backup option
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Button(
                        onClick = {
                            onCreateManualBackup(includePhotos)
                            onDismiss()
                        },
                        modifier = Modifier.weight(1f),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.secondaryContainer,
                            contentColor = MaterialTheme.colorScheme.onSecondaryContainer
                        )
                    ) {
                        Icon(
                            Icons.Default.SaveAlt,
                            contentDescription = null,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(stringResource(R.string.backup_now))
                    }
                }

                // Info text
                Text(
                    text = stringResource(id = R.string.backup_storage_description),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        },
        confirmButton = {
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                // Cancel automatic backup button (if scheduled)
                if (isBackupScheduled) {
                    TextButton(
                        onClick = {
                            onCancelBackup()
                            onDismiss()
                        }
                    ) {
                        Text(stringResource(R.string.disable))
                    }
                }

                // Schedule/Update automatic backup button
                Button(
                    onClick = {
                        onScheduleBackup(selectedFrequency, includePhotos)
                        onDismiss()
                    }
                ) {
                    Text(if (isBackupScheduled) "Update" else "Enable")
                }
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(stringResource(id = R.string.action_close))
            }
        },
        shape = MaterialTheme.shapes.large,
        modifier = Modifier.fillMaxWidth(0.95f)
    )
}

@Composable
private fun LicenseItem(
    libraryName: String,
    license: String,
    copyright: String
) {
    Surface(
        shape = MaterialTheme.shapes.medium,
        color = MaterialTheme.colorScheme.surfaceVariant
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Text(
                text = libraryName,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = androidx.compose.ui.text.font.FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = license,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.primary
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = copyright,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
private fun VisibleFieldsDialog(
    showContactThumbnails: Boolean,
    showPhoneNumbers: Boolean,
    showPrivateContacts: Boolean,
    startNameWithSurname: Boolean,
    onShowContactThumbnailsChange: (Boolean) -> Unit,
    onShowPhoneNumbersChange: (Boolean) -> Unit,
    onShowPrivateContactsChange: (Boolean) -> Unit,
    onStartNameWithSurnameChange: (Boolean) -> Unit,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        icon = {
            Icon(
                Icons.Default.Checklist,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary
            )
        },
        title = {
            Text(stringResource(R.string.visible_contact_fields), style = MaterialTheme.typography.headlineSmall)
        },
        text = {
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text(
                    text = stringResource(id = R.string.choose_fields_to_display),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(bottom = 8.dp)
                )

                // Contact Thumbnails
                FieldToggleItem(
                    icon = Icons.Default.PhotoCamera,
                    title = stringResource(R.string.contact_photos),
                    description = stringResource(R.string.show_contact_thumbnails_lists),
                    checked = showContactThumbnails,
                    onCheckedChange = onShowContactThumbnailsChange
                )

                HorizontalDivider()

                // Phone Numbers
                FieldToggleItem(
                    icon = Icons.Default.Phone,
                    title = stringResource(R.string.phone_numbers_field),
                    description = stringResource(R.string.display_phone_numbers_below),
                    checked = showPhoneNumbers,
                    onCheckedChange = onShowPhoneNumbersChange
                )

                HorizontalDivider()

                // Private Contacts
                FieldToggleItem(
                    icon = Icons.Default.Visibility,
                    title = stringResource(R.string.private_contacts),
                    description = stringResource(R.string.show_contacts_marked_private),
                    checked = showPrivateContacts,
                    onCheckedChange = onShowPrivateContactsChange
                )

                HorizontalDivider()

                // Name Order
                FieldToggleItem(
                    icon = Icons.Default.Person,
                    title = stringResource(R.string.start_name_with_surname),
                    description = stringResource(R.string.display_last_name_first_format),
                    checked = startNameWithSurname,
                    onCheckedChange = onStartNameWithSurnameChange
                )
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text(stringResource(id = R.string.action_done))
            }
        },
        shape = MaterialTheme.shapes.large,
        modifier = Modifier.fillMaxWidth(0.95f)
    )
}

@Composable
private fun VisibleTabsDialog(
    showDialpadButton: Boolean,
    onShowDialpadButtonChange: (Boolean) -> Unit,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        icon = {
            Icon(
                Icons.Default.Tab,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary
            )
        },
        title = {
            Text(stringResource(R.string.visible_tabs), style = MaterialTheme.typography.headlineSmall)
        },
        text = {
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text(
                    text = stringResource(id = R.string.manage_visible_ui_elements),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(bottom = 8.dp)
                )

                // Main Tabs (Always visible - informational)
                Surface(
                    shape = MaterialTheme.shapes.medium,
                    color = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.5f)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(12.dp)
                    ) {
                        Text(
                            text = stringResource(id = R.string.main_navigation_tabs),
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = androidx.compose.ui.text.font.FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSecondaryContainer
                        )
                        Spacer(modifier = Modifier.height(8.dp))

                        TabInfoItem(
                            icon = Icons.Default.Contacts,
                            title = stringResource(R.string.nav_contacts),
                            alwaysVisible = true
                        )
                        TabInfoItem(
                            icon = Icons.Default.Star,
                            title = stringResource(R.string.favorites),
                            alwaysVisible = true
                        )
                        TabInfoItem(
                            icon = Icons.Default.History,
                            title = stringResource(R.string.recents_tab),
                            alwaysVisible = true
                        )
                        TabInfoItem(
                            icon = Icons.Default.Group,
                            title = stringResource(R.string.nav_groups),
                            alwaysVisible = true
                        )
                    }
                }

                HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))

                // Optional Features
                Text(
                    text = stringResource(id = R.string.optional_features),
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = androidx.compose.ui.text.font.FontWeight.Bold
                )

                FieldToggleItem(
                    icon = Icons.Default.Dialpad,
                    title = stringResource(R.string.dialpad_button),
                    description = stringResource(R.string.display_quick_dial_button),
                    checked = showDialpadButton,
                    onCheckedChange = onShowDialpadButtonChange
                )
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text(stringResource(id = R.string.action_done))
            }
        },
        shape = MaterialTheme.shapes.large,
        modifier = Modifier.fillMaxWidth(0.95f)
    )
}

@Composable
private fun FieldToggleItem(
    icon: ImageVector,
    title: String,
    description: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onCheckedChange(!checked) }
            .padding(vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.size(24.dp)
        )
        Spacer(modifier = Modifier.width(16.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = title,
                style = MaterialTheme.typography.bodyLarge
            )
            Text(
                text = description,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
        Spacer(modifier = Modifier.width(8.dp))
        Switch(
            checked = checked,
            onCheckedChange = onCheckedChange
        )
    }
}

@Composable
private fun TabInfoItem(
    icon: ImageVector,
    title: String,
    alwaysVisible: Boolean
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.onSecondaryContainer,
            modifier = Modifier.size(20.dp)
        )
        Spacer(modifier = Modifier.width(12.dp))
        Text(
            text = title,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSecondaryContainer,
            modifier = Modifier.weight(1f)
        )
        if (alwaysVisible) {
            Surface(
                shape = MaterialTheme.shapes.small,
                color = MaterialTheme.colorScheme.primary.copy(alpha = 0.2f)
            ) {
                Text(
                    text = stringResource(id = R.string.always_visible),
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp)
                )
            }
        }
    }
}
