package com.contacts.android.contacts.presentation.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.WorkspacePremium
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalInspectionMode
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.hilt.navigation.compose.hiltViewModel
import com.contacts.android.contacts.R
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.ViewModel
import com.contacts.android.contacts.ads.AdMobManager
import com.contacts.android.contacts.data.preferences.UserPreferences
import com.google.android.gms.ads.AdListener
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.AdSize
import com.google.android.gms.ads.AdView
import com.google.android.gms.ads.LoadAdError
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

/**
 * ViewModel for managing ad display based on premium status
 */
@HiltViewModel
class AdBannerViewModel @Inject constructor(
    private val userPreferences: UserPreferences
) : ViewModel() {
    val isPremium: Flow<Boolean> = userPreferences.isPremium
}

/**
 * AdMob Banner Ad Component for Jetpack Compose
 * Non-intrusive placement at list footers
 * Will not show ads if user has premium subscription
 *
 * Features:
 * - Proper lifecycle management (pause/resume/destroy)
 * - Error handling with silent failures
 * - Preview support
 * - Adaptive sizing support
 * - Premium subscription support (no ads for premium users)
 * - Premium upsell banner above ads
 */
@Composable
fun AdMobBanner(
    modifier: Modifier = Modifier,
    adUnitId: String = AdMobManager.BANNER_AD_UNIT_ID,
    viewModel: AdBannerViewModel = hiltViewModel(),
    onUpgradeToPremium: (() -> Unit)? = null
) {
    val isInPreview = LocalInspectionMode.current
    val context = LocalContext.current
    val isPremium by viewModel.isPremium.collectAsState(initial = false)

    // Don't show ads if user is premium
    if (isPremium) {
        return
    }

    Column(modifier = modifier.fillMaxWidth()) {
        // Premium upsell mini-banner above ad
        if (onUpgradeToPremium != null) {
            PremiumUpsellMiniBanner(
                onClick = onUpgradeToPremium
            )
        }

        if (isInPreview) {
            // Show placeholder in preview
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(stringResource(R.string.ad_banner_placeholder))
            }
        } else {
            val lifecycleOwner = LocalLifecycleOwner.current

            // Remember the AdView to handle lifecycle properly
            val adView = remember {
                AdView(context).apply {
                    setAdSize(AdSize.BANNER)
                    this.adUnitId = adUnitId

                    // Add listener for error handling
                    adListener = object : AdListener() {
                        override fun onAdFailedToLoad(error: LoadAdError) {
                            // Silent failure - ads are non-critical
                            // Log error in debug builds if needed
                        }

                        override fun onAdLoaded() {
                            // Ad loaded successfully
                        }
                    }
                }
            }

            // Handle lifecycle with proper pause/resume
            DisposableEffect(adView, lifecycleOwner) {
                val observer = LifecycleEventObserver { _, event ->
                    when (event) {
                        Lifecycle.Event.ON_PAUSE -> adView.pause()
                        Lifecycle.Event.ON_RESUME -> adView.resume()
                        else -> {}
                    }
                }
                lifecycleOwner.lifecycle.addObserver(observer)

                // Load ad when composable enters composition
                adView.loadAd(AdRequest.Builder().build())

                onDispose {
                    // Clean up when composable leaves composition
                    lifecycleOwner.lifecycle.removeObserver(observer)
                    adView.destroy()
                }
            }

            AndroidView(
                modifier = Modifier
                    .fillMaxWidth()
                    .wrapContentHeight(),
                factory = { adView }
            )
        }
    }
}

/**
 * Compact premium upsell banner shown above ads
 * Clickable to navigate to premium screen
 */
@Composable
private fun PremiumUpsellMiniBanner(
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    var isDismissed by remember { mutableStateOf(false) }

    if (isDismissed) return

    Surface(
        modifier = modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.9f),
        tonalElevation = 1.dp
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 12.dp, vertical = 6.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.weight(1f)
            ) {
                Icon(
                    imageVector = Icons.Default.WorkspacePremium,
                    contentDescription = null,
                    modifier = Modifier.size(16.dp),
                    tint = MaterialTheme.colorScheme.primary
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = stringResource(R.string.remove_ads_go_premium),
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onPrimaryContainer,
                    fontWeight = FontWeight.Medium
                )
            }

            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                TextButton(
                    onClick = onClick,
                    contentPadding = PaddingValues(horizontal = 8.dp, vertical = 0.dp),
                    modifier = Modifier.height(24.dp)
                ) {
                    Text(
                        text = stringResource(R.string.upgrade_action),
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )
                }

                IconButton(
                    onClick = { isDismissed = true },
                    modifier = Modifier.size(20.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Close,
                        contentDescription = stringResource(R.string.action_close),
                        modifier = Modifier.size(14.dp),
                        tint = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.6f)
                    )
                }
            }
        }
    }
}

/**
 * Adaptive Banner that uses the full width of the screen
 * Better for modern devices with various screen sizes
 * Will not show ads if user has premium subscription
 */
@Composable
fun AdMobAdaptiveBanner(
    modifier: Modifier = Modifier,
    adUnitId: String = AdMobManager.BANNER_AD_UNIT_ID,
    viewModel: AdBannerViewModel = hiltViewModel()
) {
    val isInPreview = LocalInspectionMode.current
    val context = LocalContext.current
    val configuration = LocalConfiguration.current
    val density = LocalDensity.current
    val isPremium by viewModel.isPremium.collectAsState(initial = false)

    // Don't show ads if user is premium
    if (isPremium) {
        return
    }

    if (isInPreview) {
        // Show placeholder in preview
        Box(
            modifier = modifier
                .fillMaxWidth()
                .height(50.dp),
            contentAlignment = Alignment.Center
        ) {
            androidx.compose.material3.Text(stringResource(R.string.adaptive_banner_placeholder))
        }
    } else {
        val lifecycleOwner = LocalLifecycleOwner.current

        // Calculate the ad width in dp
        val adWidth = configuration.screenWidthDp

        // Remember the AdView to handle lifecycle properly
        val adView = remember(adWidth) {
            AdView(context).apply {
                // Use adaptive banner size
                setAdSize(AdSize.getCurrentOrientationAnchoredAdaptiveBannerAdSize(context, adWidth))
                this.adUnitId = adUnitId

                // Add listener for error handling
                adListener = object : AdListener() {
                    override fun onAdFailedToLoad(error: LoadAdError) {
                        // Silent failure - ads are non-critical
                    }

                    override fun onAdLoaded() {
                        // Ad loaded successfully
                    }
                }
            }
        }

        // Handle lifecycle with proper pause/resume
        DisposableEffect(adView, lifecycleOwner) {
            val observer = LifecycleEventObserver { _, event ->
                when (event) {
                    Lifecycle.Event.ON_PAUSE -> adView.pause()
                    Lifecycle.Event.ON_RESUME -> adView.resume()
                    else -> {}
                }
            }
            lifecycleOwner.lifecycle.addObserver(observer)

            // Load ad when composable enters composition
            adView.loadAd(AdRequest.Builder().build())

            onDispose {
                // Clean up when composable leaves composition
                lifecycleOwner.lifecycle.removeObserver(observer)
                adView.destroy()
            }
        }

        AndroidView(
            modifier = modifier
                .fillMaxWidth()
                .wrapContentHeight(),
            factory = { adView }
        )
    }
}

/**
 * Smart Banner that adapts to screen size (deprecated, use AdMobAdaptiveBanner instead)
 */
@Deprecated(
    message = "Use AdMobAdaptiveBanner for better results",
    replaceWith = ReplaceWith("AdMobAdaptiveBanner(modifier, adUnitId)")
)
@Composable
fun AdMobSmartBanner(
    modifier: Modifier = Modifier,
    adUnitId: String = AdMobManager.BANNER_AD_UNIT_ID
) {
    val isInPreview = LocalInspectionMode.current
    val context = LocalContext.current

    if (isInPreview) {
        // Show placeholder in preview
        Box(
            modifier = modifier
                .fillMaxWidth()
                .height(50.dp),
            contentAlignment = Alignment.Center
        ) {
            androidx.compose.material3.Text(stringResource(R.string.smart_banner_placeholder))
        }
    } else {
        // Remember the AdView to handle lifecycle properly
        val adView = remember {
            @Suppress("DEPRECATION")
            AdView(context).apply {
                setAdSize(AdSize.SMART_BANNER)
                this.adUnitId = adUnitId

                // Add listener for error handling
                adListener = object : AdListener() {
                    override fun onAdFailedToLoad(error: LoadAdError) {
                        // Silent failure - ads are non-critical
                    }
                }
            }
        }

        // Handle lifecycle
        DisposableEffect(adView) {
            adView.loadAd(AdRequest.Builder().build())

            onDispose {
                adView.destroy()
            }
        }

        AndroidView(
            modifier = modifier
                .fillMaxWidth()
                .wrapContentHeight(),
            factory = { adView }
        )
    }
}
