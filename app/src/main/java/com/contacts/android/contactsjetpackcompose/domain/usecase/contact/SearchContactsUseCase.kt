package com.contacts.android.contactsjetpackcompose.domain.usecase.contact

import com.contacts.android.contactsjetpackcompose.domain.model.Contact
import com.contacts.android.contactsjetpackcompose.domain.repository.ContactRepository
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
