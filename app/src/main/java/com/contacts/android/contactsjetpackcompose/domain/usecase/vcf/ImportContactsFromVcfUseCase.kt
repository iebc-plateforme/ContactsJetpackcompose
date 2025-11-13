package com.contacts.android.contactsjetpackcompose.domain.usecase.vcf

import android.net.Uri
import com.contacts.android.contactsjetpackcompose.data.vcf.VcfParser
import com.contacts.android.contactsjetpackcompose.domain.repository.ContactRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import javax.inject.Inject

/**
 * Use case for importing contacts from a vCard (.vcf) file
 *
 * Parses the vCard file and saves all contacts to the local database.
 * Returns the number of successfully imported contacts.
 */
class ImportContactsFromVcfUseCase @Inject constructor(
    private val vcfParser: VcfParser,
    private val contactRepository: ContactRepository
) {
    /**
     * Import contacts from a vCard file
     *
     * @param uri The URI of the .vcf file to import
     * @return Result containing the number of imported contacts, or an error
     */
    suspend operator fun invoke(uri: Uri): Result<Int> = withContext(Dispatchers.IO) {
        try {
            // Parse contacts from the VCF file
            val contacts = vcfParser.parse(uri)

            if (contacts.isEmpty()) {
                return@withContext Result.failure(
                    IllegalArgumentException("No valid contacts found in the file")
                )
            }

            // Save each contact to the database
            var importedCount = 0
            contacts.forEach { contact ->
                try {
                    contactRepository.insertContact(contact)
                    importedCount++
                } catch (e: Exception) {
                    // Log but continue with next contact
                    e.printStackTrace()
                }
            }

            if (importedCount == 0) {
                Result.failure(Exception("Failed to import any contacts"))
            } else {
                Result.success(importedCount)
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
