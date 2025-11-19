package com.contacts.android.contacts.domain.usecase.contact

import com.contacts.android.contacts.domain.model.Contact
import com.contacts.android.contacts.domain.repository.ContactRepository
import javax.inject.Inject

class DeleteContactUseCase @Inject constructor(
    private val repository: ContactRepository
) {
    suspend operator fun invoke(contact: Contact): Result<Unit> {
        return try {
            repository.deleteContact(listOf(contact))
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend operator fun invoke(contactId: Long): Result<Unit> {
        return try {
            repository.deleteContactById(contactId)
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
