package com.contacts.android.contacts.domain.usecase.group

import com.contacts.android.contacts.domain.model.Group
import com.contacts.android.contacts.domain.repository.GroupRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class GetGroupByIdUseCase @Inject constructor(
    private val repository: GroupRepository
) {
    operator fun invoke(id: Long): Flow<Group?> {
        return repository.getGroupById(id)
    }
}
