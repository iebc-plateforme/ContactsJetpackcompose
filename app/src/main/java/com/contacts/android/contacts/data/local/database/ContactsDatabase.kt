package com.contacts.android.contacts.data.local.database

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.contacts.android.contacts.data.local.converter.TypeConverters as AppTypeConverters
import com.contacts.android.contacts.data.local.dao.*
import com.contacts.android.contacts.data.local.entity.*
import com.contacts.android.contacts.data.local.relation.GroupWithContactCount

@Database(
    entities = [
        ContactEntity::class,
        PhoneNumberEntity::class,
        EmailEntity::class,
        AddressEntity::class,
        WebsiteEntity::class,
        InstantMessageEntity::class,
        EventEntity::class,
        GroupEntity::class,
        ContactGroupCrossRef::class
    ],
    views = [GroupWithContactCount::class],
    version = 5, // v5: Added Fossify-complete fields (prefix, middle, suffix, nickname, websites, IMs, events, ringtone)
    exportSchema = true
)
@TypeConverters(AppTypeConverters::class)
abstract class ContactsDatabase : RoomDatabase() {

    abstract fun contactDao(): ContactDao
    abstract fun phoneNumberDao(): PhoneNumberDao
    abstract fun emailDao(): EmailDao
    abstract fun addressDao(): AddressDao
    abstract fun websiteDao(): WebsiteDao
    abstract fun instantMessageDao(): InstantMessageDao
    abstract fun eventDao(): EventDao
    abstract fun groupDao(): GroupDao
    abstract fun contactGroupDao(): ContactGroupDao

    companion object {
        const val DATABASE_NAME = "contacts_database"
    }
}
