package com.contacts.android.contactsjetpackcompose.domain.usecase.vcf

import android.content.Context
import android.net.Uri
import com.contacts.android.contactsjetpackcompose.data.vcf.VcfBuilder
import com.contacts.android.contactsjetpackcompose.domain.model.Contact
import com.contacts.android.contactsjetpackcompose.domain.repository.ContactRepository
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.withContext
import java.io.OutputStreamWriter
import java.text.SimpleDateFormat
import java.util.*
import javax.inject.Inject

/**
 * Use case for exporting contacts to a vCard (.vcf) file
 *
 * Generates a vCard file containing all or selected contacts.
 */
class ExportContactsToVcfUseCase @Inject constructor(
    @ApplicationContext private val context: Context,
    private val vcfBuilder: VcfBuilder,
    private val contactRepository: ContactRepository
) {
    /**
     * Export all contacts to a vCard file
     *
     * @param outputUri The URI where the .vcf file should be written
     * @param includePhotos Whether to include contact photos (increases file size)
     * @return Result containing the number of exported contacts, or an error
     */
    suspend fun exportAll(
        outputUri: Uri,
        includePhotos: Boolean = false
    ): Result<Int> = withContext(Dispatchers.IO) {
        try {
            // Get all contacts
            val contacts = contactRepository.getAllContacts().first()

            if (contacts.isEmpty()) {
                return@withContext Result.failure(
                    IllegalStateException("No contacts to export")
                )
            }

            // Generate VCF content
            val vcfContent = vcfBuilder.buildMultiple(contacts, includePhotos)

            // Write to file
            writeToUri(outputUri, vcfContent)

            Result.success(contacts.size)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * Export selected contacts to a vCard file
     *
     * @param contactIds List of contact IDs to export
     * @param outputUri The URI where the .vcf file should be written
     * @param includePhotos Whether to include contact photos
     * @return Result containing the number of exported contacts, or an error
     */
    suspend fun exportSelected(
        contactIds: List<Long>,
        outputUri: Uri,
        includePhotos: Boolean = false
    ): Result<Int> = withContext(Dispatchers.IO) {
        try {
            if (contactIds.isEmpty()) {
                return@withContext Result.failure(
                    IllegalArgumentException("No contacts selected for export")
                )
            }

            // Get selected contacts
            val contacts = mutableListOf<Contact>()
            contactIds.forEach { id ->
                contactRepository.getContactById(id).first()?.let { contact ->
                    contacts.add(contact)
                }
            }

            if (contacts.isEmpty()) {
                return@withContext Result.failure(
                    IllegalStateException("Selected contacts not found")
                )
            }

            // Generate VCF content
            val vcfContent = vcfBuilder.buildMultiple(contacts, includePhotos)

            // Write to file
            writeToUri(outputUri, vcfContent)

            Result.success(contacts.size)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * Get a default filename for export
     */
    fun getDefaultFilename(): String {
        val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.US)
        val date = dateFormat.format(Date())
        return "contacts_$date.vcf"
    }

    private fun writeToUri(uri: Uri, content: String) {
        context.contentResolver.openOutputStream(uri)?.use { outputStream ->
            OutputStreamWriter(outputStream, Charsets.UTF_8).use { writer ->
                writer.write(content)
                writer.flush()
            }
        } ?: throw IllegalStateException("Could not open output stream for URI: $uri")
    }
}
