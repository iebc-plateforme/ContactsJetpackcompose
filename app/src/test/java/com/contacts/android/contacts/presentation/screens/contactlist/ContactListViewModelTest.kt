package com.contacts.android.contacts.presentation.screens.contactlist

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import app.cash.turbine.test
import com.contacts.android.contacts.domain.model.*
import com.contacts.android.contacts.domain.usecase.contact.*
import com.contacts.android.contacts.domain.usecase.group.GetAllGroupsUseCase
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.just
import io.mockk.mockk
import io.mockk.Runs
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.*
import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

@OptIn(ExperimentalCoroutinesApi::class)
class ContactListViewModelTest {

    @get:Rule
    val instantTaskExecutorRule = InstantTaskExecutorRule()

    private lateinit var getAllContactsUseCase: GetAllContactsUseCase
    private lateinit var getFavoriteContactsUseCase: GetFavoriteContactsUseCase
    private lateinit var searchContactsUseCase: SearchContactsUseCase
    private lateinit var deleteContactUseCase: DeleteContactUseCase
    private lateinit var deleteMultipleContactsUseCase: DeleteMultipleContactsUseCase
    private lateinit var toggleFavoriteUseCase: ToggleFavoriteUseCase
    private lateinit var getAllGroupsUseCase: GetAllGroupsUseCase
    private lateinit var viewModel: ContactListViewModel

    private val testDispatcher = StandardTestDispatcher()

    @Before
    fun setup() {
        Dispatchers.setMain(testDispatcher)

        getAllContactsUseCase = mockk()
        getFavoriteContactsUseCase = mockk()
        searchContactsUseCase = mockk()
        deleteContactUseCase = mockk()
        deleteMultipleContactsUseCase = mockk()
        toggleFavoriteUseCase = mockk()
        getAllGroupsUseCase = mockk()

        // Default mock behavior
        coEvery { getAllContactsUseCase() } returns flowOf(emptyList())
        coEvery { getFavoriteContactsUseCase() } returns flowOf(emptyList())
        coEvery { getAllGroupsUseCase() } returns flowOf(emptyList())
    }

    @After
    fun teardown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `initial state is correct`() = runTest {
        // When
        viewModel = ContactListViewModel(
            getAllContactsUseCase,
            getFavoriteContactsUseCase,
            searchContactsUseCase,
            deleteContactUseCase,
            deleteMultipleContactsUseCase,
            toggleFavoriteUseCase,
            getAllGroupsUseCase
        )

        // Then
        viewModel.state.test {
            val state = awaitItem()
            assertEquals(emptyList(), state.contacts)
            assertEquals("", state.searchQuery)
            assertEquals(false, state.isLoading)
            assertEquals(false, state.isSelectionMode)
        }
    }

    @Test
    fun `loadContacts fetches contacts from use case`() = runTest {
        // Given
        val contact = Contact(
            id = 1,
            firstName = "John",
            lastName = "Doe",
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

        coEvery { getAllContactsUseCase() } returns flowOf(listOf(contact))

        // When
        viewModel = ContactListViewModel(
            getAllContactsUseCase,
            getFavoriteContactsUseCase,
            searchContactsUseCase,
            deleteContactUseCase,
            deleteMultipleContactsUseCase,
            toggleFavoriteUseCase,
            getAllGroupsUseCase
        )

        advanceUntilIdle()

        // Then
        viewModel.state.test {
            val state = awaitItem()
            assertEquals(1, state.contacts.size)
            assertEquals("John", state.contacts[0].firstName)
        }
    }

    @Test
    fun `onSearchQueryChanged updates search query`() = runTest {
        // Given
        viewModel = ContactListViewModel(
            getAllContactsUseCase,
            getFavoriteContactsUseCase,
            searchContactsUseCase,
            deleteContactUseCase,
            deleteMultipleContactsUseCase,
            toggleFavoriteUseCase,
            getAllGroupsUseCase
        )

        val searchResults = listOf(
            Contact(
                id = 1,
                firstName = "Alice",
                lastName = "Anderson",
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
        )

        coEvery { searchContactsUseCase("ali") } returns flowOf(searchResults)

        // When
        viewModel.onEvent(ContactListEvent.OnSearchQueryChanged("ali"))
        advanceUntilIdle()

        // Then
        viewModel.state.test {
            val state = awaitItem()
            assertEquals("ali", state.searchQuery)
            assertEquals(1, state.contacts.size)
            assertEquals("Alice", state.contacts[0].firstName)
        }
    }

    @Test
    fun `deleteContact calls delete use case`() = runTest {
        // Given
        viewModel = ContactListViewModel(
            getAllContactsUseCase,
            getFavoriteContactsUseCase,
            searchContactsUseCase,
            deleteContactUseCase,
            deleteMultipleContactsUseCase,
            toggleFavoriteUseCase,
            getAllGroupsUseCase
        )

        coEvery { deleteContactUseCase(1) } just Runs

        // When
        viewModel.onEvent(ContactListEvent.DeleteContact(1))
        advanceUntilIdle()

        // Then
        coVerify { deleteContactUseCase(1) }
    }

    @Test
    fun `toggleFavorite updates contact favorite status`() = runTest {
        // Given
        viewModel = ContactListViewModel(
            getAllContactsUseCase,
            getFavoriteContactsUseCase,
            searchContactsUseCase,
            deleteContactUseCase,
            deleteMultipleContactsUseCase,
            toggleFavoriteUseCase,
            getAllGroupsUseCase
        )

        coEvery { toggleFavoriteUseCase(1, true) } just Runs

        // When
        viewModel.onEvent(ContactListEvent.ToggleFavorite(1, true))
        advanceUntilIdle()

        // Then
        coVerify { toggleFavoriteUseCase(1, true) }
    }

    @Test
    fun `enterSelectionMode enables selection mode`() = runTest {
        // Given
        viewModel = ContactListViewModel(
            getAllContactsUseCase,
            getFavoriteContactsUseCase,
            searchContactsUseCase,
            deleteContactUseCase,
            deleteMultipleContactsUseCase,
            toggleFavoriteUseCase,
            getAllGroupsUseCase
        )

        // When
        viewModel.onEvent(ContactListEvent.EnterSelectionMode)
        advanceUntilIdle()

        // Then
        viewModel.state.test {
            val state = awaitItem()
            assertTrue(state.isSelectionMode)
        }
    }

    @Test
    fun `exitSelectionMode disables selection mode and clears selections`() = runTest {
        // Given
        viewModel = ContactListViewModel(
            getAllContactsUseCase,
            getFavoriteContactsUseCase,
            searchContactsUseCase,
            deleteContactUseCase,
            deleteMultipleContactsUseCase,
            toggleFavoriteUseCase,
            getAllGroupsUseCase
        )

        viewModel.onEvent(ContactListEvent.EnterSelectionMode)
        viewModel.onEvent(ContactListEvent.ToggleContactSelection(1))
        advanceUntilIdle()

        // When
        viewModel.onEvent(ContactListEvent.ExitSelectionMode)
        advanceUntilIdle()

        // Then
        viewModel.state.test {
            val state = awaitItem()
            assertEquals(false, state.isSelectionMode)
            assertEquals(emptySet<Long>(), state.selectedContactIds)
        }
    }

    @Test
    fun `toggleContactSelection adds and removes contact from selection`() = runTest {
        // Given
        viewModel = ContactListViewModel(
            getAllContactsUseCase,
            getFavoriteContactsUseCase,
            searchContactsUseCase,
            deleteContactUseCase,
            deleteMultipleContactsUseCase,
            toggleFavoriteUseCase,
            getAllGroupsUseCase
        )

        viewModel.onEvent(ContactListEvent.EnterSelectionMode)
        advanceUntilIdle()

        // When - Add to selection
        viewModel.onEvent(ContactListEvent.ToggleContactSelection(1))
        advanceUntilIdle()

        // Then
        viewModel.state.test {
            val state1 = awaitItem()
            assertTrue(state1.selectedContactIds.contains(1))
        }

        // When - Remove from selection
        viewModel.onEvent(ContactListEvent.ToggleContactSelection(1))
        advanceUntilIdle()

        // Then
        viewModel.state.test {
            val state2 = awaitItem()
            assertEquals(false, state2.selectedContactIds.contains(1))
        }
    }

    @Test
    fun `deleteSelectedContacts calls delete multiple use case`() = runTest {
        // Given
        viewModel = ContactListViewModel(
            getAllContactsUseCase,
            getFavoriteContactsUseCase,
            searchContactsUseCase,
            deleteContactUseCase,
            deleteMultipleContactsUseCase,
            toggleFavoriteUseCase,
            getAllGroupsUseCase
        )

        coEvery { deleteMultipleContactsUseCase(any()) } returns Result.success(2)

        viewModel.onEvent(ContactListEvent.EnterSelectionMode)
        viewModel.onEvent(ContactListEvent.ToggleContactSelection(1))
        viewModel.onEvent(ContactListEvent.ToggleContactSelection(2))
        advanceUntilIdle()

        // When
        viewModel.onEvent(ContactListEvent.DeleteSelectedContacts)
        advanceUntilIdle()

        // Then
        coVerify { deleteMultipleContactsUseCase(listOf(1, 2)) }
    }
}
