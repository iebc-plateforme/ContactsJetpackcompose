# Database Recovery Guide for Lost Contacts

## For the Affected User

Your data was deleted due to a sync bug. Here are the recovery options:

## Method 1: ADB Backup (No Root Required) ⭐ RECOMMENDED

This method can recover the database files without root access.

### Step 1: Enable USB Debugging
1. Go to Settings > About Phone
2. Tap "Build Number" 7 times to enable Developer Mode
3. Go to Settings > Developer Options
4. Enable "USB Debugging"

### Step 2: Install ADB
- **Windows**: Download [Platform Tools](https://developer.android.com/studio/releases/platform-tools)
- **Mac/Linux**: Install via package manager or download Platform Tools

### Step 3: Backup the App Data
```bash
# Connect your phone and authorize USB debugging
adb devices

# Create a backup of the app data
adb backup -f contacts_backup.ab -noapk com.contacts.android.contacts

# On your phone, confirm the backup (you may need to set a password or click "Back up my data")
```

### Step 4: Extract the Backup
```bash
# Convert .ab to .tar (download abe tool from https://github.com/nelenkov/android-backup-extractor)
java -jar abe.jar unpack contacts_backup.ab contacts_backup.tar

# Extract the tar file
tar -xvf contacts_backup.tar

# Your database files will be in: apps/com.contacts.android.contacts/db/
```

### Step 5: Examine Database Files
Look for these files:
- `contacts_database.db` - Main database
- `contacts_database.db-wal` - Write-Ahead Log (may contain deleted data!)
- `contacts_database.db-shm` - Shared memory file

## Method 2: Root Access Required

If you have root access:

```bash
adb shell
su
cd /data/data/com.contacts.android.contacts/databases/
ls -la

# Copy all database files to accessible location
cp contacts_database* /sdcard/recovery/
exit
exit

# Pull files from device
adb pull /sdcard/recovery/ ./database_files/
```

## Method 3: SQLite Recovery

Once you have the database files:

### Using SQLite Browser (GUI)
1. Download [DB Browser for SQLite](https://sqlitebrowser.org/)
2. Open `contacts_database.db`
3. Check these tables for your data:
   - `contacts` - Main contact entries
   - `phone_numbers` - Phone numbers
   - `emails` - Email addresses
   - `addresses` - Physical addresses

### Using Command Line
```bash
# Install SQLite
# Windows: Download from https://sqlite.org/download.html
# Mac: brew install sqlite
# Linux: apt-get install sqlite3

# Open database
sqlite3 contacts_database.db

# Check if data still exists
SELECT * FROM contacts;
SELECT * FROM phone_numbers;
SELECT * FROM emails;

# Check WAL file (may contain deleted records)
sqlite3 contacts_database.db-wal
.dump
```

## Method 4: Professional Recovery Software

If the above methods don't work, try:
- **Dr.Fone for Android** (Paid but effective)
- **DiskDigger** (Free, available on Play Store)
- **EaseUS MobiSaver** (Free trial)
- **Tenorshare UltData** (Paid)

## What to Do After Recovery

### If you recover the data:
1. **DO NOT open the app yet**
2. Export the recovered contacts to VCF format
3. Update the app to the fixed version (coming soon)
4. Import your contacts back

### If you can't recover:
We're deeply sorry for this loss. We've fixed the bug and added:
- Optional permission mode
- Manual contact protection
- Backup features
- Sync warnings

## Prevention Tool: Export Script

We've created a recovery mode APK that you can install to:
1. Access your local database without triggering sync
2. Export all contacts to VCF
3. Examine what data exists

This APK will be available in the next release with a special "Recovery Mode" flag.

## Need Help?

If you need assistance with any of these steps:
1. Reply with which method you're trying
2. Share any error messages (remove personal data first)
3. Let us know your Android version and device model

## For Developers

The recovery tool source code is in `recovery_tools/DataRecoveryHelper.kt`.
This can be integrated into the app as a hidden "Recovery Mode" accessible via:
- Long-press on app icon > Recovery Mode
- Or via ADB: `adb shell am start -n com.contacts.android.contacts/.RecoveryActivity`
