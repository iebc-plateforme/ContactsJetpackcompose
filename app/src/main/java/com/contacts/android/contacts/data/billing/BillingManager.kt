package com.contacts.android.contacts.data.billing

import android.app.Activity
import android.content.Context
import com.android.billingclient.api.*
import com.contacts.android.contacts.domain.model.*
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.suspendCancellableCoroutine
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.coroutines.resume

/**
 * Manager class for Google Play Billing operations
 * Handles subscription purchases, queries, and verification
 */
@Singleton
class BillingManager @Inject constructor(
    @ApplicationContext private val context: Context
) : PurchasesUpdatedListener {

    private val scope = CoroutineScope(Dispatchers.IO)

    private var billingClient: BillingClient? = null

    private val _billingState = MutableStateFlow<BillingState>(BillingState.Disconnected)
    val billingState: StateFlow<BillingState> = _billingState.asStateFlow()

    private val _purchaseResult = MutableStateFlow<PurchaseResult?>(null)
    val purchaseResult: StateFlow<PurchaseResult?> = _purchaseResult.asStateFlow()

    private val _availableProducts = MutableStateFlow<List<SubscriptionProduct>>(emptyList())
    val availableProducts: StateFlow<List<SubscriptionProduct>> = _availableProducts.asStateFlow()

    /**
     * Initialize billing client and establish connection
     */
    fun initializeBilling() {
        if (billingClient != null && billingClient?.isReady == true) {
            _billingState.value = BillingState.Connected
            return
        }

        _billingState.value = BillingState.Connecting

        billingClient = BillingClient.newBuilder(context)
            .setListener(this)
            .enablePendingPurchases()
            .build()

        billingClient?.startConnection(object : BillingClientStateListener {
            override fun onBillingSetupFinished(billingResult: BillingResult) {
                if (billingResult.responseCode == BillingClient.BillingResponseCode.OK) {
                    _billingState.value = BillingState.Connected
                    // Query available products and active purchases
                    scope.launch {
                        queryProducts()
                        queryActivePurchases()
                    }
                } else {
                    _billingState.value = BillingState.Error(
                        "Billing setup failed: ${billingResult.debugMessage}"
                    )
                }
            }

            override fun onBillingServiceDisconnected() {
                _billingState.value = BillingState.Disconnected
                // Try to reconnect
                scope.launch {
                    kotlinx.coroutines.delay(3000)
                    if (_billingState.value is BillingState.Disconnected) {
                        initializeBilling()
                    }
                }
            }
        })
    }

    /**
     * Query available subscription products from Google Play
     * Queries INAPP and SUBS separately as they cannot be mixed in one request
     */
    suspend fun queryProducts(): Result<List<SubscriptionProduct>> = suspendCancellableCoroutine { continuation ->
        val client = billingClient
        if (client == null || !client.isReady) {
            continuation.resume(Result.failure(Exception("Billing client not ready")))
            return@suspendCancellableCoroutine
        }

        val allProducts = mutableListOf<SubscriptionProduct>()

        // First, query INAPP products (Lifetime)
        val inAppProductList = listOf(
            QueryProductDetailsParams.Product.newBuilder()
                .setProductId(SubscriptionType.LIFETIME.productId)
                .setProductType(BillingClient.ProductType.INAPP)
                .build()
        )

        val inAppParams = QueryProductDetailsParams.newBuilder()
            .setProductList(inAppProductList)
            .build()

        client.queryProductDetailsAsync(inAppParams) { inAppResult, inAppProductDetailsList ->
            if (inAppResult.responseCode == BillingClient.BillingResponseCode.OK) {
                // Process INAPP products
                val inAppProducts = inAppProductDetailsList.mapNotNull { productDetails ->
                    val pricing = productDetails.oneTimePurchaseOfferDetails
                    pricing?.let {
                        SubscriptionProduct(
                            productId = productDetails.productId,
                            type = SubscriptionType.LIFETIME,
                            title = productDetails.title,
                            description = productDetails.description,
                            price = it.formattedPrice,
                            priceAmountMicros = it.priceAmountMicros,
                            priceCurrencyCode = it.priceCurrencyCode,
                            isPopular = false
                        )
                    }
                }
                allProducts.addAll(inAppProducts)

                // Now query SUBS products (Annual)
                val subsProductList = listOf(
                    QueryProductDetailsParams.Product.newBuilder()
                        .setProductId(SubscriptionType.ANNUAL.productId)
                        .setProductType(BillingClient.ProductType.SUBS)
                        .build()
                )

                val subsParams = QueryProductDetailsParams.newBuilder()
                    .setProductList(subsProductList)
                    .build()

                client.queryProductDetailsAsync(subsParams) { subsResult, subsProductDetailsList ->
                    if (subsResult.responseCode == BillingClient.BillingResponseCode.OK) {
                        // Process SUBS products
                        val subsProducts = subsProductDetailsList.mapNotNull { productDetails ->
                            val pricing = productDetails.subscriptionOfferDetails?.firstOrNull()
                                ?.pricingPhases?.pricingPhaseList?.firstOrNull()
                            pricing?.let {
                                SubscriptionProduct(
                                    productId = productDetails.productId,
                                    type = SubscriptionType.ANNUAL,
                                    title = productDetails.title,
                                    description = productDetails.description,
                                    price = it.formattedPrice,
                                    priceAmountMicros = it.priceAmountMicros,
                                    priceCurrencyCode = it.priceCurrencyCode,
                                    subscriptionPeriod = it.billingPeriod,
                                    isPopular = true,
                                    savingsPercentage = 60
                                )
                            }
                        }
                        allProducts.addAll(subsProducts)

                        _availableProducts.value = allProducts
                        continuation.resume(Result.success(allProducts))
                    } else {
                        // SUBS query failed, but we have INAPP
                        _availableProducts.value = allProducts
                        continuation.resume(Result.success(allProducts))
                    }
                }
            } else {
                continuation.resume(Result.failure(Exception(inAppResult.debugMessage)))
            }
        }
    }

    /**
     * Launch purchase flow for a product
     */
    suspend fun launchPurchaseFlow(activity: Activity, productId: String): PurchaseResult {
        val client = billingClient
        if (client == null || !client.isReady) {
            return PurchaseResult.Error("Billing client not ready")
        }

        // Get product details
        val productList = listOf(
            QueryProductDetailsParams.Product.newBuilder()
                .setProductId(productId)
                .setProductType(
                    if (productId == SubscriptionType.LIFETIME.productId)
                        BillingClient.ProductType.INAPP
                    else
                        BillingClient.ProductType.SUBS
                )
                .build()
        )

        val params = QueryProductDetailsParams.newBuilder()
            .setProductList(productList)
            .build()

        return suspendCancellableCoroutine { continuation ->
            client.queryProductDetailsAsync(params) { billingResult, productDetailsList ->
                if (billingResult.responseCode != BillingClient.BillingResponseCode.OK || productDetailsList.isEmpty()) {
                    continuation.resume(PurchaseResult.Error("Product not found"))
                    return@queryProductDetailsAsync
                }

                val productDetails = productDetailsList.first()
                val productDetailsParamsList = if (productDetails.productType == BillingClient.ProductType.SUBS) {
                    val offerToken = productDetails.subscriptionOfferDetails?.firstOrNull()?.offerToken
                    if (offerToken == null) {
                        continuation.resume(PurchaseResult.Error("No subscription offer available"))
                        return@queryProductDetailsAsync
                    }
                    listOf(
                        BillingFlowParams.ProductDetailsParams.newBuilder()
                            .setProductDetails(productDetails)
                            .setOfferToken(offerToken)
                            .build()
                    )
                } else {
                    listOf(
                        BillingFlowParams.ProductDetailsParams.newBuilder()
                            .setProductDetails(productDetails)
                            .build()
                    )
                }

                val flowParams = BillingFlowParams.newBuilder()
                    .setProductDetailsParamsList(productDetailsParamsList)
                    .build()

                val launchResult = client.launchBillingFlow(activity, flowParams)

                if (launchResult.responseCode == BillingClient.BillingResponseCode.OK) {
                    // Wait for purchase callback
                    scope.launch {
                        _purchaseResult.collect { result ->
                            result?.let {
                                continuation.resume(it)
                            }
                        }
                    }
                } else {
                    continuation.resume(PurchaseResult.Error(launchResult.debugMessage))
                }
            }
        }
    }

    /**
     * Callback when purchase is updated
     */
    override fun onPurchasesUpdated(billingResult: BillingResult, purchases: List<Purchase>?) {
        when (billingResult.responseCode) {
            BillingClient.BillingResponseCode.OK -> {
                purchases?.forEach { purchase ->
                    scope.launch {
                        handlePurchase(purchase)
                    }
                }
            }
            BillingClient.BillingResponseCode.USER_CANCELED -> {
                _purchaseResult.value = PurchaseResult.Cancelled
            }
            BillingClient.BillingResponseCode.ITEM_ALREADY_OWNED -> {
                _purchaseResult.value = PurchaseResult.Success
            }
            else -> {
                _purchaseResult.value = PurchaseResult.Error(billingResult.debugMessage)
            }
        }
    }

    /**
     * Handle a purchase
     */
    private suspend fun handlePurchase(purchase: Purchase) {
        if (purchase.purchaseState == Purchase.PurchaseState.PURCHASED) {
            // Acknowledge the purchase if not already acknowledged
            if (!purchase.isAcknowledged) {
                acknowledgePurchase(purchase)
            }
            _purchaseResult.value = PurchaseResult.Success
        } else if (purchase.purchaseState == Purchase.PurchaseState.PENDING) {
            _purchaseResult.value = PurchaseResult.Pending
        }
    }

    /**
     * Acknowledge a purchase
     */
    private suspend fun acknowledgePurchase(purchase: Purchase) = suspendCancellableCoroutine { continuation ->
        val client = billingClient ?: run {
            continuation.resume(Unit)
            return@suspendCancellableCoroutine
        }

        val params = AcknowledgePurchaseParams.newBuilder()
            .setPurchaseToken(purchase.purchaseToken)
            .build()

        client.acknowledgePurchase(params) { billingResult ->
            continuation.resume(Unit)
        }
    }

    /**
     * Query active purchases
     */
    suspend fun queryActivePurchases(): List<Purchase> = suspendCancellableCoroutine { continuation ->
        val client = billingClient
        if (client == null || !client.isReady) {
            continuation.resume(emptyList())
            return@suspendCancellableCoroutine
        }

        // Query in-app purchases (lifetime)
        val inAppParams = QueryPurchasesParams.newBuilder()
            .setProductType(BillingClient.ProductType.INAPP)
            .build()

        client.queryPurchasesAsync(inAppParams) { inAppResult, inAppPurchases ->
            if (inAppResult.responseCode == BillingClient.BillingResponseCode.OK) {
                // Query subscriptions (annual)
                val subsParams = QueryPurchasesParams.newBuilder()
                    .setProductType(BillingClient.ProductType.SUBS)
                    .build()

                client.queryPurchasesAsync(subsParams) { subsResult, subsPurchases ->
                    if (subsResult.responseCode == BillingClient.BillingResponseCode.OK) {
                        val allPurchases = (inAppPurchases + subsPurchases).filter {
                            it.purchaseState == Purchase.PurchaseState.PURCHASED
                        }
                        continuation.resume(allPurchases)
                    } else {
                        continuation.resume(inAppPurchases.filter {
                            it.purchaseState == Purchase.PurchaseState.PURCHASED
                        })
                    }
                }
            } else {
                continuation.resume(emptyList())
            }
        }
    }

    /**
     * End billing connection
     */
    fun endConnection() {
        billingClient?.endConnection()
        billingClient = null
        _billingState.value = BillingState.Disconnected
    }
}
