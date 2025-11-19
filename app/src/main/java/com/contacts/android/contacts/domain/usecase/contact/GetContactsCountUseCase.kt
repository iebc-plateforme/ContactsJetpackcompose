package com.contacts.android.contacts.domain.usecase.contact

import com.contacts.android.contacts.domain.repository.ContactRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class GetContactsCountUseCase @Inject constructor(
    private val repository: ContactRepository
) {
    suspend fun getCount(): Int {
        return repository.getContactCount()
    }

    fun getCountFlow(): Flow<Int> {
        return repository.getContactCountFlow()
    }
}
