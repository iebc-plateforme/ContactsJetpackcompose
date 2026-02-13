package com.contacts.android.contacts

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.provider.ContactsContract
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier

import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import com.contacts.android.contacts.ads.AdMobManager
import com.contacts.android.contacts.data.preferences.AppLanguage
import com.contacts.android.contacts.data.preferences.ColorTheme
import com.contacts.android.contacts.data.preferences.ThemeMode
import com.contacts.android.contacts.data.preferences.UserPreferences
import com.contacts.android.contacts.presentation.components.RatingDialog
import com.contacts.android.contacts.presentation.components.ThankYouDialog
import com.contacts.android.contacts.presentation.navigation.ContactsNavGraph
import com.contacts.android.contacts.presentation.screens.rateus.RateUsViewModel
import com.contacts.android.contacts.presentation.theme.ContactsTheme
import com.contacts.android.contacts.util.AnalyticsManager
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

import android.Manifest
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Contacts
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.ui.Alignment
import androidx.compose.ui.unit.dp
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.rememberMultiplePermissionsState

@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    @Inject
    lateinit var userPreferences: UserPreferences

    @Inject
    lateinit var adMobManager: AdMobManager

    @Inject
    lateinit var analyticsManager: AnalyticsManager

    // Injection du RateUsViewModel
    private val rateUsViewModel: RateUsViewModel by viewModels()

    @OptIn(ExperimentalPermissionsApi::class)
    override fun onCreate(savedInstanceState: Bundle?) {
        // Install splash screen before super.onCreate()
        val splashScreen = installSplashScreen()

        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        // Keep splash screen visible while loading
        // The splash will be dismissed when the first frame is drawn
        splashScreen.setKeepOnScreenCondition {
            false // Return true to keep splash visible, false to dismiss
        }

        // Initialize analytics
        analyticsManager.logAppStart()

        // Vérifier le compteur d'ouvertures (Logic Rate Us)
        // On vérifie savedInstanceState pour ne pas incrémenter lors d'une rotation d'écran
        if (savedInstanceState == null) {
            rateUsViewModel.onAppStart()
        }
        setContent {
            // Observe preferences from DataStore
            val themeMode by userPreferences.themeMode.collectAsState(initial = ThemeMode.SYSTEM)
            val colorTheme by userPreferences.colorTheme.collectAsState(initial = ColorTheme.BLUE)
            val appLanguage by userPreferences.appLanguage.collectAsState(initial = AppLanguage.ENGLISH)
            val fontScale by userPreferences.fontScale.collectAsState(initial = 1.0f)
            val defaultTab by userPreferences.defaultTab.collectAsState(initial = com.contacts.android.contacts.data.preferences.DefaultTab.CONTACTS)
            val edgeToEdge by userPreferences.edgeToEdgeDisplay.collectAsState(initial = true)

            // Observer l'état du système de notation
            val showRateDialog by rateUsViewModel.showRateDialog.collectAsState()
            val showThankYouDialog by rateUsViewModel.showThankYouDialog.collectAsState()

            // Permission State
            val permissionsState = rememberMultiplePermissionsState(
                permissions = listOf(
                    Manifest.permission.READ_CONTACTS,
                    Manifest.permission.WRITE_CONTACTS,
                    Manifest.permission.GET_ACCOUNTS
                )
            )

            // Request permissions on launch
            LaunchedEffect(Unit) {
                if (!permissionsState.allPermissionsGranted) {
                    permissionsState.launchMultiplePermissionRequest()
                }
            }

            // Apply edge-to-edge display setting
            LaunchedEffect(edgeToEdge) {
                // Edge-to-edge is already enabled in onCreate
                // This is just for future control if needed
            }

            // Note: Automatic language change disabled to prevent flash loop issue
            // Users can change language through system settings

            // Determine dark theme based on theme mode
            val darkTheme = when (themeMode) {
                ThemeMode.LIGHT -> false
                ThemeMode.DARK -> true
                ThemeMode.SYSTEM -> isSystemInDarkTheme()
            }

            ContactsTheme(
                darkTheme = darkTheme,
                colorTheme = colorTheme,
                fontScale = fontScale
            ) {
            // Fossify behavior: app stays usable even if contacts permission is denied.
            // Permissions are requested, but UI is not blocked.
            val prefillPhoneNumber = extractPhoneNumberFromIntent(intent)
            val isExternalInsert = intent?.action == Intent.ACTION_INSERT ||
                intent?.action == Intent.ACTION_INSERT_OR_EDIT
            val startDestination = if (isExternalInsert) {
                com.contacts.android.contacts.presentation.navigation.Screen.EditContact.createRoute(
                    contactId = null,
                    phoneNumber = prefillPhoneNumber
                )
            } else {
                com.contacts.android.contacts.presentation.navigation.Screen.Main.route
            }

            ContactsNavGraph(
                modifier = Modifier.fillMaxSize(),
                navController = androidx.navigation.compose.rememberNavController(),
                startDestination = startDestination,
                isExternalInsert = isExternalInsert,
                defaultTab = defaultTab,
                adMobManager = adMobManager,
                userPreferences = userPreferences,
                analyticsManager = analyticsManager
            )
            // Affichage des dialogues par dessus l'interface
            if (showRateDialog) {
                RatingDialog(
                    onDismiss = { rateUsViewModel.dismissRateDialog() },
                    onSubmit = { stars ->
                        rateUsViewModel.onRateSubmit(stars) {
                            launchPlayStore()
                        }
                    }
                )
            }

            if (showThankYouDialog) {
                ThankYouDialog(
                    onDismiss = { rateUsViewModel.dismissThankYouDialog() }
                )
            }
        }
    }
    }

    private fun launchPlayStore() {
        try {
            startActivity(Intent(Intent.ACTION_VIEW, Uri.parse("market://details?id=$packageName")))
        } catch (e: android.content.ActivityNotFoundException) {
            startActivity(Intent(Intent.ACTION_VIEW, Uri.parse("https://play.google.com/store/apps/details?id=$packageName")))
        }
    }

    private fun extractPhoneNumberFromIntent(intent: Intent?): String? {
        if (intent == null) return null

        val dataUri = intent.data
        if (dataUri?.scheme == "tel") {
            val fromTel = dataUri.schemeSpecificPart?.trim()
            if (!fromTel.isNullOrBlank()) {
                return fromTel
            }
        }

        val extras = intent.extras ?: return null
        val candidates = listOf(
            ContactsContract.Intents.Insert.PHONE,
            Intent.EXTRA_PHONE_NUMBER,
            "phone",
            "number"
        )

        return candidates.firstNotNullOfOrNull { key ->
            extras.getString(key)?.trim()?.takeIf { it.isNotBlank() }
        }
    }
}
