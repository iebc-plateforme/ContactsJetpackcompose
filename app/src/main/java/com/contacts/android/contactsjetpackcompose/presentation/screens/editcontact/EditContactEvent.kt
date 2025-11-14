package com.contacts.android.contactsjetpackcompose.presentation.screens.editcontact

import com.contacts.android.contactsjetpackcompose.domain.model.*

sealed class EditContactEvent {
    data class FirstNameChanged(val value: String) : EditContactEvent()
    data class LastNameChanged(val value: String) : EditContactEvent()
    data class PhotoUriChanged(val value: String?) : EditContactEvent()
    data class OrganizationChanged(val value: String) : EditContactEvent()
    data class TitleChanged(val value: String) : EditContactEvent()
    data class NotesChanged(val value: String) : EditContactEvent()
    data class BirthdayChanged(val value: String) : EditContactEvent()

    // Phone number events
    object AddPhoneNumber : EditContactEvent()
    data class RemovePhoneNumber(val index: Int) : EditContactEvent()
    data class PhoneNumberChanged(val index: Int, val number: String) : EditContactEvent()
    data class PhoneTypeChanged(val index: Int, val type: PhoneType) : EditContactEvent()

    // Email events
    object AddEmail : EditContactEvent()
    data class RemoveEmail(val index: Int) : EditContactEvent()
    data class EmailChanged(val index: Int, val email: String) : EditContactEvent()
    data class EmailTypeChanged(val index: Int, val type: EmailType) : EditContactEvent()

    // Address events
    object AddAddress : EditContactEvent()
    data class RemoveAddress(val index: Int) : EditContactEvent()
    data class AddressStreetChanged(val index: Int, val value: String) : EditContactEvent()
    data class AddressCityChanged(val index: Int, val value: String) : EditContactEvent()
    data class AddressStateChanged(val index: Int, val value: String) : EditContactEvent()
    data class AddressPostalCodeChanged(val index: Int, val value: String) : EditContactEvent()
    data class AddressCountryChanged(val index: Int, val value: String) : EditContactEvent()
    data class AddressTypeChanged(val index: Int, val type: AddressType) : EditContactEvent()

    object SaveContact : EditContactEvent()
}
