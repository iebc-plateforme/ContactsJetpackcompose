package com.contacts.android.contacts.presentation.screens.contactdetail

sealed class ContactDetailEvent {
    object ToggleFavorite : ContactDetailEvent()
    object ShowDeleteDialog : ContactDetailEvent()
    object HideDeleteDialog : ContactDetailEvent()
    object DeleteContact : ContactDetailEvent()
    object ShareContact : ContactDetailEvent()
    data class CallContact(val phoneNumber: String) : ContactDetailEvent()
    data class MessageContact(val phoneNumber: String) : ContactDetailEvent()
    data class EmailContact(val email: String) : ContactDetailEvent()
}
