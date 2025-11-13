package com.contacts.android.contactsjetpackcompose.data.repository

import com.contacts.android.contactsjetpackcompose.data.local.dao.*
import com.contacts.android.contactsjetpackcompose.data.local.entity.ContactGroupCrossRef
import com.contacts.android.contactsjetpackcompose.data.mapper.*
import com.contacts.android.contactsjetpackcompose.domain.model.Contact
import com.contacts.android.contactsjetpackcompose.domain.repository.ContactRepository
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
    private val contactGroupDao: ContactGroupDao
) : ContactRepository {

    override fun getAllContacts(): Flow<List<Contact>> {
        return contactDao.getAllContactsFlow().map { contacts ->
            contacts.map { it.toDomain() }
        }
    }

    override fun getContactById(id: Long): Flow<Contact?> {
        return contactDao.getContactByIdFlow(id).map { it?.toDomain() }
    }

    override suspend fun insertContact(contact: Contact): Long {
        val contactId = contactDao.insertContact(contact.toEntity())

        // Insert related entities
        if (contact.phoneNumbers.isNotEmpty()) {
            phoneNumberDao.insertPhoneNumbers(
                contact.phoneNumbers.map { it.toEntity(contactId) }
            )
        }

        if (contact.emails.isNotEmpty()) {
            emailDao.insertEmails(
                contact.emails.map { it.toEntity(contactId) }
            )
        }

        if (contact.addresses.isNotEmpty()) {
            addressDao.insertAddresses(
                contact.addresses.map { it.toEntity(contactId) }
            )
        }

        if (contact.groups.isNotEmpty()) {
            contactGroupDao.insertContactGroups(
                contact.groups.map { ContactGroupCrossRef(contactId, it.id) }
            )
        }

        return contactId
    }

    override suspend fun updateContact(contact: Contact) {
        // Update main contact
        contactDao.updateContact(contact.toEntity())

        // Delete and re-insert related entities for simplicity
        phoneNumberDao.deletePhoneNumbersByContactId(contact.id)
        emailDao.deleteEmailsByContactId(contact.id)
        addressDao.deleteAddressesByContactId(contact.id)
        contactGroupDao.deleteContactGroupsByContactId(contact.id)

        // Re-insert
        if (contact.phoneNumbers.isNotEmpty()) {
            phoneNumberDao.insertPhoneNumbers(
                contact.phoneNumbers.map { it.toEntity(contact.id) }
            )
        }

        if (contact.emails.isNotEmpty()) {
            emailDao.insertEmails(
                contact.emails.map { it.toEntity(contact.id) }
            )
        }

        if (contact.addresses.isNotEmpty()) {
            addressDao.insertAddresses(
                contact.addresses.map { it.toEntity(contact.id) }
            )
        }

        if (contact.groups.isNotEmpty()) {
            contactGroupDao.insertContactGroups(
                contact.groups.map { ContactGroupCrossRef(contact.id, it.id) }
            )
        }
    }

    override suspend fun deleteContact(contact: Contact) {
        contactDao.deleteContact(contact.toEntity())
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
        contactDao.deleteAllContacts()
    }
}
