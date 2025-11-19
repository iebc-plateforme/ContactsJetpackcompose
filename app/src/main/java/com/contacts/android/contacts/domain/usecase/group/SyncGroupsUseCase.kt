package com.contacts.android.contacts.domain.usecase.group

import com.contacts.android.contacts.data.provider.ContactsProvider
import com.contacts.android.contacts.data.provider.SystemGroupData
import com.contacts.android.contacts.domain.model.Group
import com.contacts.android.contacts.domain.repository.GroupRepository
import kotlinx.coroutines.flow.first
import javax.inject.Inject

/**
 * Use case to sync groups from Android ContactsContract to local database
 * Following Fossify's pattern of reading system groups
 */
class SyncGroupsUseCase @Inject constructor(
    private val contactsProvider: ContactsProvider,
    private val groupRepository: GroupRepository
) {
    suspend operator fun invoke(): Result<Unit> {
        return try {
            // Get groups from system ContactsContract
            val systemGroups = contactsProvider.getSystemGroups()

            // Get current groups from local database
            val databaseGroups = groupRepository.getAllGroups().first()

            // Create maps for comparison
            val systemGroupMap = systemGroups.associateBy { it.id }
            val databaseGroupMap = databaseGroups.associateBy { it.id }

            val groupsToInsert = mutableListOf<Group>()
            val groupsToUpdate = mutableListOf<Group>()

            // Find groups to insert or update
            for (systemGroup in systemGroups) {
                val databaseGroup = databaseGroupMap[systemGroup.id]
                if (databaseGroup == null) {
                    // New group from system
                    groupsToInsert.add(systemGroup.toDomainModel())
                } else if (systemGroup.hasChanges(databaseGroup)) {
                    // Existing group with changes
                    groupsToUpdate.add(systemGroup.toDomainModel())
                }
            }

            // Find groups to delete (exist in DB but not in system anymore)
            val groupsToDelete = databaseGroups.filter {
                it.isSystemGroup && it.id !in systemGroupMap
            }

            // Perform sync in a single transaction
            groupRepository.syncGroups(
                groupsToInsert,
                groupsToUpdate,
                groupsToDelete
            )

            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * Convert SystemGroupData to domain Group model
     */
    private fun SystemGroupData.toDomainModel(): Group {
        return Group(
            id = id,
            name = title,
            contactCount = contactCount,
            isSystemGroup = true,
            systemId = systemId,
            accountName = accountName,
            accountType = accountType
        )
    }

    /**
     * Check if system group has changes compared to database group
     */
    private fun SystemGroupData.hasChanges(group: Group): Boolean {
        return title != group.name ||
               contactCount != group.contactCount ||
               isVisible != true
    }
}
