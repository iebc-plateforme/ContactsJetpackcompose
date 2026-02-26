package com.contacts.android.contacts.data.pdf

import android.content.Context
import android.graphics.*
import android.graphics.pdf.PdfDocument
import android.net.Uri
import com.contacts.android.contacts.domain.model.*
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class PdfExportBuilder @Inject constructor(
    @ApplicationContext private val context: Context
) {
    companion object {
        private const val PAGE_WIDTH = 595  // A4
        private const val PAGE_HEIGHT = 842 // A4
        private const val MARGIN = 48f
        private const val CONTENT_WIDTH = PAGE_WIDTH - MARGIN * 2
    }

    fun buildSingleContact(contact: Contact): PdfDocument {
        val document = PdfDocument()
        drawContactPage(document, contact, 1)
        return document
    }

    fun buildMultipleContacts(contacts: List<Contact>): PdfDocument {
        val document = PdfDocument()
        contacts.forEachIndexed { index, contact ->
            drawContactPage(document, contact, index + 1)
        }
        return document
    }

    private fun drawContactPage(document: PdfDocument, contact: Contact, pageNumber: Int) {
        val pageInfo = PdfDocument.PageInfo.Builder(PAGE_WIDTH, PAGE_HEIGHT, pageNumber).create()
        val page = document.startPage(pageInfo)
        val canvas = page.canvas
        var y = MARGIN

        // Title paint
        val titlePaint = Paint().apply {
            color = Color.parseColor("#1976D2")
            textSize = 24f
            typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
            isAntiAlias = true
        }

        // Subtitle paint
        val subtitlePaint = Paint().apply {
            color = Color.GRAY
            textSize = 14f
            isAntiAlias = true
        }

        // Section header paint
        val sectionPaint = Paint().apply {
            color = Color.parseColor("#1976D2")
            textSize = 14f
            typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
            isAntiAlias = true
        }

        // Body paint
        val bodyPaint = Paint().apply {
            color = Color.DKGRAY
            textSize = 12f
            isAntiAlias = true
        }

        // Label paint
        val labelPaint = Paint().apply {
            color = Color.GRAY
            textSize = 10f
            isAntiAlias = true
        }

        // Line paint
        val linePaint = Paint().apply {
            color = Color.parseColor("#E0E0E0")
            strokeWidth = 1f
        }

        // --- Draw avatar circle with initials ---
        val avatarBitmap = loadAvatarBitmap(contact.photoUri)
        val avatarSize = 64f
        val avatarCx = MARGIN + avatarSize / 2
        val avatarCy = y + avatarSize / 2

        if (avatarBitmap != null) {
            val scaled = Bitmap.createScaledBitmap(avatarBitmap, avatarSize.toInt(), avatarSize.toInt(), true)
            val circleBitmap = getCircularBitmap(scaled)
            canvas.drawBitmap(circleBitmap, MARGIN, y, null)
            scaled.recycle()
            circleBitmap.recycle()
            avatarBitmap.recycle()
        } else {
            val circlePaint = Paint().apply {
                color = Color.parseColor("#1976D2")
                isAntiAlias = true
            }
            canvas.drawCircle(avatarCx, avatarCy, avatarSize / 2, circlePaint)
            val initialsPaint = Paint().apply {
                color = Color.WHITE
                textSize = 22f
                typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
                textAlign = Paint.Align.CENTER
                isAntiAlias = true
            }
            val initials = contact.initials
            val textBounds = Rect()
            initialsPaint.getTextBounds(initials, 0, initials.length, textBounds)
            canvas.drawText(initials, avatarCx, avatarCy + textBounds.height() / 2f, initialsPaint)
        }

        // --- Name next to avatar ---
        val nameX = MARGIN + avatarSize + 16f
        canvas.drawText(contact.displayName, nameX, y + 28f, titlePaint)

        // Organization / title
        val orgLine = listOfNotNull(contact.title, contact.organization).joinToString(" - ")
        if (orgLine.isNotBlank()) {
            canvas.drawText(orgLine, nameX, y + 48f, subtitlePaint)
        }

        y += avatarSize + 24f

        // Separator line
        canvas.drawLine(MARGIN, y, PAGE_WIDTH - MARGIN, y, linePaint)
        y += 16f

        // --- Phone Numbers ---
        if (contact.phoneNumbers.isNotEmpty()) {
            canvas.drawText("PHONE", MARGIN, y + 12f, sectionPaint)
            y += 24f
            for (phone in contact.phoneNumbers) {
                canvas.drawText(phone.number, MARGIN + 16f, y + 12f, bodyPaint)
                canvas.drawText(phone.type.name.lowercase().replaceFirstChar { it.uppercase() }, MARGIN + 16f + 200f, y + 12f, labelPaint)
                y += 20f
                if (y > PAGE_HEIGHT - MARGIN - 40f) break
            }
            y += 8f
        }

        // --- Emails ---
        if (contact.emails.isNotEmpty()) {
            canvas.drawText("EMAIL", MARGIN, y + 12f, sectionPaint)
            y += 24f
            for (email in contact.emails) {
                canvas.drawText(email.email, MARGIN + 16f, y + 12f, bodyPaint)
                canvas.drawText(email.type.name.lowercase().replaceFirstChar { it.uppercase() }, MARGIN + 16f + 250f, y + 12f, labelPaint)
                y += 20f
                if (y > PAGE_HEIGHT - MARGIN - 40f) break
            }
            y += 8f
        }

        // --- Addresses ---
        if (contact.addresses.isNotEmpty()) {
            canvas.drawText("ADDRESS", MARGIN, y + 12f, sectionPaint)
            y += 24f
            for (address in contact.addresses) {
                val parts = listOfNotNull(
                    address.street?.takeIf { it.isNotBlank() },
                    address.city?.takeIf { it.isNotBlank() },
                    address.state?.takeIf { it.isNotBlank() },
                    address.postalCode?.takeIf { it.isNotBlank() },
                    address.country?.takeIf { it.isNotBlank() }
                )
                val addressLine = parts.joinToString(", ")
                if (addressLine.isNotBlank()) {
                    // Wrap long address text
                    val lines = wrapText(addressLine, bodyPaint, CONTENT_WIDTH - 16f)
                    for (line in lines) {
                        canvas.drawText(line, MARGIN + 16f, y + 12f, bodyPaint)
                        y += 18f
                    }
                }
                y += 4f
                if (y > PAGE_HEIGHT - MARGIN - 40f) break
            }
            y += 8f
        }

        // --- Birthday ---
        if (!contact.birthday.isNullOrBlank()) {
            canvas.drawText("BIRTHDAY", MARGIN, y + 12f, sectionPaint)
            y += 24f
            canvas.drawText(contact.birthday, MARGIN + 16f, y + 12f, bodyPaint)
            y += 28f
        }

        // --- Notes ---
        if (!contact.notes.isNullOrBlank()) {
            canvas.drawText("NOTES", MARGIN, y + 12f, sectionPaint)
            y += 24f
            val noteLines = wrapText(contact.notes, bodyPaint, CONTENT_WIDTH - 16f)
            for (line in noteLines) {
                if (y > PAGE_HEIGHT - MARGIN - 20f) break
                canvas.drawText(line, MARGIN + 16f, y + 12f, bodyPaint)
                y += 18f
            }
            y += 8f
        }

        // --- Footer ---
        val footerPaint = Paint().apply {
            color = Color.LTGRAY
            textSize = 9f
            isAntiAlias = true
            textAlign = Paint.Align.CENTER
        }
        canvas.drawText(
            "Exported from Contacts App",
            PAGE_WIDTH / 2f,
            PAGE_HEIGHT - MARGIN / 2f,
            footerPaint
        )

        document.finishPage(page)
    }

    private fun loadAvatarBitmap(photoUri: String?): Bitmap? {
        if (photoUri.isNullOrBlank()) return null
        return try {
            val uri = Uri.parse(photoUri)
            context.contentResolver.openInputStream(uri)?.use { inputStream ->
                BitmapFactory.decodeStream(inputStream)
            }
        } catch (e: Exception) {
            null
        }
    }

    private fun getCircularBitmap(bitmap: Bitmap): Bitmap {
        val size = minOf(bitmap.width, bitmap.height)
        val output = Bitmap.createBitmap(size, size, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(output)
        val paint = Paint().apply {
            isAntiAlias = true
        }
        val rect = Rect(0, 0, size, size)
        canvas.drawCircle(size / 2f, size / 2f, size / 2f, paint)
        paint.xfermode = PorterDuffXfermode(PorterDuff.Mode.SRC_IN)
        canvas.drawBitmap(bitmap, rect, rect, paint)
        return output
    }

    private fun wrapText(text: String, paint: Paint, maxWidth: Float): List<String> {
        val lines = mutableListOf<String>()
        for (paragraph in text.split("\n")) {
            val words = paragraph.split(" ")
            var currentLine = ""
            for (word in words) {
                val testLine = if (currentLine.isEmpty()) word else "$currentLine $word"
                if (paint.measureText(testLine) <= maxWidth) {
                    currentLine = testLine
                } else {
                    if (currentLine.isNotEmpty()) lines.add(currentLine)
                    currentLine = word
                }
            }
            if (currentLine.isNotEmpty()) lines.add(currentLine)
        }
        return lines
    }
}
