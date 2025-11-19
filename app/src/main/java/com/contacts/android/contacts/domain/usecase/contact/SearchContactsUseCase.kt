package com.contacts.android.contacts.domain.usecase.contact

import com.contacts.android.contacts.domain.model.Contact
import com.contacts.android.contacts.domain.repository.ContactRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class SearchContactsUseCase @Inject constructor(
    private val repository: ContactRepository
) {
    operator fun invoke(query: String): Flow<List<Contact>> {
        return if (query.isBlank()) {
            repository.getAllContacts()
        } else {
            repository.searchContacts(query.trim())
        }
    }
}
