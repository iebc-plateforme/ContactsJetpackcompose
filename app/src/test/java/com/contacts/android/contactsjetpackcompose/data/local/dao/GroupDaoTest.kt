package com.contacts.android.contactsjetpackcompose.data.local.dao

import android.content.Context
import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import com.contacts.android.contactsjetpackcompose.data.local.database.ContactsDatabase
import com.contacts.android.contactsjetpackcompose.data.local.entity.ContactEntity
import com.contacts.android.contactsjetpackcompose.data.local.entity.ContactGroupCrossRef
import com.contacts.android.contactsjetpackcompose.data.local.entity.GroupEntity
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

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [28])
class GroupDaoTest {

    @get:Rule
    val instantTaskExecutorRule = InstantTaskExecutorRule()

    private lateinit var database: ContactsDatabase
    private lateinit var groupDao: GroupDao
    private lateinit var contactDao: ContactDao
    private lateinit var contactGroupDao: ContactGroupDao

    @Before
    fun setup() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        database = Room.inMemoryDatabaseBuilder(
            context,
            ContactsDatabase::class.java
        )
            .allowMainThreadQueries()
            .build()

        groupDao = database.groupDao()
        contactDao = database.contactDao()
        contactGroupDao = database.contactGroupDao()
    }

    @After
    fun teardown() {
        database.close()
    }

    @Test
    fun `insertGroup and getGroupById returns correct group`() = runTest {
        // Given
        val group = GroupEntity(
            id = 1,
            name = "Family",
            createdAt = System.currentTimeMillis()
        )

        // When
        groupDao.insertGroup(group)
        val retrieved = groupDao.getGroupById(1)

        // Then
        assertNotNull(retrieved)
        assertEquals("Family", retrieved?.name)
    }

    @Test
    fun `getAllGroups returns all groups`() = runTest {
        // Given
        val group1 = GroupEntity(1, "Family", System.currentTimeMillis())
        val group2 = GroupEntity(2, "Friends", System.currentTimeMillis())
        val group3 = GroupEntity(3, "Work", System.currentTimeMillis())

        // When
        groupDao.insertGroup(group1)
        groupDao.insertGroup(group2)
        groupDao.insertGroup(group3)
        val groups = groupDao.getAllGroups().first()

        // Then
        assertEquals(3, groups.size)
    }

    @Test
    fun `updateGroup modifies existing group`() = runTest {
        // Given
        val group = GroupEntity(1, "Family", System.currentTimeMillis())
        groupDao.insertGroup(group)

        // When
        val updated = group.copy(name = "Close Family")
        groupDao.updateGroup(updated)
        val retrieved = groupDao.getGroupById(1)

        // Then
        assertNotNull(retrieved)
        assertEquals("Close Family", retrieved?.name)
    }

    @Test
    fun `deleteGroup removes group from database`() = runTest {
        // Given
        val group = GroupEntity(1, "Family", System.currentTimeMillis())
        groupDao.insertGroup(group)

        // When
        groupDao.deleteGroup(group)
        val retrieved = groupDao.getGroupById(1)

        // Then
        assertNull(retrieved)
    }

    @Test
    fun `getGroupsForContact returns groups associated with contact`() = runTest {
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
        val group1 = GroupEntity(1, "Family", System.currentTimeMillis())
        val group2 = GroupEntity(2, "Friends", System.currentTimeMillis())

        contactDao.insertContact(contact)
        groupDao.insertGroup(group1)
        groupDao.insertGroup(group2)

        contactGroupDao.insertContactGroup(ContactGroupCrossRef(1, 1))
        contactGroupDao.insertContactGroup(ContactGroupCrossRef(1, 2))

        // When
        val groups = groupDao.getGroupsForContact(1)

        // Then
        assertEquals(2, groups.size)
    }

    @Test
    fun `deleteGroupById removes group and relationships`() = runTest {
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
        val group = GroupEntity(1, "Family", System.currentTimeMillis())

        contactDao.insertContact(contact)
        groupDao.insertGroup(group)
        contactGroupDao.insertContactGroup(ContactGroupCrossRef(1, 1))

        // When
        groupDao.deleteGroupById(1)
        val retrieved = groupDao.getGroupById(1)
        val groups = groupDao.getGroupsForContact(1)

        // Then
        assertNull(retrieved)
        assertEquals(0, groups.size)
    }

    @Test
    fun `getContactsByGroupId returns contacts in group`() = runTest {
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
        val group = GroupEntity(1, "Family", System.currentTimeMillis())

        contactDao.insertContact(contact1)
        contactDao.insertContact(contact2)
        groupDao.insertGroup(group)

        contactGroupDao.insertContactGroup(ContactGroupCrossRef(1, 1))
        contactGroupDao.insertContactGroup(ContactGroupCrossRef(2, 1))

        // When
        val contacts = contactGroupDao.getContactsByGroupId(1)

        // Then
        assertEquals(2, contacts.size)
    }
}
