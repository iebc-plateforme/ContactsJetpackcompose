package com.contacts.android.contacts.util

import android.content.Context
import android.os.Bundle
import com.google.firebase.analytics.FirebaseAnalytics
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Helper class to manage Firebase Analytics tracking throughout the app.
 */
@Singleton
class AnalyticsManager @Inject constructor(
    @ApplicationContext private val context: Context
) {
    private val firebaseAnalytics = FirebaseAnalytics.getInstance(context)

    fun logEvent(name: String, params: Bundle? = null) {
        firebaseAnalytics.logEvent(name, params)
    }

    fun logScreenView(screenName: String, screenClass: String? = null) {
        val bundle = Bundle().apply {
            putString(FirebaseAnalytics.Param.SCREEN_NAME, screenName)
            putString(FirebaseAnalytics.Param.SCREEN_CLASS, screenClass ?: screenName)
        }
        firebaseAnalytics.logEvent(FirebaseAnalytics.Event.SCREEN_VIEW, bundle)
    }

    fun setUserProperty(name: String, value: String) {
        firebaseAnalytics.setUserProperty(name, value)
    }

    fun logAppStart() {
        logEvent(FirebaseAnalytics.Event.APP_OPEN)
    }

    fun logContactCreated(source: String) {
        val bundle = Bundle().apply {
            putString("source", source)
        }
        logEvent("contact_created", bundle)
    }

    fun logContactDeleted() {
        logEvent("contact_deleted")
    }

    // --- Search ---
    fun logSearch(query: String) {
        val bundle = Bundle().apply {
            putString(FirebaseAnalytics.Param.SEARCH_TERM, query)
        }
        logEvent(FirebaseAnalytics.Event.SEARCH, bundle)
    }

    // --- Contact actions ---
    fun logContactShared() {
        logEvent("contact_shared")
    }

    fun logContactCalled() {
        logEvent("contact_called")
    }

    fun logContactMessaged() {
        logEvent("contact_messaged")
    }

    fun logContactEmailed() {
        logEvent("contact_emailed")
    }

    fun logFavoriteToggled(isFavorite: Boolean) {
        val bundle = Bundle().apply {
            putBoolean("is_favorite", isFavorite)
        }
        logEvent("favorite_toggled", bundle)
    }

    // --- Import / Export ---
    fun logContactsImported(count: Int) {
        val bundle = Bundle().apply {
            putInt("count", count)
        }
        logEvent("contacts_imported", bundle)
    }

    fun logContactsExported(count: Int) {
        val bundle = Bundle().apply {
            putInt("count", count)
        }
        logEvent("contacts_exported", bundle)
    }

    // --- Merge ---
    fun logContactsMerged(count: Int) {
        val bundle = Bundle().apply {
            putInt("count", count)
        }
        logEvent("contacts_merged", bundle)
    }

    // --- Groups ---
    fun logGroupCreated() {
        logEvent("group_created")
    }

    fun logGroupDeleted() {
        logEvent("group_deleted")
    }

    fun logContactsAddedToGroup(count: Int) {
        val bundle = Bundle().apply {
            putInt("count", count)
        }
        logEvent("contacts_added_to_group", bundle)
    }

    fun logGroupSmsSent(contactCount: Int) {
        val bundle = Bundle().apply {
            putInt("contact_count", contactCount)
        }
        logEvent("group_sms_sent", bundle)
    }

    fun logGroupEmailSent(contactCount: Int) {
        val bundle = Bundle().apply {
            putInt("contact_count", contactCount)
        }
        logEvent("group_email_sent", bundle)
    }

    // --- Settings ---
    fun logThemeChanged(theme: String) {
        val bundle = Bundle().apply {
            putString("theme", theme)
        }
        logEvent("theme_changed", bundle)
    }

    fun logLanguageChanged(language: String) {
        val bundle = Bundle().apply {
            putString("language", language)
        }
        logEvent("language_changed", bundle)
    }

    // --- Backup ---
    fun logBackupCreated() {
        logEvent("backup_created")
    }

    fun logBackupRestored() {
        logEvent("backup_restored")
    }

    // --- Duplicates ---
    fun logDuplicatesDetected(count: Int) {
        val bundle = Bundle().apply {
            putInt("count", count)
        }
        logEvent("duplicates_detected", bundle)
    }

    // --- Premium conversion tracking ---
    fun logPremiumFeatureBlocked(feature: String, source: String) {
        val bundle = Bundle().apply {
            putString("feature", feature)
            putString("source", source)
        }
        logEvent("premium_feature_blocked", bundle)
    }

    fun logPremiumScreenViewed(source: String) {
        val bundle = Bundle().apply {
            putString("source", source)
        }
        logEvent("premium_screen_viewed", bundle)
    }

    fun logPremiumNudgeShown(nudgeType: String) {
        val bundle = Bundle().apply {
            putString("nudge_type", nudgeType)
        }
        logEvent("premium_nudge_shown", bundle)
    }

    fun logPremiumNudgeClicked(nudgeType: String) {
        val bundle = Bundle().apply {
            putString("nudge_type", nudgeType)
        }
        logEvent("premium_nudge_clicked", bundle)
    }

    // --- Premium purchases ---
    fun logPurchaseAttempted(productId: String) {
        val bundle = Bundle().apply {
            putString("product_id", productId)
        }
        logEvent("purchase_attempted", bundle)
    }

    fun logPurchaseCompleted(productId: String) {
        val bundle = Bundle().apply {
            putString("product_id", productId)
        }
        logEvent("purchase_completed", bundle)
    }

    fun logPurchaseRestored() {
        logEvent("purchase_restored")
    }

    // --- QR / Business card ---
    fun logQrCodeScanned() {
        logEvent("qr_code_scanned")
    }

    fun logBusinessCardScanned() {
        logEvent("business_card_scanned")
    }

    // --- Sort / Filter ---
    fun logSortOrderChanged(order: String) {
        val bundle = Bundle().apply {
            putString("order", order)
        }
        logEvent("sort_order_changed", bundle)
    }

    fun logFilterChanged(filter: String) {
        val bundle = Bundle().apply {
            putString("filter", filter)
        }
        logEvent("filter_changed", bundle)
    }
}
