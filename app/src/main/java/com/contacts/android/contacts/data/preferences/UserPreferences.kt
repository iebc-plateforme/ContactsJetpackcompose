package com.contacts.android.contacts.data.preferences

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.*
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "user_preferences")

@Singleton
class UserPreferences @Inject constructor(
    private val context: Context
) {
    private val dataStore = context.dataStore

    companion object {
        private val THEME_MODE_KEY = stringPreferencesKey("theme_mode")
        private val COLOR_THEME_KEY = stringPreferencesKey("color_theme")
        private val APP_LANGUAGE_KEY = stringPreferencesKey("app_language")
        private val SHOW_CONTACT_THUMBNAILS_KEY = booleanPreferencesKey("show_contact_thumbnails")
        private val SHOW_PHONE_NUMBERS_KEY = booleanPreferencesKey("show_phone_numbers")
        private val SHOW_ONLY_WITH_PHONE_KEY = booleanPreferencesKey("show_only_with_phone")
        private val SHOW_DUPLICATES_KEY = booleanPreferencesKey("show_duplicates")
        private val FONT_SCALE_KEY = floatPreferencesKey("font_scale")
        private val DEFAULT_TAB_KEY = stringPreferencesKey("default_tab")
        private val CONTACT_CLICK_ACTION_KEY = stringPreferencesKey("contact_click_action")
        private val EDGE_TO_EDGE_DISPLAY_KEY = booleanPreferencesKey("edge_to_edge_display")
        private val START_NAME_WITH_SURNAME_KEY = booleanPreferencesKey("start_name_with_surname")
        private val SHOW_PRIVATE_CONTACTS_KEY = booleanPreferencesKey("show_private_contacts")
        private val SHOW_DIALPAD_BUTTON_KEY = booleanPreferencesKey("show_dialpad_button")
        private val FORMAT_PHONE_NUMBERS_KEY = booleanPreferencesKey("format_phone_numbers")
        private val CALL_CONFIRMATION_KEY = booleanPreferencesKey("call_confirmation")
        private val SWIPE_DELETE_CONFIRMATION_KEY = booleanPreferencesKey("swipe_delete_confirmation")
        private val SORT_ORDER_KEY = intPreferencesKey("sort_order")
        private val CONTACT_FILTER_KEY = stringPreferencesKey("contact_filter")
    }

    val themeMode: Flow<ThemeMode> = dataStore.data.map { preferences ->
        ThemeMode.valueOf(preferences[THEME_MODE_KEY] ?: ThemeMode.SYSTEM.name)
    }

    val colorTheme: Flow<ColorTheme> = dataStore.data.map { preferences ->
        ColorTheme.valueOf(preferences[COLOR_THEME_KEY] ?: ColorTheme.BLUE.name)
    }

    val appLanguage: Flow<AppLanguage> = dataStore.data.map { preferences ->
        AppLanguage.valueOf(preferences[APP_LANGUAGE_KEY] ?: AppLanguage.ENGLISH.name)
    }

    val showContactThumbnails: Flow<Boolean> = dataStore.data.map { preferences ->
        preferences[SHOW_CONTACT_THUMBNAILS_KEY] ?: true
    }

    val showPhoneNumbers: Flow<Boolean> = dataStore.data.map { preferences ->
        preferences[SHOW_PHONE_NUMBERS_KEY] ?: true
    }

    val showOnlyWithPhone: Flow<Boolean> = dataStore.data.map { preferences ->
        preferences[SHOW_ONLY_WITH_PHONE_KEY] ?: false
    }

    val showDuplicates: Flow<Boolean> = dataStore.data.map { preferences ->
        preferences[SHOW_DUPLICATES_KEY] ?: false
    }

    val fontScale: Flow<Float> = dataStore.data.map { preferences ->
        preferences[FONT_SCALE_KEY] ?: 1.0f
    }

    val defaultTab: Flow<DefaultTab> = dataStore.data.map { preferences ->
        DefaultTab.valueOf(preferences[DEFAULT_TAB_KEY] ?: DefaultTab.CONTACTS.name)
    }

    val contactClickAction: Flow<ContactClickAction> = dataStore.data.map { preferences ->
        ContactClickAction.valueOf(preferences[CONTACT_CLICK_ACTION_KEY] ?: ContactClickAction.VIEW_DETAILS.name)
    }

    val edgeToEdgeDisplay: Flow<Boolean> = dataStore.data.map { preferences ->
        preferences[EDGE_TO_EDGE_DISPLAY_KEY] ?: true
    }

    val startNameWithSurname: Flow<Boolean> = dataStore.data.map { preferences ->
        preferences[START_NAME_WITH_SURNAME_KEY] ?: false
    }

    val showPrivateContacts: Flow<Boolean> = dataStore.data.map { preferences ->
        preferences[SHOW_PRIVATE_CONTACTS_KEY] ?: true
    }

    val showDialpadButton: Flow<Boolean> = dataStore.data.map { preferences ->
        preferences[SHOW_DIALPAD_BUTTON_KEY] ?: true
    }

    val formatPhoneNumbers: Flow<Boolean> = dataStore.data.map { preferences ->
        preferences[FORMAT_PHONE_NUMBERS_KEY] ?: true
    }

    val callConfirmation: Flow<Boolean> = dataStore.data.map { preferences ->
        preferences[CALL_CONFIRMATION_KEY] ?: false
    }

    val swipeDeleteConfirmation: Flow<Boolean> = dataStore.data.map { preferences ->
        preferences[SWIPE_DELETE_CONFIRMATION_KEY] ?: true // Default: true for safety
    }

    val sortOrder: Flow<com.contacts.android.contacts.domain.model.SortOrder> = dataStore.data.map { preferences ->
        val sortValue = preferences[SORT_ORDER_KEY] ?: com.contacts.android.contacts.domain.model.SortOrder.DEFAULT.toInt()
        com.contacts.android.contacts.domain.model.SortOrder.fromInt(sortValue)
    }

    val contactFilter: Flow<com.contacts.android.contacts.domain.model.ContactFilter> = dataStore.data.map { preferences ->
        val filterString = preferences[CONTACT_FILTER_KEY] ?: ""
        com.contacts.android.contacts.domain.model.ContactFilter.fromString(filterString)
    }

    suspend fun setThemeMode(themeMode: ThemeMode) {
        dataStore.edit { preferences ->
            preferences[THEME_MODE_KEY] = themeMode.name
        }
    }

    suspend fun setColorTheme(colorTheme: ColorTheme) {
        dataStore.edit { preferences ->
            preferences[COLOR_THEME_KEY] = colorTheme.name
        }
    }

    suspend fun setAppLanguage(language: AppLanguage) {
        dataStore.edit { preferences ->
            preferences[APP_LANGUAGE_KEY] = language.name
        }
    }

    suspend fun setShowContactThumbnails(show: Boolean) {
        dataStore.edit { preferences ->
            preferences[SHOW_CONTACT_THUMBNAILS_KEY] = show
        }
    }

    suspend fun setShowPhoneNumbers(show: Boolean) {
        dataStore.edit { preferences ->
            preferences[SHOW_PHONE_NUMBERS_KEY] = show
        }
    }

    suspend fun setShowOnlyWithPhone(show: Boolean) {
        dataStore.edit { preferences ->
            preferences[SHOW_ONLY_WITH_PHONE_KEY] = show
        }
    }

    suspend fun setShowDuplicates(show: Boolean) {
        dataStore.edit { preferences ->
            preferences[SHOW_DUPLICATES_KEY] = show
        }
    }

    suspend fun setFontScale(scale: Float) {
        dataStore.edit { preferences ->
            preferences[FONT_SCALE_KEY] = scale
        }
    }

    suspend fun setDefaultTab(tab: DefaultTab) {
        dataStore.edit { preferences ->
            preferences[DEFAULT_TAB_KEY] = tab.name
        }
    }

    suspend fun setContactClickAction(action: ContactClickAction) {
        dataStore.edit { preferences ->
            preferences[CONTACT_CLICK_ACTION_KEY] = action.name
        }
    }

    suspend fun setEdgeToEdgeDisplay(enabled: Boolean) {
        dataStore.edit { preferences ->
            preferences[EDGE_TO_EDGE_DISPLAY_KEY] = enabled
        }
    }

    suspend fun setStartNameWithSurname(enabled: Boolean) {
        dataStore.edit { preferences ->
            preferences[START_NAME_WITH_SURNAME_KEY] = enabled
        }
    }

    suspend fun setShowPrivateContacts(show: Boolean) {
        dataStore.edit { preferences ->
            preferences[SHOW_PRIVATE_CONTACTS_KEY] = show
        }
    }

    suspend fun setShowDialpadButton(show: Boolean) {
        dataStore.edit { preferences ->
            preferences[SHOW_DIALPAD_BUTTON_KEY] = show
        }
    }

    suspend fun setFormatPhoneNumbers(format: Boolean) {
        dataStore.edit { preferences ->
            preferences[FORMAT_PHONE_NUMBERS_KEY] = format
        }
    }

    suspend fun setCallConfirmation(enabled: Boolean) {
        dataStore.edit { preferences ->
            preferences[CALL_CONFIRMATION_KEY] = enabled
        }
    }

    suspend fun setSwipeDeleteConfirmation(enabled: Boolean) {
        dataStore.edit { preferences ->
            preferences[SWIPE_DELETE_CONFIRMATION_KEY] = enabled
        }
    }

    suspend fun setSortOrder(sortOrder: com.contacts.android.contacts.domain.model.SortOrder) {
        dataStore.edit { preferences ->
            preferences[SORT_ORDER_KEY] = sortOrder.toInt()
        }
    }

    suspend fun setContactFilter(filter: com.contacts.android.contacts.domain.model.ContactFilter) {
        dataStore.edit { preferences ->
            preferences[CONTACT_FILTER_KEY] = filter.toString()
        }
    }
}

enum class ThemeMode {
    LIGHT, DARK, SYSTEM
}

enum class ColorTheme {
    BLUE, GREEN, PURPLE, ORANGE, RED, PINK
}

enum class AppLanguage(val displayName: String, val locale: String) {
    ENGLISH("English", "en"),
    FRENCH("Français", "fr"),
    SPANISH("Español", "es"),
    ARABIC("العربية", "ar"),
    CHINESE("中文", "zh"),
    HINDI("हिन्दी", "hi"),
    PORTUGUESE("Português", "pt"),
    RUSSIAN("Русский", "ru"),
    GERMAN("Deutsch", "de"),
    JAPANESE("日本語", "ja"),
    ITALIAN("Italiano", "it"),
    KOREAN("한국어", "ko")
}

enum class DefaultTab(val displayName: String) {
    CONTACTS("Contacts"),
    FAVORITES("Favorites"),
    GROUPS("Groups")
}

enum class ContactClickAction(val displayName: String) {
    VIEW_DETAILS("View details"),
    CALL("Call"),
    MESSAGE("Send message"),
    ASK_EVERY_TIME("Ask every time")
}

enum class FontSize(val displayName: String, val scale: Float) {
    SMALL("Small", 0.85f),
    MEDIUM("Medium", 1.0f),
    LARGE("Large", 1.15f),
    EXTRA_LARGE("Extra Large", 1.3f)
}
