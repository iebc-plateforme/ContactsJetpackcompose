package com.contacts.android.contactsjetpackcompose.data.provider

import android.content.ContentResolver
import android.database.Cursor
import android.provider.ContactsContract
import android.util.Log
import com.contacts.android.contactsjetpackcompose.domain.model.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import javax.inject.Inject

class ContactsProvider @Inject constructor(
    private val contentResolver: ContentResolver
) {

    /**
     * Reads system-defined groups from Android Contacts Provider
     * These include default groups like Coworkers, Family, Friends, etc.
     */
    suspend fun getSystemGroups(): List<SystemGroupData> = withContext(Dispatchers.IO) {
        val groups = mutableListOf<SystemGroupData>()

        try {
            contentResolver.query(
                ContactsContract.Groups.CONTENT_URI,
                arrayOf(
                    ContactsContract.Groups._ID,
                    ContactsContract.Groups.TITLE,
                    ContactsContract.Groups.SYSTEM_ID,
                    ContactsContract.Groups.ACCOUNT_NAME,
                    ContactsContract.Groups.ACCOUNT_TYPE,
                    ContactsContract.Groups.GROUP_VISIBLE,
                    ContactsContract.Groups.DELETED
                ),
                "${ContactsContract.Groups.DELETED} = 0 AND ${ContactsContract.Groups.GROUP_VISIBLE} = 1",
                null,
                ContactsContract.Groups.TITLE + " ASC"
            )?.use { cursor ->
                while (cursor.moveToNext()) {
                    val id = cursor.getLong(0)
                    val title = cursor.getString(1) ?: continue
                    val systemId = cursor.getString(2)
                    val accountName = cursor.getString(3)
                    val accountType = cursor.getString(4)
                    val visible = cursor.getInt(5) == 1

                    // Get contact count for this group
                    val contactCount = getGroupContactCount(id)

                    groups.add(
                        SystemGroupData(
                            id = id,
                            title = title,
                            systemId = systemId,
                            accountName = accountName,
                            accountType = accountType,
                            isVisible = visible,
                            contactCount = contactCount
                        )
                    )
                }
            }
        } catch (e: Exception) {
            Log.e("ContactsProvider", "Error reading system groups", e)
        }

        groups
    }

    /**
     * Gets the number of contacts in a system group
     */
    private fun getGroupContactCount(groupId: Long): Int {
        var count = 0
        try {
            contentResolver.query(
                ContactsContract.Data.CONTENT_URI,
                arrayOf(ContactsContract.Data.CONTACT_ID),
                "${ContactsContract.Data.MIMETYPE} = ? AND ${ContactsContract.CommonDataKinds.GroupMembership.GROUP_ROW_ID} = ?",
                arrayOf(
                    ContactsContract.CommonDataKinds.GroupMembership.CONTENT_ITEM_TYPE,
                    groupId.toString()
                ),
                null
            )?.use { cursor ->
                count = cursor.count
            }
        } catch (e: Exception) {
            Log.e("ContactsProvider", "Error counting group contacts", e)
        }
        return count
    }
    suspend fun getAllContacts(): List<ContactData> = withContext(Dispatchers.IO) {
        val contacts = mutableMapOf<Long, ContactData>()

        // Read basic contact info
        contentResolver.query(
            ContactsContract.Contacts.CONTENT_URI,
            arrayOf(
                ContactsContract.Contacts._ID,
                ContactsContract.Contacts.DISPLAY_NAME_PRIMARY,
                ContactsContract.Contacts.STARRED,
                ContactsContract.Contacts.PHOTO_URI
            ),
            null,
            null,
            ContactsContract.Contacts.DISPLAY_NAME_PRIMARY + " ASC"
        )?.use { cursor ->
            while (cursor.moveToNext()) {
                val id = cursor.getLong(0)
                val displayName = cursor.getString(1) ?: ""
                val isStarred = cursor.getInt(2) == 1
                val photoUri = cursor.getString(3)

                contacts[id] = ContactData(
                    id = id,
                    displayName = displayName,
                    isFavorite = isStarred,
                    photoUri = photoUri,
                    phoneNumbers = mutableListOf(),
                    emails = mutableListOf(),
                    addresses = mutableListOf()
                )
            }
        }

        // Read phone numbers
        contacts.keys.forEach { contactId ->
            contentResolver.query(
                ContactsContract.CommonDataKinds.Phone.CONTENT_URI,
                arrayOf(
                    ContactsContract.CommonDataKinds.Phone.NUMBER,
                    ContactsContract.CommonDataKinds.Phone.TYPE,
                    ContactsContract.CommonDataKinds.Phone.LABEL
                ),
                "${ContactsContract.CommonDataKinds.Phone.CONTACT_ID} = ?",
                arrayOf(contactId.toString()),
                null
            )?.use { cursor ->
                while (cursor.moveToNext()) {
                    val number = cursor.getString(0) ?: ""
                    val type = cursor.getInt(1)
                    val label = cursor.getString(2)

                    contacts[contactId]?.phoneNumbers?.add(
                        PhoneNumberData(
                            number = number,
                            type = mapPhoneType(type, label)
                        )
                    )
                }
            }
        }

        // Read emails
        contacts.keys.forEach { contactId ->
            contentResolver.query(
                ContactsContract.CommonDataKinds.Email.CONTENT_URI,
                arrayOf(
                    ContactsContract.CommonDataKinds.Email.ADDRESS,
                    ContactsContract.CommonDataKinds.Email.TYPE,
                    ContactsContract.CommonDataKinds.Email.LABEL
                ),
                "${ContactsContract.CommonDataKinds.Email.CONTACT_ID} = ?",
                arrayOf(contactId.toString()),
                null
            )?.use { cursor ->
                while (cursor.moveToNext()) {
                    val address = cursor.getString(0) ?: ""
                    val type = cursor.getInt(1)
                    val label = cursor.getString(2)

                    contacts[contactId]?.emails?.add(
                        EmailData(
                            address = address,
                            type = mapEmailType(type, label)
                        )
                    )
                }
            }
        }

        // Read addresses
        contacts.keys.forEach { contactId ->
            contentResolver.query(
                ContactsContract.CommonDataKinds.StructuredPostal.CONTENT_URI,
                arrayOf(
                    ContactsContract.CommonDataKinds.StructuredPostal.STREET,
                    ContactsContract.CommonDataKinds.StructuredPostal.CITY,
                    ContactsContract.CommonDataKinds.StructuredPostal.REGION,
                    ContactsContract.CommonDataKinds.StructuredPostal.POSTCODE,
                    ContactsContract.CommonDataKinds.StructuredPostal.COUNTRY,
                    ContactsContract.CommonDataKinds.StructuredPostal.TYPE,
                    ContactsContract.CommonDataKinds.StructuredPostal.LABEL
                ),
                "${ContactsContract.CommonDataKinds.StructuredPostal.CONTACT_ID} = ?",
                arrayOf(contactId.toString()),
                null
            )?.use { cursor ->
                while (cursor.moveToNext()) {
                    val street = cursor.getString(0) ?: ""
                    val city = cursor.getString(1) ?: ""
                    val state = cursor.getString(2) ?: ""
                    val postalCode = cursor.getString(3) ?: ""
                    val country = cursor.getString(4) ?: ""
                    val type = cursor.getInt(5)
                    val label = cursor.getString(6)

                    contacts[contactId]?.addresses?.add(
                        AddressData(
                            street = street,
                            city = city,
                            state = state,
                            postalCode = postalCode,
                            country = country,
                            type = mapAddressType(type, label)
                        )
                    )
                }
            }
        }

        contacts.values.toList()
    }

    private fun mapPhoneType(type: Int, label: String?): PhoneType {
        return when (type) {
            ContactsContract.CommonDataKinds.Phone.TYPE_MOBILE -> PhoneType.MOBILE
            ContactsContract.CommonDataKinds.Phone.TYPE_HOME -> PhoneType.HOME
            ContactsContract.CommonDataKinds.Phone.TYPE_WORK -> PhoneType.WORK
            ContactsContract.CommonDataKinds.Phone.TYPE_FAX_WORK -> PhoneType.FAX
            ContactsContract.CommonDataKinds.Phone.TYPE_FAX_HOME -> PhoneType.FAX
            ContactsContract.CommonDataKinds.Phone.TYPE_PAGER -> PhoneType.PAGER
            ContactsContract.CommonDataKinds.Phone.TYPE_MAIN -> PhoneType.MOBILE
            ContactsContract.CommonDataKinds.Phone.TYPE_CUSTOM -> PhoneType.CUSTOM
            else -> PhoneType.OTHER
        }
    }

    private fun mapEmailType(type: Int, label: String?): EmailType {
        return when (type) {
            ContactsContract.CommonDataKinds.Email.TYPE_HOME -> EmailType.HOME
            ContactsContract.CommonDataKinds.Email.TYPE_WORK -> EmailType.WORK
            ContactsContract.CommonDataKinds.Email.TYPE_MOBILE -> EmailType.OTHER
            ContactsContract.CommonDataKinds.Email.TYPE_CUSTOM -> EmailType.CUSTOM
            else -> EmailType.OTHER
        }
    }

    private fun mapAddressType(type: Int, label: String?): AddressType {
        return when (type) {
            ContactsContract.CommonDataKinds.StructuredPostal.TYPE_HOME -> AddressType.HOME
            ContactsContract.CommonDataKinds.StructuredPostal.TYPE_WORK -> AddressType.WORK
            ContactsContract.CommonDataKinds.StructuredPostal.TYPE_CUSTOM -> AddressType.CUSTOM
            else -> AddressType.OTHER
        }
    }
}

data class ContactData(
    val id: Long,
    val displayName: String,
    val isFavorite: Boolean,
    val photoUri: String?,
    val phoneNumbers: MutableList<PhoneNumberData>,
    val emails: MutableList<EmailData>,
    val addresses: MutableList<AddressData>
)

data class PhoneNumberData(
    val number: String,
    val type: PhoneType
)

data class EmailData(
    val address: String,
    val type: EmailType
)

data class AddressData(
    val street: String,
    val city: String,
    val state: String,
    val postalCode: String,
    val country: String,
    val type: AddressType
)

data class SystemGroupData(
    val id: Long,
    val title: String,
    val systemId: String?,
    val accountName: String?,
    val accountType: String?,
    val isVisible: Boolean,
    val contactCount: Int
)
