package com.contacts.android.contacts.presentation.screens.premium

import android.app.Activity
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.contacts.android.contacts.BuildConfig
import com.contacts.android.contacts.data.repository.PremiumRepositoryImpl
import com.contacts.android.contacts.domain.model.*
import com.contacts.android.contacts.domain.repository.PremiumRepository
import com.contacts.android.contacts.domain.usecase.premium.CheckPremiumStatusUseCase
import com.contacts.android.contacts.domain.usecase.premium.GetAvailableProductsUseCase
import com.contacts.android.contacts.domain.usecase.premium.HasFeatureAccessUseCase
import com.contacts.android.contacts.domain.usecase.premium.RestorePurchasesUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * ViewModel for Premium subscription screen
 *
 * Test Mode:
 * - Debug builds automatically use mock products (no Google Play Console needed)
 * - Release builds use real Google Play Billing
 * - Controlled by BuildConfig.USE_PREMIUM_TEST_MODE
 */
@HiltViewModel
class PremiumViewModel @Inject constructor(
    private val premiumRepository: PremiumRepository,
    private val premiumRepositoryImpl: PremiumRepositoryImpl,
    private val checkPremiumStatusUseCase: CheckPremiumStatusUseCase,
    private val getAvailableProductsUseCase: GetAvailableProductsUseCase,
    private val restorePurchasesUseCase: RestorePurchasesUseCase,
    private val hasFeatureAccessUseCase: HasFeatureAccessUseCase
) : ViewModel() {

    companion object {
        // Automatically set by build type:
        // - Debug: true (mock products for local testing)
        // - Release: false (real Google Play Billing)
        private val USE_TEST_MODE = BuildConfig.USE_PREMIUM_TEST_MODE
    }

    private val _uiState = MutableStateFlow<PremiumUiState>(PremiumUiState.Loading)
    val uiState: StateFlow<PremiumUiState> = _uiState.asStateFlow()

    val premiumStatus: StateFlow<PremiumStatus> = checkPremiumStatusUseCase()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = PremiumStatus()
        )

    val billingState: StateFlow<BillingState> = premiumRepository.billingState
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = BillingState.Disconnected
        )

    init {
        initializeBilling()
    }

    private fun initializeBilling() {
        viewModelScope.launch {
            _uiState.value = PremiumUiState.Loading
            premiumRepository.initializeBilling()

            // Wait for billing to be connected
            billingState.collect { state ->
                when (state) {
                    is BillingState.Connected -> {
                        loadProducts()
                    }
                    is BillingState.Error -> {
                        _uiState.value = PremiumUiState.Error(state.message)
                    }
                    else -> {
                        // Keep loading
                    }
                }
            }
        }
    }

    private suspend fun loadProducts() {
        if (USE_TEST_MODE) {
            // Use test products for local testing
            kotlinx.coroutines.delay(500) // Simulate network delay
            val testProducts = listOf(
                SubscriptionProduct(
                    productId = "premium_annual",
                    type = SubscriptionType.ANNUAL,
                    title = "Premium Annual (Test)",
                    description = "Unlock all premium features for one year - Best Value!",
                    price = "$2.49",
                    priceAmountMicros = 2490000,
                    priceCurrencyCode = "USD",
                    subscriptionPeriod = "P1Y",
                    isPopular = true,
                    savingsPercentage = 50
                ),
                SubscriptionProduct(
                    productId = "premium_lifetime",
                    type = SubscriptionType.LIFETIME,
                    title = "Premium Lifetime (Test)",
                    description = "Unlock all premium features forever - One-time payment!",
                    price = "$4.99",
                    priceAmountMicros = 4990000,
                    priceCurrencyCode = "USD",
                    isPopular = false
                )
            )
            _uiState.value = PremiumUiState.Success(testProducts)
        } else {
            // Real Google Play Billing products
            val result = getAvailableProductsUseCase()
            result.fold(
                onSuccess = { products ->
                    _uiState.value = PremiumUiState.Success(products)
                },
                onFailure = { error ->
                    _uiState.value = PremiumUiState.Error(
                        error.message ?: "Failed to load products"
                    )
                }
            )
        }
    }

    fun purchaseProduct(activity: Activity, productId: String) {
        viewModelScope.launch {
            _uiState.value = PremiumUiState.Purchasing

            if (USE_TEST_MODE) {
                // Simulate test purchase
                kotlinx.coroutines.delay(1500) // Simulate processing

                // Simulate successful purchase and save premium status
                val subscriptionType = SubscriptionType.fromProductId(productId)
                val purchaseDate = System.currentTimeMillis()
                val expiryDate = if (subscriptionType == SubscriptionType.LIFETIME) {
                    null // Lifetime doesn't expire
                } else {
                    purchaseDate + (365L * 24 * 60 * 60 * 1000) // 1 year
                }

                // Save premium status locally
                premiumRepositoryImpl.updatePremiumStatusTest(
                    isPremium = true,
                    productId = productId,
                    purchaseDate = purchaseDate,
                    expiryDate = expiryDate,
                    isAutoRenewing = subscriptionType != SubscriptionType.LIFETIME
                )

                _uiState.value = PremiumUiState.PurchaseSuccess
                kotlinx.coroutines.delay(1000)
                loadProducts()
            } else {
                // Real purchase flow
                try {
                    val result = premiumRepositoryImpl.launchPurchaseFlow(activity, productId)

                    when (result) {
                        is PurchaseResult.Success -> {
                            _uiState.value = PremiumUiState.PurchaseSuccess
                            loadProducts()
                        }
                        is PurchaseResult.Cancelled -> {
                            loadProducts()
                        }
                        is PurchaseResult.Error -> {
                            _uiState.value = PremiumUiState.Error(result.message)
                            kotlinx.coroutines.delay(2000)
                            loadProducts()
                        }
                        is PurchaseResult.Pending -> {
                            _uiState.value = PremiumUiState.PurchasePending
                            kotlinx.coroutines.delay(2000)
                            loadProducts()
                        }
                    }
                } catch (e: Exception) {
                    _uiState.value = PremiumUiState.Error(
                        e.message ?: "Purchase failed"
                    )
                    kotlinx.coroutines.delay(2000)
                    loadProducts()
                }
            }
        }
    }

    fun restorePurchases() {
        viewModelScope.launch {
            _uiState.value = PremiumUiState.Loading

            val result = restorePurchasesUseCase()
            result.fold(
                onSuccess = { hasPurchases ->
                    if (hasPurchases) {
                        _uiState.value = PremiumUiState.RestoreSuccess
                        kotlinx.coroutines.delay(2000)
                    }
                    loadProducts()
                },
                onFailure = { error ->
                    _uiState.value = PremiumUiState.Error(
                        error.message ?: "Failed to restore purchases"
                    )
                    kotlinx.coroutines.delay(2000)
                    loadProducts()
                }
            )
        }
    }

    fun dismissError() {
        viewModelScope.launch {
            loadProducts()
        }
    }

    override fun onCleared() {
        super.onCleared()
        premiumRepository.endBillingConnection()
    }
}

/**
 * UI State for Premium screen
 */
sealed class PremiumUiState {
    data object Loading : PremiumUiState()
    data class Success(val products: List<SubscriptionProduct>) : PremiumUiState()
    data class Error(val message: String) : PremiumUiState()
    data object Purchasing : PremiumUiState()
    data object PurchaseSuccess : PremiumUiState()
    data object PurchasePending : PremiumUiState()
    data object RestoreSuccess : PremiumUiState()
}
