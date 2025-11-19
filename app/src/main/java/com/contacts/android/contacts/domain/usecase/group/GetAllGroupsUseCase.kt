package com.contacts.android.contacts.domain.usecase.group

import com.contacts.android.contacts.domain.model.Group
import com.contacts.android.contacts.domain.repository.GroupRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class GetAllGroupsUseCase @Inject constructor(
    private val repository: GroupRepository
) {
    operator fun invoke(): Flow<List<Group>> {
        return repository.getAllGroups()
    }
}
