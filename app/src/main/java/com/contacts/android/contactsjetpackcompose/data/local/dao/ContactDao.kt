package com.contacts.android.contactsjetpackcompose.data.local.dao

import androidx.room.*
import com.contacts.android.contactsjetpackcompose.data.local.entity.ContactEntity
import com.contacts.android.contactsjetpackcompose.data.local.entity.ContactWithDetails
import kotlinx.coroutines.flow.Flow

@Dao
interface ContactDao {

    @Transaction
    @Query("SELECT * FROM contacts ORDER BY firstName COLLATE NOCASE ASC, lastName COLLATE NOCASE ASC")
    fun getAllContactsFlow(): Flow<List<ContactWithDetails>>

    @Transaction
    @Query("SELECT * FROM contacts WHERE id = :id")
    suspend fun getContactById(id: Long): ContactWithDetails?

    @Transaction
    @Query("SELECT * FROM contacts WHERE id = :id")
    fun getContactByIdFlow(id: Long): Flow<ContactWithDetails?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertContact(contact: ContactEntity): Long

    @Update
    suspend fun updateContact(contact: ContactEntity)

    @Delete
    suspend fun deleteContact(contact: ContactEntity)

    @Query("DELETE FROM contacts WHERE id = :id")
    suspend fun deleteContactById(id: Long)

    @Transaction
    @Query("SELECT * FROM contacts WHERE isFavorite = 1 ORDER BY firstName COLLATE NOCASE ASC, lastName COLLATE NOCASE ASC")
    fun getFavoriteContacts(): Flow<List<ContactWithDetails>>

    @Transaction
    @Query("""
        SELECT * FROM contacts
        WHERE firstName LIKE '%' || :query || '%'
        OR lastName LIKE '%' || :query || '%'
        ORDER BY firstName COLLATE NOCASE ASC, lastName COLLATE NOCASE ASC
    """)
    fun searchContacts(query: String): Flow<List<ContactWithDetails>>

    @Query("UPDATE contacts SET isFavorite = :isFavorite WHERE id = :id")
    suspend fun toggleFavorite(id: Long, isFavorite: Boolean)

    @Query("SELECT COUNT(*) FROM contacts")
    suspend fun getContactCount(): Int

    @Query("SELECT COUNT(*) FROM contacts")
    fun getContactCountFlow(): Flow<Int>

    @Query("DELETE FROM contacts")
    suspend fun deleteAllContacts()
}
