package com.contacts.android.contacts.data.local.dao

import androidx.room.*
import com.contacts.android.contacts.data.local.entity.InstantMessageEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface InstantMessageDao {
    @Query("SELECT * FROM instant_messages WHERE contactId = :contactId")
    fun getInstantMessagesForContact(contactId: Long): Flow<List<InstantMessageEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(instantMessage: InstantMessageEntity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(instantMessages: List<InstantMessageEntity>)

    @Update
    suspend fun update(instantMessage: InstantMessageEntity)

    @Delete
    suspend fun delete(instantMessage: InstantMessageEntity)

    @Query("DELETE FROM instant_messages WHERE contactId = :contactId")
    suspend fun deleteAllForContact(contactId: Long)
}
