package com.contacts.android.contactsjetpackcompose.data.local.entity

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "contacts",
    indices = [
        Index(value = ["firstName"]), // Optimize alphabetical sorting and search
        Index(value = ["lastName"]),  // Optimize search by last name
        Index(value = ["isFavorite"]) // Optimize favorite filtering
    ]
)
data class ContactEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val firstName: String,
    val lastName: String = "",
    val photoUri: String? = null,
    val organization: String? = null,
    val title: String? = null,
    val notes: String? = null,
    val birthday: String? = null, // ISO format: YYYY-MM-DD
    val isFavorite: Boolean = false,
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis()
)
