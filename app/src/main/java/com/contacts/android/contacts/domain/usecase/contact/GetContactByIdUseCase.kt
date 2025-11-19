package com.contacts.android.contacts.domain.usecase.contact

import com.contacts.android.contacts.domain.model.Contact
import com.contacts.android.contacts.domain.repository.ContactRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class GetContactByIdUseCase @Inject constructor(
    private val repository: ContactRepository
) {
    operator fun invoke(id: Long): Flow<Contact?> {
        return repository.getContactById(id)
    }
}
