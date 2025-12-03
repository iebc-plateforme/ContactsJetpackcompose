package com.contacts.android.contacts.domain.usecase.premium

import com.contacts.android.contacts.domain.repository.PremiumRepository
import javax.inject.Inject

/**
 * Use case to restore previous purchases
 */
class RestorePurchasesUseCase @Inject constructor(
    private val premiumRepository: PremiumRepository
) {
    suspend operator fun invoke(): Result<Boolean> {
        return premiumRepository.restorePurchases()
    }
}
