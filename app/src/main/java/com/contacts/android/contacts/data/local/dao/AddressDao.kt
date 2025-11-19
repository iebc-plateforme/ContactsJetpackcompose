package com.contacts.android.contacts.data.local.dao

import androidx.room.*
import com.contacts.android.contacts.data.local.entity.AddressEntity

@Dao
interface AddressDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAddress(address: AddressEntity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAddresses(addresses: List<AddressEntity>)

    @Update
    suspend fun updateAddress(address: AddressEntity)

    @Delete
    suspend fun deleteAddress(address: AddressEntity)

    @Query("DELETE FROM addresses WHERE id = :id")
    suspend fun deleteAddressById(id: Long)

    @Query("DELETE FROM addresses WHERE contactId = :contactId")
    suspend fun deleteAddressesByContactId(contactId: Long)

    @Query("SELECT * FROM addresses WHERE contactId = :contactId")
    suspend fun getAddressesByContactId(contactId: Long): List<AddressEntity>

    @Query("SELECT * FROM addresses WHERE id = :id")
    suspend fun getAddressById(id: Long): AddressEntity?

    @Query("DELETE FROM addresses")
    suspend fun deleteAll()
}
