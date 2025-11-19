package com.contacts.android.contacts.data.local.entity

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "groups",
    indices = [
        Index(value = ["name"], unique = false) // Allow duplicate names (system groups can have same names from different accounts)
    ]
)
data class GroupEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val name: String,
    val createdAt: Long = System.currentTimeMillis(),
    val isSystemGroup: Boolean = false, // True if synced from Android Contacts Provider
    val systemId: String? = null, // Android system group ID
    val accountName: String? = null, // Account name (e.g., user@gmail.com)
    val accountType: String? = null // Account type (e.g., com.google)
)
