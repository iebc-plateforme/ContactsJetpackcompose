package com.contacts.android.contacts.data.local.database

import android.util.Log
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase

/**
 * Database migrations for ContactsDatabase
 *
 * CRITICAL: These migrations preserve user data (favorites and groups) from old versions
 */

/**
 * Comprehensive migration from version 1 to 7
 * This handles users coming from the old v136 app or any early version
 */
val MIGRATION_1_7 = object : Migration(1, 7) {
    override fun migrate(database: SupportSQLiteDatabase) {
        Log.d("DatabaseMigration", "Migrating from version 1 to 7 - preserving favorites and groups")

        // Step 1: Preserve old data using phone numbers/emails as identifiers (since contact IDs will change after sync)
        val oldGroupsData = mutableListOf<Triple<Long, String, Long>>() // id, name, createdAt

        // Create a map of contactId -> (phone numbers, email addresses, isFavorite, groupIds)
        data class ContactIdentifiers(
            val phoneNumbers: MutableList<String> = mutableListOf(),
            val emails: MutableList<String> = mutableListOf(),
            val firstName: String = "",
            val lastName: String = "",
            val isFavorite: Boolean = false,
            val groupIds: MutableList<Long> = mutableListOf()
        )
        val contactIdentifiersMap = mutableMapOf<Long, ContactIdentifiers>()

        try {
            // Collect contact identifiers (names, phones, emails, favorites)
            database.query("SELECT id, firstName, lastName, isFavorite FROM contacts").use { cursor ->
                while (cursor.moveToNext()) {
                    val id = cursor.getLong(0)
                    val firstName = cursor.getString(1) ?: ""
                    val lastName = cursor.getString(2) ?: ""
                    val isFavorite = cursor.getInt(3) == 1

                    contactIdentifiersMap[id] = ContactIdentifiers(
                        firstName = firstName,
                        lastName = lastName,
                        isFavorite = isFavorite
                    )
                }
            }
        } catch (e: Exception) {
            Log.w("DatabaseMigration", "Could not read contacts: ${e.message}")
        }

        try {
            // Collect phone numbers for each contact
            database.query("SELECT contactId, number FROM phone_numbers").use { cursor ->
                while (cursor.moveToNext()) {
                    val contactId = cursor.getLong(0)
                    val number = cursor.getString(1) ?: ""
                    contactIdentifiersMap[contactId]?.phoneNumbers?.add(number)
                }
            }
        } catch (e: Exception) {
            Log.w("DatabaseMigration", "Could not read phone numbers: ${e.message}")
        }

        try {
            // Collect emails for each contact
            database.query("SELECT contactId, email FROM emails").use { cursor ->
                while (cursor.moveToNext()) {
                    val contactId = cursor.getLong(0)
                    val email = cursor.getString(1) ?: ""
                    contactIdentifiersMap[contactId]?.emails?.add(email)
                }
            }
        } catch (e: Exception) {
            Log.w("DatabaseMigration", "Could not read emails: ${e.message}")
        }

        try {
            // Preserve groups
            database.query("SELECT id, name, createdAt FROM `groups`").use { cursor ->
                while (cursor.moveToNext()) {
                    val id = cursor.getLong(0)
                    val name = cursor.getString(1) ?: ""
                    val createdAt = cursor.getLong(2)
                    oldGroupsData.add(Triple(id, name, createdAt))
                }
            }
            Log.d("DatabaseMigration", "Preserved ${oldGroupsData.size} groups")
        } catch (e: Exception) {
            Log.w("DatabaseMigration", "Could not preserve groups: ${e.message}")
        }

        try {
            // Collect group associations for each contact
            database.query("SELECT contactId, groupId FROM contact_group_cross_ref").use { cursor ->
                while (cursor.moveToNext()) {
                    val contactId = cursor.getLong(0)
                    val groupId = cursor.getLong(1)
                    contactIdentifiersMap[contactId]?.groupIds?.add(groupId)
                }
            }
        } catch (e: Exception) {
            Log.w("DatabaseMigration", "Could not read contact-group associations: ${e.message}")
        }

        // Count how many contacts we preserved
        val favoritesCount = contactIdentifiersMap.values.count { it.isFavorite }
        val contactsWithGroups = contactIdentifiersMap.values.count { it.groupIds.isNotEmpty() }
        Log.d("DatabaseMigration", "Preserved $favoritesCount favorites and $contactsWithGroups contacts with group associations")

        // Step 2: Drop all old tables
        database.execSQL("DROP TABLE IF EXISTS contacts")
        database.execSQL("DROP TABLE IF EXISTS phone_numbers")
        database.execSQL("DROP TABLE IF EXISTS emails")
        database.execSQL("DROP TABLE IF EXISTS addresses")
        database.execSQL("DROP TABLE IF EXISTS `groups`")
        database.execSQL("DROP TABLE IF EXISTS contact_group_cross_ref")
        database.execSQL("DROP TABLE IF EXISTS websites")
        database.execSQL("DROP TABLE IF EXISTS instant_messages")
        database.execSQL("DROP TABLE IF EXISTS events")

        // Step 3: Create new schema (version 7)
        createVersion7Schema(database)

        // Step 4: Create temporary table to store migration data for post-sync restoration
        database.execSQL("""
            CREATE TABLE IF NOT EXISTS migration_temp_data (
                id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                phoneNumber TEXT,
                email TEXT,
                firstName TEXT NOT NULL,
                lastName TEXT NOT NULL,
                isFavorite INTEGER NOT NULL DEFAULT 0,
                groupIds TEXT
            )
        """.trimIndent())

        // Step 5: Store contact identifiers in temp table for post-sync restoration
        var insertedCount = 0
        for ((_, identifiers) in contactIdentifiersMap) {
            // Only store if contact has favorites or group associations
            if (identifiers.isFavorite || identifiers.groupIds.isNotEmpty()) {
                val phoneNumber = identifiers.phoneNumbers.firstOrNull() ?: ""
                val email = identifiers.emails.firstOrNull() ?: ""
                val groupIdsJson = identifiers.groupIds.joinToString(",")

                if (phoneNumber.isNotEmpty() || email.isNotEmpty()) {
                    database.execSQL("""
                        INSERT INTO migration_temp_data (phoneNumber, email, firstName, lastName, isFavorite, groupIds)
                        VALUES (?, ?, ?, ?, ?, ?)
                    """, arrayOf(phoneNumber, email, identifiers.firstName, identifiers.lastName, if (identifiers.isFavorite) 1 else 0, groupIdsJson))
                    insertedCount++
                }
            }
        }
        Log.d("DatabaseMigration", "Stored $insertedCount contact identifiers for post-sync restoration")

        // Step 6: Restore preserved groups (maintaining their IDs)
        for ((id, name, createdAt) in oldGroupsData) {
            database.execSQL("""
                INSERT OR IGNORE INTO `groups` (id, name, createdAt, isSystemGroup, systemId, accountName, accountType)
                VALUES (?, ?, ?, 0, NULL, NULL, NULL)
            """, arrayOf(id, name, createdAt))
        }
        Log.d("DatabaseMigration", "Restored ${oldGroupsData.size} groups")

        Log.d("DatabaseMigration", "Migration 1→7 complete. Favorites and group associations will be restored after sync.")
    }
}

/**
 * Migration from version 2 to 7
 */
val MIGRATION_2_7 = object : Migration(2, 7) {
    override fun migrate(database: SupportSQLiteDatabase) {
        Log.d("DatabaseMigration", "Migrating from version 2 to 7")
        // Same logic as 1→7
        MIGRATION_1_7.migrate(database)
    }
}

/**
 * Migration from version 3 to 7
 */
val MIGRATION_3_7 = object : Migration(3, 7) {
    override fun migrate(database: SupportSQLiteDatabase) {
        Log.d("DatabaseMigration", "Migrating from version 3 to 7")
        // Same logic as 1→7
        MIGRATION_1_7.migrate(database)
    }
}

/**
 * Helper function to create version 7 schema
 */
private fun createVersion7Schema(database: SupportSQLiteDatabase) {
    // Create contacts table with all v7 fields
    database.execSQL("""
        CREATE TABLE IF NOT EXISTS contacts (
            id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
            prefix TEXT DEFAULT NULL,
            firstName TEXT NOT NULL,
            middleName TEXT DEFAULT NULL,
            lastName TEXT NOT NULL DEFAULT '',
            suffix TEXT DEFAULT NULL,
            nickname TEXT DEFAULT NULL,
            photoUri TEXT DEFAULT NULL,
            organization TEXT DEFAULT NULL,
            title TEXT DEFAULT NULL,
            notes TEXT DEFAULT NULL,
            birthday TEXT DEFAULT NULL,
            ringtone TEXT DEFAULT NULL,
            isFavorite INTEGER NOT NULL DEFAULT 0,
            source TEXT NOT NULL DEFAULT '',
            accountName TEXT DEFAULT NULL,
            accountType TEXT DEFAULT NULL,
            createdAt INTEGER NOT NULL,
            updatedAt INTEGER NOT NULL
        )
    """.trimIndent())

    // Create indices
    database.execSQL("CREATE INDEX IF NOT EXISTS index_contacts_firstName ON contacts(firstName)")
    database.execSQL("CREATE INDEX IF NOT EXISTS index_contacts_lastName ON contacts(lastName)")
    database.execSQL("CREATE INDEX IF NOT EXISTS index_contacts_isFavorite ON contacts(isFavorite)")
    database.execSQL("CREATE INDEX IF NOT EXISTS index_contacts_source ON contacts(source)")
    database.execSQL("CREATE INDEX IF NOT EXISTS index_contacts_accountType ON contacts(accountType)")

    // Create phone_numbers table
    database.execSQL("""
        CREATE TABLE IF NOT EXISTS phone_numbers (
            id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
            contactId INTEGER NOT NULL,
            number TEXT NOT NULL,
            type TEXT NOT NULL,
            FOREIGN KEY(contactId) REFERENCES contacts(id) ON DELETE CASCADE
        )
    """.trimIndent())
    database.execSQL("CREATE INDEX IF NOT EXISTS index_phone_numbers_contactId ON phone_numbers(contactId)")

    // Create emails table
    database.execSQL("""
        CREATE TABLE IF NOT EXISTS emails (
            id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
            contactId INTEGER NOT NULL,
            email TEXT NOT NULL,
            type TEXT NOT NULL,
            FOREIGN KEY(contactId) REFERENCES contacts(id) ON DELETE CASCADE
        )
    """.trimIndent())
    database.execSQL("CREATE INDEX IF NOT EXISTS index_emails_contactId ON emails(contactId)")

    // Create addresses table
    database.execSQL("""
        CREATE TABLE IF NOT EXISTS addresses (
            id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
            contactId INTEGER NOT NULL,
            street TEXT NOT NULL,
            city TEXT NOT NULL,
            state TEXT NOT NULL,
            postalCode TEXT NOT NULL,
            country TEXT NOT NULL,
            type TEXT NOT NULL,
            FOREIGN KEY(contactId) REFERENCES contacts(id) ON DELETE CASCADE
        )
    """.trimIndent())
    database.execSQL("CREATE INDEX IF NOT EXISTS index_addresses_contactId ON addresses(contactId)")

    // Create websites table
    database.execSQL("""
        CREATE TABLE IF NOT EXISTS websites (
            id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
            contactId INTEGER NOT NULL,
            url TEXT NOT NULL,
            type TEXT NOT NULL,
            FOREIGN KEY(contactId) REFERENCES contacts(id) ON DELETE CASCADE
        )
    """.trimIndent())
    database.execSQL("CREATE INDEX IF NOT EXISTS index_websites_contactId ON websites(contactId)")

    // Create instant_messages table
    database.execSQL("""
        CREATE TABLE IF NOT EXISTS instant_messages (
            id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
            contactId INTEGER NOT NULL,
            handle TEXT NOT NULL,
            protocol TEXT NOT NULL,
            FOREIGN KEY(contactId) REFERENCES contacts(id) ON DELETE CASCADE
        )
    """.trimIndent())
    database.execSQL("CREATE INDEX IF NOT EXISTS index_instant_messages_contactId ON instant_messages(contactId)")

    // Create events table
    database.execSQL("""
        CREATE TABLE IF NOT EXISTS events (
            id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
            contactId INTEGER NOT NULL,
            date TEXT NOT NULL,
            type TEXT NOT NULL,
            FOREIGN KEY(contactId) REFERENCES contacts(id) ON DELETE CASCADE
        )
    """.trimIndent())
    database.execSQL("CREATE INDEX IF NOT EXISTS index_events_contactId ON events(contactId)")

    // Create groups table
    database.execSQL("""
        CREATE TABLE IF NOT EXISTS `groups` (
            id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
            name TEXT NOT NULL,
            createdAt INTEGER NOT NULL,
            isSystemGroup INTEGER NOT NULL DEFAULT 0,
            systemId TEXT DEFAULT NULL,
            accountName TEXT DEFAULT NULL,
            accountType TEXT DEFAULT NULL
        )
    """.trimIndent())
    database.execSQL("CREATE INDEX IF NOT EXISTS index_groups_name ON `groups`(name)")

    // Create contact_group_cross_ref table
    database.execSQL("""
        CREATE TABLE IF NOT EXISTS contact_group_cross_ref (
            contactId INTEGER NOT NULL,
            groupId INTEGER NOT NULL,
            PRIMARY KEY(contactId, groupId),
            FOREIGN KEY(contactId) REFERENCES contacts(id) ON DELETE CASCADE,
            FOREIGN KEY(groupId) REFERENCES `groups`(id) ON DELETE CASCADE
        )
    """.trimIndent())
    database.execSQL("CREATE INDEX IF NOT EXISTS index_contact_group_cross_ref_contactId ON contact_group_cross_ref(contactId)")
    database.execSQL("CREATE INDEX IF NOT EXISTS index_contact_group_cross_ref_groupId ON contact_group_cross_ref(groupId)")

    // Create view for groups with contact count
    database.execSQL("""
        CREATE VIEW IF NOT EXISTS GroupWithContactCount AS
        SELECT
            g.id,
            g.name,
            g.createdAt,
            g.isSystemGroup,
            g.systemId,
            g.accountName,
            g.accountType,
            COUNT(DISTINCT cgcr.contactId) as contactCount
        FROM `groups` g
        LEFT JOIN contact_group_cross_ref cgcr ON g.id = cgcr.groupId
        GROUP BY g.id
    """.trimIndent())
}

val MIGRATION_4_5 = object : Migration(4, 5) {
    override fun migrate(database: SupportSQLiteDatabase) {
        // Add new name fields to contacts table
        database.execSQL("ALTER TABLE contacts ADD COLUMN prefix TEXT DEFAULT NULL")
        database.execSQL("ALTER TABLE contacts ADD COLUMN middleName TEXT DEFAULT NULL")
        database.execSQL("ALTER TABLE contacts ADD COLUMN suffix TEXT DEFAULT NULL")
        database.execSQL("ALTER TABLE contacts ADD COLUMN nickname TEXT DEFAULT NULL")
        database.execSQL("ALTER TABLE contacts ADD COLUMN ringtone TEXT DEFAULT NULL")

        // Create websites table
        database.execSQL("""
            CREATE TABLE IF NOT EXISTS websites (
                id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                contactId INTEGER NOT NULL,
                url TEXT NOT NULL,
                type TEXT NOT NULL,
                FOREIGN KEY(contactId) REFERENCES contacts(id) ON DELETE CASCADE
            )
        """.trimIndent())
        database.execSQL("CREATE INDEX IF NOT EXISTS index_websites_contactId ON websites(contactId)")

        // Create instant_messages table
        database.execSQL("""
            CREATE TABLE IF NOT EXISTS instant_messages (
                id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                contactId INTEGER NOT NULL,
                handle TEXT NOT NULL,
                protocol TEXT NOT NULL,
                FOREIGN KEY(contactId) REFERENCES contacts(id) ON DELETE CASCADE
            )
        """.trimIndent())
        database.execSQL("CREATE INDEX IF NOT EXISTS index_instant_messages_contactId ON instant_messages(contactId)")

        // Create events table
        database.execSQL("""
            CREATE TABLE IF NOT EXISTS events (
                id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                contactId INTEGER NOT NULL,
                date TEXT NOT NULL,
                type TEXT NOT NULL,
                FOREIGN KEY(contactId) REFERENCES contacts(id) ON DELETE CASCADE
            )
        """.trimIndent())
        database.execSQL("CREATE INDEX IF NOT EXISTS index_events_contactId ON events(contactId)")
    }
}

val MIGRATION_5_6 = object : Migration(5, 6) {
    override fun migrate(database: SupportSQLiteDatabase) {
        // Add system group tracking fields to groups table
        // These fields are needed to sync groups from Android ContactsContract
        database.execSQL("ALTER TABLE `groups` ADD COLUMN isSystemGroup INTEGER NOT NULL DEFAULT 0")
        database.execSQL("ALTER TABLE `groups` ADD COLUMN systemId TEXT DEFAULT NULL")
        database.execSQL("ALTER TABLE `groups` ADD COLUMN accountName TEXT DEFAULT NULL")
        database.execSQL("ALTER TABLE `groups` ADD COLUMN accountType TEXT DEFAULT NULL")

        // Drop the old unique index on name since system groups can have duplicate names from different accounts
        database.execSQL("DROP INDEX IF EXISTS index_groups_name")

        // Create a new non-unique index on name for search optimization
        database.execSQL("CREATE INDEX IF NOT EXISTS index_groups_name ON `groups`(name)")
    }
}

val MIGRATION_6_7 = object : Migration(6, 7) {
    override fun migrate(database: SupportSQLiteDatabase) {
        // Add account/source fields to contacts table for Fossify-like filtering
        database.execSQL("ALTER TABLE contacts ADD COLUMN source TEXT NOT NULL DEFAULT ''")
        database.execSQL("ALTER TABLE contacts ADD COLUMN accountName TEXT DEFAULT NULL")
        database.execSQL("ALTER TABLE contacts ADD COLUMN accountType TEXT DEFAULT NULL")

        // Create indices for efficient account filtering
        database.execSQL("CREATE INDEX IF NOT EXISTS index_contacts_source ON contacts(source)")
        database.execSQL("CREATE INDEX IF NOT EXISTS index_contacts_accountType ON contacts(accountType)")
    }
}

/**
 * All migrations in chronological order
 * CRITICAL: Includes migrations from old versions (1, 2, 3) to preserve user data from v136
 */
val ALL_MIGRATIONS = arrayOf(
    MIGRATION_1_7,
    MIGRATION_2_7,
    MIGRATION_3_7,
    MIGRATION_4_5,
    MIGRATION_5_6,
    MIGRATION_6_7
)
