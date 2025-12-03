package com.contacts.android.contacts.domain.model

/**
 * Represents the premium subscription status
 */
data class PremiumStatus(
    val isPremium: Boolean = false,
    val subscriptionType: SubscriptionType? = null,
    val purchaseDate: Long? = null,
    val expiryDate: Long? = null,
    val productId: String? = null,
    val isAutoRenewing: Boolean = false
)

/**
 * Types of subscription available
 */
enum class SubscriptionType(val productId: String, val displayName: String) {
    LIFETIME("premium_lifetime", "Lifetime Premium"),
    ANNUAL("premium_annual", "Annual Subscription");

    companion object {
        fun fromProductId(productId: String): SubscriptionType? {
            return entries.find { it.productId == productId }
        }
    }
}

/**
 * Premium features available to subscribers
 * NOTE: Only list features that are actually implemented in the app
 */
enum class PremiumFeature(
    val title: String,
    val description: String,
    val icon: String
) {
    NO_ADS(
        "No Advertisements",
        "Enjoy an ad-free experience throughout the app",
        "block"
    ),
    EXCLUSIVE_THEMES(
        "Exclusive Themes",
        "Access to all premium color themes including Gradient, Gold, Emerald, and more",
        "palette"
    );

    companion object {
        /**
         * Returns all available premium features
         * Currently only 2 real features: No Ads and Exclusive Themes
         */
        fun getAllFeatures() = entries
    }
}

/**
 * Billing connection state
 */
sealed class BillingState {
    data object Disconnected : BillingState()
    data object Connecting : BillingState()
    data object Connected : BillingState()
    data class Error(val message: String) : BillingState()
}

/**
 * Purchase result
 */
sealed class PurchaseResult {
    data object Success : PurchaseResult()
    data object Cancelled : PurchaseResult()
    data class Error(val message: String) : PurchaseResult()
    data object Pending : PurchaseResult()
}

/**
 * Product details for UI display
 */
data class SubscriptionProduct(
    val productId: String,
    val type: SubscriptionType,
    val title: String,
    val description: String,
    val price: String,
    val priceAmountMicros: Long,
    val priceCurrencyCode: String,
    val subscriptionPeriod: String? = null,
    val freeTrialPeriod: String? = null,
    val isPopular: Boolean = false,
    val savingsPercentage: Int? = null
)
