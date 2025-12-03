package com.contacts.android.contacts.presentation.screens.settings

import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.contacts.android.contacts.data.preferences.ColorTheme
import com.contacts.android.contacts.data.preferences.ThemeMode
import com.contacts.android.contacts.data.preferences.UserPreferences
import com.contacts.android.contacts.data.worker.BackupFrequency
import com.contacts.android.contacts.data.worker.BackupScheduler
import com.contacts.android.contacts.domain.usecase.backup.CreateBackupUseCase
import com.contacts.android.contacts.domain.usecase.backup.RestoreBackupUseCase
import com.contacts.android.contacts.domain.usecase.contact.DetectDuplicatesUseCase
import com.contacts.android.contacts.domain.usecase.contact.DuplicateGroup
import com.contacts.android.contacts.domain.usecase.contact.MergeContactsUseCase
import com.contacts.android.contacts.domain.usecase.vcf.ExportContactsToVcfUseCase
import com.contacts.android.contacts.domain.usecase.vcf.ImportContactsFromVcfUseCase
import com.contacts.android.contacts.presentation.util.LocaleHelper
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.io.File
import javax.inject.Inject

@HiltViewModel
class SettingsViewModel @Inject constructor(
    private val userPreferences: UserPreferences,
    private val importContactsUseCase: ImportContactsFromVcfUseCase,
    private val exportContactsUseCase: ExportContactsToVcfUseCase,
    private val detectDuplicatesUseCase: DetectDuplicatesUseCase,
    private val mergeContactsUseCase: MergeContactsUseCase,
    private val createBackupUseCase: CreateBackupUseCase,
    private val restoreBackupUseCase: RestoreBackupUseCase,
    private val backupScheduler: BackupScheduler
) : ViewModel() {

    // State for language change animation
    private val _isLanguageChanging = MutableStateFlow(false)
    val isLanguageChanging: StateFlow<Boolean> = _isLanguageChanging.asStateFlow()

    // State for import/export operations
    private val _importExportState = MutableStateFlow<ImportExportState>(ImportExportState.Idle)
    val importExportState: StateFlow<ImportExportState> = _importExportState.asStateFlow()

    // State for duplicate detection
    private val _duplicatesState = MutableStateFlow<DuplicatesState>(DuplicatesState.Idle)
    val duplicatesState: StateFlow<DuplicatesState> = _duplicatesState.asStateFlow()

    // State for backup operations
    private val _backupState = MutableStateFlow<BackupState>(BackupState.Idle)
    val backupState: StateFlow<BackupState> = _backupState.asStateFlow()

    // Observe all preference values as StateFlow
    val themeMode: StateFlow<ThemeMode> = userPreferences.themeMode
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.Eagerly,
            initialValue = ThemeMode.SYSTEM
        )

    val colorTheme: StateFlow<ColorTheme> = userPreferences.colorTheme
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.Eagerly,
            initialValue = ColorTheme.BLUE
        )

    val appLanguage: StateFlow<com.contacts.android.contacts.data.preferences.AppLanguage> =
        userPreferences.appLanguage.stateIn(
            scope = viewModelScope,
            started = SharingStarted.Eagerly,
            initialValue = com.contacts.android.contacts.data.preferences.AppLanguage.ENGLISH
        )

    val showContactThumbnails: StateFlow<Boolean> = userPreferences.showContactThumbnails
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.Eagerly,
            initialValue = true
        )

    val showPhoneNumbers: StateFlow<Boolean> = userPreferences.showPhoneNumbers
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.Eagerly,
            initialValue = true
        )

    val showOnlyWithPhone: StateFlow<Boolean> = userPreferences.showOnlyWithPhone
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.Eagerly,
            initialValue = false
        )

    val showDuplicates: StateFlow<Boolean> = userPreferences.showDuplicates
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.Eagerly,
            initialValue = false
        )

    val fontScale: StateFlow<Float> = userPreferences.fontScale
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.Eagerly,
            initialValue = 1.0f
        )

    val defaultTab: StateFlow<com.contacts.android.contacts.data.preferences.DefaultTab> =
        userPreferences.defaultTab.stateIn(
            scope = viewModelScope,
            started = SharingStarted.Eagerly,
            initialValue = com.contacts.android.contacts.data.preferences.DefaultTab.CONTACTS
        )

    val contactClickAction: StateFlow<com.contacts.android.contacts.data.preferences.ContactClickAction> =
        userPreferences.contactClickAction.stateIn(
            scope = viewModelScope,
            started = SharingStarted.Eagerly,
            initialValue = com.contacts.android.contacts.data.preferences.ContactClickAction.VIEW_DETAILS
        )

    val edgeToEdgeDisplay: StateFlow<Boolean> = userPreferences.edgeToEdgeDisplay
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.Eagerly,
            initialValue = true
        )

    val startNameWithSurname: StateFlow<Boolean> = userPreferences.startNameWithSurname
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.Eagerly,
            initialValue = false
        )

    val showPrivateContacts: StateFlow<Boolean> = userPreferences.showPrivateContacts
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.Eagerly,
            initialValue = true
        )

    val showDialpadButton: StateFlow<Boolean> = userPreferences.showDialpadButton
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.Eagerly,
            initialValue = true
        )

    val formatPhoneNumbers: StateFlow<Boolean> = userPreferences.formatPhoneNumbers
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.Eagerly,
            initialValue = true
        )

    val callConfirmation: StateFlow<Boolean> = userPreferences.callConfirmation
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.Eagerly,
            initialValue = false
        )

    val swipeDeleteConfirmation: StateFlow<Boolean> = userPreferences.swipeDeleteConfirmation
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.Eagerly,
            initialValue = true  // Default: true for safety
        )

    val isPremium: StateFlow<Boolean> = userPreferences.isPremium
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.Eagerly,
            initialValue = false
        )

    // Functions to update preferences
    fun setThemeMode(mode: ThemeMode) {
        viewModelScope.launch {
            userPreferences.setThemeMode(mode)
        }
    }

    fun setColorTheme(theme: ColorTheme) {
        viewModelScope.launch {
            userPreferences.setColorTheme(theme)
        }
    }

    /**
     * Sets the app language using the modern App Locale API
     * This triggers instant UI update without requiring app restart
     *
     * How it works:
     * 1. Calls LocaleHelper.setLocale() which uses AppCompatDelegate.setApplicationLocales()
     * 2. This triggers a configuration change that Compose reacts to automatically
     * 3. All strings and layouts update instantly - no restart needed!
     * 4. On Android 13+, this also updates the system per-app language setting
     */
    fun setAppLanguage(language: com.contacts.android.contacts.data.preferences.AppLanguage) {
        viewModelScope.launch {
            // Save preference to DataStore (for persistence across app restarts)
            userPreferences.setAppLanguage(language)

            // Apply locale change using App Locale API (instant update, no restart needed!)
            LocaleHelper.setLocale(language.locale)

            // Note: No need for _isLanguageChanging state or activity recreation
            // The App Locale API handles this gracefully with automatic recomposition
        }
    }

    /**
     * Resets language to system default
     */
    fun resetToSystemLanguage() {
        viewModelScope.launch {
            // Clear app-specific locale and revert to system default
            LocaleHelper.clearLocale()

            // Optionally update preference to reflect system default
            // (You might want to add a "SYSTEM_DEFAULT" option to AppLanguage enum)
        }
    }

    fun resetLanguageChangingState() {
        _isLanguageChanging.value = false
    }

    fun setShowContactThumbnails(show: Boolean) {
        viewModelScope.launch {
            userPreferences.setShowContactThumbnails(show)
        }
    }

    fun setShowPhoneNumbers(show: Boolean) {
        viewModelScope.launch {
            userPreferences.setShowPhoneNumbers(show)
        }
    }

    fun setShowOnlyWithPhone(show: Boolean) {
        viewModelScope.launch {
            userPreferences.setShowOnlyWithPhone(show)
        }
    }

    fun setShowDuplicates(show: Boolean) {
        viewModelScope.launch {
            userPreferences.setShowDuplicates(show)
        }
    }

    fun setFontScale(scale: Float) {
        viewModelScope.launch {
            userPreferences.setFontScale(scale)
        }
    }

    fun setDefaultTab(tab: com.contacts.android.contacts.data.preferences.DefaultTab) {
        viewModelScope.launch {
            userPreferences.setDefaultTab(tab)
        }
    }

    fun setContactClickAction(action: com.contacts.android.contacts.data.preferences.ContactClickAction) {
        viewModelScope.launch {
            userPreferences.setContactClickAction(action)
        }
    }

    fun setEdgeToEdgeDisplay(enabled: Boolean) {
        viewModelScope.launch {
            userPreferences.setEdgeToEdgeDisplay(enabled)
        }
    }

    fun setStartNameWithSurname(enabled: Boolean) {
        viewModelScope.launch {
            userPreferences.setStartNameWithSurname(enabled)
        }
    }

    fun setShowPrivateContacts(show: Boolean) {
        viewModelScope.launch {
            userPreferences.setShowPrivateContacts(show)
        }
    }

    fun setShowDialpadButton(show: Boolean) {
        viewModelScope.launch {
            userPreferences.setShowDialpadButton(show)
        }
    }

    fun setFormatPhoneNumbers(format: Boolean) {
        viewModelScope.launch {
            userPreferences.setFormatPhoneNumbers(format)
        }
    }

    fun setCallConfirmation(enabled: Boolean) {
        viewModelScope.launch {
            userPreferences.setCallConfirmation(enabled)
        }
    }

    fun setSwipeDeleteConfirmation(enabled: Boolean) {
        viewModelScope.launch {
            userPreferences.setSwipeDeleteConfirmation(enabled)
        }
    }

    // Import/Export functions
    fun importContacts(uri: Uri) {
        viewModelScope.launch {
            _importExportState.value = ImportExportState.Loading
            importContactsUseCase(uri)
                .onSuccess { count ->
                    _importExportState.value = ImportExportState.Success(
                        "Successfully imported $count contact${if (count != 1) "s" else ""}"
                    )
                }
                .onFailure { error ->
                    _importExportState.value = ImportExportState.Error(
                        error.message ?: "Failed to import contacts"
                    )
                }
        }
    }

    fun exportContacts(uri: Uri, includePhotos: Boolean = false) {
        viewModelScope.launch {
            _importExportState.value = ImportExportState.Loading
            exportContactsUseCase.exportAll(uri, includePhotos)
                .onSuccess { count ->
                    _importExportState.value = ImportExportState.Success(
                        "Successfully exported $count contact${if (count != 1) "s" else ""}"
                    )
                }
                .onFailure { error ->
                    _importExportState.value = ImportExportState.Error(
                        error.message ?: "Failed to export contacts"
                    )
                }
        }
    }

    fun getDefaultExportFilename(): String {
        return exportContactsUseCase.getDefaultFilename()
    }

    fun clearImportExportState() {
        _importExportState.value = ImportExportState.Idle
    }

    // Duplicate detection and merge functions
    fun detectDuplicates() {
        viewModelScope.launch {
            _duplicatesState.value = DuplicatesState.Loading
            detectDuplicatesUseCase()
                .onSuccess { duplicateGroups ->
                    if (duplicateGroups.isEmpty()) {
                        _duplicatesState.value = DuplicatesState.NoDuplicates
                    } else {
                        _duplicatesState.value = DuplicatesState.Found(duplicateGroups)
                    }
                }
                .onFailure { error ->
                    _duplicatesState.value = DuplicatesState.Error(
                        error.message ?: "Failed to detect duplicates"
                    )
                }
        }
    }

    fun mergeContacts(contactIds: List<Long>, targetContactId: Long? = null) {
        viewModelScope.launch {
            _duplicatesState.value = DuplicatesState.Loading
            mergeContactsUseCase(contactIds, targetContactId)
                .onSuccess {
                    _duplicatesState.value = DuplicatesState.MergeSuccess
                    // Re-detect duplicates after merge
                    detectDuplicates()
                }
                .onFailure { error ->
                    _duplicatesState.value = DuplicatesState.Error(
                        error.message ?: "Failed to merge contacts"
                    )
                }
        }
    }

    fun clearDuplicatesState() {
        _duplicatesState.value = DuplicatesState.Idle
    }

    // Backup functions
    fun createBackup(includePhotos: Boolean = false) {
        viewModelScope.launch {
            _backupState.value = BackupState.Loading
            createBackupUseCase(includePhotos)
                .onSuccess { backupPath ->
                    _backupState.value = BackupState.Success("Backup created successfully")
                }
                .onFailure { error ->
                    _backupState.value = BackupState.Error(
                        error.message ?: "Failed to create backup"
                    )
                }
        }
    }

    fun restoreBackup(backupFile: File) {
        viewModelScope.launch {
            _backupState.value = BackupState.Loading
            restoreBackupUseCase(backupFile)
                .onSuccess { count ->
                    _backupState.value = BackupState.Success(
                        "Restored $count contact${if (count != 1) "s" else ""}"
                    )
                }
                .onFailure { error ->
                    _backupState.value = BackupState.Error(
                        error.message ?: "Failed to restore backup"
                    )
                }
        }
    }

    fun getBackupFiles(): List<File> {
        return createBackupUseCase.getBackupFiles()
    }

    fun scheduleAutomaticBackup(frequency: BackupFrequency, includePhotos: Boolean = false) {
        backupScheduler.scheduleBackup(frequency, includePhotos)
        _backupState.value = BackupState.Success("Automatic backup scheduled")
    }

    fun cancelAutomaticBackup() {
        backupScheduler.cancelBackup()
        _backupState.value = BackupState.Success("Automatic backup canceled")
    }

    fun isBackupScheduled(): Boolean {
        return backupScheduler.isBackupScheduled()
    }

    fun clearBackupState() {
        _backupState.value = BackupState.Idle
    }

    /**
     * Clear premium status for testing purposes
     * ONLY USE FOR LOCAL TESTING - REMOVE IN PRODUCTION
     */
    fun clearPremiumStatusForTesting() {
        viewModelScope.launch {
            userPreferences.clearPremiumStatus()
        }
    }
}

sealed interface ImportExportState {
    object Idle : ImportExportState
    object Loading : ImportExportState
    data class Success(val message: String) : ImportExportState
    data class Error(val message: String) : ImportExportState
}

sealed interface DuplicatesState {
    object Idle : DuplicatesState
    object Loading : DuplicatesState
    object NoDuplicates : DuplicatesState
    data class Found(val duplicateGroups: List<DuplicateGroup>) : DuplicatesState
    object MergeSuccess : DuplicatesState
    data class Error(val message: String) : DuplicatesState
}

sealed interface BackupState {
    object Idle : BackupState
    object Loading : BackupState
    data class Success(val message: String) : BackupState
    data class Error(val message: String) : BackupState
}
