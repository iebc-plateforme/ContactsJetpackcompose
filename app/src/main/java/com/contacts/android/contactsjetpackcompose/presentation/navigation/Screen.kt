package com.contacts.android.contactsjetpackcompose.presentation.navigation

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
}
