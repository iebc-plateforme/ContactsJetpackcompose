package com.contacts.android.contacts.domain.repository

import com.contacts.android.contacts.domain.model.Contact
import kotlinx.coroutines.flow.Flow

interface ContactRepository {

    fun getAllContacts(): Flow<List<Contact>>

    suspend fun getAllContactsOnce(): List<Contact>

    fun getContactById(id: Long): Flow<Contact?>

    suspend fun insertContact(contact: Contact): Long

    suspend fun updateContact(contact: Contact)

    suspend fun deleteContact(contacts: List<Contact>)

    suspend fun deleteContactById(id: Long)

    fun getFavoriteContacts(): Flow<List<Contact>>

    fun searchContacts(query: String): Flow<List<Contact>>

    suspend fun toggleFavorite(id: Long, isFavorite: Boolean)

    suspend fun getContactCount(): Int

    fun getContactCountFlow(): Flow<Int>

    suspend fun deleteAllContacts()

    suspend fun syncContacts(insert: List<Contact>, update: List<Contact>, delete: List<Contact>)
}
