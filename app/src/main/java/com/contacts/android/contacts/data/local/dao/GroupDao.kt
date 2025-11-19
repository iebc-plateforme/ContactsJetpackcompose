package com.contacts.android.contacts.data.local.dao

import androidx.room.*
import com.contacts.android.contacts.data.local.entity.GroupEntity
import com.contacts.android.contacts.data.local.relation.GroupWithContactCount
import kotlinx.coroutines.flow.Flow

@Dao
interface GroupDao {

    @Query("SELECT * FROM GroupWithContactCount ORDER BY name COLLATE NOCASE ASC")
    fun getAllGroups(): Flow<List<GroupWithContactCount>>

    @Query("SELECT * FROM groups WHERE id = :id")
    suspend fun getGroupById(id: Long): GroupEntity?

    @Query("SELECT * FROM groups WHERE id = :id")
    fun getGroupByIdFlow(id: Long): Flow<GroupEntity?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertGroup(group: GroupEntity): Long

    @Update
    suspend fun updateGroup(group: GroupEntity)

    @Delete
    suspend fun deleteGroup(group: GroupEntity)

    @Query("DELETE FROM groups WHERE id = :id")
    suspend fun deleteGroupById(id: Long)

    @Query("""
        SELECT groups.* FROM groups
        INNER JOIN contact_group_cross_ref ON groups.id = contact_group_cross_ref.groupId
        WHERE contact_group_cross_ref.contactId = :contactId
        ORDER BY name COLLATE NOCASE ASC
    """)
    suspend fun getGroupsForContact(contactId: Long): List<GroupEntity>

    @Query("SELECT COUNT(*) FROM groups")
    suspend fun getGroupCount(): Int

    @Query("SELECT COUNT(*) FROM groups")
    fun getGroupCountFlow(): Flow<Int>

    @Query("DELETE FROM groups")
    suspend fun deleteAll()
}
