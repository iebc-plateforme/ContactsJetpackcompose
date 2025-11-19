package com.contacts.android.contacts.data.local.dao

import androidx.room.*
import com.contacts.android.contacts.data.local.entity.WebsiteEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface WebsiteDao {
    @Query("SELECT * FROM websites WHERE contactId = :contactId")
    fun getWebsitesForContact(contactId: Long): Flow<List<WebsiteEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(website: WebsiteEntity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(websites: List<WebsiteEntity>)

    @Update
    suspend fun update(website: WebsiteEntity)

    @Delete
    suspend fun delete(website: WebsiteEntity)

    @Query("DELETE FROM websites WHERE contactId = :contactId")
    suspend fun deleteAllForContact(contactId: Long)
}
