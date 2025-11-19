package com.contacts.android.contacts.domain.usecase.contact

import com.contacts.android.contacts.domain.model.*
import com.contacts.android.contacts.domain.repository.ContactRepository
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.just
import io.mockk.mockk
import io.mockk.Runs
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Test
import kotlin.test.assertTrue

class SaveContactUseCaseTest {

    private lateinit var contactRepository: ContactRepository
    private lateinit var saveContactUseCase: SaveContactUseCase

    @Before
    fun setup() {
        contactRepository = mockk()
        saveContactUseCase = SaveContactUseCase(contactRepository)
    }

    @Test
    fun `saveContact inserts new contact when id is 0`() = runTest {
        // Given
        val contact = Contact(
            id = 0,
            firstName = "John",
            lastName = "Doe",
            photoUri = null,
            isFavorite = false,
            phoneNumbers = listOf(
                PhoneNumber(0, 0, "1234567890", PhoneType.MOBILE, null)
            ),
            emails = emptyList(),
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

        coEvery { contactRepository.insertContact(any()) } just Runs

        // When
        val result = saveContactUseCase(contact)

        // Then
        assertTrue(result.isSuccess)
        coVerify { contactRepository.insertContact(contact) }
        coVerify(exactly = 0) { contactRepository.updateContact(any()) }
    }

    @Test
    fun `saveContact updates existing contact when id is not 0`() = runTest {
        // Given
        val contact = Contact(
            id = 1,
            firstName = "Jane",
            lastName = "Smith",
            photoUri = null,
            isFavorite = true,
            phoneNumbers = listOf(
                PhoneNumber(1, 1, "0987654321", PhoneType.HOME, null)
            ),
            emails = listOf(
                Email(1, 1, "jane@example.com", EmailType.WORK, null)
            ),
            addresses = emptyList(),
            groups = emptyList(),
            organization = "Acme Corp",
            title = "Manager",
            notes = "Important contact",
            website = null,
            birthday = null,
            createdAt = System.currentTimeMillis(),
            updatedAt = System.currentTimeMillis()
        )

        coEvery { contactRepository.updateContact(any()) } just Runs

        // When
        val result = saveContactUseCase(contact)

        // Then
        assertTrue(result.isSuccess)
        coVerify { contactRepository.updateContact(contact) }
        coVerify(exactly = 0) { contactRepository.insertContact(any()) }
    }

    @Test
    fun `saveContact handles repository errors gracefully`() = runTest {
        // Given
        val contact = Contact(
            id = 0,
            firstName = "Test",
            lastName = "User",
            photoUri = null,
            isFavorite = false,
            phoneNumbers = emptyList(),
            emails = emptyList(),
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

        coEvery { contactRepository.insertContact(any()) } throws Exception("Database error")

        // When
        val result = saveContactUseCase(contact)

        // Then
        assertTrue(result.isFailure)
        coVerify { contactRepository.insertContact(contact) }
    }

    @Test
    fun `saveContact saves contact with multiple phone numbers and emails`() = runTest {
        // Given
        val contact = Contact(
            id = 0,
            firstName = "Multi",
            lastName = "Field",
            photoUri = null,
            isFavorite = false,
            phoneNumbers = listOf(
                PhoneNumber(0, 0, "1111111111", PhoneType.MOBILE, null),
                PhoneNumber(0, 0, "2222222222", PhoneType.HOME, null),
                PhoneNumber(0, 0, "3333333333", PhoneType.WORK, null)
            ),
            emails = listOf(
                Email(0, 0, "personal@test.com", EmailType.HOME, null),
                Email(0, 0, "work@test.com", EmailType.WORK, null)
            ),
            addresses = listOf(
                Address(
                    id = 0,
                    contactId = 0,
                    street = "123 Main St",
                    city = "Springfield",
                    state = "IL",
                    postalCode = "62701",
                    country = "USA",
                    type = AddressType.HOME,
                    label = null
                )
            ),
            groups = emptyList(),
            organization = "Test Org",
            title = "Tester",
            notes = "Test contact with multiple fields",
            website = "https://test.com",
            birthday = null,
            createdAt = System.currentTimeMillis(),
            updatedAt = System.currentTimeMillis()
        )

        coEvery { contactRepository.insertContact(any()) } just Runs

        // When
        val result = saveContactUseCase(contact)

        // Then
        assertTrue(result.isSuccess)
        coVerify { contactRepository.insertContact(contact) }
    }
}
