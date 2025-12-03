package com.contacts.android.contacts

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
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
import com.google.firebase.analytics.FirebaseAnalytics
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class MainActivity : AppCompatActivity() {

    @Inject
    lateinit var userPreferences: UserPreferences

    @Inject
    lateinit var adMobManager: AdMobManager
    private lateinit var analytics: FirebaseAnalytics
    // Injection du RateUsViewModel
    private val rateUsViewModel: RateUsViewModel by viewModels()

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
                ContactsNavGraph(
                    modifier = Modifier.fillMaxSize(),
                    defaultTab = defaultTab,
                    adMobManager = adMobManager,
                    userPreferences = userPreferences
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
}