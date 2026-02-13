# Data Loss Incident Report

## Problem Summary

A user reported losing their manually created "notes" (stored as contacts) after granting the READ_CONTACTS permission to the app.

## Root Cause Analysis

### 1. Forced Permission Requirement
**Location:** `ContactsNavGraph.kt:47`

```kotlin
RequestContactsPermission {
    NavHost(navController = navController, ...)
}
```

The entire app UI is wrapped inside `RequestContactsPermission`, which blocks ALL access to the app until the user grants READ_CONTACTS and WRITE_CONTACTS permissions. This prevents users from accessing their manually created local contacts.

### 2. Automatic Destructive Sync
**Location:** `ContactListViewModel.kt:54-60`

```kotlin
val count = getContactsCountUseCase.getCount()
if (count == 0) {
    syncContacts()
}
```

While this code checks if count == 0, the sync can still be triggered automatically when the app starts with permissions granted.

### 3. Deletion of Local-Only Contacts
**Location:** `SyncContactsUseCase.kt:43`

```kotlin
val contactsToDelete = databaseContacts.filter { it.id !in providerContactMap }
```

**CRITICAL BUG**: This logic deletes ALL contacts from the local database that don't exist in the system's ContactsProvider. This includes:
- Manually created contacts
- Notes saved as contacts
- Any local-only data

## What Happened to the User

1. User was using the app WITHOUT granting contacts permission
2. User created "notes" as contacts in the local Room database
3. App was updated or changed to REQUIRE permission (blocking all access)
4. User eventually granted permission to access the app
5. App immediately synchronized with system ContactsProvider
6. All user's manually created "notes" were deleted because they didn't exist in the system contacts

## Data Recovery Options for Users

### Option 1: Android Auto-Backup (Best chance)
If the user has Android Auto-Backup enabled:
1. Go to Settings > System > Backup
2. Check if backups exist before the permission was granted
3. Uninstall the app
4. Restore from backup
5. Reinstall the app but DO NOT grant permission

### Option 2: Database File Recovery
The database might still have recoverable data in WAL (Write-Ahead Log) files:

**Database location:** `/data/data/com.contacts.android.contacts/databases/`

Files to check:
- `contacts_database.db`
- `contacts_database.db-wal` (Write-Ahead Log - may contain deleted data)
- `contacts_database.db-shm` (Shared Memory)

**Recovery steps (requires root or ADB backup):**
```bash
# Via ADB (doesn't require root on most devices)
adb backup -f backup.ab -noapk com.contacts.android.contacts
# Extract and examine the database files

# Or with root access
adb shell
su
cp /data/data/com.contacts.android.contacts/databases/contacts_database* /sdcard/
```

### Option 3: Third-Party Recovery Tools
- Use Android data recovery tools that can scan for deleted SQLite records
- Tools like Dr.Fone, DiskDigger, or similar may recover deleted database entries

## Immediate Fixes Required

### Fix 1: Make Permission Optional
**File:** `ContactsNavGraph.kt`

The app should work WITHOUT contacts permission for users who only want to create manual contacts.

```kotlin
// BEFORE (forces permission):
RequestContactsPermission {
    NavHost(...)
}

// AFTER (permission optional):
NavHost(...) // Sync only when permission granted and user explicitly requests it
```

### Fix 2: Add "Source" Field to Distinguish Contact Types
Add a field to identify contact origin:
- `SOURCE_MANUAL` = Created manually in app
- `SOURCE_SYSTEM` = Synced from system contacts

### Fix 3: Never Delete Manual Contacts During Sync
**File:** `SyncContactsUseCase.kt:43`

```kotlin
// BEFORE (deletes all local-only contacts):
val contactsToDelete = databaseContacts.filter { it.id !in providerContactMap }

// AFTER (preserve manual contacts):
val contactsToDelete = databaseContacts.filter {
    it.id !in providerContactMap && it.source != "MANUAL"
}
```

### Fix 4: Add Sync Confirmation Dialog
Before first sync, show a warning dialog:
```
"Sync with system contacts?

This will sync your local contacts with your device's contact list.
Contacts that only exist locally will be preserved.

[Cancel] [Sync Now]"
```

### Fix 5: Add Manual Sync Option
Remove automatic sync on startup. Add a manual "Sync" button in settings.

## Preventing Future Data Loss

1. Add data export/backup feature (VCF export already exists - make it prominent)
2. Implement proper migration warnings
3. Add a "local-only" mode that never touches system contacts
4. Show clear warnings before destructive operations
5. Implement undo/restore functionality for deletions

## User Communication

**Response to user:**

"I'm very sorry this happened. Your notes were deleted due to a bug in how the app handles contact synchronization.

The app was designed to sync with your phone's contacts, and when you granted permission, it deleted all contacts that didn't exist in your phone's contact list - including your manually created notes.

**Recovery options:**
1. Check if you have an Android backup from before granting permission
2. Try using Android data recovery tools
3. If you have root/ADB access, the data might still be in database log files

**We're fixing this by:**
1. Making the permission optional
2. Never deleting manually created contacts
3. Adding warnings before sync operations
4. Adding a backup/export feature

Would you like help with any of the recovery options?"

## Priority
**CRITICAL** - This is a data loss bug that affects users who rely on the app for local data storage.
