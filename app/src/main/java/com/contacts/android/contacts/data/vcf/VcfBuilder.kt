package com.contacts.android.contacts.data.vcf

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.util.Base64
import com.contacts.android.contacts.domain.model.*
import dagger.hilt.android.qualifiers.ApplicationContext
import java.io.ByteArrayOutputStream
import java.text.SimpleDateFormat
import java.util.*
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Builder for creating vCard 3.0 format files
 * Generates .vcf content from Contact objects
 */
@Singleton
class VcfBuilder @Inject constructor(
    @ApplicationContext private val context: Context
) {
    companion object {
        private const val VCARD_VERSION = "3.0"
        private const val LINE_MAX_LENGTH = 75
    }

    /**
     * Build a vCard string for a single contact
     * @param contact The contact to export
     * @param includePhoto Whether to include photo data (increases file size)
     * @return vCard 3.0 formatted string
     */
    fun buildSingle(contact: Contact, includePhoto: Boolean = true): String {
        return buildVCard(contact, includePhoto)
    }

    /**
     * Build a vCard string for multiple contacts
     * @param contacts List of contacts to export
     * @param includePhotos Whether to include photo data
     * @return vCard 3.0 formatted string with all contacts
     */
    fun buildMultiple(contacts: List<Contact>, includePhotos: Boolean = true): String {
        return contacts.joinToString("\n") { contact ->
            buildVCard(contact, includePhotos)
        }
    }

    private fun buildVCard(contact: Contact, includePhoto: Boolean): String {
        val builder = StringBuilder()

        // Header
        builder.appendLine("BEGIN:VCARD")
        builder.appendLine("VERSION:$VCARD_VERSION")

        // Structured name (N): Family;Given;Middle;Prefix;Suffix
        val structuredName = listOf(
            contact.lastName,
            contact.firstName,
            "", // middle
            "", // prefix
            ""  // suffix
        ).joinToString(";")
        builder.appendProperty("N", structuredName)

        // Formatted name (FN)
        builder.appendProperty("FN", contact.displayName)

        // Organization
        contact.organization?.takeIf { it.isNotBlank() }?.let {
            builder.appendProperty("ORG", it)
        }

        // Title
        contact.title?.takeIf { it.isNotBlank() }?.let {
            builder.appendProperty("TITLE", it)
        }

        // Phone numbers
        contact.phoneNumbers.forEach { phone ->
            val typeParam = when (phone.type) {
                PhoneType.MOBILE -> "CELL"
                PhoneType.HOME -> "HOME"
                PhoneType.WORK -> "WORK"
                PhoneType.FAX -> "FAX"
                PhoneType.PAGER -> "PAGER"
                PhoneType.OTHER -> "VOICE"
                PhoneType.CUSTOM -> "VOICE"
            }

            val params = buildString {
                append("TYPE=$typeParam")
                phone.label?.takeIf { it.isNotBlank() && phone.type == PhoneType.CUSTOM }?.let {
                    append(";LABEL=\"${it.escapeVcf()}\"")
                }
            }

            builder.appendProperty("TEL", phone.number, params)
        }

        // Email addresses
        contact.emails.forEach { email ->
            val typeParam = when (email.type) {
                EmailType.HOME -> "HOME"
                EmailType.WORK -> "WORK"
                EmailType.OTHER -> "INTERNET"
                EmailType.CUSTOM -> "INTERNET"
            }

            val params = buildString {
                append("TYPE=$typeParam")
                email.label?.takeIf { it.isNotBlank() && email.type == EmailType.CUSTOM }?.let {
                    append(";LABEL=\"${it.escapeVcf()}\"")
                }
            }

            builder.appendProperty("EMAIL", email.email, params)
        }

        // Addresses
        contact.addresses.forEach { address ->
            val typeParam = when (address.type) {
                AddressType.HOME -> "HOME"
                AddressType.WORK -> "WORK"
                AddressType.OTHER -> "OTHER"
                AddressType.CUSTOM -> "OTHER"
            }

            // ADR format: PO Box;Extended;Street;City;State;Postal Code;Country
            val adrValue = listOf(
                "", // PO Box
                "", // Extended
                address.street ?: "",
                address.city ?: "",
                address.state ?: "",
                address.postalCode ?: "",
                address.country ?: ""
            ).joinToString(";") { it.escapeVcf() }

            val params = buildString {
                append("TYPE=$typeParam")
                address.label?.takeIf { it.isNotBlank() && address.type == AddressType.CUSTOM }?.let {
                    append(";LABEL=\"${it.escapeVcf()}\"")
                }
            }

            builder.appendProperty("ADR", adrValue, params)
        }

        // Notes
        contact.notes?.takeIf { it.isNotBlank() }?.let {
            builder.appendProperty("NOTE", it.escapeVcf())
        }

        // Categories (groups)
        if (contact.groups.isNotEmpty()) {
            val categories = contact.groups.joinToString(",") { it.name.escapeVcf() }
            builder.appendProperty("CATEGORIES", categories)
        }

        // Photo
        if (includePhoto && contact.photoUri != null) {
            try {
                val photoData = loadPhotoAsBase64(contact.photoUri)
                if (photoData != null) {
                    builder.appendProperty("PHOTO", photoData, "ENCODING=BASE64;TYPE=JPEG")
                }
            } catch (e: Exception) {
                // Skip photo if loading fails
            }
        }

        // Timestamp
        val timestamp = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'", Locale.US).apply {
            timeZone = TimeZone.getTimeZone("UTC")
        }.format(Date())
        builder.appendProperty("REV", timestamp)

        // Footer
        builder.appendLine("END:VCARD")

        return builder.toString()
    }

    private fun StringBuilder.appendProperty(
        name: String,
        value: String,
        parameters: String? = null
    ) {
        val property = if (parameters != null) {
            "$name;$parameters:$value"
        } else {
            "$name:$value"
        }

        // Fold lines longer than 75 characters per RFC 2426
        if (property.length <= LINE_MAX_LENGTH) {
            appendLine(property)
        } else {
            // First line
            appendLine(property.substring(0, LINE_MAX_LENGTH))

            // Continuation lines (prefixed with space)
            var start = LINE_MAX_LENGTH
            while (start < property.length) {
                val end = minOf(start + LINE_MAX_LENGTH - 1, property.length)
                appendLine(" ${property.substring(start, end)}")
                start = end
            }
        }
    }

    private fun loadPhotoAsBase64(photoUri: String): String? {
        return try {
            val uri = Uri.parse(photoUri)
            val bitmap = context.contentResolver.openInputStream(uri)?.use { inputStream ->
                BitmapFactory.decodeStream(inputStream)
            } ?: return null

            // Compress to JPEG
            val outputStream = ByteArrayOutputStream()
            bitmap.compress(Bitmap.CompressFormat.JPEG, 85, outputStream)
            val byteArray = outputStream.toByteArray()

            // Encode to Base64
            Base64.encodeToString(byteArray, Base64.NO_WRAP)
        } catch (e: Exception) {
            null
        }
    }
}

/**
 * Escape special characters for vCard format
 */
private fun String.escapeVcf(): String {
    return this
        .replace("\\", "\\\\")
        .replace(",", "\\,")
        .replace(";", "\\;")
        .replace(":", "\\:")
        .replace("\n", "\\n")
        .replace("\r", "\\r")
}
