package com.contacts.android.contacts.data.repository

import android.util.Log
import com.contacts.android.contacts.data.local.dao.ContactDao
import com.contacts.android.contacts.data.local.dao.ContactGroupDao
import com.contacts.android.contacts.data.local.dao.GroupDao
import com.contacts.android.contacts.data.local.entity.ContactGroupCrossRef
import com.contacts.android.contacts.data.mapper.toDomain
import com.contacts.android.contacts.data.mapper.toEntity
import com.contacts.android.contacts.data.provider.SystemContactsWriter
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
    private val contactGroupDao: ContactGroupDao,
    private val contactDao: ContactDao,
    private val systemContactsWriter: SystemContactsWriter
) : GroupRepository {

    companion object {
        private const val TAG = "GroupRepositoryImpl"
    }

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

    override suspend fun getGroupsByName(name: String): List<Group> {
        return groupDao.getGroupsByName(name).map { groupEntity ->
            val contactCount = contactGroupDao.getContactCountForGroup(groupEntity.id)
            groupEntity.toDomain(contactCount = contactCount)
        }
    }

    override suspend fun insertGroup(group: Group): Long {
        // 1. Insert into local Room DB
        val localId = groupDao.insertGroup(group.toEntity())

        // 2. Also insert into Android system ContactsContract so other apps can see it
        try {
            val systemGroupId = systemContactsWriter.insertGroup(
                title = group.name,
                accountName = group.accountName,
                accountType = group.accountType
            )
            if (systemGroupId != null) {
                groupDao.updateSystemGroupId(localId, systemGroupId)
                Log.d(TAG, "Group synced to system: localId=$localId, systemGroupId=$systemGroupId")
            }
        } catch (e: Exception) {
            Log.e(TAG, "Failed to sync group to system (local insert succeeded)", e)
        }

        return localId
    }

    override suspend fun updateGroup(group: Group) {
        groupDao.updateGroup(group.toEntity())

        // Also update in Android system
        try {
            val systemGroupId = group.systemGroupId ?: groupDao.getSystemGroupId(group.id)
            if (systemGroupId != null) {
                systemContactsWriter.updateGroup(systemGroupId, group.name)
            }
        } catch (e: Exception) {
            Log.e(TAG, "Failed to update group in system", e)
        }
    }

    override suspend fun deleteGroup(group: Group) {
        // Delete from Android system first
        try {
            val systemGroupId = group.systemGroupId ?: groupDao.getSystemGroupId(group.id)
            if (systemGroupId != null) {
                systemContactsWriter.deleteGroup(systemGroupId)
            }
        } catch (e: Exception) {
            Log.e(TAG, "Failed to delete group from system", e)
        }

        groupDao.deleteGroup(group.toEntity())
    }

    override suspend fun deleteGroupById(id: Long) {
        // Delete from Android system first
        try {
            val systemGroupId = groupDao.getSystemGroupId(id)
            if (systemGroupId != null) {
                systemContactsWriter.deleteGroup(systemGroupId)
            }
        } catch (e: Exception) {
            Log.e(TAG, "Failed to delete group from system", e)
        }

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
        // 1. Insert into local Room DB
        contactGroupDao.insertContactGroup(ContactGroupCrossRef(contactId, groupId))

        // 2. Also add to Android system GroupMembership
        try {
            var rawContactId = contactDao.getSystemRawContactId(contactId)

            // Fallback: look up raw_contact_id from the system using contact_id
            // This handles contacts synced before systemRawContactId was stored
            if (rawContactId == null) {
                rawContactId = systemContactsWriter.getRawContactIdForContact(contactId)
                // Save it for future use
                if (rawContactId != null) {
                    contactDao.updateSystemRawContactId(contactId, rawContactId)
                }
            }

            val systemGroupId = groupDao.getSystemGroupId(groupId)
            if (rawContactId != null && systemGroupId != null) {
                systemContactsWriter.addContactToGroup(rawContactId, systemGroupId)
            }
        } catch (e: Exception) {
            Log.e(TAG, "Failed to add contact to group in system", e)
        }
    }

    override suspend fun removeContactFromGroup(contactId: Long, groupId: Long) {
        // 1. Remove from Android system first
        try {
            var rawContactId = contactDao.getSystemRawContactId(contactId)

            // Fallback: look up raw_contact_id from the system
            if (rawContactId == null) {
                rawContactId = systemContactsWriter.getRawContactIdForContact(contactId)
                if (rawContactId != null) {
                    contactDao.updateSystemRawContactId(contactId, rawContactId)
                }
            }

            val systemGroupId = groupDao.getSystemGroupId(groupId)
            if (rawContactId != null && systemGroupId != null) {
                systemContactsWriter.removeContactFromGroup(rawContactId, systemGroupId)
            }
        } catch (e: Exception) {
            Log.e(TAG, "Failed to remove contact from group in system", e)
        }

        // 2. Remove from local Room DB
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

    override suspend fun syncExistingGroupsToSystem() {
        try {
            // 1. Find all local groups that don't have a systemGroupId yet
            val unsyncedGroups = groupDao.getUnsyncedGroups()
            Log.d(TAG, "Found ${unsyncedGroups.size} unsynced groups to push to system")

            for (groupEntity in unsyncedGroups) {
                try {
                    // Create group in Android system
                    val systemGroupId = systemContactsWriter.insertGroup(
                        title = groupEntity.name,
                        accountName = groupEntity.accountName,
                        accountType = groupEntity.accountType
                    )
                    if (systemGroupId != null) {
                        // Save the systemGroupId back to local DB
                        groupDao.updateSystemGroupId(groupEntity.id, systemGroupId)
                        Log.d(TAG, "Synced group '${groupEntity.name}' to system: systemGroupId=$systemGroupId")

                        // 2. Sync all contact memberships for this group
                        val contactIds = contactGroupDao.getContactIdsByGroupId(groupEntity.id)
                        for (contactId in contactIds) {
                            try {
                                var rawContactId = contactDao.getSystemRawContactId(contactId)
                                if (rawContactId == null) {
                                    rawContactId = systemContactsWriter.getRawContactIdForContact(contactId)
                                    if (rawContactId != null) {
                                        contactDao.updateSystemRawContactId(contactId, rawContactId)
                                    }
                                }
                                if (rawContactId != null) {
                                    systemContactsWriter.addContactToGroup(rawContactId, systemGroupId)
                                    Log.d(TAG, "Synced membership: contact=$contactId -> group=${groupEntity.id}")
                                }
                            } catch (e: Exception) {
                                Log.e(TAG, "Failed to sync membership for contact $contactId", e)
                            }
                        }
                    }
                } catch (e: Exception) {
                    Log.e(TAG, "Failed to sync group '${groupEntity.name}' to system", e)
                }
            }

            // 3. Also sync memberships for groups that already have systemGroupId
            //    but whose memberships may not be in the system yet
            val allGroupEntities = groupDao.getAllGroupEntities()
            for (groupEntity in allGroupEntities) {
                val systemGroupId = groupEntity.systemGroupId ?: continue
                if (groupEntity.isSystemGroup) continue // Skip system groups, they manage their own memberships

                val contactIds = contactGroupDao.getContactIdsByGroupId(groupEntity.id)
                for (contactId in contactIds) {
                    try {
                        var rawContactId = contactDao.getSystemRawContactId(contactId)
                        if (rawContactId == null) {
                            rawContactId = systemContactsWriter.getRawContactIdForContact(contactId)
                            if (rawContactId != null) {
                                contactDao.updateSystemRawContactId(contactId, rawContactId)
                            }
                        }
                        if (rawContactId != null) {
                            systemContactsWriter.addContactToGroup(rawContactId, systemGroupId)
                        }
                    } catch (e: Exception) {
                        Log.e(TAG, "Failed to re-sync membership for contact $contactId in group ${groupEntity.id}", e)
                    }
                }
            }

            Log.d(TAG, "Retroactive sync to system completed")
        } catch (e: Exception) {
            Log.e(TAG, "Failed retroactive sync to system", e)
        }
    }
}
