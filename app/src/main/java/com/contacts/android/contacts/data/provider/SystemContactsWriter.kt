package com.contacts.android.contacts.data.provider

import android.content.ContentProviderOperation
import android.content.ContentResolver
import android.content.ContentUris
import android.content.Context
import android.provider.ContactsContract
import android.util.Log
import com.contacts.android.contacts.domain.model.*
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import javax.inject.Inject

/**
 * Handles writing contacts to Android's ContactsContract.
 * This allows contacts created in the app to be visible to other apps.
 */
class SystemContactsWriter @Inject constructor(
    private val contentResolver: ContentResolver,
    @ApplicationContext private val context: Context
) {
    companion object {
        private const val TAG = "SystemContactsWriter"
    }

    /**
     * Insert a new contact into Android's ContactsContract.
     * @param contact The contact to insert
     * @param accountName Optional account name (e.g., "user@gmail.com")
     * @param accountType Optional account type (e.g., "com.google")
     * @return The raw_contact_id if successful, null if failed
     */
    suspend fun insertContact(
        contact: Contact,
        accountName: String? = null,
        accountType: String? = null
    ): Long? = withContext(Dispatchers.IO) {
        try {
            val operations = ArrayList<ContentProviderOperation>()
            val backReferenceIndex = 0

            // 1. Create RawContact
            operations.add(
                ContentProviderOperation.newInsert(ContactsContract.RawContacts.CONTENT_URI)
                    .withValue(ContactsContract.RawContacts.ACCOUNT_NAME, accountName)
                    .withValue(ContactsContract.RawContacts.ACCOUNT_TYPE, accountType)
                    .build()
            )

            // 2. Add StructuredName
            operations.add(
                ContentProviderOperation.newInsert(ContactsContract.Data.CONTENT_URI)
                    .withValueBackReference(ContactsContract.Data.RAW_CONTACT_ID, backReferenceIndex)
                    .withValue(
                        ContactsContract.Data.MIMETYPE,
                        ContactsContract.CommonDataKinds.StructuredName.CONTENT_ITEM_TYPE
                    )
                    .withValue(ContactsContract.CommonDataKinds.StructuredName.PREFIX, contact.prefix)
                    .withValue(ContactsContract.CommonDataKinds.StructuredName.GIVEN_NAME, contact.firstName)
                    .withValue(ContactsContract.CommonDataKinds.StructuredName.MIDDLE_NAME, contact.middleName)
                    .withValue(ContactsContract.CommonDataKinds.StructuredName.FAMILY_NAME, contact.lastName)
                    .withValue(ContactsContract.CommonDataKinds.StructuredName.SUFFIX, contact.suffix)
                    .build()
            )

            // 3. Add Nickname if present
            contact.nickname?.let { nickname ->
                operations.add(
                    ContentProviderOperation.newInsert(ContactsContract.Data.CONTENT_URI)
                        .withValueBackReference(ContactsContract.Data.RAW_CONTACT_ID, backReferenceIndex)
                        .withValue(
                            ContactsContract.Data.MIMETYPE,
                            ContactsContract.CommonDataKinds.Nickname.CONTENT_ITEM_TYPE
                        )
                        .withValue(ContactsContract.CommonDataKinds.Nickname.NAME, nickname)
                        .build()
                )
            }

            // 4. Add Phone Numbers
            contact.phoneNumbers.forEach { phone ->
                operations.add(
                    ContentProviderOperation.newInsert(ContactsContract.Data.CONTENT_URI)
                        .withValueBackReference(ContactsContract.Data.RAW_CONTACT_ID, backReferenceIndex)
                        .withValue(
                            ContactsContract.Data.MIMETYPE,
                            ContactsContract.CommonDataKinds.Phone.CONTENT_ITEM_TYPE
                        )
                        .withValue(ContactsContract.CommonDataKinds.Phone.NUMBER, phone.number)
                        .withValue(ContactsContract.CommonDataKinds.Phone.TYPE, mapPhoneTypeToSystem(phone.type))
                        .withValue(ContactsContract.CommonDataKinds.Phone.LABEL, phone.label)
                        .build()
                )
            }

            // 5. Add Emails
            contact.emails.forEach { email ->
                operations.add(
                    ContentProviderOperation.newInsert(ContactsContract.Data.CONTENT_URI)
                        .withValueBackReference(ContactsContract.Data.RAW_CONTACT_ID, backReferenceIndex)
                        .withValue(
                            ContactsContract.Data.MIMETYPE,
                            ContactsContract.CommonDataKinds.Email.CONTENT_ITEM_TYPE
                        )
                        .withValue(ContactsContract.CommonDataKinds.Email.ADDRESS, email.email)
                        .withValue(ContactsContract.CommonDataKinds.Email.TYPE, mapEmailTypeToSystem(email.type))
                        .withValue(ContactsContract.CommonDataKinds.Email.LABEL, email.label)
                        .build()
                )
            }

            // 6. Add Addresses
            contact.addresses.forEach { address ->
                operations.add(
                    ContentProviderOperation.newInsert(ContactsContract.Data.CONTENT_URI)
                        .withValueBackReference(ContactsContract.Data.RAW_CONTACT_ID, backReferenceIndex)
                        .withValue(
                            ContactsContract.Data.MIMETYPE,
                            ContactsContract.CommonDataKinds.StructuredPostal.CONTENT_ITEM_TYPE
                        )
                        .withValue(ContactsContract.CommonDataKinds.StructuredPostal.STREET, address.street)
                        .withValue(ContactsContract.CommonDataKinds.StructuredPostal.CITY, address.city)
                        .withValue(ContactsContract.CommonDataKinds.StructuredPostal.REGION, address.state)
                        .withValue(ContactsContract.CommonDataKinds.StructuredPostal.POSTCODE, address.postalCode)
                        .withValue(ContactsContract.CommonDataKinds.StructuredPostal.COUNTRY, address.country)
                        .withValue(ContactsContract.CommonDataKinds.StructuredPostal.TYPE, mapAddressTypeToSystem(address.type))
                        .withValue(ContactsContract.CommonDataKinds.StructuredPostal.LABEL, address.label)
                        .build()
                )
            }

            // 7. Add Organization
            if (!contact.organization.isNullOrBlank() || !contact.title.isNullOrBlank()) {
                operations.add(
                    ContentProviderOperation.newInsert(ContactsContract.Data.CONTENT_URI)
                        .withValueBackReference(ContactsContract.Data.RAW_CONTACT_ID, backReferenceIndex)
                        .withValue(
                            ContactsContract.Data.MIMETYPE,
                            ContactsContract.CommonDataKinds.Organization.CONTENT_ITEM_TYPE
                        )
                        .withValue(ContactsContract.CommonDataKinds.Organization.COMPANY, contact.organization)
                        .withValue(ContactsContract.CommonDataKinds.Organization.TITLE, contact.title)
                        .build()
                )
            }

            // 8. Add Note
            contact.notes?.takeIf { it.isNotBlank() }?.let { notes ->
                operations.add(
                    ContentProviderOperation.newInsert(ContactsContract.Data.CONTENT_URI)
                        .withValueBackReference(ContactsContract.Data.RAW_CONTACT_ID, backReferenceIndex)
                        .withValue(
                            ContactsContract.Data.MIMETYPE,
                            ContactsContract.CommonDataKinds.Note.CONTENT_ITEM_TYPE
                        )
                        .withValue(ContactsContract.CommonDataKinds.Note.NOTE, notes)
                        .build()
                )
            }

            // 9. Add Birthday
            contact.birthday?.takeIf { it.isNotBlank() }?.let { birthday ->
                operations.add(
                    ContentProviderOperation.newInsert(ContactsContract.Data.CONTENT_URI)
                        .withValueBackReference(ContactsContract.Data.RAW_CONTACT_ID, backReferenceIndex)
                        .withValue(
                            ContactsContract.Data.MIMETYPE,
                            ContactsContract.CommonDataKinds.Event.CONTENT_ITEM_TYPE
                        )
                        .withValue(ContactsContract.CommonDataKinds.Event.START_DATE, birthday)
                        .withValue(
                            ContactsContract.CommonDataKinds.Event.TYPE,
                            ContactsContract.CommonDataKinds.Event.TYPE_BIRTHDAY
                        )
                        .build()
                )
            }

            // 10. Set favorite status
            if (contact.isFavorite) {
                operations.add(
                    ContentProviderOperation.newInsert(ContactsContract.Data.CONTENT_URI)
                        .withValueBackReference(ContactsContract.Data.RAW_CONTACT_ID, backReferenceIndex)
                        .withValue(ContactsContract.RawContacts.STARRED, 1)
                        .build()
                )
            }

            // Execute batch
            val results = contentResolver.applyBatch(ContactsContract.AUTHORITY, operations)

            // Extract raw_contact_id from first result
            val rawContactId = results.firstOrNull()?.uri?.lastPathSegment?.toLongOrNull()
            Log.d(TAG, "Contact inserted successfully with rawContactId: $rawContactId")
            rawContactId

        } catch (e: Exception) {
            Log.e(TAG, "Failed to insert contact to system", e)
            null
        }
    }

    /**
     * Update an existing contact in Android's ContactsContract.
     * Uses delete-then-reinsert strategy for data rows (same approach as Fossify).
     * @param contact The contact with updated data
     * @param rawContactId The existing raw_contact_id in the system
     * @return true if successful, false otherwise
     */
    suspend fun updateContact(contact: Contact, rawContactId: Long): Boolean = withContext(Dispatchers.IO) {
        try {
            val operations = ArrayList<ContentProviderOperation>()

            // Delete all existing data for this raw contact (except the raw contact itself)
            val mimeTypes = listOf(
                ContactsContract.CommonDataKinds.StructuredName.CONTENT_ITEM_TYPE,
                ContactsContract.CommonDataKinds.Nickname.CONTENT_ITEM_TYPE,
                ContactsContract.CommonDataKinds.Phone.CONTENT_ITEM_TYPE,
                ContactsContract.CommonDataKinds.Email.CONTENT_ITEM_TYPE,
                ContactsContract.CommonDataKinds.StructuredPostal.CONTENT_ITEM_TYPE,
                ContactsContract.CommonDataKinds.Organization.CONTENT_ITEM_TYPE,
                ContactsContract.CommonDataKinds.Note.CONTENT_ITEM_TYPE,
                ContactsContract.CommonDataKinds.Event.CONTENT_ITEM_TYPE
            )

            mimeTypes.forEach { mimeType ->
                operations.add(
                    ContentProviderOperation.newDelete(ContactsContract.Data.CONTENT_URI)
                        .withSelection(
                            "${ContactsContract.Data.RAW_CONTACT_ID} = ? AND ${ContactsContract.Data.MIMETYPE} = ?",
                            arrayOf(rawContactId.toString(), mimeType)
                        )
                        .build()
                )
            }

            // Re-insert StructuredName
            operations.add(
                ContentProviderOperation.newInsert(ContactsContract.Data.CONTENT_URI)
                    .withValue(ContactsContract.Data.RAW_CONTACT_ID, rawContactId)
                    .withValue(
                        ContactsContract.Data.MIMETYPE,
                        ContactsContract.CommonDataKinds.StructuredName.CONTENT_ITEM_TYPE
                    )
                    .withValue(ContactsContract.CommonDataKinds.StructuredName.PREFIX, contact.prefix)
                    .withValue(ContactsContract.CommonDataKinds.StructuredName.GIVEN_NAME, contact.firstName)
                    .withValue(ContactsContract.CommonDataKinds.StructuredName.MIDDLE_NAME, contact.middleName)
                    .withValue(ContactsContract.CommonDataKinds.StructuredName.FAMILY_NAME, contact.lastName)
                    .withValue(ContactsContract.CommonDataKinds.StructuredName.SUFFIX, contact.suffix)
                    .build()
            )

            // Re-insert Nickname
            contact.nickname?.let { nickname ->
                operations.add(
                    ContentProviderOperation.newInsert(ContactsContract.Data.CONTENT_URI)
                        .withValue(ContactsContract.Data.RAW_CONTACT_ID, rawContactId)
                        .withValue(
                            ContactsContract.Data.MIMETYPE,
                            ContactsContract.CommonDataKinds.Nickname.CONTENT_ITEM_TYPE
                        )
                        .withValue(ContactsContract.CommonDataKinds.Nickname.NAME, nickname)
                        .build()
                )
            }

            // Re-insert Phone Numbers
            contact.phoneNumbers.forEach { phone ->
                operations.add(
                    ContentProviderOperation.newInsert(ContactsContract.Data.CONTENT_URI)
                        .withValue(ContactsContract.Data.RAW_CONTACT_ID, rawContactId)
                        .withValue(
                            ContactsContract.Data.MIMETYPE,
                            ContactsContract.CommonDataKinds.Phone.CONTENT_ITEM_TYPE
                        )
                        .withValue(ContactsContract.CommonDataKinds.Phone.NUMBER, phone.number)
                        .withValue(ContactsContract.CommonDataKinds.Phone.TYPE, mapPhoneTypeToSystem(phone.type))
                        .withValue(ContactsContract.CommonDataKinds.Phone.LABEL, phone.label)
                        .build()
                )
            }

            // Re-insert Emails
            contact.emails.forEach { email ->
                operations.add(
                    ContentProviderOperation.newInsert(ContactsContract.Data.CONTENT_URI)
                        .withValue(ContactsContract.Data.RAW_CONTACT_ID, rawContactId)
                        .withValue(
                            ContactsContract.Data.MIMETYPE,
                            ContactsContract.CommonDataKinds.Email.CONTENT_ITEM_TYPE
                        )
                        .withValue(ContactsContract.CommonDataKinds.Email.ADDRESS, email.email)
                        .withValue(ContactsContract.CommonDataKinds.Email.TYPE, mapEmailTypeToSystem(email.type))
                        .withValue(ContactsContract.CommonDataKinds.Email.LABEL, email.label)
                        .build()
                )
            }

            // Re-insert Addresses
            contact.addresses.forEach { address ->
                operations.add(
                    ContentProviderOperation.newInsert(ContactsContract.Data.CONTENT_URI)
                        .withValue(ContactsContract.Data.RAW_CONTACT_ID, rawContactId)
                        .withValue(
                            ContactsContract.Data.MIMETYPE,
                            ContactsContract.CommonDataKinds.StructuredPostal.CONTENT_ITEM_TYPE
                        )
                        .withValue(ContactsContract.CommonDataKinds.StructuredPostal.STREET, address.street)
                        .withValue(ContactsContract.CommonDataKinds.StructuredPostal.CITY, address.city)
                        .withValue(ContactsContract.CommonDataKinds.StructuredPostal.REGION, address.state)
                        .withValue(ContactsContract.CommonDataKinds.StructuredPostal.POSTCODE, address.postalCode)
                        .withValue(ContactsContract.CommonDataKinds.StructuredPostal.COUNTRY, address.country)
                        .withValue(ContactsContract.CommonDataKinds.StructuredPostal.TYPE, mapAddressTypeToSystem(address.type))
                        .withValue(ContactsContract.CommonDataKinds.StructuredPostal.LABEL, address.label)
                        .build()
                )
            }

            // Re-insert Organization
            if (!contact.organization.isNullOrBlank() || !contact.title.isNullOrBlank()) {
                operations.add(
                    ContentProviderOperation.newInsert(ContactsContract.Data.CONTENT_URI)
                        .withValue(ContactsContract.Data.RAW_CONTACT_ID, rawContactId)
                        .withValue(
                            ContactsContract.Data.MIMETYPE,
                            ContactsContract.CommonDataKinds.Organization.CONTENT_ITEM_TYPE
                        )
                        .withValue(ContactsContract.CommonDataKinds.Organization.COMPANY, contact.organization)
                        .withValue(ContactsContract.CommonDataKinds.Organization.TITLE, contact.title)
                        .build()
                )
            }

            // Re-insert Note
            contact.notes?.takeIf { it.isNotBlank() }?.let { notes ->
                operations.add(
                    ContentProviderOperation.newInsert(ContactsContract.Data.CONTENT_URI)
                        .withValue(ContactsContract.Data.RAW_CONTACT_ID, rawContactId)
                        .withValue(
                            ContactsContract.Data.MIMETYPE,
                            ContactsContract.CommonDataKinds.Note.CONTENT_ITEM_TYPE
                        )
                        .withValue(ContactsContract.CommonDataKinds.Note.NOTE, notes)
                        .build()
                )
            }

            // Re-insert Birthday
            contact.birthday?.takeIf { it.isNotBlank() }?.let { birthday ->
                operations.add(
                    ContentProviderOperation.newInsert(ContactsContract.Data.CONTENT_URI)
                        .withValue(ContactsContract.Data.RAW_CONTACT_ID, rawContactId)
                        .withValue(
                            ContactsContract.Data.MIMETYPE,
                            ContactsContract.CommonDataKinds.Event.CONTENT_ITEM_TYPE
                        )
                        .withValue(ContactsContract.CommonDataKinds.Event.START_DATE, birthday)
                        .withValue(
                            ContactsContract.CommonDataKinds.Event.TYPE,
                            ContactsContract.CommonDataKinds.Event.TYPE_BIRTHDAY
                        )
                        .build()
                )
            }

            // Update favorite status on raw contact
            operations.add(
                ContentProviderOperation.newUpdate(
                    ContentUris.withAppendedId(ContactsContract.RawContacts.CONTENT_URI, rawContactId)
                )
                    .withValue(ContactsContract.RawContacts.STARRED, if (contact.isFavorite) 1 else 0)
                    .build()
            )

            // Execute batch
            contentResolver.applyBatch(ContactsContract.AUTHORITY, operations)
            Log.d(TAG, "Contact updated successfully for rawContactId: $rawContactId")
            true

        } catch (e: Exception) {
            Log.e(TAG, "Failed to update contact in system", e)
            false
        }
    }

    /**
     * Delete a contact from Android's ContactsContract.
     * @param rawContactId The raw_contact_id to delete
     * @return true if successful, false otherwise
     */
    suspend fun deleteContact(rawContactId: Long): Boolean = withContext(Dispatchers.IO) {
        try {
            val uri = ContentUris.withAppendedId(ContactsContract.RawContacts.CONTENT_URI, rawContactId)
            val deleted = contentResolver.delete(uri, null, null)
            Log.d(TAG, "Contact deleted: rawContactId=$rawContactId, rows=$deleted")
            deleted > 0
        } catch (e: Exception) {
            Log.e(TAG, "Failed to delete contact from system", e)
            false
        }
    }

    private fun mapPhoneTypeToSystem(type: PhoneType): Int = when (type) {
        PhoneType.MOBILE -> ContactsContract.CommonDataKinds.Phone.TYPE_MOBILE
        PhoneType.HOME -> ContactsContract.CommonDataKinds.Phone.TYPE_HOME
        PhoneType.WORK -> ContactsContract.CommonDataKinds.Phone.TYPE_WORK
        PhoneType.FAX -> ContactsContract.CommonDataKinds.Phone.TYPE_FAX_WORK
        PhoneType.PAGER -> ContactsContract.CommonDataKinds.Phone.TYPE_PAGER
        PhoneType.OTHER -> ContactsContract.CommonDataKinds.Phone.TYPE_OTHER
        PhoneType.CUSTOM -> ContactsContract.CommonDataKinds.Phone.TYPE_CUSTOM
    }

    private fun mapEmailTypeToSystem(type: EmailType): Int = when (type) {
        EmailType.HOME -> ContactsContract.CommonDataKinds.Email.TYPE_HOME
        EmailType.WORK -> ContactsContract.CommonDataKinds.Email.TYPE_WORK
        EmailType.OTHER -> ContactsContract.CommonDataKinds.Email.TYPE_OTHER
        EmailType.CUSTOM -> ContactsContract.CommonDataKinds.Email.TYPE_CUSTOM
    }

    private fun mapAddressTypeToSystem(type: AddressType): Int = when (type) {
        AddressType.HOME -> ContactsContract.CommonDataKinds.StructuredPostal.TYPE_HOME
        AddressType.WORK -> ContactsContract.CommonDataKinds.StructuredPostal.TYPE_WORK
        AddressType.OTHER -> ContactsContract.CommonDataKinds.StructuredPostal.TYPE_OTHER
        AddressType.CUSTOM -> ContactsContract.CommonDataKinds.StructuredPostal.TYPE_CUSTOM
    }
}
