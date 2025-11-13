package com.contacts.android.contactsjetpackcompose.presentation.screens.settings

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.contacts.android.contactsjetpackcompose.data.preferences.ColorTheme
import com.contacts.android.contactsjetpackcompose.data.preferences.ThemeMode
import com.contacts.android.contactsjetpackcompose.presentation.theme.getThemePreviewColor
import kotlinx.coroutines.delay

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
    val currentFontSize = com.contacts.android.contactsjetpackcompose.data.preferences.FontSize.values()
        .find { it.scale == fontScale } ?: com.contacts.android.contactsjetpackcompose.data.preferences.FontSize.MEDIUM

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
                    message = "No duplicate contacts found",
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
                    message = "Contacts merged successfully",
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
                title = { Text("Settings") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(
                            Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back"
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface,
                    titleContentColor = MaterialTheme.colorScheme.onSurface
                )
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .verticalScroll(rememberScrollState())
        ) {
            // Appearance Section
            SettingsSectionHeader(text = "Appearance")

            SettingsItem(
                icon = Icons.Default.Palette,
                title = "Color theme",
                subtitle = colorTheme.name.lowercase().replaceFirstChar { it.uppercase() },
                onClick = { showColorThemeDialog = true }
            )

            SettingsItem(
                icon = Icons.Default.DarkMode,
                title = "Theme mode",
                subtitle = when (themeMode) {
                    ThemeMode.LIGHT -> "Light"
                    ThemeMode.DARK -> "Dark"
                    ThemeMode.SYSTEM -> "System default"
                },
                onClick = { showThemeModeDialog = true }
            )

            // Language option temporarily disabled due to technical issues
            // Users can change language through system settings
            /*
            SettingsItem(
                icon = Icons.Default.Language,
                title = "Language",
                subtitle = appLanguage.displayName,
                onClick = { showLanguageDialog = true }
            )
            */

            SettingsItem(
                icon = Icons.Default.FormatSize,
                title = "Font size",
                subtitle = currentFontSize.displayName,
                onClick = { showFontSizeDialog = true }
            )

            HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))

            // Display Section
            SettingsSectionHeader(text = "Display")

            SettingsItem(
                icon = Icons.Default.Checklist,
                title = "Manage visible contact fields",
                subtitle = "Choose which fields to display",
                onClick = { showVisibleFieldsDialog = true }
            )

            SettingsItem(
                icon = Icons.Default.Tab,
                title = "Manage visible tabs",
                subtitle = "Select which tabs to show",
                onClick = { showVisibleTabsDialog = true }
            )

            SettingsSwitchItem(
                icon = Icons.Default.PhotoCamera,
                title = "Show contact thumbnails",
                subtitle = "Display contact photos in lists",
                checked = showContactThumbnails,
                onCheckedChange = { viewModel.setShowContactThumbnails(it) }
            )

            SettingsSwitchItem(
                icon = Icons.Default.Phone,
                title = "Show phone numbers",
                subtitle = "Display phone numbers below contact names",
                checked = showPhoneNumbers,
                onCheckedChange = { viewModel.setShowPhoneNumbers(it) }
            )

            SettingsSwitchItem(
                icon = Icons.Default.Person,
                title = "Start name with surname",
                subtitle = "Display last name first",
                checked = startNameWithSurname,
                onCheckedChange = { viewModel.setStartNameWithSurname(it) }
            )

            SettingsSwitchItem(
                icon = Icons.Default.Visibility,
                title = "Show private contacts",
                subtitle = "Display contacts marked as private",
                checked = showPrivateContacts,
                onCheckedChange = { viewModel.setShowPrivateContacts(it) }
            )

            SettingsSwitchItem(
                icon = Icons.Default.Fullscreen,
                title = "Edge-to-edge display",
                subtitle = "Extend content to screen edges",
                checked = edgeToEdgeDisplay,
                onCheckedChange = { viewModel.setEdgeToEdgeDisplay(it) }
            )

            HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))

            // Behavior Section
            SettingsSectionHeader(text = "Behavior")

            SettingsItem(
                icon = Icons.Default.Tab,
                title = "Default tab",
                subtitle = defaultTab.displayName,
                onClick = { showDefaultTabDialog = true }
            )

            SettingsItem(
                icon = Icons.Default.TouchApp,
                title = "On contact clicked",
                subtitle = contactClickAction.displayName,
                onClick = { showContactClickActionDialog = true }
            )

            SettingsSwitchItem(
                icon = Icons.Default.Dialpad,
                title = "Show dialpad button",
                subtitle = "Display quick dial button on Contacts tab",
                checked = showDialpadButton,
                onCheckedChange = { viewModel.setShowDialpadButton(it) }
            )

            SettingsSwitchItem(
                icon = Icons.Default.Call,
                title = "Call confirmation",
                subtitle = "Ask before making a phone call",
                checked = callConfirmation,
                onCheckedChange = { viewModel.setCallConfirmation(it) }
            )

            SettingsSwitchItem(
                icon = Icons.Default.FormatListNumbered,
                title = "Format phone numbers",
                subtitle = "Automatically format phone numbers",
                checked = formatPhoneNumbers,
                onCheckedChange = { viewModel.setFormatPhoneNumbers(it) }
            )

            HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))

            // Contact Management Section
            SettingsSectionHeader(text = "Contact Management")

            SettingsSwitchItem(
                icon = Icons.Default.FilterList,
                title = "Show only contacts with phone",
                subtitle = "Hide contacts without phone numbers",
                checked = showOnlyWithPhone,
                onCheckedChange = { viewModel.setShowOnlyWithPhone(it) }
            )

            SettingsSwitchItem(
                icon = Icons.Default.CopyAll,
                title = "Show duplicates",
                subtitle = "Display potentially duplicate contacts",
                checked = showDuplicates,
                onCheckedChange = { viewModel.setShowDuplicates(it) }
            )

            SettingsItem(
                icon = Icons.Default.CloudUpload,
                title = "Import contacts",
                subtitle = "Import from vCard file",
                onClick = { importLauncher.launch("text/x-vcard") },
                showProgress = importExportState is ImportExportState.Loading
            )

            SettingsItem(
                icon = Icons.Default.CloudDownload,
                title = "Export contacts",
                subtitle = "Export to vCard file",
                onClick = { exportLauncher.launch(viewModel.getDefaultExportFilename()) },
                showProgress = importExportState is ImportExportState.Loading
            )

            SettingsItem(
                icon = Icons.Default.MergeType,
                title = "Merge duplicate contacts",
                subtitle = "Find and merge duplicate entries",
                onClick = { viewModel.detectDuplicates() }
            )

            SettingsItem(
                icon = Icons.Default.Backup,
                title = "Automatic backups",
                subtitle = "Schedule regular contact backups",
                onClick = { showBackupConfigDialog = true }
            )

            HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))

            // Privacy Section
            SettingsSectionHeader(text = "Privacy")

            SettingsItem(
                icon = Icons.Default.Lock,
                title = "Privacy policy",
                subtitle = "View our privacy policy",
                onClick = { showPrivacyPolicyDialog = true }
            )

            HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))

            // About Section
            SettingsSectionHeader(text = "About")

            SettingsItem(
                icon = Icons.Default.Info,
                title = "About",
                subtitle = "Version 1.0.0",
                onClick = { showAboutDialog = true }
            )

            SettingsItem(
                icon = Icons.Default.Code,
                title = "Open source licenses",
                subtitle = "View third-party licenses",
                onClick = { showLicensesDialog = true }
            )

            Spacer(modifier = Modifier.height(16.dp))
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
                Text("Select Color Theme", style = MaterialTheme.typography.headlineSmall)
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
                                    contentDescription = "Selected",
                                    tint = MaterialTheme.colorScheme.primary
                                )
                            }
                        }
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = { showColorThemeDialog = false }) {
                    Text("Cancel")
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
                Text("Select Theme Mode", style = MaterialTheme.typography.headlineSmall)
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
                                    contentDescription = "Selected",
                                    tint = MaterialTheme.colorScheme.primary
                                )
                            }
                        }
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = { showThemeModeDialog = false }) {
                    Text("Cancel")
                }
            },
            shape = MaterialTheme.shapes.large
        )
    }

    // Language Dialog - Disabled
    // if (showLanguageDialog) { ... }

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
                Text("Select Font Size", style = MaterialTheme.typography.headlineSmall)
            },
            text = {
                Column {
                    com.contacts.android.contactsjetpackcompose.data.preferences.FontSize.values().forEach { fontSize ->
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
                                text = fontSize.displayName,
                                style = MaterialTheme.typography.bodyLarge,
                                modifier = Modifier.weight(1f)
                            )

                            if (fontSize == currentFontSize) {
                                Icon(
                                    Icons.Default.Check,
                                    contentDescription = "Selected",
                                    tint = MaterialTheme.colorScheme.primary
                                )
                            }
                        }
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = { showFontSizeDialog = false }) {
                    Text("Cancel")
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
                Text("Default Tab", style = MaterialTheme.typography.headlineSmall)
            },
            text = {
                Column {
                    com.contacts.android.contactsjetpackcompose.data.preferences.DefaultTab.values().forEach { tab ->
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
                                text = tab.displayName,
                                style = MaterialTheme.typography.bodyLarge,
                                modifier = Modifier.weight(1f)
                            )

                            if (tab == defaultTab) {
                                Icon(
                                    Icons.Default.Check,
                                    contentDescription = "Selected",
                                    tint = MaterialTheme.colorScheme.primary
                                )
                            }
                        }
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = { showDefaultTabDialog = false }) {
                    Text("Cancel")
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
                Text("On Contact Clicked", style = MaterialTheme.typography.headlineSmall)
            },
            text = {
                Column {
                    com.contacts.android.contactsjetpackcompose.data.preferences.ContactClickAction.values().forEach { action ->
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
                                text = action.displayName,
                                style = MaterialTheme.typography.bodyLarge,
                                modifier = Modifier.weight(1f)
                            )

                            if (action == contactClickAction) {
                                Icon(
                                    Icons.Default.Check,
                                    contentDescription = "Selected",
                                    tint = MaterialTheme.colorScheme.primary
                                )
                            }
                        }
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = { showContactClickActionDialog = false }) {
                    Text("Cancel")
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
                    "Contacts App",
                    style = MaterialTheme.typography.headlineSmall
                )
            },
            text = {
                Column {
                    Text("Version 1.0.0")
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        "A modern contacts management app built with Jetpack Compose",
                        style = MaterialTheme.typography.bodyMedium
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        "Built with Clean Architecture and Material Design 3",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            },
            confirmButton = {
                TextButton(onClick = { showAboutDialog = false }) {
                    Text("Close")
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
                Text("Privacy Policy", style = MaterialTheme.typography.headlineSmall)
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
                    Text("Close")
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
                Text("Open Source Licenses", style = MaterialTheme.typography.headlineSmall)
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
                    Text("Close")
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
    duplicateGroups: List<com.contacts.android.contactsjetpackcompose.domain.usecase.contact.DuplicateGroup>,
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
                        com.contacts.android.contactsjetpackcompose.domain.usecase.contact.DuplicateReason.SAME_NAME ->
                            "These contacts have the same name"
                        com.contacts.android.contactsjetpackcompose.domain.usecase.contact.DuplicateReason.SAME_PHONE ->
                            "These contacts have the same phone number"
                        com.contacts.android.contactsjetpackcompose.domain.usecase.contact.DuplicateReason.FUZZY_NAME ->
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
                    text = "All unique information from other contacts will be merged into the selected contact.",
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
                        Text("Skip")
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
                    Text("Merge")
                }
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        },
        shape = MaterialTheme.shapes.large,
        modifier = Modifier.fillMaxWidth(0.95f)
    )
}

@Composable
private fun BackupConfigDialog(
    isBackupScheduled: Boolean,
    onScheduleBackup: (frequency: com.contacts.android.contactsjetpackcompose.data.worker.BackupFrequency, includePhotos: Boolean) -> Unit,
    onCancelBackup: () -> Unit,
    onCreateManualBackup: (includePhotos: Boolean) -> Unit,
    onDismiss: () -> Unit
) {
    var selectedFrequency by remember {
        mutableStateOf(com.contacts.android.contactsjetpackcompose.data.worker.BackupFrequency.WEEKLY)
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
                    text = "Backup Frequency",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = androidx.compose.ui.text.font.FontWeight.Medium
                )

                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    com.contacts.android.contactsjetpackcompose.data.worker.BackupFrequency.values().forEach { frequency ->
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
                                        com.contacts.android.contactsjetpackcompose.data.worker.BackupFrequency.DAILY ->
                                            "Backup every day"
                                        com.contacts.android.contactsjetpackcompose.data.worker.BackupFrequency.WEEKLY ->
                                            "Backup every 7 days"
                                        com.contacts.android.contactsjetpackcompose.data.worker.BackupFrequency.MONTHLY ->
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
                            text = "Include contact photos",
                            style = MaterialTheme.typography.bodyLarge
                        )
                        Text(
                            text = "Photos will increase backup file size",
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
                        Text("Backup Now")
                    }
                }

                // Info text
                Text(
                    text = "Backups are stored in the app's private storage and include all contact information.",
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
                        Text("Disable")
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
                Text("Close")
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
            Text("Visible Contact Fields", style = MaterialTheme.typography.headlineSmall)
        },
        text = {
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text(
                    text = "Choose which fields to display in contact lists and details:",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(bottom = 8.dp)
                )

                // Contact Thumbnails
                FieldToggleItem(
                    icon = Icons.Default.PhotoCamera,
                    title = "Contact photos",
                    description = "Show contact thumbnails in lists",
                    checked = showContactThumbnails,
                    onCheckedChange = onShowContactThumbnailsChange
                )

                HorizontalDivider()

                // Phone Numbers
                FieldToggleItem(
                    icon = Icons.Default.Phone,
                    title = "Phone numbers",
                    description = "Display phone numbers below contact names",
                    checked = showPhoneNumbers,
                    onCheckedChange = onShowPhoneNumbersChange
                )

                HorizontalDivider()

                // Private Contacts
                FieldToggleItem(
                    icon = Icons.Default.Visibility,
                    title = "Private contacts",
                    description = "Show contacts marked as private",
                    checked = showPrivateContacts,
                    onCheckedChange = onShowPrivateContactsChange
                )

                HorizontalDivider()

                // Name Order
                FieldToggleItem(
                    icon = Icons.Default.Person,
                    title = "Start name with surname",
                    description = "Display last name first (e.g., Doe, John)",
                    checked = startNameWithSurname,
                    onCheckedChange = onStartNameWithSurnameChange
                )
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text("Done")
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
            Text("Visible Tabs", style = MaterialTheme.typography.headlineSmall)
        },
        text = {
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text(
                    text = "Manage which tabs and buttons are visible in the app:",
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
                            text = "Main Navigation Tabs",
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = androidx.compose.ui.text.font.FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSecondaryContainer
                        )
                        Spacer(modifier = Modifier.height(8.dp))

                        TabInfoItem(
                            icon = Icons.Default.Contacts,
                            title = "Contacts",
                            alwaysVisible = true
                        )
                        TabInfoItem(
                            icon = Icons.Default.Star,
                            title = "Favorites",
                            alwaysVisible = true
                        )
                        TabInfoItem(
                            icon = Icons.Default.History,
                            title = "Recents",
                            alwaysVisible = true
                        )
                        TabInfoItem(
                            icon = Icons.Default.Group,
                            title = "Groups",
                            alwaysVisible = true
                        )
                    }
                }

                HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))

                // Optional Features
                Text(
                    text = "Optional Features",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = androidx.compose.ui.text.font.FontWeight.Bold
                )

                FieldToggleItem(
                    icon = Icons.Default.Dialpad,
                    title = "Dialpad button",
                    description = "Show quick dial button on Contacts tab",
                    checked = showDialpadButton,
                    onCheckedChange = onShowDialpadButtonChange
                )
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text("Done")
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
                    text = "Always visible",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp)
                )
            }
        }
    }
}
