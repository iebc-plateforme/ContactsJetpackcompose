package com.contacts.android.contactsjetpackcompose.domain.usecase.group

import com.contacts.android.contactsjetpackcompose.domain.model.Group
import com.contacts.android.contactsjetpackcompose.domain.repository.GroupRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class GetAllGroupsUseCase @Inject constructor(
    private val repository: GroupRepository
) {
    operator fun invoke(): Flow<List<Group>> {
        return repository.getAllGroups()
    }
}
