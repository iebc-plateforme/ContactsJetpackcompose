package com.contacts.android.contacts.domain.usecase.contact

import com.contacts.android.contacts.domain.repository.ContactRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import javax.inject.Inject

/**
 * Use case for deleting multiple contacts at once (batch delete)
 */
class DeleteMultipleContactsUseCase @Inject constructor(
    private val contactRepository: ContactRepository
) {
    /**
     * Delete multiple contacts by their IDs
     *
     * @param contactIds List of contact IDs to delete
     * @return Result with number of deleted contacts, or error
     */
    suspend operator fun invoke(contactIds: List<Long>): Result<Int> = withContext(Dispatchers.IO) {
        try {
            if (contactIds.isEmpty()) {
                return@withContext Result.failure(
                    IllegalArgumentException("No contacts selected for deletion")
                )
            }

            var deletedCount = 0
            contactIds.forEach { id ->
                try {
                    contactRepository.deleteContactById(id)
                    deletedCount++
                } catch (e: Exception) {
                    // Log but continue with other contacts
                    e.printStackTrace()
                }
            }

            if (deletedCount == 0) {
                Result.failure(Exception("Failed to delete any contacts"))
            } else {
                Result.success(deletedCount)
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
