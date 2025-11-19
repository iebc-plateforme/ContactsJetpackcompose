package com.contacts.android.contacts.data.local.entity

import androidx.room.Embedded
import androidx.room.Junction
import androidx.room.Relation

data class ContactWithDetails(
    @Embedded val contact: ContactEntity,

    @Relation(
        parentColumn = "id",
        entityColumn = "contactId"
    )
    val phoneNumbers: List<PhoneNumberEntity> = emptyList(),

    @Relation(
        parentColumn = "id",
        entityColumn = "contactId"
    )
    val emails: List<EmailEntity> = emptyList(),

    @Relation(
        parentColumn = "id",
        entityColumn = "contactId"
    )
    val addresses: List<AddressEntity> = emptyList(),

    @Relation(
        parentColumn = "id",
        entityColumn = "id",
        associateBy = Junction(
            value = ContactGroupCrossRef::class,
            parentColumn = "contactId",
            entityColumn = "groupId"
        )
    )
    val groups: List<GroupEntity> = emptyList()
)
