package com.contacts.android.contactsjetpackcompose.data.local.dao

import androidx.room.*
import com.contacts.android.contactsjetpackcompose.data.local.entity.ContactGroupCrossRef
import com.contacts.android.contactsjetpackcompose.data.local.entity.ContactWithDetails
import kotlinx.coroutines.flow.Flow

@Dao
interface ContactGroupDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertContactGroup(crossRef: ContactGroupCrossRef)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertContactGroups(crossRefs: List<ContactGroupCrossRef>)

    @Delete
    suspend fun deleteContactGroup(crossRef: ContactGroupCrossRef)

    @Query("DELETE FROM contact_group_cross_ref WHERE contactId = :contactId AND groupId = :groupId")
    suspend fun deleteContactGroup(contactId: Long, groupId: Long)

    @Query("DELETE FROM contact_group_cross_ref WHERE contactId = :contactId")
    suspend fun deleteContactGroupsByContactId(contactId: Long)

    @Query("DELETE FROM contact_group_cross_ref WHERE groupId = :groupId")
    suspend fun deleteContactGroupsByGroupId(groupId: Long)

    @Transaction
    @Query("""
        SELECT contacts.* FROM contacts
        INNER JOIN contact_group_cross_ref ON contacts.id = contact_group_cross_ref.contactId
        WHERE contact_group_cross_ref.groupId = :groupId
        ORDER BY firstName COLLATE NOCASE ASC, lastName COLLATE NOCASE ASC
    """)
    fun getContactsByGroupId(groupId: Long): Flow<List<ContactWithDetails>>

    @Query("""
        SELECT COUNT(*) FROM contact_group_cross_ref
        WHERE groupId = :groupId
    """)
    suspend fun getContactCountForGroup(groupId: Long): Int

    @Query("""
        SELECT COUNT(*) FROM contact_group_cross_ref
        WHERE groupId = :groupId
    """)
    fun getContactCountForGroupFlow(groupId: Long): Flow<Int>

    @Query("""
        SELECT contactId FROM contact_group_cross_ref
        WHERE groupId = :groupId
    """)
    suspend fun getContactIdsByGroupId(groupId: Long): List<Long>
}
