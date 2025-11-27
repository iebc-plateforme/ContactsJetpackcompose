package com.contacts.android.contacts.presentation.util

import com.contacts.android.contacts.domain.model.Address
import com.contacts.android.contacts.domain.model.Contact
import com.contacts.android.contacts.domain.model.Email
import com.contacts.android.contacts.domain.model.EmailType
import com.contacts.android.contacts.domain.model.PhoneNumber
import com.contacts.android.contacts.domain.model.PhoneType
import com.contacts.android.contacts.domain.model.Website
import com.contacts.android.contacts.domain.model.AddressType

/**
 * Utility object for encoding and decoding vCard format
 * vCard 3.0 specification
 */
object VCardUtils {

    /**
     * Encode a Contact into vCard 3.0 format
     */
    fun encodeToVCard(contact: Contact): String = buildString {
        appendLine("BEGIN:VCARD")
        appendLine("VERSION:3.0")

        // Full name (FN) - required
        appendLine("FN:${contact.displayName}")

        // Structured name (N) - Last;First;Middle;Prefix;Suffix
        val name = buildString {
            append(contact.lastName.escapeVCard())
            append(";")
            append(contact.firstName.escapeVCard())
            append(";")
            append((contact.middleName ?: "").escapeVCard())
            append(";")
            append((contact.prefix ?: "").escapeVCard())
            append(";")
            append((contact.suffix ?: "").escapeVCard())
        }
        appendLine("N:$name")

        // Nickname
        contact.nickname?.let {
            appendLine("NICKNAME:${it.escapeVCard()}")
        }

        // Phone numbers
        contact.phoneNumbers.forEach { phone ->
            val typeStr = when (phone.type) {
                PhoneType.MOBILE -> "CELL"
                PhoneType.HOME -> "HOME"
                PhoneType.WORK -> "WORK"
                PhoneType.FAX -> "FAX"
                PhoneType.PAGER -> "PAGER"
                PhoneType.OTHER -> "VOICE"
                PhoneType.CUSTOM -> phone.label ?: "VOICE"
            }
            appendLine("TEL;TYPE=$typeStr:${phone.number}")
        }

        // Emails
        contact.emails.forEach { email ->
            val typeStr = when (email.type) {
                EmailType.HOME -> "HOME"
                EmailType.WORK -> "WORK"
                EmailType.OTHER -> "INTERNET"
                EmailType.CUSTOM -> email.label ?: "INTERNET"
            }
            appendLine("EMAIL;TYPE=$typeStr:${email.email}")
        }

        // Addresses
        contact.addresses.forEach { address ->
            val typeStr = when (address.type) {
                AddressType.HOME -> "HOME"
                AddressType.WORK -> "WORK"
                AddressType.OTHER -> "POSTAL"
                AddressType.CUSTOM -> address.label ?: "POSTAL"
            }
            // ADR format: ;;street;city;state;postalCode;country
            val adr = buildString {
                append(";;") // post office box and extended address (empty)
                append((address.street ?: "").escapeVCard())
                append(";")
                append((address.city ?: "").escapeVCard())
                append(";")
                append((address.state ?: "").escapeVCard())
                append(";")
                append((address.postalCode ?: "").escapeVCard())
                append(";")
                append((address.country ?: "").escapeVCard())
            }
            appendLine("ADR;TYPE=$typeStr:$adr")
        }

        // Organization
        contact.organization?.let {
            val org = buildString {
                append(it.escapeVCard())
                contact.title?.let { title ->
                    append(";${title.escapeVCard()}")
                }
            }
            appendLine("ORG:$org")
        }

        // Title (if no organization)
        if (contact.organization == null && contact.title != null) {
            appendLine("TITLE:${contact.title.escapeVCard()}")
        }

        // Websites
        contact.websites.forEach { website ->
            appendLine("URL:${website.url}")
        }

        // Birthday (ISO format: YYYY-MM-DD -> YYYYMMDD)
        contact.birthday?.let { birthday ->
            val formattedBirthday = birthday.replace("-", "")
            appendLine("BDAY:$formattedBirthday")
        }

        // Note
        contact.notes?.let {
            appendLine("NOTE:${it.escapeVCard()}")
        }

        appendLine("END:VCARD")
    }

    /**
     * Decode vCard 3.0 format into a Contact
     * Returns null if the vCard is invalid
     */
    fun decodeFromVCard(vCardData: String): Contact? {
        try {
            val lines = vCardData.lines()
                .map { it.trim() }
                .filter { it.isNotEmpty() }

            if (!lines.any { it.startsWith("BEGIN:VCARD") } ||
                !lines.any { it.startsWith("END:VCARD") }
            ) {
                return null
            }

            var firstName = ""
            var middleName: String? = null
            var lastName = ""
            var prefix: String? = null
            var suffix: String? = null
            var nickname: String? = null
            val phoneNumbers = mutableListOf<PhoneNumber>()
            val emails = mutableListOf<Email>()
            val addresses = mutableListOf<Address>()
            val websites = mutableListOf<Website>()
            var organization: String? = null
            var title: String? = null
            var birthday: String? = null
            var notes: String? = null

            for (line in lines) {
                when {
                    line.startsWith("FN:") -> {
                        // Full name - we'll use the structured name (N) instead
                    }
                    line.startsWith("N:") -> {
                        val parts = line.substring(2).split(";")
                        if (parts.isNotEmpty()) lastName = parts[0].unescapeVCard()
                        if (parts.size > 1) firstName = parts[1].unescapeVCard()
                        if (parts.size > 2) middleName = parts[2].takeIf { it.isNotEmpty() }?.unescapeVCard()
                        if (parts.size > 3) prefix = parts[3].takeIf { it.isNotEmpty() }?.unescapeVCard()
                        if (parts.size > 4) suffix = parts[4].takeIf { it.isNotEmpty() }?.unescapeVCard()
                    }
                    line.startsWith("NICKNAME:") -> {
                        nickname = line.substring(9).unescapeVCard()
                    }
                    line.startsWith("TEL") -> {
                        val (typeInfo, number) = parseVCardLine(line, "TEL")
                        val phoneType = parsePhoneType(typeInfo)
                        if (number.isNotEmpty()) {
                            phoneNumbers.add(PhoneNumber(number = number, type = phoneType))
                        }
                    }
                    line.startsWith("EMAIL") -> {
                        val (typeInfo, email) = parseVCardLine(line, "EMAIL")
                        val emailType = parseEmailType(typeInfo)
                        if (email.isNotEmpty()) {
                            emails.add(Email(email = email, type = emailType))
                        }
                    }
                    line.startsWith("ADR") -> {
                        val (typeInfo, adr) = parseVCardLine(line, "ADR")
                        val addressType = parseAddressType(typeInfo)
                        // ADR format: ;;street;city;state;postalCode;country
                        val parts = adr.split(";")
                        if (parts.size >= 3) {
                            val address = Address(
                                street = parts.getOrNull(2)?.takeIf { it.isNotEmpty() }?.unescapeVCard(),
                                city = parts.getOrNull(3)?.takeIf { it.isNotEmpty() }?.unescapeVCard(),
                                state = parts.getOrNull(4)?.takeIf { it.isNotEmpty() }?.unescapeVCard(),
                                postalCode = parts.getOrNull(5)?.takeIf { it.isNotEmpty() }?.unescapeVCard(),
                                country = parts.getOrNull(6)?.takeIf { it.isNotEmpty() }?.unescapeVCard(),
                                type = addressType
                            )
                            if (address.isNotEmpty) {
                                addresses.add(address)
                            }
                        }
                    }
                    line.startsWith("ORG:") -> {
                        val parts = line.substring(4).split(";")
                        organization = parts[0].unescapeVCard()
                        if (parts.size > 1) {
                            title = parts[1].unescapeVCard()
                        }
                    }
                    line.startsWith("TITLE:") -> {
                        if (title == null) {
                            title = line.substring(6).unescapeVCard()
                        }
                    }
                    line.startsWith("URL:") -> {
                        val url = line.substring(4).trim()
                        if (url.isNotEmpty()) {
                            websites.add(Website(url = url))
                        }
                    }
                    line.startsWith("BDAY:") -> {
                        // YYYYMMDD -> YYYY-MM-DD
                        val bday = line.substring(5).trim()
                        birthday = if (bday.length == 8 && bday.all { it.isDigit() }) {
                            "${bday.substring(0, 4)}-${bday.substring(4, 6)}-${bday.substring(6, 8)}"
                        } else {
                            bday
                        }
                    }
                    line.startsWith("NOTE:") -> {
                        notes = line.substring(5).unescapeVCard()
                    }
                }
            }

            // Return null if no essential data
            if (firstName.isEmpty() && lastName.isEmpty() && phoneNumbers.isEmpty() && emails.isEmpty()) {
                return null
            }

            return Contact(
                firstName = firstName,
                middleName = middleName,
                lastName = lastName,
                prefix = prefix,
                suffix = suffix,
                nickname = nickname,
                phoneNumbers = phoneNumbers,
                emails = emails,
                addresses = addresses,
                websites = websites,
                organization = organization,
                title = title,
                birthday = birthday,
                notes = notes
            )
        } catch (e: Exception) {
            return null
        }
    }

    private fun parseVCardLine(line: String, prefix: String): Pair<String, String> {
        val colonIndex = line.indexOf(':')
        if (colonIndex == -1) return "" to ""

        val typeInfo = line.substring(prefix.length, colonIndex)
        val value = line.substring(colonIndex + 1)
        return typeInfo to value
    }

    private fun parsePhoneType(typeInfo: String): PhoneType {
        return when {
            typeInfo.contains("CELL", ignoreCase = true) -> PhoneType.MOBILE
            typeInfo.contains("HOME", ignoreCase = true) -> PhoneType.HOME
            typeInfo.contains("WORK", ignoreCase = true) -> PhoneType.WORK
            typeInfo.contains("FAX", ignoreCase = true) -> PhoneType.FAX
            typeInfo.contains("PAGER", ignoreCase = true) -> PhoneType.PAGER
            else -> PhoneType.MOBILE
        }
    }

    private fun parseEmailType(typeInfo: String): EmailType {
        return when {
            typeInfo.contains("HOME", ignoreCase = true) -> EmailType.HOME
            typeInfo.contains("WORK", ignoreCase = true) -> EmailType.WORK
            else -> EmailType.OTHER
        }
    }

    private fun parseAddressType(typeInfo: String): AddressType {
        return when {
            typeInfo.contains("HOME", ignoreCase = true) -> AddressType.HOME
            typeInfo.contains("WORK", ignoreCase = true) -> AddressType.WORK
            else -> AddressType.OTHER
        }
    }

    private fun String.escapeVCard(): String {
        return this
            .replace("\\", "\\\\")
            .replace(",", "\\,")
            .replace(";", "\\;")
            .replace("\n", "\\n")
    }

    private fun String.unescapeVCard(): String {
        return this
            .replace("\\n", "\n")
            .replace("\\;", ";")
            .replace("\\,", ",")
            .replace("\\\\", "\\")
    }
}
