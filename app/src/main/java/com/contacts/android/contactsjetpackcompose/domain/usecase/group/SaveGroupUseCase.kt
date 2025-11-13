package com.contacts.android.contactsjetpackcompose.domain.usecase.group

import com.contacts.android.contactsjetpackcompose.domain.model.Group
import com.contacts.android.contactsjetpackcompose.domain.repository.GroupRepository
import javax.inject.Inject

class SaveGroupUseCase @Inject constructor(
    private val repository: GroupRepository
) {
    suspend operator fun invoke(group: Group): Result<Long> {
        return try {
            if (group.name.isBlank()) {
                return Result.failure(IllegalArgumentException("Group name cannot be empty"))
            }

            val id = if (group.id == 0L) {
                repository.insertGroup(group)
            } else {
                repository.updateGroup(group)
                group.id
            }
            Result.success(id)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
