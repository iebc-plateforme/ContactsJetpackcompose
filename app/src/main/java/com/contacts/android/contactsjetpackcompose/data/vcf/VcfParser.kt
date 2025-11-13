package com.contacts.android.contactsjetpackcompose.data.vcf

import android.content.Context
import android.net.Uri
import android.util.Base64
import com.contacts.android.contactsjetpackcompose.domain.model.*
import dagger.hilt.android.qualifiers.ApplicationContext
import java.io.BufferedReader
import java.io.InputStreamReader
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Parser for vCard 2.1, 3.0, and 4.0 formats
 * Extracts contact information from .vcf files
 */
@Singleton
class VcfParser @Inject constructor(
    @ApplicationContext private val context: Context
) {
    /**
     * Parse contacts from a vCard file URI
     * @param uri The URI of the .vcf file
     * @return List of parsed contacts
     */
    suspend fun parse(uri: Uri): List<Contact> {
        val contacts = mutableListOf<Contact>()

        context.contentResolver.openInputStream(uri)?.use { inputStream ->
            BufferedReader(InputStreamReader(inputStream, Charsets.UTF_8)).use { reader ->
                var currentContact: ContactBuilder? = null
                var currentProperty = StringBuilder()
                var isMultiline = false

                reader.forEachLine { rawLine ->
                    val line = rawLine.trim()

                    // Handle multiline values (lines starting with space or tab)
                    if (line.startsWith(" ") || line.startsWith("\t")) {
                        if (isMultiline) {
                            currentProperty.append(line.substring(1))
                        }
                        return@forEachLine
                    }

                    // Process previous property if exists
                    if (currentProperty.isNotEmpty() && !isMultiline) {
                        currentContact?.processProperty(currentProperty.toString())
                        currentProperty.clear()
                    }

                    when {
                        line.startsWith("BEGIN:VCARD", ignoreCase = true) -> {
                            currentContact = ContactBuilder()
                        }
                        line.startsWith("END:VCARD", ignoreCase = true) -> {
                            currentContact?.let {
                                // Process last property
                                if (currentProperty.isNotEmpty()) {
                                    it.processProperty(currentProperty.toString())
                                    currentProperty.clear()
                                }
                                // Build and add contact if it has at least a name
                                it.build()?.let { contact -> contacts.add(contact) }
                            }
                            currentContact = null
                        }
                        else -> {
                            currentProperty.append(line)
                            // Check if this line might continue
                            isMultiline = !line.contains(":")
                        }
                    }
                }
            }
        }

        return contacts
    }

    /**
     * Builder class to construct Contact objects from vCard properties
     */
    private class ContactBuilder {
        private var firstName: String = ""
        private var lastName: String = ""
        private var organization: String = ""
        private var title: String = ""
        private var notes: String = ""
        private var photoUri: String? = null
        private var photoBytes: ByteArray? = null
        private val phoneNumbers = mutableListOf<PhoneNumber>()
        private val emails = mutableListOf<Email>()
        private val addresses = mutableListOf<Address>()
        private val groups = mutableListOf<Group>()

        fun processProperty(propertyLine: String) {
            val colonIndex = propertyLine.indexOf(':')
            if (colonIndex == -1) return

            val propertyPart = propertyLine.substring(0, colonIndex)
            val value = propertyLine.substring(colonIndex + 1).unescapeVcf()

            if (value.isBlank()) return

            // Split property into name and parameters
            val parts = propertyPart.split(';')
            val propertyName = parts[0].uppercase()
            val parameters = parseParameters(parts.drop(1))

            when (propertyName) {
                "N" -> parseStructuredName(value)
                "FN" -> parseFormattedName(value)
                "ORG" -> organization = value.split(';').firstOrNull() ?: value
                "TITLE" -> title = value
                "NOTE" -> notes = value
                "TEL" -> parsePhoneNumber(value, parameters)
                "EMAIL" -> parseEmail(value, parameters)
                "ADR" -> parseAddress(value, parameters)
                "PHOTO" -> parsePhoto(value, parameters)
                "CATEGORIES" -> parseCategories(value)
            }
        }

        private fun parseStructuredName(value: String) {
            // Format: Family;Given;Middle;Prefix;Suffix
            val parts = value.split(';')
            lastName = parts.getOrNull(0)?.trim() ?: ""
            firstName = parts.getOrNull(1)?.trim() ?: ""
        }

        private fun parseFormattedName(value: String) {
            // If we don't have a structured name, try to extract from formatted name
            if (firstName.isEmpty() && lastName.isEmpty()) {
                val parts = value.trim().split(' ')
                when (parts.size) {
                    1 -> firstName = parts[0]
                    2 -> {
                        firstName = parts[0]
                        lastName = parts[1]
                    }
                    else -> {
                        firstName = parts.first()
                        lastName = parts.last()
                    }
                }
            }
        }

        private fun parsePhoneNumber(value: String, parameters: Map<String, String>) {
            val type = when {
                parameters.containsKey("CELL") || parameters.containsKey("MOBILE") -> PhoneType.MOBILE
                parameters.containsKey("HOME") -> PhoneType.HOME
                parameters.containsKey("WORK") -> PhoneType.WORK
                parameters.containsKey("FAX") -> PhoneType.FAX
                parameters.containsKey("PAGER") -> PhoneType.PAGER
                else -> {
                    // Check TYPE parameter
                    val typeValue = parameters["TYPE"]?.uppercase()
                    when {
                        typeValue?.contains("CELL") == true || typeValue?.contains("MOBILE") == true -> PhoneType.MOBILE
                        typeValue?.contains("HOME") == true -> PhoneType.HOME
                        typeValue?.contains("WORK") == true -> PhoneType.WORK
                        typeValue?.contains("FAX") == true -> PhoneType.FAX
                        typeValue?.contains("PAGER") == true -> PhoneType.PAGER
                        else -> PhoneType.MOBILE
                    }
                }
            }

            phoneNumbers.add(PhoneNumber(
                id = 0,
                number = value.trim(),
                type = type,
                label = parameters["LABEL"] ?: ""
            ))
        }

        private fun parseEmail(value: String, parameters: Map<String, String>) {
            val type = when {
                parameters.containsKey("HOME") -> EmailType.HOME
                parameters.containsKey("WORK") -> EmailType.WORK
                else -> {
                    val typeValue = parameters["TYPE"]?.uppercase()
                    when {
                        typeValue?.contains("HOME") == true -> EmailType.HOME
                        typeValue?.contains("WORK") == true -> EmailType.WORK
                        else -> EmailType.HOME
                    }
                }
            }

            emails.add(Email(
                id = 0,
                email = value.trim(),
                type = type,
                label = parameters["LABEL"] ?: ""
            ))
        }

        private fun parseAddress(value: String, parameters: Map<String, String>) {
            // Format: PO Box;Extended;Street;City;State;Postal Code;Country
            val parts = value.split(';')

            val street = listOfNotNull(
                parts.getOrNull(0)?.takeIf { it.isNotBlank() },
                parts.getOrNull(1)?.takeIf { it.isNotBlank() },
                parts.getOrNull(2)?.takeIf { it.isNotBlank() }
            ).joinToString(", ")

            val type = when {
                parameters.containsKey("HOME") -> AddressType.HOME
                parameters.containsKey("WORK") -> AddressType.WORK
                else -> {
                    val typeValue = parameters["TYPE"]?.uppercase()
                    when {
                        typeValue?.contains("HOME") == true -> AddressType.HOME
                        typeValue?.contains("WORK") == true -> AddressType.WORK
                        else -> AddressType.HOME
                    }
                }
            }

            addresses.add(Address(
                id = 0,
                street = street,
                city = parts.getOrNull(3)?.trim() ?: "",
                state = parts.getOrNull(4)?.trim() ?: "",
                postalCode = parts.getOrNull(5)?.trim() ?: "",
                country = parts.getOrNull(6)?.trim() ?: "",
                type = type,
                label = parameters["LABEL"] ?: ""
            ))
        }

        private fun parsePhoto(value: String, parameters: Map<String, String>) {
            when {
                parameters["ENCODING"]?.uppercase() == "BASE64" ||
                parameters["ENCODING"]?.uppercase() == "B" -> {
                    // Photo is base64 encoded inline
                    try {
                        photoBytes = Base64.decode(value.replace("\n", "").replace("\r", ""), Base64.DEFAULT)
                    } catch (e: Exception) {
                        // Ignore invalid base64
                    }
                }
                value.startsWith("http://") || value.startsWith("https://") -> {
                    // Photo is a URL
                    photoUri = value
                }
                else -> {
                    // Might be a file path or URI
                    photoUri = value
                }
            }
        }

        private fun parseCategories(value: String) {
            // Categories are comma-separated group names
            value.split(',').forEach { category ->
                val trimmed = category.trim()
                if (trimmed.isNotEmpty()) {
                    groups.add(Group(
                        id = 0,
                        name = trimmed,
                        contactCount = 0
                    ))
                }
            }
        }

        private fun parseParameters(paramList: List<String>): Map<String, String> {
            val params = mutableMapOf<String, String>()
            paramList.forEach { param ->
                val equalIndex = param.indexOf('=')
                if (equalIndex != -1) {
                    val key = param.substring(0, equalIndex).uppercase()
                    val value = param.substring(equalIndex + 1).trim('"')
                    params[key] = value
                } else {
                    // Parameter without value (e.g., HOME, WORK, CELL)
                    params[param.uppercase()] = ""
                }
            }
            return params
        }

        fun build(): Contact? {
            // Contact must have at least a name or phone number
            if (firstName.isEmpty() && lastName.isEmpty() && phoneNumbers.isEmpty()) {
                return null
            }

            return Contact(
                id = 0, // Will be assigned by database
                firstName = firstName,
                lastName = lastName,
                organization = organization,
                title = title,
                photoUri = photoUri,
                notes = notes,
                isFavorite = false,
                phoneNumbers = phoneNumbers,
                emails = emails,
                addresses = addresses,
                groups = groups
            )
        }
    }
}

/**
 * Unescape vCard special characters
 */
private fun String.unescapeVcf(): String {
    return this
        .replace("\\n", "\n")
        .replace("\\r", "\r")
        .replace("\\t", "\t")
        .replace("\\,", ",")
        .replace("\\;", ";")
        .replace("\\:", ":")
        .replace("\\\\", "\\")
}
