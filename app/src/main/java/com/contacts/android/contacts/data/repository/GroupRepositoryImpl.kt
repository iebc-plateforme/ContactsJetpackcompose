package com.contacts.android.contacts.data.repository

import com.contacts.android.contacts.data.local.dao.ContactGroupDao
import com.contacts.android.contacts.data.local.dao.GroupDao
import com.contacts.android.contacts.data.local.entity.ContactGroupCrossRef
import com.contacts.android.contacts.data.mapper.toDomain
import com.contacts.android.contacts.data.mapper.toEntity
import com.contacts.android.contacts.domain.model.Contact
import com.contacts.android.contacts.domain.model.Group
import com.contacts.android.contacts.domain.repository.GroupRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class GroupRepositoryImpl @Inject constructor(
    private val groupDao: GroupDao,
    private val contactGroupDao: ContactGroupDao
) : GroupRepository {

    override fun getAllGroups(): Flow<List<Group>> {
        return groupDao.getAllGroups().map {
            it.map { group -> group.toDomain() }
        }
    }

    override fun getGroupById(id: Long): Flow<Group?> {
        return groupDao.getGroupByIdFlow(id).flatMapLatest { groupEntity ->
            if (groupEntity == null) {
                flowOf(null)
            } else {
                contactGroupDao.getContactCountForGroupFlow(groupEntity.id).map { contactCount ->
                    groupEntity.toDomain(contactCount = contactCount)
                }
            }
        }
    }

    override suspend fun insertGroup(group: Group): Long {
        return groupDao.insertGroup(group.toEntity())
    }

    override suspend fun updateGroup(group: Group) {
        groupDao.updateGroup(group.toEntity())
    }

    override suspend fun deleteGroup(group: Group) {
        groupDao.deleteGroup(group.toEntity())
    }

    override suspend fun deleteGroupById(id: Long) {
        groupDao.deleteGroupById(id)
    }

    override fun getGroupsForContact(contactId: Long): Flow<List<Group>> {
        return kotlinx.coroutines.flow.flow {
            val groupEntities = groupDao.getGroupsForContact(contactId)
            val groups = groupEntities.map { groupEntity ->
                val contactCount = contactGroupDao.getContactCountForGroup(groupEntity.id)
                groupEntity.toDomain(contactCount = contactCount)
            }
            emit(groups)
        }
    }

    override suspend fun addContactToGroup(contactId: Long, groupId: Long) {
        contactGroupDao.insertContactGroup(ContactGroupCrossRef(contactId, groupId))
    }

    override suspend fun removeContactFromGroup(contactId: Long, groupId: Long) {
        contactGroupDao.deleteContactGroup(contactId, groupId)
    }

    override fun getContactsByGroupId(groupId: Long): Flow<List<Contact>> {
        return contactGroupDao.getContactsByGroupId(groupId).map { contacts ->
            contacts.map { it.toDomain() }
        }
    }

    override suspend fun getContactCountForGroup(groupId: Long): Int {
        return contactGroupDao.getContactCountForGroup(groupId)
    }

    override suspend fun getGroupCount(): Int {
        return groupDao.getGroupCount()
    }

    override fun getGroupCountFlow(): Flow<Int> {
        return groupDao.getGroupCountFlow()
    }

    override suspend fun syncGroups(
        groupsToInsert: List<Group>,
        groupsToUpdate: List<Group>,
        groupsToDelete: List<Group>
    ) {
        // Insert new groups
        groupsToInsert.forEach { group ->
            groupDao.insertGroup(group.toEntity())
        }

        // Update existing groups
        groupsToUpdate.forEach { group ->
            groupDao.updateGroup(group.toEntity())
        }

        // Delete removed groups
        groupsToDelete.forEach { group ->
            groupDao.deleteGroup(group.toEntity())
        }
    }
}
