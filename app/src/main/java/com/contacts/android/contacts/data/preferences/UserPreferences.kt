package com.contacts.android.contacts.data.preferences

import android.content.Context
import androidx.annotation.StringRes
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.*
import androidx.datastore.preferences.preferencesDataStore
import com.contacts.android.contacts.R
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
        private val APP_OPEN_COUNT_KEY = intPreferencesKey("app_open_count")

        private val RATING_COMPLETED_KEY = booleanPreferencesKey("rating_completed")
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

        // Premium subscription keys
        private val IS_PREMIUM_KEY = booleanPreferencesKey("is_premium")
        private val PREMIUM_PRODUCT_ID_KEY = stringPreferencesKey("premium_product_id")
        private val PREMIUM_PURCHASE_DATE_KEY = longPreferencesKey("premium_purchase_date")
        private val PREMIUM_EXPIRY_DATE_KEY = longPreferencesKey("premium_expiry_date")
        private val PREMIUM_AUTO_RENEWING_KEY = booleanPreferencesKey("premium_auto_renewing")
    }

    // AJOUTER CES FLOWS
    val appOpenCount: Flow<Int> = dataStore.data.map { preferences ->
        preferences[APP_OPEN_COUNT_KEY] ?: 0
    }

    val isRatingCompleted: Flow<Boolean> = dataStore.data.map { preferences ->
        preferences[RATING_COMPLETED_KEY] ?: false
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

    // AJOUTER CES FONCTIONS
    suspend fun incrementAppOpenCount() {
        dataStore.edit { preferences ->
            val current = preferences[APP_OPEN_COUNT_KEY] ?: 0
            preferences[APP_OPEN_COUNT_KEY] = current + 1
        }
    }

    suspend fun setRatingCompleted(completed: Boolean) {
        dataStore.edit { preferences ->
            preferences[RATING_COMPLETED_KEY] = completed
        }
    }

    // Premium subscription flows
    val isPremium: Flow<Boolean> = dataStore.data.map { preferences ->
        preferences[IS_PREMIUM_KEY] ?: false
    }

    val premiumProductId: Flow<String?> = dataStore.data.map { preferences ->
        preferences[PREMIUM_PRODUCT_ID_KEY]
    }

    val premiumPurchaseDate: Flow<Long?> = dataStore.data.map { preferences ->
        preferences[PREMIUM_PURCHASE_DATE_KEY]
    }

    val premiumExpiryDate: Flow<Long?> = dataStore.data.map { preferences ->
        preferences[PREMIUM_EXPIRY_DATE_KEY]
    }

    val premiumAutoRenewing: Flow<Boolean> = dataStore.data.map { preferences ->
        preferences[PREMIUM_AUTO_RENEWING_KEY] ?: false
    }

    // Premium subscription setters
    suspend fun setPremiumStatus(
        isPremium: Boolean,
        productId: String? = null,
        purchaseDate: Long? = null,
        expiryDate: Long? = null,
        isAutoRenewing: Boolean = false
    ) {
        dataStore.edit { preferences ->
            preferences[IS_PREMIUM_KEY] = isPremium
            productId?.let { preferences[PREMIUM_PRODUCT_ID_KEY] = it }
            purchaseDate?.let { preferences[PREMIUM_PURCHASE_DATE_KEY] = it }
            expiryDate?.let { preferences[PREMIUM_EXPIRY_DATE_KEY] = it }
            preferences[PREMIUM_AUTO_RENEWING_KEY] = isAutoRenewing
        }
    }

    suspend fun clearPremiumStatus() {
        dataStore.edit { preferences ->
            preferences.remove(IS_PREMIUM_KEY)
            preferences.remove(PREMIUM_PRODUCT_ID_KEY)
            preferences.remove(PREMIUM_PURCHASE_DATE_KEY)
            preferences.remove(PREMIUM_EXPIRY_DATE_KEY)
            preferences.remove(PREMIUM_AUTO_RENEWING_KEY)
        }
    }
}

enum class ThemeMode {
    LIGHT, DARK, SYSTEM
}

enum class ColorTheme(
    val primaryColor: Long,
    val secondaryColor: Long,
    val tertiaryColor: Long,
    val isPremium: Boolean = false
) {
    // Standard themes (free)
    BLUE(0xFF1976D2, 0xFF388E3C, 0xFFFFA000),
    GREEN(0xFF2E7D32, 0xFF689F38, 0xFFFBC02D),
    PURPLE(0xFF6A1B9A, 0xFFE91E63, 0xFFFF6F00),
    ORANGE(0xFFE64A19, 0xFFF57C00, 0xFFFFD54F),
    RED(0xFFC62828, 0xFFD32F2F, 0xFFFF6F00),
    PINK(0xFFC2185B, 0xFFAD1457, 0xFFE91E63),
    TEAL(0xFF00796B, 0xFF0097A7, 0xFF7CB342),
    INDIGO(0xFF303F9F, 0xFF3949AB, 0xFF5C6BC0),
    BROWN(0xFF5D4037, 0xFF6D4C41, 0xFFA1887F),
    CYAN(0xFF0097A7, 0xFF00ACC1, 0xFF26C6DA),

    // Premium exclusive themes
    GRADIENT(0xFFFF6B6B, 0xFFFFD93D, 0xFF6BCF7F, isPremium = true),
    GOLD(0xFFFFD700, 0xFFFFA500, 0xFFFF8C00, isPremium = true),
    EMERALD(0xFF50C878, 0xFF009B77, 0xFF00A86B, isPremium = true);

    companion object {
        fun getStandardThemes(): List<ColorTheme> {
            return values().filter { !it.isPremium }
        }

        fun getPremiumThemes(): List<ColorTheme> {
            return values().filter { it.isPremium }
        }
    }
}

enum class AppLanguage(val displayName: String, val locale: String) {
    // A
    ARABIC("العربية", "ar"),

    // B
    BAKHTIARI("بختیاری", "bqi"),
    BASQUE("Euskara", "eu"),
    BENGALI("বাংলা", "bn"),
    BENGALI_BANGLADESH("বাংলা (বাংলাদেশ)", "bn-rBD"),
    BOSNIAN("Bosanski", "bs"),
    BRETON("Brezhoneg", "br"),
    BULGARIAN("Български", "bg"),
    BURMESE("မြန်မာ", "my"),

    // C
    CATALAN("Català", "ca"),
    CHINESE("中文", "zh"),
    CHINESE_SIMPLIFIED("简体中文", "zh-rCN"),
    CHINESE_TRADITIONAL_HK("繁體中文 (香港)", "zh-rHK"),
    CHINESE_TRADITIONAL_TW("繁體中文 (台灣)", "zh-rTW"),
    CREE("ᓀᐦᐃᔭᐍᐏᐣ", "cr"),
    CROATIAN("Hrvatski", "hr"),
    CZECH("Čeština", "cs"),

    // D
    DANISH("Dansk", "da"),
    DUTCH("Nederlands", "nl"),

    // E
    ENGLISH("English", "en"),
    ENGLISH_UK("English (UK)", "en-rGB"),
    ENGLISH_INDIA("English (India)", "en-rIN"),
    ESPERANTO("Esperanto", "eo"),
    ESTONIAN("Eesti", "et"),

    // F
    FILIPINO("Filipino", "fil"),
    FINNISH("Suomi", "fi"),
    FRENCH("Français", "fr"),
    FULFULDE_ADLAM("𞤊𞤵𞤤𞤬𞤵𞤤𞤣𞤫", "ff-Adlm"),
    FULFULDE_LATIN("Fulfulde", "ff-Latn"),

    // G
    GALICIAN("Galego", "gl"),
    GERMAN("Deutsch", "de"),
    GREEK("Ελληνικά", "el"),

    // H
    HEBREW("עברית", "iw"),
    HINDI("हिन्दी", "hi"),
    HINDI_INDIA("हिन्दी (भारत)", "hi-rIN"),
    HUNGARIAN("Magyar", "hu"),

    // I
    ICELANDIC("Íslenska", "is"),
    INDONESIAN("Bahasa Indonesia", "in"),
    INTERLINGUA("Interlingua", "ia"),
    IRISH("Gaeilge", "ga"),
    ITALIAN("Italiano", "it"),

    // J
    JAPANESE("日本語", "ja"),

    // K
    KANNADA("ಕನ್ನಡ", "kn"),
    KANURI("Kanuri", "kr"),
    KOREAN("한국어", "ko-rKR"),
    KURDISH_SORANI("کوردی", "ckb"),

    // L
    LATGALIAN("Latgaļu", "ltg"),
    LATIN_AMERICAN_SPANISH("Español (Latinoamérica)", "b+es+419"),
    LATVIAN("Latviešu", "lv"),
    LITHUANIAN("Lietuvių", "lt"),

    // M
    MACEDONIAN("Македонски", "mk"),
    MALAY("Bahasa Melayu", "ms"),
    MALAYALAM("മലയാളം", "ml"),

    // N
    NEPALI("नेपाली", "ne"),
    NORWEGIAN_BOKMAL("Norsk bokmål", "nb-rNO"),
    NORWEGIAN_NYNORSK("Norsk nynorsk", "nn"),

    // O
    OCCITAN("Occitan", "oc"),
    ODIA("ଓଡ଼ିଆ", "or"),

    // P
    PERSIAN("فارسی", "fa"),
    POLISH("Polski", "pl"),
    PORTUGUESE("Português", "pt"),
    PORTUGUESE_BRAZIL("Português (Brasil)", "pt-rBR"),
    PORTUGUESE_PORTUGAL("Português (Portugal)", "pt-rPT"),
    PULAAR_ADLAM("𞤆𞤵𞤤𞤢𞥄𞤪", "fuf-Adlm"),
    PULAAR_LATIN("Pulaar", "fuf-Latn"),
    PUNJABI("ਪੰਜਾਬੀ", "pa"),
    PUNJABI_PAKISTAN("پنجابی", "pa-rPK"),

    // R
    ROMANIAN("Română", "ro"),
    RUSSIAN("Русский", "ru"),

    // S
    SANTALI("ᱥᱟᱱᱛᱟᱲᱤ", "sat"),
    SERBIAN("Српски", "sr"),
    SINHALA("සිංහල", "si"),
    SLOVAK("Slovenčina", "sk"),
    SLOVENIAN("Slovenščina", "sl"),
    SPANISH("Español", "es"),
    SPANISH_US("Español (EE. UU.)", "es-rUS"),
    SWEDISH("Svenska", "sv"),

    // T
    TAMAZIGHT("ⵜⴰⵎⴰⵣⵉⵖⵜ", "zgh"),
    TAMIL("தமிழ்", "ta"),
    TELUGU("తెలుగు", "te"),
    THAI("ไทย", "th"),
    TURKISH("Türkçe", "tr"),

    // U
    UKRAINIAN("Українська", "uk"),
    URDU("اردو", "ur"),

    // V
    VIETNAMESE("Tiếng Việt", "vi"),

    // W
    WELSH("Cymraeg", "cy")
}

enum class DefaultTab(@StringRes val displayNameRes: Int) {
    CONTACTS(R.string.nav_contacts),
    FAVORITES(R.string.nav_favorites),
    GROUPS(R.string.nav_groups)
}

enum class ContactClickAction(@StringRes val displayNameRes: Int) {
    VIEW_DETAILS(R.string.view_details),
    CALL(R.string.action_call),
    MESSAGE(R.string.send_message),
    ASK_EVERY_TIME(R.string.ask_every_time)
}

enum class FontSize(@StringRes val displayNameRes: Int, val scale: Float) {
    SMALL(R.string.font_size_small, 0.85f),
    MEDIUM(R.string.font_size_medium, 1.0f),
    LARGE(R.string.font_size_large, 1.15f),
    EXTRA_LARGE(R.string.font_size_extra_large, 1.3f)
}
