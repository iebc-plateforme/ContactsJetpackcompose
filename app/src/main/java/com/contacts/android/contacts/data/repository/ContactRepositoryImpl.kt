package com.contacts.android.contacts.data.repository

import androidx.room.withTransaction
import com.contacts.android.contacts.data.local.database.ContactsDatabase
import com.contacts.android.contacts.data.local.dao.*
import com.contacts.android.contacts.data.local.entity.ContactGroupCrossRef
import com.contacts.android.contacts.data.mapper.*
import com.contacts.android.contacts.domain.model.Contact
import com.contacts.android.contacts.domain.repository.ContactRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ContactRepositoryImpl @Inject constructor(
    private val contactDao: ContactDao,
    private val phoneNumberDao: PhoneNumberDao,
    private val emailDao: EmailDao,
    private val addressDao: AddressDao,
    private val contactGroupDao: ContactGroupDao,
    private val db: ContactsDatabase
) : ContactRepository {

    override fun getAllContacts(): Flow<List<Contact>> {
        return contactDao.getAllContactsFlow().map { contacts ->
            contacts.map { it.toDomain() }
        }
    }

    override suspend fun getAllContactsOnce(): List<Contact> {
        return contactDao.getAllContacts().map { it.toDomain() }
    }

    override fun getContactById(id: Long): Flow<Contact?> {
        return contactDao.getContactByIdFlow(id).map { it?.toDomain() }
    }

    override suspend fun insertContact(contact: Contact): Long {
        return db.withTransaction {
            _insertContact(contact)
        }
    }

    override suspend fun updateContact(contact: Contact) {
        db.withTransaction {
            _updateContact(contact)
        }
    }

    override suspend fun deleteContact(contacts: List<Contact>) {
        contactDao.deleteContacts(contacts.map { it.toEntity() })
    }

    override suspend fun deleteContactById(id: Long) {
        contactDao.deleteContactById(id)
    }

    override fun getFavoriteContacts(): Flow<List<Contact>> {
        return contactDao.getFavoriteContacts().map { contacts ->
            contacts.map { it.toDomain() }
        }
    }

    override fun searchContacts(query: String): Flow<List<Contact>> {
        return contactDao.searchContacts(query).map { contacts ->
            contacts.map { it.toDomain() }
        }
    }

    override suspend fun toggleFavorite(id: Long, isFavorite: Boolean) {
        contactDao.toggleFavorite(id, isFavorite)
    }

    override suspend fun getContactCount(): Int {
        return contactDao.getContactCount()
    }

    override fun getContactCountFlow(): Flow<Int> {
        return contactDao.getContactCountFlow()
    }

    override suspend fun deleteAllContacts() {
        db.withTransaction {
            phoneNumberDao.deleteAll()
            emailDao.deleteAll()
            addressDao.deleteAll()
            contactGroupDao.deleteAll()
            contactDao.deleteAllContacts()
        }
    }

    override suspend fun syncContacts(insert: List<Contact>, update: List<Contact>, delete: List<Contact>) {
        db.withTransaction {
            if (insert.isNotEmpty()) {
                insert.forEach { _insertContact(it) }
            }
            if (update.isNotEmpty()) {
                update.forEach { _updateContact(it) }
            }
            if (delete.isNotEmpty()) {
                deleteContact(delete)
            }
        }
    }

    private suspend fun _insertContact(contact: Contact): Long {
        val contactId = contactDao.insertContact(contact.toEntity())
        if (contact.phoneNumbers.isNotEmpty()) {
            phoneNumberDao.insertPhoneNumbers(contact.phoneNumbers.map { it.toEntity(contactId) })
        }
        if (contact.emails.isNotEmpty()) {
            emailDao.insertEmails(contact.emails.map { it.toEntity(contactId) })
        }
        if (contact.addresses.isNotEmpty()) {
            addressDao.insertAddresses(contact.addresses.map { it.toEntity(contactId) })
        }
        if (contact.groups.isNotEmpty()) {
            contactGroupDao.insertContactGroups(contact.groups.map { ContactGroupCrossRef(contactId, it.id) })
        }
        return contactId
    }

    private suspend fun _updateContact(contact: Contact) {
        contactDao.updateContact(contact.toEntity())
        phoneNumberDao.deletePhoneNumbersByContactId(contact.id)
        emailDao.deleteEmailsByContactId(contact.id)
        addressDao.deleteAddressesByContactId(contact.id)
        contactGroupDao.deleteContactGroupsByContactId(contact.id)

        if (contact.phoneNumbers.isNotEmpty()) {
            phoneNumberDao.insertPhoneNumbers(contact.phoneNumbers.map { it.toEntity(contact.id) })
        }
        if (contact.emails.isNotEmpty()) {
            emailDao.insertEmails(contact.emails.map { it.toEntity(contact.id) })
        }
        if (contact.addresses.isNotEmpty()) {
            addressDao.insertAddresses(contact.addresses.map { it.toEntity(contact.id) })
        }
        if (contact.groups.isNotEmpty()) {
            contactGroupDao.insertContactGroups(contact.groups.map { ContactGroupCrossRef(contact.id, it.id) })
        }
    }
}
