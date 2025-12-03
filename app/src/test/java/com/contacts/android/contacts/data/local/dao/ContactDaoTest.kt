package com.contacts.android.contacts.data.local.dao

import android.content.Context
import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import com.contacts.android.contacts.data.local.database.ContactsDatabase
import com.contacts.android.contacts.data.local.entity.*
import com.contacts.android.contacts.domain.model.EmailType
import com.contacts.android.contacts.domain.model.PhoneType
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertNull
import kotlin.test.assertTrue

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [28])
class ContactDaoTest {

    @get:Rule
    val instantTaskExecutorRule = InstantTaskExecutorRule()

    private lateinit var database: ContactsDatabase
    private lateinit var contactDao: ContactDao
    private lateinit var phoneNumberDao: PhoneNumberDao
    private lateinit var emailDao: EmailDao
    private lateinit var addressDao: AddressDao

    @Before
    fun setup() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        database = Room.inMemoryDatabaseBuilder(
            context,
            ContactsDatabase::class.java
        )
            .allowMainThreadQueries()
            .build()

        contactDao = database.contactDao()
        phoneNumberDao = database.phoneNumberDao()
        emailDao = database.emailDao()
        addressDao = database.addressDao()
    }

    @After
    fun teardown() {
        database.close()
    }

    @Test
    fun `insertContact and getContactById returns correct contact`() = runTest {
        // Given
        val contact = ContactEntity(
            id = 1,
            firstName = "John",
            lastName = "Doe",
            photoUri = null,
            isFavorite = false,
            createdAt = System.currentTimeMillis(),
            updatedAt = System.currentTimeMillis()
        )

        // When
        contactDao.insertContact(contact)
        val retrieved = contactDao.getContactById(1)

        // Then
        assertNotNull(retrieved)
        assertEquals("John", retrieved?.contact?.firstName)
        assertEquals("Doe", retrieved?.contact?.lastName)
    }

    @Test
    fun `insertContact with phone numbers returns contact with phones`() = runTest {
        // Given
        val contact = ContactEntity(
            id = 1,
            firstName = "Jane",
            lastName = "Smith",
            photoUri = null,
            isFavorite = false,
            createdAt = System.currentTimeMillis(),
            updatedAt = System.currentTimeMillis()
        )
        val phone = PhoneNumberEntity(
            id = 1,
            contactId = 1,
            number = "1234567890",
            type = PhoneType.MOBILE,
            label = null
        )

        // When
        contactDao.insertContact(contact)
        phoneNumberDao.insertPhoneNumber(phone)
        val retrieved = contactDao.getContactById(1)

        // Then
        assertNotNull(retrieved)
        assertEquals(1, retrieved?.phoneNumbers?.size)
        assertEquals("1234567890", retrieved?.phoneNumbers?.first()?.number)
        assertEquals(PhoneType.MOBILE, retrieved?.phoneNumbers?.first()?.type)
    }

    @Test
    fun `updateContact modifies existing contact`() = runTest {
        // Given
        val contact = ContactEntity(
            id = 1,
            firstName = "John",
            lastName = "Doe",
            photoUri = null,
            isFavorite = false,
            createdAt = System.currentTimeMillis(),
            updatedAt = System.currentTimeMillis()
        )
        contactDao.insertContact(contact)

        // When
        val updated = contact.copy(firstName = "Jane", isFavorite = true)
        contactDao.updateContact(updated)
        val retrieved = contactDao.getContactById(1)

        // Then
        assertNotNull(retrieved)
        assertEquals("Jane", retrieved?.contact?.firstName)
        assertTrue(retrieved?.contact?.isFavorite == true)
    }

    @Test
    fun `deleteContact removes contact from database`() = runTest {
        // Given
        val contact = ContactEntity(
            id = 1,
            firstName = "John",
            lastName = "Doe",
            photoUri = null,
            isFavorite = false,
            createdAt = System.currentTimeMillis(),
            updatedAt = System.currentTimeMillis()
        )
        contactDao.insertContact(contact)

        // When
        contactDao.deleteContactById(contact.id)
        val retrieved = contactDao.getContactById(1)

        // Then
        assertNull(retrieved)
    }

    @Test
    fun `getAllContacts returns all contacts ordered by firstName`() = runTest {
        // Given
        val contact1 = ContactEntity(
            id = 1,
            firstName = "Alice",
            lastName = "Anderson",
            photoUri = null,
            isFavorite = false,
            createdAt = System.currentTimeMillis(),
            updatedAt = System.currentTimeMillis()
        )
        val contact2 = ContactEntity(
            id = 2,
            firstName = "Bob",
            lastName = "Brown",
            photoUri = null,
            isFavorite = false,
            createdAt = System.currentTimeMillis(),
            updatedAt = System.currentTimeMillis()
        )
        val contact3 = ContactEntity(
            id = 3,
            firstName = "Charlie",
            lastName = "Clark",
            photoUri = null,
            isFavorite = false,
            createdAt = System.currentTimeMillis(),
            updatedAt = System.currentTimeMillis()
        )

        // When
        contactDao.insertContact(contact2)
        contactDao.insertContact(contact1)
        contactDao.insertContact(contact3)
        val contacts = contactDao.getAllContactsFlow().first()

        // Then
        assertEquals(3, contacts.size)
        assertEquals("Alice", contacts[0].contact.firstName)
        assertEquals("Bob", contacts[1].contact.firstName)
        assertEquals("Charlie", contacts[2].contact.firstName)
    }

    @Test
    fun `getFavoriteContacts returns only favorite contacts`() = runTest {
        // Given
        val contact1 = ContactEntity(
            id = 1,
            firstName = "John",
            lastName = "Doe",
            photoUri = null,
            isFavorite = true,
            createdAt = System.currentTimeMillis(),
            updatedAt = System.currentTimeMillis()
        )
        val contact2 = ContactEntity(
            id = 2,
            firstName = "Jane",
            lastName = "Smith",
            photoUri = null,
            isFavorite = false,
            createdAt = System.currentTimeMillis(),
            updatedAt = System.currentTimeMillis()
        )

        // When
        contactDao.insertContact(contact1)
        contactDao.insertContact(contact2)
        val favorites = contactDao.getFavoriteContacts().first()

        // Then
        assertEquals(1, favorites.size)
        assertEquals("John", favorites[0].contact.firstName)
        assertTrue(favorites[0].contact.isFavorite)
    }

    @Test
    fun `searchContacts finds contacts by firstName`() = runTest {
        // Given
        val contact1 = ContactEntity(
            id = 1,
            firstName = "John",
            lastName = "Doe",
            photoUri = null,
            isFavorite = false,
            createdAt = System.currentTimeMillis(),
            updatedAt = System.currentTimeMillis()
        )
        val contact2 = ContactEntity(
            id = 2,
            firstName = "Jane",
            lastName = "Smith",
            photoUri = null,
            isFavorite = false,
            createdAt = System.currentTimeMillis(),
            updatedAt = System.currentTimeMillis()
        )

        // When
        contactDao.insertContact(contact1)
        contactDao.insertContact(contact2)
        val results = contactDao.searchContacts("joh").first()

        // Then
        assertEquals(1, results.size)
        assertEquals("John", results[0].contact.firstName)
    }

    @Test
    fun `searchContacts finds contacts by lastName`() = runTest {
        // Given
        val contact1 = ContactEntity(
            id = 1,
            firstName = "John",
            lastName = "Doe",
            photoUri = null,
            isFavorite = false,
            createdAt = System.currentTimeMillis(),
            updatedAt = System.currentTimeMillis()
        )
        val contact2 = ContactEntity(
            id = 2,
            firstName = "Jane",
            lastName = "Smith",
            photoUri = null,
            isFavorite = false,
            createdAt = System.currentTimeMillis(),
            updatedAt = System.currentTimeMillis()
        )

        // When
        contactDao.insertContact(contact1)
        contactDao.insertContact(contact2)
        val results = contactDao.searchContacts("smi").first()

        // Then
        assertEquals(1, results.size)
        assertEquals("Jane", results[0].contact.firstName)
    }

    @Test
    fun `toggleFavorite updates favorite status`() = runTest {
        // Given
        val contact = ContactEntity(
            id = 1,
            firstName = "John",
            lastName = "Doe",
            photoUri = null,
            isFavorite = false,
            createdAt = System.currentTimeMillis(),
            updatedAt = System.currentTimeMillis()
        )
        contactDao.insertContact(contact)

        // When
        contactDao.toggleFavorite(1, true)
        val retrieved = contactDao.getContactById(1)

        // Then
        assertNotNull(retrieved)
        assertTrue(retrieved?.contact?.isFavorite == true)
    }

    @Test
    fun `deleteContactById removes contact and cascades to related entities`() = runTest {
        // Given
        val contact = ContactEntity(
            id = 1,
            firstName = "John",
            lastName = "Doe",
            photoUri = null,
            isFavorite = false,
            createdAt = System.currentTimeMillis(),
            updatedAt = System.currentTimeMillis()
        )
        val phone = PhoneNumberEntity(
            id = 1,
            contactId = 1,
            number = "1234567890",
            type = PhoneType.MOBILE,
            label = null
        )
        val email = EmailEntity(
            id = 1,
            contactId = 1,
            email = "john@example.com",
            type = EmailType.HOME,
            label = null
        )

        contactDao.insertContact(contact)
        phoneNumberDao.insertPhoneNumber(phone)
        emailDao.insertEmail(email)

        // When
        contactDao.deleteContactById(1)
        val retrieved = contactDao.getContactById(1)
        val phones = phoneNumberDao.getPhoneNumbersByContactId(1)
        val emails = emailDao.getEmailsByContactId(1)

        // Then
        assertNull(retrieved)
        assertTrue(phones.isEmpty())
        assertTrue(emails.isEmpty())
    }

    @Test
    fun `insertContact with emails returns contact with emails`() = runTest {
        // Given
        val contact = ContactEntity(
            id = 1,
            firstName = "John",
            lastName = "Doe",
            photoUri = null,
            isFavorite = false,
            createdAt = System.currentTimeMillis(),
            updatedAt = System.currentTimeMillis()
        )
        val email = EmailEntity(
            id = 1,
            contactId = 1,
            email = "john@example.com",
            type = EmailType.HOME,
            label = null
        )

        // When
        contactDao.insertContact(contact)
        emailDao.insertEmail(email)
        val retrieved = contactDao.getContactById(1)

        // Then
        assertNotNull(retrieved)
        assertEquals(1, retrieved?.emails?.size)
        assertEquals("john@example.com", retrieved?.emails?.first()?.email)
    }
}