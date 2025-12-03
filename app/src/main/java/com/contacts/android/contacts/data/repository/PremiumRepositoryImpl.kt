package com.contacts.android.contacts.data.repository

import android.app.Activity
import com.android.billingclient.api.Purchase
import com.contacts.android.contacts.data.billing.BillingManager
import com.contacts.android.contacts.data.preferences.UserPreferences
import com.contacts.android.contacts.domain.model.*
import com.contacts.android.contacts.domain.repository.PremiumRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Implementation of PremiumRepository
 * Manages premium subscription state and billing operations
 */
@Singleton
class PremiumRepositoryImpl @Inject constructor(
    private val billingManager: BillingManager,
    private val userPreferences: UserPreferences
) : PremiumRepository {

    private val scope = CoroutineScope(Dispatchers.IO)

    override val premiumStatus: Flow<PremiumStatus> = combine(
        userPreferences.isPremium,
        userPreferences.premiumProductId,
        userPreferences.premiumPurchaseDate,
        userPreferences.premiumExpiryDate,
        userPreferences.premiumAutoRenewing
    ) { isPremium, productId, purchaseDate, expiryDate, autoRenewing ->
        PremiumStatus(
            isPremium = isPremium,
            subscriptionType = productId?.let { SubscriptionType.fromProductId(it) },
            purchaseDate = purchaseDate,
            expiryDate = expiryDate,
            productId = productId,
            isAutoRenewing = autoRenewing
        )
    }

    override val billingState: Flow<BillingState> = billingManager.billingState

    override suspend fun getAvailableProducts(): Result<List<SubscriptionProduct>> {
        return billingManager.queryProducts()
    }

    override suspend fun purchaseProduct(productId: String): PurchaseResult {
        // Note: This method needs to be called with an Activity context
        // It should be called from the UI layer
        return PurchaseResult.Error("Use launchPurchaseFlow from ViewModel with Activity")
    }

    /**
     * Launch purchase flow (to be called from ViewModel with Activity)
     */
    suspend fun launchPurchaseFlow(activity: Activity, productId: String): PurchaseResult {
        val result = billingManager.launchPurchaseFlow(activity, productId)

        if (result is PurchaseResult.Success) {
            // Update premium status in preferences
            val subscriptionType = SubscriptionType.fromProductId(productId)
            val purchaseDate = System.currentTimeMillis()
            val expiryDate = if (subscriptionType == SubscriptionType.LIFETIME) {
                null // Lifetime doesn't expire
            } else {
                // Annual subscription expires in 1 year
                purchaseDate + (365L * 24 * 60 * 60 * 1000)
            }

            userPreferences.setPremiumStatus(
                isPremium = true,
                productId = productId,
                purchaseDate = purchaseDate,
                expiryDate = expiryDate,
                isAutoRenewing = subscriptionType != SubscriptionType.LIFETIME
            )
        }

        return result
    }

    override suspend fun restorePurchases(): Result<Boolean> {
        return try {
            val purchases = billingManager.queryActivePurchases()

            if (purchases.isEmpty()) {
                // No active purchases found, clear premium status
                userPreferences.clearPremiumStatus()
                Result.success(false)
            } else {
                // Process the first active purchase (user should only have one)
                val purchase = purchases.first()
                updatePremiumStatusFromPurchase(purchase)
                Result.success(true)
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun isPremium(): Boolean {
        return userPreferences.isPremium.first()
    }

    override suspend fun hasFeatureAccess(feature: String): Boolean {
        // Check if user has premium status
        val isPremium = isPremium()

        // All premium features require premium status
        return isPremium
    }

    override suspend fun initializeBilling() {
        billingManager.initializeBilling()

        // After billing is initialized, restore purchases to sync status
        scope.launch {
            billingManager.billingState.collect { state ->
                if (state is BillingState.Connected) {
                    restorePurchases()
                }
            }
        }
    }

    override fun endBillingConnection() {
        billingManager.endConnection()
    }

    /**
     * Update premium status from a purchase object
     */
    private suspend fun updatePremiumStatusFromPurchase(purchase: Purchase) {
        val productId = purchase.products.firstOrNull() ?: return
        val subscriptionType = SubscriptionType.fromProductId(productId) ?: return

        val purchaseDate = purchase.purchaseTime
        val expiryDate = if (subscriptionType == SubscriptionType.LIFETIME) {
            null
        } else {
            // For subscriptions, calculate expiry from purchase time
            purchaseDate + (365L * 24 * 60 * 60 * 1000)
        }

        userPreferences.setPremiumStatus(
            isPremium = true,
            productId = productId,
            purchaseDate = purchaseDate,
            expiryDate = expiryDate,
            isAutoRenewing = purchase.isAutoRenewing
        )
    }

    /**
     * Check if subscription is expired (for annual subscriptions)
     */
    private fun isSubscriptionExpired(expiryDate: Long?): Boolean {
        if (expiryDate == null) return false // Lifetime doesn't expire
        return System.currentTimeMillis() > expiryDate
    }

    /**
     * Validate premium status (check if subscription is still valid)
     */
    suspend fun validatePremiumStatus() {
        val status = premiumStatus.first()

        if (status.isPremium && status.subscriptionType == SubscriptionType.ANNUAL) {
            status.expiryDate?.let { expiryDate ->
                if (isSubscriptionExpired(expiryDate)) {
                    // Subscription expired, restore purchases to check if renewed
                    restorePurchases()
                }
            }
        }
    }

    /**
     * Update premium status for testing purposes (without real purchase)
     * Only use this for local testing!
     */
    suspend fun updatePremiumStatusTest(
        isPremium: Boolean,
        productId: String,
        purchaseDate: Long,
        expiryDate: Long?,
        isAutoRenewing: Boolean
    ) {
        userPreferences.setPremiumStatus(
            isPremium = isPremium,
            productId = productId,
            purchaseDate = purchaseDate,
            expiryDate = expiryDate,
            isAutoRenewing = isAutoRenewing
        )
    }
}
