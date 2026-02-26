package com.contacts.android.contacts.domain.usecase.pdf

import android.content.Context
import android.content.Intent
import androidx.core.content.FileProvider
import com.contacts.android.contacts.data.pdf.PdfExportBuilder
import com.contacts.android.contacts.domain.repository.ContactRepository
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileOutputStream
import javax.inject.Inject

class ExportSingleContactToPdfUseCase @Inject constructor(
    @ApplicationContext private val context: Context,
    private val pdfExportBuilder: PdfExportBuilder,
    private val contactRepository: ContactRepository
) {
    suspend operator fun invoke(contactId: Long): Result<Intent> = withContext(Dispatchers.IO) {
        try {
            val contact = contactRepository.getContactById(contactId).first()
                ?: return@withContext Result.failure(IllegalArgumentException("Contact not found"))

            val document = pdfExportBuilder.buildSingleContact(contact)

            val cacheDir = File(context.cacheDir, "pdf")
            if (!cacheDir.exists()) cacheDir.mkdirs()

            val fileName = "${contact.displayName.replace(" ", "_")}.pdf"
            val pdfFile = File(cacheDir, fileName)

            FileOutputStream(pdfFile).use { outputStream ->
                document.writeTo(outputStream)
            }
            document.close()

            val contentUri = FileProvider.getUriForFile(
                context,
                "${context.packageName}.fileprovider",
                pdfFile
            )

            val shareIntent = Intent(Intent.ACTION_SEND).apply {
                type = "application/pdf"
                putExtra(Intent.EXTRA_STREAM, contentUri)
                putExtra(Intent.EXTRA_SUBJECT, "Contact: ${contact.displayName}")
                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            }

            Result.success(shareIntent)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
