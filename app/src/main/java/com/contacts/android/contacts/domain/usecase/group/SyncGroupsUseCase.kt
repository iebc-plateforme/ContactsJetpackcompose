package com.contacts.android.contacts.domain.usecase.group

import com.contacts.android.contacts.data.provider.ContactsProvider
import com.contacts.android.contacts.data.provider.SystemGroupData
import com.contacts.android.contacts.domain.model.Group
import com.contacts.android.contacts.domain.repository.GroupRepository
import kotlinx.coroutines.flow.first
import javax.inject.Inject

/**
 * Use case to sync groups from Android ContactsContract to local database.
 * FIXED: Now includes aggressive orphan cleanup to remove "9 copies" bugs.
 */
class SyncGroupsUseCase @Inject constructor(
    private val contactsProvider: ContactsProvider,
    private val groupRepository: GroupRepository
) {
    suspend operator fun invoke(): Result<Unit> {
        return try {
            // 1. Source of Truth: System Groups
            val systemGroups = contactsProvider.getSystemGroups()

            // 2. Local Cache: Database Groups
            val databaseGroups = groupRepository.getAllGroups().first()

            val groupsToInsert = mutableListOf<Group>()
            val groupsToUpdate = mutableListOf<Group>()
            val groupsToDelete = mutableListOf<Group>()

            // Track which local IDs were matched to a system group
            val matchedDatabaseIds = mutableSetOf<Long>()

            // 3. Smart Matching Logic
            for (systemGroup in systemGroups) {
                // Try to find this system group in our local DB
                // We match if:
                // A) The system IDs match (Ideal)
                // B) OR The Name AND Account match (Fallback)
                // C) OR The Name is "Starred in Android" and localized title is "Favorites" (Legacy fix)
                val existingGroup = databaseGroups.find { dbGroup ->
                    val idMatch = (dbGroup.systemId != null && dbGroup.systemId == systemGroup.systemId)

                    val nameMatch = (dbGroup.isSystemGroup &&
                            dbGroup.name == systemGroup.title &&
                            dbGroup.accountName == systemGroup.accountName &&
                            dbGroup.accountType == systemGroup.accountType)

                    // Special check for the "Starred in Android" -> "Favorites" transition
                    val legacyFavoriteMatch = (dbGroup.name == "Starred in Android" && systemGroup.title == "Favorites")

                    idMatch || nameMatch || legacyFavoriteMatch
                }

                if (existingGroup == null) {
                    // New group found -> Insert
                    groupsToInsert.add(systemGroup.toDomainModel())
                } else {
                    // Group exists -> Update
                    matchedDatabaseIds.add(existingGroup.id)
                    if (systemGroup.hasChanges(existingGroup)) {
                        groupsToUpdate.add(systemGroup.toDomainModel(localId = existingGroup.id))
                    }
                }
            }

            // 4 & 5. Orphan & Duplicate Cleanup (THE FIX)
            // Any system group in the DB that was NOT matched above is a duplicate or obsolete.
            // This effectively deletes the "9 copies" of Starred in Android.
            if (systemGroups.isNotEmpty()) {
                val orphans = databaseGroups.filter { dbGroup ->
                    dbGroup.isSystemGroup && !matchedDatabaseIds.contains(dbGroup.id)
                }
                groupsToDelete.addAll(orphans)
            }

            // 6. Execute Transaction
            if (groupsToInsert.isNotEmpty() || groupsToUpdate.isNotEmpty() || groupsToDelete.isNotEmpty()) {
                groupRepository.syncGroups(
                    groupsToInsert,
                    groupsToUpdate,
                    groupsToDelete
                )
            }

            Result.success(Unit)
        } catch (e: Exception) {
            e.printStackTrace()
            Result.failure(e)
        }
    }

    private fun SystemGroupData.toDomainModel(localId: Long = 0): Group {
        return Group(
            id = localId,
            name = title,
            contactCount = contactCount,
            isSystemGroup = true,
            systemId = systemId,
            accountName = accountName,
            accountType = accountType
        )
    }

    private fun SystemGroupData.hasChanges(group: Group): Boolean {
        return title != group.name ||
                contactCount != group.contactCount ||
                accountName != group.accountName ||
                accountType != group.accountType
    }
}