package com.contacts.android.contacts.domain.usecase.vcf

import android.content.Context
import android.content.Intent
import androidx.core.content.FileProvider
import com.contacts.android.contacts.data.vcf.VcfBuilder
import com.contacts.android.contacts.domain.model.Contact
import com.contacts.android.contacts.domain.repository.ContactRepository
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.withContext
import java.io.File
import javax.inject.Inject

/**
 * Use case for exporting a single contact as vCard
 *
 * Creates a temporary vCard file and returns a share intent.
 * Useful for sharing a contact via messaging, email, etc.
 */
class ExportSingleContactUseCase @Inject constructor(
    @ApplicationContext private val context: Context,
    private val vcfBuilder: VcfBuilder,
    private val contactRepository: ContactRepository
) {
    /**
     * Export a single contact and create a share intent
     *
     * @param contactId The ID of the contact to export
     * @param includePhoto Whether to include the contact photo
     * @return Result containing a share Intent, or an error
     */
    suspend operator fun invoke(
        contactId: Long,
        includePhoto: Boolean = true
    ): Result<Intent> = withContext(Dispatchers.IO) {
        try {
            // Get the contact
            val contact = contactRepository.getContactById(contactId).first()
                ?: return@withContext Result.failure(
                    IllegalArgumentException("Contact not found")
                )

            // Generate VCF content
            val vcfContent = vcfBuilder.buildSingle(contact, includePhoto)

            // Create temporary file
            val vcfFile = createTempVcfFile(contact, vcfContent)

            // Create share intent
            val shareIntent = createShareIntent(vcfFile, contact)

            Result.success(shareIntent)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * Export a contact object directly (without loading from DB)
     *
     * @param contact The contact to export
     * @param includePhoto Whether to include the contact photo
     * @return Result containing a share Intent, or an error
     */
    suspend fun exportContact(
        contact: Contact,
        includePhoto: Boolean = true
    ): Result<Intent> = withContext(Dispatchers.IO) {
        try {
            // Generate VCF content
            val vcfContent = vcfBuilder.buildSingle(contact, includePhoto)

            // Create temporary file
            val vcfFile = createTempVcfFile(contact, vcfContent)

            // Create share intent
            val shareIntent = createShareIntent(vcfFile, contact)

            Result.success(shareIntent)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    private fun createTempVcfFile(contact: Contact, vcfContent: String): File {
        // Create cache directory for VCF files
        val cacheDir = File(context.cacheDir, "vcf")
        if (!cacheDir.exists()) {
            cacheDir.mkdirs()
        }

        // Create file with contact name
        val fileName = "${contact.displayName.replace(" ", "_")}.vcf"
        val vcfFile = File(cacheDir, fileName)

        // Write content
        vcfFile.writeText(vcfContent, Charsets.UTF_8)

        return vcfFile
    }

    private fun createShareIntent(vcfFile: File, contact: Contact): Intent {
        // Get content URI using FileProvider
        val contentUri = FileProvider.getUriForFile(
            context,
            "${context.packageName}.fileprovider",
            vcfFile
        )

        // Create share intent
        return Intent(Intent.ACTION_SEND).apply {
            type = "text/x-vcard"
            putExtra(Intent.EXTRA_STREAM, contentUri)
            putExtra(Intent.EXTRA_SUBJECT, "Contact: ${contact.displayName}")
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        }
    }
}
