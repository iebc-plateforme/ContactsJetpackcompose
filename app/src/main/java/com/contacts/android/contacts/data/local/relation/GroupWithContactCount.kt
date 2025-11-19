package com.contacts.android.contacts.data.local.relation

import androidx.room.ColumnInfo
import androidx.room.DatabaseView

@DatabaseView("""
    SELECT g.id, g.name, g.createdAt, COUNT(c.contactId) as contactCount
    FROM `groups` g
    LEFT JOIN contact_group_cross_ref c ON g.id = c.groupId
    GROUP BY g.id, g.name, g.createdAt
""")
data class GroupWithContactCount(
    val id: Long,
    val name: String,
    val createdAt: Long,
    @ColumnInfo(name = "contactCount")
    val contactCount: Int
)
