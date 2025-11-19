package com.contacts.android.contacts.domain.model

import android.telephony.PhoneNumberUtils
import java.util.Locale

data class PhoneNumber(
    val id: Long = 0,
    val number: String,
    val type: PhoneType = PhoneType.MOBILE,
    val label: String? = null
) {
    val displayType: String
        get() = label?.takeIf { it.isNotBlank() } ?: type.toDisplayString()

    /**
     * Format the phone number according to locale
     * Like Fossify Contacts
     */
    fun getFormattedNumber(): String {
        return try {
            val countryCode = Locale.getDefault().country
            PhoneNumberUtils.formatNumber(number, countryCode) ?: number
        } catch (e: Exception) {
            number
        }
    }
}
