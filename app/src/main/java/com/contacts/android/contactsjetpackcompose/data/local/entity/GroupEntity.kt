package com.contacts.android.contactsjetpackcompose.data.local.entity

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "groups",
    indices = [
        Index(value = ["name"], unique = true) // Optimize group name search and enforce uniqueness
    ]
)
data class GroupEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val name: String,
    val createdAt: Long = System.currentTimeMillis()
)
