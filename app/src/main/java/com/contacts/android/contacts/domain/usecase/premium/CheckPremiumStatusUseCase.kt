package com.contacts.android.contacts.domain.usecase.premium

import com.contacts.android.contacts.domain.model.PremiumStatus
import com.contacts.android.contacts.domain.repository.PremiumRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

/**
 * Use case to check the current premium subscription status
 */
class CheckPremiumStatusUseCase @Inject constructor(
    private val premiumRepository: PremiumRepository
) {
    operator fun invoke(): Flow<PremiumStatus> {
        return premiumRepository.premiumStatus
    }

    suspend fun isPremium(): Boolean {
        return premiumRepository.isPremium()
    }
}
