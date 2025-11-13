package com.contacts.android.contactsjetpackcompose.data.repository

import com.contacts.android.contactsjetpackcompose.data.local.dao.*
import com.contacts.android.contactsjetpackcompose.data.local.entity.*
import com.contacts.android.contactsjetpackcompose.data.mapper.*
import com.contacts.android.contactsjetpackcompose.domain.model.*
import io.mockk.*
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull

class ContactRepositoryImplTest {

    private lateinit var contactDao: ContactDao
    private lateinit var phoneNumberDao: PhoneNumberDao
    private lateinit var emailDao: EmailDao
    private lateinit var addressDao: AddressDao
    private lateinit var contactGroupDao: ContactGroupDao
    private lateinit var repository: ContactRepositoryImpl

    @Before
    fun setup() {
        contactDao = mockk()
        phoneNumberDao = mockk()
        emailDao = mockk()
        addressDao = mockk()
        contactGroupDao = mockk()

        repository = ContactRepositoryImpl(
            contactDao = contactDao,
            phoneNumberDao = phoneNumberDao,
            emailDao = emailDao,
            addressDao = addressDao,
            contactGroupDao = contactGroupDao
        )
    }

    @Test
    fun `getAllContacts returns mapped contacts`() = runTest {
        // Given
        val contactEntity = ContactEntity(
            id = 1,
            firstName = "John",
            lastName = "Doe",
            photoUri = null,
            isFavorite = false,
            createdAt = System.currentTimeMillis(),
            updatedAt = System.currentTimeMillis()
        )
        val phoneEntity = PhoneNumberEntity(
            id = 1,
            contactId = 1,
            number = "1234567890",
            type = PhoneType.MOBILE,
            label = null
        )
        val contactWithDetails = ContactWithDetails(
            contact = contactEntity,
            phoneNumbers = listOf(phoneEntity),
            emails = emptyList(),
            addresses = emptyList(),
            groups = emptyList()
        )

        coEvery { contactDao.getAllContacts() } returns flowOf(listOf(contactWithDetails))

        // When
        val result = repository.getAllContacts().first()

        // Then
        assertEquals(1, result.size)
        assertEquals("John", result[0].firstName)
        assertEquals("Doe", result[0].lastName)
        assertEquals(1, result[0].phoneNumbers.size)
        assertEquals("1234567890", result[0].phoneNumbers[0].number)
        coVerify { contactDao.getAllContacts() }
    }

    @Test
    fun `getContactById returns mapped contact`() = runTest {
        // Given
        val contactEntity = ContactEntity(
            id = 1,
            firstName = "Jane",
            lastName = "Smith",
            photoUri = null,
            isFavorite = true,
            createdAt = System.currentTimeMillis(),
            updatedAt = System.currentTimeMillis()
        )
        val emailEntity = EmailEntity(
            id = 1,
            contactId = 1,
            email = "jane@example.com",
            type = EmailType.WORK,
            label = null
        )
        val contactWithDetails = ContactWithDetails(
            contact = contactEntity,
            phoneNumbers = emptyList(),
            emails = listOf(emailEntity),
            addresses = emptyList(),
            groups = emptyList()
        )

        coEvery { contactDao.getContactById(1) } returns flowOf(contactWithDetails)

        // When
        val result = repository.getContactById(1).first()

        // Then
        assertNotNull(result)
        assertEquals("Jane", result?.firstName)
        assertEquals("Smith", result?.lastName)
        assertEquals(true, result?.isFavorite)
        assertEquals(1, result?.emails?.size)
        assertEquals("jane@example.com", result?.emails?.get(0)?.email)
        coVerify { contactDao.getContactById(1) }
    }

    @Test
    fun `insertContact inserts contact and related entities`() = runTest {
        // Given
        val contact = Contact(
            id = 0,
            firstName = "Alice",
            lastName = "Johnson",
            photoUri = null,
            isFavorite = false,
            phoneNumbers = listOf(
                PhoneNumber(0, 0, "1111111111", PhoneType.MOBILE, null)
            ),
            emails = listOf(
                Email(0, 0, "alice@test.com", EmailType.HOME, null)
            ),
            addresses = emptyList(),
            groups = emptyList(),
            organization = null,
            title = null,
            notes = null,
            website = null,
            birthday = null,
            createdAt = System.currentTimeMillis(),
            updatedAt = System.currentTimeMillis()
        )

        coEvery { contactDao.insertContact(any()) } returns 1L
        coEvery { phoneNumberDao.insertPhoneNumbers(any()) } just Runs
        coEvery { emailDao.insertEmails(any()) } just Runs
        coEvery { addressDao.insertAddresses(any()) } just Runs

        // When
        repository.insertContact(contact)

        // Then
        coVerify { contactDao.insertContact(any()) }
        coVerify { phoneNumberDao.insertPhoneNumbers(any()) }
        coVerify { emailDao.insertEmails(any()) }
    }

    @Test
    fun `updateContact updates contact and related entities`() = runTest {
        // Given
        val contact = Contact(
            id = 1,
            firstName = "Bob",
            lastName = "Brown",
            photoUri = null,
            isFavorite = false,
            phoneNumbers = listOf(
                PhoneNumber(1, 1, "2222222222", PhoneType.HOME, null)
            ),
            emails = emptyList(),
            addresses = emptyList(),
            groups = emptyList(),
            organization = "Acme Corp",
            title = "Manager",
            notes = "Test notes",
            website = null,
            birthday = null,
            createdAt = System.currentTimeMillis(),
            updatedAt = System.currentTimeMillis()
        )

        coEvery { contactDao.updateContact(any()) } just Runs
        coEvery { phoneNumberDao.deletePhoneNumbersByContactId(1) } just Runs
        coEvery { phoneNumberDao.insertPhoneNumbers(any()) } just Runs
        coEvery { emailDao.deleteEmailsByContactId(1) } just Runs
        coEvery { emailDao.insertEmails(any()) } just Runs
        coEvery { addressDao.deleteAddressesByContactId(1) } just Runs
        coEvery { addressDao.insertAddresses(any()) } just Runs

        // When
        repository.updateContact(contact)

        // Then
        coVerify { contactDao.updateContact(any()) }
        coVerify { phoneNumberDao.deletePhoneNumbersByContactId(1) }
        coVerify { phoneNumberDao.insertPhoneNumbers(any()) }
    }

    @Test
    fun `deleteContactById deletes contact`() = runTest {
        // Given
        coEvery { contactDao.deleteContactById(1) } just Runs

        // When
        repository.deleteContactById(1)

        // Then
        coVerify { contactDao.deleteContactById(1) }
    }

    @Test
    fun `searchContacts returns filtered contacts`() = runTest {
        // Given
        val contactEntity = ContactEntity(
            id = 1,
            firstName = "Charlie",
            lastName = "Davis",
            photoUri = null,
            isFavorite = false,
            createdAt = System.currentTimeMillis(),
            updatedAt = System.currentTimeMillis()
        )
        val contactWithDetails = ContactWithDetails(
            contact = contactEntity,
            phoneNumbers = emptyList(),
            emails = emptyList(),
            addresses = emptyList(),
            groups = emptyList()
        )

        coEvery { contactDao.searchContacts("char") } returns flowOf(listOf(contactWithDetails))

        // When
        val result = repository.searchContacts("char").first()

        // Then
        assertEquals(1, result.size)
        assertEquals("Charlie", result[0].firstName)
        coVerify { contactDao.searchContacts("char") }
    }

    @Test
    fun `getFavoriteContacts returns favorite contacts only`() = runTest {
        // Given
        val contactEntity = ContactEntity(
            id = 1,
            firstName = "Diana",
            lastName = "Evans",
            photoUri = null,
            isFavorite = true,
            createdAt = System.currentTimeMillis(),
            updatedAt = System.currentTimeMillis()
        )
        val contactWithDetails = ContactWithDetails(
            contact = contactEntity,
            phoneNumbers = emptyList(),
            emails = emptyList(),
            addresses = emptyList(),
            groups = emptyList()
        )

        coEvery { contactDao.getFavoriteContacts() } returns flowOf(listOf(contactWithDetails))

        // When
        val result = repository.getFavoriteContacts().first()

        // Then
        assertEquals(1, result.size)
        assertEquals(true, result[0].isFavorite)
        coVerify { contactDao.getFavoriteContacts() }
    }

    @Test
    fun `toggleFavorite updates favorite status`() = runTest {
        // Given
        coEvery { contactDao.toggleFavorite(1, true) } just Runs

        // When
        repository.toggleFavorite(1, true)

        // Then
        coVerify { contactDao.toggleFavorite(1, true) }
    }
}
