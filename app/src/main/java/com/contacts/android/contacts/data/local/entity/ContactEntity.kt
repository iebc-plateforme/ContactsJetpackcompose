package com.contacts.android.contacts.data.local.entity

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "contacts",
    indices = [
        Index(value = ["firstName"]), // Optimize alphabetical sorting and search
        Index(value = ["lastName"]),  // Optimize search by last name
        Index(value = ["isFavorite"]), // Optimize favorite filtering
        Index(value = ["source"]),     // Optimize filtering by account/source
        Index(value = ["accountType"]) // Optimize filtering by account type
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
    // Account/Source information (for filtering like Fossify)
    val source: String = "", // Account name/type identifier (e.g., "user@gmail.com", "Phone", "SIM")
    val accountName: String? = null, // Account name (e.g., "user@gmail.com")
    val accountType: String? = null, // Account type (e.g., "com.google", "com.whatsapp")
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis()
)
