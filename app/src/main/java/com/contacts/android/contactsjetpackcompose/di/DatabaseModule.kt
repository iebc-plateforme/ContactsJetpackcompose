package com.contacts.android.contactsjetpackcompose.di

import android.content.Context
import androidx.room.Room
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import com.contacts.android.contactsjetpackcompose.data.local.database.ContactsDatabase
import com.contacts.android.contactsjetpackcompose.data.local.dao.ContactDao
import com.contacts.android.contactsjetpackcompose.data.local.dao.PhoneNumberDao
import com.contacts.android.contactsjetpackcompose.data.local.dao.EmailDao
import com.contacts.android.contactsjetpackcompose.data.local.dao.AddressDao
import com.contacts.android.contactsjetpackcompose.data.local.dao.GroupDao
import com.contacts.android.contactsjetpackcompose.data.local.dao.ContactGroupDao
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {

    /**
     * Migration from version 3 to 4: Add birthday field to contacts table
     */
    private val MIGRATION_3_4 = object : Migration(3, 4) {
        override fun migrate(database: SupportSQLiteDatabase) {
            // Add birthday column to contacts table (nullable)
            database.execSQL("ALTER TABLE contacts ADD COLUMN birthday TEXT DEFAULT NULL")
        }
    }

    @Provides
    @Singleton
    fun provideContactsDatabase(
        @ApplicationContext context: Context
    ): ContactsDatabase {
        return Room.databaseBuilder(
            context,
            ContactsDatabase::class.java,
            "contacts_database"
        )
            .addMigrations(MIGRATION_3_4)
            .fallbackToDestructiveMigration()
            .build()
    }

    @Provides
    @Singleton
    fun provideContactDao(database: ContactsDatabase): ContactDao {
        return database.contactDao()
    }

    @Provides
    @Singleton
    fun providePhoneNumberDao(database: ContactsDatabase): PhoneNumberDao {
        return database.phoneNumberDao()
    }

    @Provides
    @Singleton
    fun provideEmailDao(database: ContactsDatabase): EmailDao {
        return database.emailDao()
    }

    @Provides
    @Singleton
    fun provideAddressDao(database: ContactsDatabase): AddressDao {
        return database.addressDao()
    }

    @Provides
    @Singleton
    fun provideGroupDao(database: ContactsDatabase): GroupDao {
        return database.groupDao()
    }

    @Provides
    @Singleton
    fun provideContactGroupDao(database: ContactsDatabase): ContactGroupDao {
        return database.contactGroupDao()
    }
}
