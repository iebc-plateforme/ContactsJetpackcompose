package com.contacts.android.contacts.data.local.dao

import androidx.room.*
import com.contacts.android.contacts.data.local.entity.PhoneNumberEntity

@Dao
interface PhoneNumberDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPhoneNumber(phone: PhoneNumberEntity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPhoneNumbers(phones: List<PhoneNumberEntity>)

    @Update
    suspend fun updatePhoneNumber(phone: PhoneNumberEntity)

    @Delete
    suspend fun deletePhoneNumber(phone: PhoneNumberEntity)

    @Query("DELETE FROM phone_numbers WHERE id = :id")
    suspend fun deletePhoneNumberById(id: Long)

    @Query("DELETE FROM phone_numbers WHERE contactId = :contactId")
    suspend fun deletePhoneNumbersByContactId(contactId: Long)

    @Query("SELECT * FROM phone_numbers WHERE contactId = :contactId")
    suspend fun getPhoneNumbersByContactId(contactId: Long): List<PhoneNumberEntity>

    @Query("SELECT * FROM phone_numbers WHERE id = :id")
    suspend fun getPhoneNumberById(id: Long): PhoneNumberEntity?

    @Query("DELETE FROM phone_numbers")
    suspend fun deleteAll()

    /**
     * Find contact ID by phone number (used for migration data restoration)
     */
    @Query("SELECT contactId FROM phone_numbers WHERE number = :phoneNumber LIMIT 1")
    suspend fun findContactByPhoneNumber(phoneNumber: String): Long?
}
