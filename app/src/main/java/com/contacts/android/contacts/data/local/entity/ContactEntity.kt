package com.contacts.android.contacts.data.local.entity

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
    // Name fields (Fossify-complete)
    val prefix: String? = null,
    val firstName: String,
    val middleName: String? = null,
    val lastName: String = "",
    val suffix: String? = null,
    val nickname: String? = null,
    // Basic info
    val photoUri: String? = null,
    val organization: String? = null,
    val title: String? = null,
    val notes: String? = null,
    val birthday: String? = null, // ISO format: YYYY-MM-DD
    val ringtone: String? = null,
    // System fields
    val isFavorite: Boolean = false,
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis()
)
