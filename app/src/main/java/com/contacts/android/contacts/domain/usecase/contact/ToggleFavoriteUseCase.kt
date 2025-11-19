package com.contacts.android.contacts.domain.usecase.contact

import com.contacts.android.contacts.domain.repository.ContactRepository
import javax.inject.Inject

class ToggleFavoriteUseCase @Inject constructor(
    private val repository: ContactRepository
) {
    suspend operator fun invoke(contactId: Long, isFavorite: Boolean): Result<Unit> {
        return try {
            repository.toggleFavorite(contactId, isFavorite)
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
