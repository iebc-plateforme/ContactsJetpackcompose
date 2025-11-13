package com.contacts.android.contactsjetpackcompose.domain.repository

import com.contacts.android.contactsjetpackcompose.domain.model.Contact
import kotlinx.coroutines.flow.Flow

interface ContactRepository {

    fun getAllContacts(): Flow<List<Contact>>

    fun getContactById(id: Long): Flow<Contact?>

    suspend fun insertContact(contact: Contact): Long

    suspend fun updateContact(contact: Contact)

    suspend fun deleteContact(contact: Contact)

    suspend fun deleteContactById(id: Long)

    fun getFavoriteContacts(): Flow<List<Contact>>

    fun searchContacts(query: String): Flow<List<Contact>>

    suspend fun toggleFavorite(id: Long, isFavorite: Boolean)

    suspend fun getContactCount(): Int

    fun getContactCountFlow(): Flow<Int>

    suspend fun deleteAllContacts()
}
