package com.contacts.android.contacts.domain.usecase.premium

import com.contacts.android.contacts.domain.model.SubscriptionProduct
import com.contacts.android.contacts.domain.repository.PremiumRepository
import javax.inject.Inject

/**
 * Use case to get available subscription products
 */
class GetAvailableProductsUseCase @Inject constructor(
    private val premiumRepository: PremiumRepository
) {
    suspend operator fun invoke(): Result<List<SubscriptionProduct>> {
        return premiumRepository.getAvailableProducts()
    }
}
