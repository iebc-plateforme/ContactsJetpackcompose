package com.contacts.android.contactsjetpackcompose.di

import com.contacts.android.contactsjetpackcompose.data.repository.ContactRepositoryImpl
import com.contacts.android.contactsjetpackcompose.data.repository.GroupRepositoryImpl
import com.contacts.android.contactsjetpackcompose.domain.repository.ContactRepository
import com.contacts.android.contactsjetpackcompose.domain.repository.GroupRepository
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class RepositoryModule {

    @Binds
    @Singleton
    abstract fun bindContactRepository(
        contactRepositoryImpl: ContactRepositoryImpl
    ): ContactRepository

    @Binds
    @Singleton
    abstract fun bindGroupRepository(
        groupRepositoryImpl: GroupRepositoryImpl
    ): GroupRepository
}
