package com.contacts.android.contactsjetpackcompose.data.local.database

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.contacts.android.contactsjetpackcompose.data.local.converter.TypeConverters as AppTypeConverters
import com.contacts.android.contactsjetpackcompose.data.local.dao.*
import com.contacts.android.contactsjetpackcompose.data.local.entity.*
import com.contacts.android.contactsjetpackcompose.data.local.relation.GroupWithContactCount
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import javax.inject.Provider

@Database(
    entities = [
        ContactEntity::class,
        PhoneNumberEntity::class,
        EmailEntity::class,
        AddressEntity::class,
        GroupEntity::class,
        ContactGroupCrossRef::class
    ],
    views = [GroupWithContactCount::class],
    version = 4, // v4: Added birthday field to ContactEntity
    exportSchema = true
)
@TypeConverters(AppTypeConverters::class)
abstract class ContactsDatabase : RoomDatabase() {

    abstract fun contactDao(): ContactDao
    abstract fun phoneNumberDao(): PhoneNumberDao
    abstract fun emailDao(): EmailDao
    abstract fun addressDao(): AddressDao
    abstract fun groupDao(): GroupDao
    abstract fun contactGroupDao(): ContactGroupDao

    companion object {
        const val DATABASE_NAME = "contacts_database"

        fun prepopulate(
            contactDao: Provider<ContactDao>,
            addressDao: Provider<AddressDao>
        ) = object : RoomDatabase.Callback() {
            override fun onCreate(db: androidx.sqlite.db.SupportSQLiteDatabase) {
                super.onCreate(db)
                CoroutineScope(Dispatchers.IO).launch {
                    val contactId = contactDao.get().insertContact(
                        ContactEntity(
                            firstName = "John",
                            lastName = "Doe",
                            birthday = "1990-01-01"
                        )
                    )
                    addressDao.get().insertAddress(
                        AddressEntity(
                            contactId = contactId,
                            street = "1600 Amphitheatre Parkway",
                            city = "Mountain View",
                            state = "CA",
                            postalCode = "94043",
                            country = "USA",
                            type = com.contacts.android.contactsjetpackcompose.domain.model.AddressType.HOME
                        )
                    )
                }
            }
        }
    }
}
