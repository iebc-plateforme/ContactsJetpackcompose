package com.contacts.android.contacts.presentation.navigation

import android.app.Activity
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.contacts.android.contacts.ads.AdMobManager
import android.graphics.Bitmap
import android.widget.Toast
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import kotlinx.coroutines.launch
import com.contacts.android.contacts.data.preferences.UserPreferences
import com.contacts.android.contacts.presentation.screens.contactdetail.ContactDetailScreen
import com.contacts.android.contacts.presentation.screens.contactdetail.ContactDetailViewModel
import com.contacts.android.contacts.presentation.screens.dialpad.DialPadScreen
import com.contacts.android.contacts.presentation.screens.editcontact.EditContactScreen
import com.contacts.android.contacts.presentation.screens.main.MainScreen
import com.contacts.android.contacts.presentation.screens.qrcode.QRCodeGenerateScreen
import com.contacts.android.contacts.presentation.screens.qrcode.QRCodeScannerScreen
import com.contacts.android.contacts.presentation.screens.settings.SettingsScreen
import com.contacts.android.contacts.presentation.util.RequestContactsPermission
import java.io.File
import java.io.FileOutputStream

@Composable
fun ContactsNavGraph(
    modifier: Modifier = Modifier,
    navController: NavHostController = rememberNavController(),
    startDestination: String = Screen.Main.route,
    defaultTab: com.contacts.android.contacts.data.preferences.DefaultTab = com.contacts.android.contacts.data.preferences.DefaultTab.CONTACTS,
    adMobManager: AdMobManager? = null,
    userPreferences: UserPreferences
) {
    RequestContactsPermission {
        NavHost(
            navController = navController,
            startDestination = startDestination,
            modifier = modifier
        ) {
            // Main Screen with Simplified UI (Following Fossify Architecture)
            composable(route = Screen.Main.route) {
                val context = LocalContext.current
                val activity = context as? Activity

                MainScreen(
                    onContactClick = { contactId ->
                        // Show interstitial ad at natural navigation point (respects frequency limits)
                        // This is a good placement as it's a natural pause in user flow
                        activity?.let { act ->
                            adMobManager?.showInterstitialAd(
                                activity = act,
                                onAdDismissed = {
                                    navController.navigate(Screen.ContactDetail.createRoute(contactId))
                                },
                                onAdFailed = {
                                    navController.navigate(Screen.ContactDetail.createRoute(contactId))
                                }
                            )
                        } ?: navController.navigate(Screen.ContactDetail.createRoute(contactId))
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
                    onScanQRCode = {
                        navController.navigate(Screen.QRCodeScanner.route)
                    },
                    onNavigateToPremium = {
                        navController.navigate(Screen.Premium.route)
                    },
                    onNavigateToPremiumSupport = {
                        navController.navigate(Screen.PremiumSupport.route)
                    },
                    defaultTab = defaultTab,
                    userPreferences = userPreferences
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
                    },
                    onShowQRCode = { contactId ->
                        navController.navigate(Screen.QRCodeGenerate.createRoute(contactId))
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
                    },
                    adMobManager = adMobManager
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
                    },
                    onNavigateToThemeSelection = {
                        navController.navigate(Screen.ThemeSelection.route)
                    },
                    onNavigateToStatistics = {
                        navController.navigate(Screen.Statistics.route)
                    },
                    onNavigateToBusinessCardScan = {
                        navController.navigate(Screen.BusinessCardScan.route)
                    },
                    onNavigateToPremium = {
                        navController.navigate(Screen.Premium.route)
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

            // QR Code Generate Screen
            composable(
                route = Screen.QRCodeGenerate.route,
                arguments = listOf(
                    navArgument("contactId") {
                        type = NavType.LongType
                    }
                )
            ) {
                val viewModel: ContactDetailViewModel = hiltViewModel()
                val state by viewModel.state.collectAsStateWithLifecycle()
                val context = LocalContext.current

                state.contact?.let { contact ->
                    QRCodeGenerateScreen(
                        contact = contact,
                        onNavigateBack = {
                            navController.popBackStack()
                        },
                        onShareQRCode = { bitmap ->
                            // Save bitmap and share
                            try {
                                val cachePath = File(context.cacheDir, "images")
                                cachePath.mkdirs()
                                val file = File(cachePath, "qr_code_${contact.id}.png")
                                val fos = FileOutputStream(file)
                                bitmap.compress(Bitmap.CompressFormat.PNG, 100, fos)
                                fos.flush()
                                fos.close()

                                val uri = androidx.core.content.FileProvider.getUriForFile(
                                    context,
                                    "${context.packageName}.fileprovider",
                                    file
                                )

                                val shareIntent = android.content.Intent(android.content.Intent.ACTION_SEND).apply {
                                    type = "image/png"
                                    putExtra(android.content.Intent.EXTRA_STREAM, uri)
                                    putExtra(android.content.Intent.EXTRA_TEXT, "Contact: ${contact.displayName}")
                                    addFlags(android.content.Intent.FLAG_GRANT_READ_URI_PERMISSION)
                                }
                                context.startActivity(android.content.Intent.createChooser(shareIntent, "Share QR Code"))
                            } catch (e: Exception) {
                                Toast.makeText(context, "Failed to share QR code: ${e.message}", Toast.LENGTH_SHORT).show()
                            }
                        }
                    )
                }
            }

            // QR Code Scanner Screen
            composable(route = Screen.QRCodeScanner.route) {
                val context = LocalContext.current
                var scannedContact by remember { mutableStateOf<com.contacts.android.contacts.domain.model.Contact?>(null) }

                if (scannedContact != null) {
                    // Navigate to edit contact screen with the scanned contact data
                    navController.navigate(Screen.EditContact.createRoute())
                    // TODO: Pass scanned contact data to EditContactScreen
                    // For now, user will need to manually enter the data or we create a separate flow
                }

                QRCodeScannerScreen(
                    onNavigateBack = {
                        navController.popBackStack()
                    },
                    onContactScanned = { contact ->
                        scannedContact = contact
                        // Navigate to edit screen with pre-filled data
                        navController.navigate(Screen.EditContact.createRoute()) {
                            popUpTo(Screen.Main.route)
                        }
                        Toast.makeText(
                            context,
                            "Contact scanned: ${contact.displayName}",
                            Toast.LENGTH_SHORT
                        ).show()
                    },
                    onError = { error ->
                        Toast.makeText(context, error, Toast.LENGTH_SHORT).show()
                    }
                )
            }

            // Theme Selection Screen
            composable(route = Screen.ThemeSelection.route) {
                val context = LocalContext.current
                val scope = rememberCoroutineScope()
                val userPreferences = remember {
                    com.contacts.android.contacts.data.preferences.UserPreferences(context)
                }
                val currentTheme by userPreferences.colorTheme.collectAsState(initial = com.contacts.android.contacts.data.preferences.ColorTheme.BLUE)
                val currentMode by userPreferences.themeMode.collectAsState(initial = com.contacts.android.contacts.data.preferences.ThemeMode.SYSTEM)
                val isPremium by userPreferences.isPremium.collectAsState(initial = false)

                com.contacts.android.contacts.presentation.screens.theme.ThemeSelectionScreen(
                    currentTheme = currentTheme,
                    currentThemeMode = currentMode,
                    onThemeSelected = { theme ->
                        scope.launch {
                            userPreferences.setColorTheme(theme)
                        }
                    },
                    onThemeModeSelected = { mode ->
                        scope.launch {
                            userPreferences.setThemeMode(mode)
                        }
                    },
                    onBackClick = {
                        navController.popBackStack()
                    },
                    isPremium = isPremium
                )
            }

            // Statistics Screen
            composable(route = Screen.Statistics.route) {
                com.contacts.android.contacts.presentation.screens.statistics.StatisticsScreen(
                    onBackClick = {
                        navController.popBackStack()
                    }
                )
            }

            // Business Card Scanner Screen
            composable(route = Screen.BusinessCardScan.route) {
                val context = LocalContext.current
                com.contacts.android.contacts.presentation.screens.businesscard.BusinessCardScanScreen(
                    onBackClick = {
                        navController.popBackStack()
                    },
                    onSaveContact = { cardData ->
                        // Navigate to EditContactScreen with pre-filled data
                        navController.navigate(Screen.EditContact.createRoute()) {
                            popUpTo(Screen.Main.route)
                        }
                        Toast.makeText(
                            context,
                            "Contact information extracted from card",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                )
            }

            // Premium Subscription Screen
            composable(route = Screen.Premium.route) {
                com.contacts.android.contacts.presentation.screens.premium.PremiumScreen(
                    onBackClick = {
                        navController.popBackStack()
                    }
                )
            }

            // Premium Support Screen
            composable(route = Screen.PremiumSupport.route) {
                com.contacts.android.contacts.presentation.screens.support.PremiumSupportScreen(
                    onNavigateBack = {
                        navController.popBackStack()
                    }
                )
            }
        }
    }
}
