package com.contacts.android.contactsjetpackcompose.data.local.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import com.contacts.android.contactsjetpackcompose.domain.model.PhoneType

@Entity(
    tableName = "phone_numbers",
    foreignKeys = [
        ForeignKey(
            entity = ContactEntity::class,
            parentColumns = ["id"],
            childColumns = ["contactId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [
        Index("contactId"), // Optimize foreign key lookups
        Index("number")     // Optimize phone number search
    ]
)
data class PhoneNumberEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val contactId: Long,
    val number: String,
    val type: PhoneType,
    val label: String? = null
)
