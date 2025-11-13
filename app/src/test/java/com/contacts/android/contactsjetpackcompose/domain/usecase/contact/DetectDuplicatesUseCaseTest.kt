package com.contacts.android.contactsjetpackcompose.domain.usecase.contact

import com.contacts.android.contactsjetpackcompose.domain.model.*
import com.contacts.android.contactsjetpackcompose.domain.repository.ContactRepository
import io.mockk.coEvery
import io.mockk.mockk
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class DetectDuplicatesUseCaseTest {

    private lateinit var contactRepository: ContactRepository
    private lateinit var detectDuplicatesUseCase: DetectDuplicatesUseCase

    @Before
    fun setup() {
        contactRepository = mockk()
        detectDuplicatesUseCase = DetectDuplicatesUseCase(contactRepository)
    }

    @Test
    fun `detectDuplicates finds contacts with same name`() = runTest {
        // Given
        val contact1 = Contact(
            id = 1,
            firstName = "John",
            lastName = "Doe",
            photoUri = null,
            isFavorite = false,
            phoneNumbers = listOf(PhoneNumber(1, 1, "1111111111", PhoneType.MOBILE, null)),
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
        val contact2 = Contact(
            id = 2,
            firstName = "John",
            lastName = "Doe",
            photoUri = null,
            isFavorite = false,
            phoneNumbers = listOf(PhoneNumber(2, 2, "2222222222", PhoneType.MOBILE, null)),
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

        coEvery { contactRepository.getAllContacts() } returns flowOf(listOf(contact1, contact2))

        // When
        val result = detectDuplicatesUseCase()

        // Then
        assertTrue(result.isSuccess)
        val duplicateGroups = result.getOrNull()
        assertEquals(1, duplicateGroups?.size)
        assertEquals(2, duplicateGroups?.first()?.contacts?.size)
        assertEquals(DuplicateReason.SAME_NAME, duplicateGroups?.first()?.reason)
    }

    @Test
    fun `detectDuplicates finds contacts with same phone number`() = runTest {
        // Given
        val contact1 = Contact(
            id = 1,
            firstName = "Alice",
            lastName = "Smith",
            photoUri = null,
            isFavorite = false,
            phoneNumbers = listOf(PhoneNumber(1, 1, "1234567890", PhoneType.MOBILE, null)),
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
        val contact2 = Contact(
            id = 2,
            firstName = "Alicia",
            lastName = "Smithson",
            photoUri = null,
            isFavorite = false,
            phoneNumbers = listOf(PhoneNumber(2, 2, "1234567890", PhoneType.HOME, null)),
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

        coEvery { contactRepository.getAllContacts() } returns flowOf(listOf(contact1, contact2))

        // When
        val result = detectDuplicatesUseCase()

        // Then
        assertTrue(result.isSuccess)
        val duplicateGroups = result.getOrNull()
        assertEquals(1, duplicateGroups?.size)
        assertEquals(DuplicateReason.SAME_PHONE, duplicateGroups?.first()?.reason)
    }

    @Test
    fun `detectDuplicates returns empty list when no duplicates`() = runTest {
        // Given
        val contact1 = Contact(
            id = 1,
            firstName = "Alice",
            lastName = "Anderson",
            photoUri = null,
            isFavorite = false,
            phoneNumbers = listOf(PhoneNumber(1, 1, "1111111111", PhoneType.MOBILE, null)),
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
        val contact2 = Contact(
            id = 2,
            firstName = "Bob",
            lastName = "Brown",
            photoUri = null,
            isFavorite = false,
            phoneNumbers = listOf(PhoneNumber(2, 2, "2222222222", PhoneType.MOBILE, null)),
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

        coEvery { contactRepository.getAllContacts() } returns flowOf(listOf(contact1, contact2))

        // When
        val result = detectDuplicatesUseCase()

        // Then
        assertTrue(result.isSuccess)
        val duplicateGroups = result.getOrNull()
        assertEquals(0, duplicateGroups?.size)
    }

    @Test
    fun `detectDuplicates handles contacts with no phone numbers`() = runTest {
        // Given
        val contact1 = Contact(
            id = 1,
            firstName = "Charlie",
            lastName = "Clark",
            photoUri = null,
            isFavorite = false,
            phoneNumbers = emptyList(),
            emails = listOf(Email(1, 1, "charlie@test.com", EmailType.HOME, null)),
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
        val contact2 = Contact(
            id = 2,
            firstName = "Charlie",
            lastName = "Clark",
            photoUri = null,
            isFavorite = false,
            phoneNumbers = emptyList(),
            emails = listOf(Email(2, 2, "charlie.clark@test.com", EmailType.WORK, null)),
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

        coEvery { contactRepository.getAllContacts() } returns flowOf(listOf(contact1, contact2))

        // When
        val result = detectDuplicatesUseCase()

        // Then
        assertTrue(result.isSuccess)
        val duplicateGroups = result.getOrNull()
        assertEquals(1, duplicateGroups?.size)
        assertEquals(DuplicateReason.SAME_NAME, duplicateGroups?.first()?.reason)
    }

    @Test
    fun `detectDuplicates normalizes phone numbers correctly`() = runTest {
        // Given
        val contact1 = Contact(
            id = 1,
            firstName = "David",
            lastName = "Davis",
            photoUri = null,
            isFavorite = false,
            phoneNumbers = listOf(PhoneNumber(1, 1, "(123) 456-7890", PhoneType.MOBILE, null)),
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
        val contact2 = Contact(
            id = 2,
            firstName = "Dave",
            lastName = "Davidson",
            photoUri = null,
            isFavorite = false,
            phoneNumbers = listOf(PhoneNumber(2, 2, "123-456-7890", PhoneType.HOME, null)),
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

        coEvery { contactRepository.getAllContacts() } returns flowOf(listOf(contact1, contact2))

        // When
        val result = detectDuplicatesUseCase()

        // Then
        assertTrue(result.isSuccess)
        val duplicateGroups = result.getOrNull()
        assertEquals(1, duplicateGroups?.size)
        assertEquals(DuplicateReason.SAME_PHONE, duplicateGroups?.first()?.reason)
    }

    @Test
    fun `detectDuplicates handles repository errors`() = runTest {
        // Given
        coEvery { contactRepository.getAllContacts() } throws Exception("Database error")

        // When
        val result = detectDuplicatesUseCase()

        // Then
        assertTrue(result.isFailure)
    }
}
