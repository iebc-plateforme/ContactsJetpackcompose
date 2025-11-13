package com.contacts.android.contactsjetpackcompose.domain.usecase.group

import com.contacts.android.contactsjetpackcompose.domain.model.Contact
import com.contacts.android.contactsjetpackcompose.domain.model.Group
import com.contacts.android.contactsjetpackcompose.domain.repository.GroupRepository
import kotlinx.coroutines.flow.first
import javax.inject.Inject

data class GroupWithContacts(
    val group: Group,
    val contacts: List<Contact>
)

class GetGroupWithContactsUseCase @Inject constructor(
    private val groupRepository: GroupRepository
) {
    suspend operator fun invoke(groupId: Long): GroupWithContacts? {
        val group = groupRepository.getGroupById(groupId).first()
        return group?.let {
            val contacts = groupRepository.getContactsByGroupId(groupId).first()
            GroupWithContacts(it, contacts)
        }
    }
}
