package com.contacts.android.contacts.presentation.navigation

sealed class Screen(val route: String) {
    object Main : Screen("main")

    object ContactList : Screen("contact_list")

    object ContactDetail : Screen("contact_detail/{contactId}") {
        fun createRoute(contactId: Long) = "contact_detail/$contactId"
    }

    object EditContact : Screen("edit_contact?contactId={contactId}") {
        fun createRoute(contactId: Long? = null) =
            if (contactId != null) "edit_contact?contactId=$contactId"
            else "edit_contact"
    }

    object Groups : Screen("groups")

    object GroupDetail : Screen("group_detail/{groupId}") {
        fun createRoute(groupId: Long) = "group_detail/$groupId"
    }

    object Settings : Screen("settings")

    object DialPad : Screen("dial_pad")

    object QRCodeGenerate : Screen("qr_code_generate/{contactId}") {
        fun createRoute(contactId: Long) = "qr_code_generate/$contactId"
    }

    object QRCodeScanner : Screen("qr_code_scanner")

    object ThemeSelection : Screen("theme_selection")

    object Statistics : Screen("statistics")

    object BusinessCardScan : Screen("business_card_scan")

    object Premium : Screen("premium")

    object PremiumSupport : Screen("premium_support")
}
