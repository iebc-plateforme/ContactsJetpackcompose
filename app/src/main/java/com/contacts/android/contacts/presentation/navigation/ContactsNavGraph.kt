package com.contacts.android.contacts.presentation.navigation

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.contacts.android.contacts.presentation.screens.contactdetail.ContactDetailScreen
import com.contacts.android.contacts.presentation.screens.dialpad.DialPadScreen
import com.contacts.android.contacts.presentation.screens.editcontact.EditContactScreen
import com.contacts.android.contacts.presentation.screens.main.MainScreen
import com.contacts.android.contacts.presentation.screens.settings.SettingsScreen
import com.contacts.android.contacts.presentation.util.RequestContactsPermission

@Composable
fun ContactsNavGraph(
    modifier: Modifier = Modifier,
    navController: NavHostController = rememberNavController(),
    startDestination: String = Screen.Main.route,
    defaultTab: com.contacts.android.contacts.data.preferences.DefaultTab = com.contacts.android.contacts.data.preferences.DefaultTab.CONTACTS
) {
    RequestContactsPermission {
        NavHost(
            navController = navController,
            startDestination = startDestination,
            modifier = modifier
        ) {
            // Main Screen with Simplified UI (Following Fossify Architecture)
            composable(route = Screen.Main.route) {
                val context = androidx.compose.ui.platform.LocalContext.current
                MainScreen(
                    onContactClick = { contactId ->
                        navController.navigate(Screen.ContactDetail.createRoute(contactId))
                    },
                    onAddContact = {
                        navController.navigate(Screen.EditContact.createRoute())
                    },
                    onGroupClick = { groupId ->
                        navController.navigate(Screen.GroupDetail.createRoute(groupId))
                    },
                    onNavigateToSettings = {
                        navController.navigate(Screen.Settings.route)
                    },
                    onLaunchDialer = {
                        // Launch system dialer
                        val intent = android.content.Intent(android.content.Intent.ACTION_DIAL)
                        context.startActivity(intent)
                    },
                    defaultTab = defaultTab
                )
            }

            // Contact Detail Screen
            composable(
                route = Screen.ContactDetail.route,
                arguments = listOf(
                    navArgument("contactId") {
                        type = NavType.LongType
                    }
                )
            ) {
                ContactDetailScreen(
                    onNavigateBack = {
                        navController.popBackStack()
                    },
                    onEditContact = { contactId ->
                        navController.navigate(Screen.EditContact.createRoute(contactId))
                    }
                )
            }

            // Edit Contact Screen
            composable(
                route = Screen.EditContact.route,
                arguments = listOf(
                    navArgument("contactId") {
                        type = NavType.LongType
                        defaultValue = 0L
                    }
                )
            ) {
                EditContactScreen(
                    onNavigateBack = {
                        navController.popBackStack()
                    }
                )
            }

            // Group Detail Screen
            composable(
                route = Screen.GroupDetail.route,
                arguments = listOf(
                    navArgument("groupId") {
                        type = NavType.LongType
                    }
                )
            ) {
                com.contacts.android.contacts.presentation.screens.groupdetail.GroupDetailScreen(
                    onBackClick = {
                        navController.popBackStack()
                    },
                    onContactClick = { contactId ->
                        navController.navigate(Screen.ContactDetail.createRoute(contactId))
                    }
                )
            }

            // Settings Screen
            composable(route = Screen.Settings.route) {
                SettingsScreen(
                    onNavigateBack = {
                        navController.popBackStack()
                    }
                )
            }

            // Dial Pad Screen
            composable(route = Screen.DialPad.route) {
                DialPadScreen(
                    onNavigateBack = {
                        navController.popBackStack()
                    }
                )
            }
        }
    }
}
