package com.contacts.android.contactsjetpackcompose.domain.usecase.group

import com.contacts.android.contactsjetpackcompose.domain.repository.GroupRepository
import javax.inject.Inject

class RemoveContactFromGroupUseCase @Inject constructor(
    private val repository: GroupRepository
) {
    suspend operator fun invoke(contactId: Long, groupId: Long): Result<Unit> {
        return try {
            repository.removeContactFromGroup(contactId, groupId)
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
