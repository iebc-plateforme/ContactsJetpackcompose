package com.contacts.android.contactsjetpackcompose.domain.usecase.group

import com.contacts.android.contactsjetpackcompose.domain.model.Group
import com.contacts.android.contactsjetpackcompose.domain.repository.GroupRepository
import javax.inject.Inject

class DeleteGroupUseCase @Inject constructor(
    private val repository: GroupRepository
) {
    suspend operator fun invoke(group: Group): Result<Unit> {
        return try {
            repository.deleteGroup(group)
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend operator fun invoke(groupId: Long): Result<Unit> {
        return try {
            repository.deleteGroupById(groupId)
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
