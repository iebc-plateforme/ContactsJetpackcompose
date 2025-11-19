package com.contacts.android.contacts.data.local.converter

import androidx.room.TypeConverter
import com.contacts.android.contacts.domain.model.PhoneType
import com.contacts.android.contacts.domain.model.EmailType
import com.contacts.android.contacts.domain.model.AddressType

class TypeConverters {

    @TypeConverter
    fun fromPhoneType(value: PhoneType): String {
        return value.name
    }

    @TypeConverter
    fun toPhoneType(value: String): PhoneType {
        return try {
            PhoneType.valueOf(value)
        } catch (e: IllegalArgumentException) {
            PhoneType.OTHER
        }
    }

    @TypeConverter
    fun fromEmailType(value: EmailType): String {
        return value.name
    }

    @TypeConverter
    fun toEmailType(value: String): EmailType {
        return try {
            EmailType.valueOf(value)
        } catch (e: IllegalArgumentException) {
            EmailType.OTHER
        }
    }

    @TypeConverter
    fun fromAddressType(value: AddressType): String {
        return value.name
    }

    @TypeConverter
    fun toAddressType(value: String): AddressType {
        return try {
            AddressType.valueOf(value)
        } catch (e: IllegalArgumentException) {
            AddressType.OTHER
        }
    }
}
