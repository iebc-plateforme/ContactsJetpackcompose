package com.contacts.android.contactsjetpackcompose.domain.usecase.group

import com.contacts.android.contactsjetpackcompose.domain.repository.GroupRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import javax.inject.Inject

/**
 * Use case for adding multiple contacts to a group at once
 */
class AddContactsToGroupUseCase @Inject constructor(
    private val groupRepository: GroupRepository
) {
    /**
     * Add multiple contacts to a group
     *
     * @param contactIds List of contact IDs to add
     * @param groupId The target group ID
     * @return Result with number of contacts added, or error
     */
    suspend operator fun invoke(contactIds: List<Long>, groupId: Long): Result<Int> = withContext(Dispatchers.IO) {
        try {
            if (contactIds.isEmpty()) {
                return@withContext Result.failure(
                    IllegalArgumentException("No contacts selected")
                )
            }

            var addedCount = 0
            contactIds.forEach { contactId ->
                try {
                    groupRepository.addContactToGroup(contactId, groupId)
                    addedCount++
                } catch (e: Exception) {
                    // Log but continue with other contacts
                    e.printStackTrace()
                }
            }

            if (addedCount == 0) {
                Result.failure(Exception("Failed to add any contacts to group"))
            } else {
                Result.success(addedCount)
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
