package com.contacts.android.contactsjetpackcompose.data.local.dao

import androidx.room.*
import com.contacts.android.contactsjetpackcompose.data.local.entity.EmailEntity

@Dao
interface EmailDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertEmail(email: EmailEntity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertEmails(emails: List<EmailEntity>)

    @Update
    suspend fun updateEmail(email: EmailEntity)

    @Delete
    suspend fun deleteEmail(email: EmailEntity)

    @Query("DELETE FROM emails WHERE id = :id")
    suspend fun deleteEmailById(id: Long)

    @Query("DELETE FROM emails WHERE contactId = :contactId")
    suspend fun deleteEmailsByContactId(contactId: Long)

    @Query("SELECT * FROM emails WHERE contactId = :contactId")
    suspend fun getEmailsByContactId(contactId: Long): List<EmailEntity>

    @Query("SELECT * FROM emails WHERE id = :id")
    suspend fun getEmailById(id: Long): EmailEntity?
}
