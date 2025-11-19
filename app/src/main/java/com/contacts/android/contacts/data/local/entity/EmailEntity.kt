package com.contacts.android.contacts.data.local.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import com.contacts.android.contacts.domain.model.EmailType

@Entity(
    tableName = "emails",
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
        Index("email")      // Optimize email search
    ]
)
data class EmailEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val contactId: Long,
    val email: String,
    val type: EmailType,
    val label: String? = null
)
