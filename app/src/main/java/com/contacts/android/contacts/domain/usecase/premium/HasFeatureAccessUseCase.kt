package com.contacts.android.contacts.domain.usecase.premium

import com.contacts.android.contacts.domain.model.PremiumFeature
import com.contacts.android.contacts.domain.repository.PremiumRepository
import javax.inject.Inject

/**
 * Use case to check if user has access to a specific premium feature
 */
class HasFeatureAccessUseCase @Inject constructor(
    private val premiumRepository: PremiumRepository
) {
    suspend operator fun invoke(feature: PremiumFeature): Boolean {
        return premiumRepository.hasFeatureAccess(feature.name)
    }

    suspend fun hasNoAds(): Boolean {
        return premiumRepository.isPremium()
    }
}
