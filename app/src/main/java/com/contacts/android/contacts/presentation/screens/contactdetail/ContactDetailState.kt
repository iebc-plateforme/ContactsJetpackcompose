package com.contacts.android.contacts.presentation.screens.contactdetail

import com.contacts.android.contacts.domain.model.Contact

data class ContactDetailState(
    val contact: Contact? = null,
    val isLoading: Boolean = false,
    val error: String? = null,
    val showDeleteDialog: Boolean = false
)
