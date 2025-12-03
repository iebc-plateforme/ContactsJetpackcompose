package com.contacts.android.contacts.domain.repository

import com.contacts.android.contacts.domain.model.BillingState
import com.contacts.android.contacts.domain.model.PremiumStatus
import com.contacts.android.contacts.domain.model.PurchaseResult
import com.contacts.android.contacts.domain.model.SubscriptionProduct
import kotlinx.coroutines.flow.Flow

/**
 * Repository interface for premium subscription management
 */
interface PremiumRepository {
    /**
     * Observe the current premium status
     */
    val premiumStatus: Flow<PremiumStatus>

    /**
     * Observe the billing connection state
     */
    val billingState: Flow<BillingState>

    /**
     * Get available subscription products
     */
    suspend fun getAvailableProducts(): Result<List<SubscriptionProduct>>

    /**
     * Purchase a subscription product
     */
    suspend fun purchaseProduct(productId: String): PurchaseResult

    /**
     * Restore previous purchases
     */
    suspend fun restorePurchases(): Result<Boolean>

    /**
     * Check if user has premium access
     */
    suspend fun isPremium(): Boolean

    /**
     * Check if a specific feature is available
     */
    suspend fun hasFeatureAccess(feature: String): Boolean

    /**
     * Initialize billing connection
     */
    suspend fun initializeBilling()

    /**
     * Clean up billing connection
     */
    fun endBillingConnection()
}
