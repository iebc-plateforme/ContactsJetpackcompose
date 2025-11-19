package com.contacts.android.contacts.di

import android.content.Context
import androidx.room.Room
import com.contacts.android.contacts.data.local.database.ContactsDatabase
import com.contacts.android.contacts.data.local.database.ALL_MIGRATIONS
import com.contacts.android.contacts.data.local.dao.*
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {

    @Provides
    @Singleton
    fun provideContactsDatabase(@ApplicationContext context: Context): ContactsDatabase {
        return Room.databaseBuilder(
            context,
            ContactsDatabase::class.java,
            ContactsDatabase.DATABASE_NAME
        )
            .addMigrations(*ALL_MIGRATIONS) // Includes migrations from v136 (versions 1-7) to preserve data
            .fallbackToDestructiveMigration() // Rare fallback for truly unknown versions only
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

    @Provides
    @Singleton
    fun provideWebsiteDao(database: ContactsDatabase): WebsiteDao {
        return database.websiteDao()
    }

    @Provides
    @Singleton
    fun provideInstantMessageDao(database: ContactsDatabase): InstantMessageDao {
        return database.instantMessageDao()
    }

    @Provides
    @Singleton
    fun provideEventDao(database: ContactsDatabase): EventDao {
        return database.eventDao()
    }
}
