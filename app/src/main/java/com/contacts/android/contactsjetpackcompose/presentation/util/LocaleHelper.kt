package com.contacts.android.contactsjetpackcompose.presentation.util

import android.content.Context
import android.content.res.Configuration
import android.os.Build
import android.os.LocaleList
import java.util.Locale

object LocaleHelper {

    /**
     * Sets the app language to the specified locale code
     * @param context The application context
     * @param localeCode The locale code (e.g., "en", "fr", "es")
     * @return The updated context with the new locale
     */
    fun setLocale(context: Context, localeCode: String): Context {
        return updateResources(context, localeCode)
    }

    /**
     * Gets the current app locale
     * @param context The application context
     * @return The current locale code
     */
    fun getCurrentLocale(context: Context): String {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            context.resources.configuration.locales[0].language
        } else {
            @Suppress("DEPRECATION")
            context.resources.configuration.locale.language
        }
    }

    /**
     * Updates the configuration resources with the new locale
     */
    private fun updateResources(context: Context, localeCode: String): Context {
        val locale = Locale(localeCode)
        Locale.setDefault(locale)

        val configuration = Configuration(context.resources.configuration)

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            configuration.setLocale(locale)
            val localeList = LocaleList(locale)
            LocaleList.setDefault(localeList)
            configuration.setLocales(localeList)
        } else {
            @Suppress("DEPRECATION")
            configuration.locale = locale
        }

        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N_MR1) {
            context.createConfigurationContext(configuration)
        } else {
            @Suppress("DEPRECATION")
            context.resources.updateConfiguration(configuration, context.resources.displayMetrics)
            context
        }
    }

    /**
     * Gets the display name for a language code
     * @param localeCode The locale code (e.g., "en", "fr", "es")
     * @return The display name in the language itself
     */
    fun getLanguageDisplayName(localeCode: String): String {
        val locale = Locale(localeCode)
        return locale.getDisplayLanguage(locale).replaceFirstChar { it.uppercase() }
    }
}
