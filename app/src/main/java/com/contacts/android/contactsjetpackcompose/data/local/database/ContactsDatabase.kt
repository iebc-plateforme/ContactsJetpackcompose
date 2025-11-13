package com.contacts.android.contactsjetpackcompose.data.local.database

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.contacts.android.contactsjetpackcompose.data.local.converter.TypeConverters as AppTypeConverters
import com.contacts.android.contactsjetpackcompose.data.local.dao.*
import com.contacts.android.contactsjetpackcompose.data.local.entity.*
import com.contacts.android.contactsjetpackcompose.data.local.relation.GroupWithContactCount

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
    version = 3, // v3: Added GroupWithContactCount view and updated group related entities
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
    }
}
