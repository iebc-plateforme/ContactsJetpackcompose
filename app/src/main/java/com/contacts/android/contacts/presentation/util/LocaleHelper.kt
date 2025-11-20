package com.contacts.android.contacts.presentation.util

import android.os.Build
import android.os.LocaleList
import androidx.appcompat.app.AppCompatDelegate
import androidx.core.os.LocaleListCompat
import java.util.Locale

/**
 * LocaleHelper using Google-recommended App Locale API (AppCompatDelegate)
 * This approach provides instant language changes without app restart
 * and is the official Google-recommended solution for per-app language preferences
 *
 * Benefits:
 * - Instant UI updates without app restart
 * - Works across all Android versions (backward compatible)
 * - Respects user's app-specific language preference
 * - Integrates with system settings on Android 13+
 * - No deprecated APIs or workarounds needed
 */
object LocaleHelper {

    /**
     * Sets the app language using the modern App Locale API
     * This triggers instant UI update without requiring app restart
     *
     * @param localeCode The locale code (e.g., "en", "fr", "es", "ff-Adlm")
     *
     * How it works:
     * - On Android 13+ (API 33+): Updates per-app language in system settings
     * - On Android 12 and below: Uses AppCompat's internal storage
     * - Triggers configuration change that Compose reacts to automatically
     */
    fun setLocale(localeCode: String) {
        // Convert Android resource format to BCP 47 format
        // e.g., "en-rGB" -> "en-GB", "b+es+419" -> "es-419"
        val bcp47Code = localeCode
            .replace("-r", "-")  // Convert regional format
            .replace("b+", "")   // Remove BCP 47 prefix
            .replace("+", "-")   // Convert + to -
        val appLocale = LocaleListCompat.forLanguageTags(bcp47Code)
        AppCompatDelegate.setApplicationLocales(appLocale)
    }

    /**
     * Gets the current app-specific locale
     * Returns the locale set via App Locale API, or system locale if none set
     *
     * @return The current locale code (e.g., "en", "fr", "es")
     */
    fun getCurrentLocale(): String {
        val appLocales = AppCompatDelegate.getApplicationLocales()

        return if (!appLocales.isEmpty) {
            // Return the first locale from app-specific settings
            appLocales[0]?.language ?: getSystemLocale()
        } else {
            // No app-specific locale set, return system locale
            getSystemLocale()
        }
    }

    /**
     * Gets the system default locale
     */
    private fun getSystemLocale(): String {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            LocaleList.getDefault()[0].language
        } else {
            @Suppress("DEPRECATION")
            Locale.getDefault().language
        }
    }

    /**
     * Clears the app-specific locale and reverts to system default
     * This is equivalent to "Use system default" option
     */
    fun clearLocale() {
        AppCompatDelegate.setApplicationLocales(LocaleListCompat.getEmptyLocaleList())
    }

    /**
     * Gets the display name for a language code
     * @param localeCode The locale code (e.g., "en", "fr", "es", "ff-Adlm")
     * @return The display name in the language itself (e.g., "English", "Français")
     */
    fun getLanguageDisplayName(localeCode: String): String {
        // Convert Android resource format to BCP 47 format
        // e.g., "en-rGB" -> "en-GB", "b+es+419" -> "es-419"
        val bcp47Code = localeCode
            .replace("-r", "-")  // Convert regional format
            .replace("b+", "")   // Remove BCP 47 prefix
            .replace("+", "-")   // Convert + to -
        val locale = Locale.forLanguageTag(bcp47Code)
        return locale.getDisplayLanguage(locale).replaceFirstChar { it.uppercase() }
    }

    /**
     * Gets the display name for current locale in the current locale
     * Useful for showing "Current language: English" in the UI
     */
    fun getCurrentLanguageDisplayName(): String {
        val currentLocale = getCurrentLocale()
        return getLanguageDisplayName(currentLocale)
    }

    /**
     * Checks if a specific locale is currently active
     */
    fun isLocaleActive(localeCode: String): Boolean {
        return getCurrentLocale() == localeCode
    }
}
