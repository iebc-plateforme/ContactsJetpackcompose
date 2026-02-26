package com.contacts.android.contacts.data.local.dao

import androidx.room.*
import com.contacts.android.contacts.data.local.entity.TagEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface TagDao {
    @Query("SELECT * FROM contact_tags WHERE contactId = :contactId")
    fun getTagsForContact(contactId: Long): Flow<List<TagEntity>>

    @Query("SELECT * FROM contact_tags WHERE contactId = :contactId")
    suspend fun getTagsForContactOnce(contactId: Long): List<TagEntity>

    @Query("SELECT DISTINCT tag FROM contact_tags ORDER BY tag COLLATE NOCASE ASC")
    fun getAllUniqueTags(): Flow<List<String>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTag(tag: TagEntity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTags(tags: List<TagEntity>)

    @Query("DELETE FROM contact_tags WHERE contactId = :contactId AND tag = :tag")
    suspend fun deleteTag(contactId: Long, tag: String)

    @Query("DELETE FROM contact_tags WHERE contactId = :contactId")
    suspend fun deleteAllTagsForContact(contactId: Long)
}
