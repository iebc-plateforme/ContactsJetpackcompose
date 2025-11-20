package com.contacts.android.contacts

import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import com.contacts.android.contacts.ads.AdMobManager
import com.contacts.android.contacts.data.preferences.AppLanguage
import com.contacts.android.contacts.data.preferences.ColorTheme
import com.contacts.android.contacts.data.preferences.ThemeMode
import com.contacts.android.contacts.data.preferences.UserPreferences
import com.contacts.android.contacts.presentation.navigation.ContactsNavGraph
import com.contacts.android.contacts.presentation.theme.ContactsTheme
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class MainActivity : AppCompatActivity() {

    @Inject
    lateinit var userPreferences: UserPreferences

    @Inject
    lateinit var adMobManager: AdMobManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            // Observe preferences from DataStore
            val themeMode by userPreferences.themeMode.collectAsState(initial = ThemeMode.SYSTEM)
            val colorTheme by userPreferences.colorTheme.collectAsState(initial = ColorTheme.BLUE)
            val appLanguage by userPreferences.appLanguage.collectAsState(initial = AppLanguage.ENGLISH)
            val fontScale by userPreferences.fontScale.collectAsState(initial = 1.0f)
            val defaultTab by userPreferences.defaultTab.collectAsState(initial = com.contacts.android.contacts.data.preferences.DefaultTab.CONTACTS)
            val edgeToEdge by userPreferences.edgeToEdgeDisplay.collectAsState(initial = true)

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
                    adMobManager = adMobManager
                )
            }
        }
    }
}