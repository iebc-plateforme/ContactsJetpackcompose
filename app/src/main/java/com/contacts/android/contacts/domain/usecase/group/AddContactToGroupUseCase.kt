package com.contacts.android.contacts.domain.usecase.group

import com.contacts.android.contacts.domain.repository.GroupRepository
import javax.inject.Inject

class AddContactToGroupUseCase @Inject constructor(
    private val repository: GroupRepository
) {
    suspend operator fun invoke(contactId: Long, groupId: Long): Result<Unit> {
        return try {
            repository.addContactToGroup(contactId, groupId)
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
