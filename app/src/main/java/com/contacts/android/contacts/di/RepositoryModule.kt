package com.contacts.android.contacts.di

import com.contacts.android.contacts.data.repository.ContactRepositoryImpl
import com.contacts.android.contacts.data.repository.GroupRepositoryImpl
import com.contacts.android.contacts.data.repository.PremiumRepositoryImpl
import com.contacts.android.contacts.domain.repository.ContactRepository
import com.contacts.android.contacts.domain.repository.GroupRepository
import com.contacts.android.contacts.domain.repository.PremiumRepository
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

    @Binds
    @Singleton
    abstract fun bindPremiumRepository(
        premiumRepositoryImpl: PremiumRepositoryImpl
    ): PremiumRepository
}
