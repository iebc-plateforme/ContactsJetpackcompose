package com.contacts.android.contactsjetpackcompose.domain.model

data class Group(
    val id: Long = 0,
    val name: String,
    val contactCount: Int = 0,
    val createdAt: Long = System.currentTimeMillis(),
    val isSystemGroup: Boolean = false, // True if from Android Contacts Provider
    val systemId: String? = null,
    val accountName: String? = null,
    val accountType: String? = null
)
