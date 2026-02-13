package com.contacts.android.contacts.domain.model

/**
 * Constants for contact source identification.
 * Used to distinguish between manually created contacts and system-synced contacts.
 */
object ContactSource {
    /**
     * Contact was created manually in the app.
     * These contacts should NEVER be deleted during sync operations.
     */
    const val MANUAL = "MANUAL"

    /**
     * Contact was synced from the device's ContactsProvider.
     * These contacts can be updated/deleted during sync to match the system state.
     */
    const val SYSTEM = "SYSTEM"

    /**
     * Contact was imported from a VCF file.
     * These contacts should be preserved during sync unless explicitly deleted.
     */
    const val IMPORTED = "IMPORTED"

    /**
     * Contact source is unknown or legacy (before source tracking was implemented).
     * For safety, treat these as manual contacts to prevent accidental deletion.
     */
    const val UNKNOWN = ""

    /**
     * Check if a contact source indicates a manually created/imported contact
     * that should be protected from deletion during sync.
     */
    fun isProtectedSource(source: String): Boolean {
        return source in listOf(MANUAL, IMPORTED, UNKNOWN)
    }

    /**
     * Check if a contact is from the system and can be safely synced/deleted.
     */
    fun isSystemSource(source: String): Boolean {
        return source == SYSTEM || (!isProtectedSource(source) && source.isNotEmpty())
    }
}
