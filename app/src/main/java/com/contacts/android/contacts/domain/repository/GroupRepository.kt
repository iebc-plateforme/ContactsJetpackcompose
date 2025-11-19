package com.contacts.android.contacts.domain.repository

import com.contacts.android.contacts.domain.model.Contact
import com.contacts.android.contacts.domain.model.Group
import kotlinx.coroutines.flow.Flow

interface GroupRepository {

    fun getAllGroups(): Flow<List<Group>>

    fun getGroupById(id: Long): Flow<Group?>

    suspend fun insertGroup(group: Group): Long

    suspend fun updateGroup(group: Group)

    suspend fun deleteGroup(group: Group)

    suspend fun deleteGroupById(id: Long)

    fun getGroupsForContact(contactId: Long): Flow<List<Group>>

    suspend fun addContactToGroup(contactId: Long, groupId: Long)

    suspend fun removeContactFromGroup(contactId: Long, groupId: Long)

    fun getContactsByGroupId(groupId: Long): Flow<List<Contact>>

    suspend fun getContactCountForGroup(groupId: Long): Int

    suspend fun getGroupCount(): Int

    fun getGroupCountFlow(): Flow<Int>

    /**
     * Sync groups from system to local database
     * Used by SyncGroupsUseCase to perform batch insert/update/delete
     */
    suspend fun syncGroups(
        groupsToInsert: List<Group>,
        groupsToUpdate: List<Group>,
        groupsToDelete: List<Group>
    )
}
