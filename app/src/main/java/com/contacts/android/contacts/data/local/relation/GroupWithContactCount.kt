package com.contacts.android.contacts.data.local.relation

import androidx.room.ColumnInfo
import androidx.room.DatabaseView

@DatabaseView("""
    SELECT g.id, g.name, g.createdAt, g.isSystemGroup, g.systemId, g.accountName, g.accountType,
           COUNT(c.contactId) as contactCount
    FROM `groups` g
    LEFT JOIN contact_group_cross_ref c ON g.id = c.groupId
    GROUP BY g.id, g.name, g.createdAt, g.isSystemGroup, g.systemId, g.accountName, g.accountType
""")
data class GroupWithContactCount(
    val id: Long,
    val name: String,
    val createdAt: Long,
    val isSystemGroup: Boolean,
    val systemId: String?,
    val accountName: String?,
    val accountType: String?,
    @ColumnInfo(name = "contactCount")
    val contactCount: Int
)
