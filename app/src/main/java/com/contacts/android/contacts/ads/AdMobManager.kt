package com.contacts.android.contacts.ads

import android.app.Activity
import android.content.Context
import com.contacts.android.contacts.data.preferences.UserPreferences
import com.google.android.gms.ads.AdError
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.FullScreenContentCallback
import com.google.android.gms.ads.LoadAdError
import com.google.android.gms.ads.MobileAds
import com.google.android.gms.ads.interstitial.InterstitialAd
import com.google.android.gms.ads.interstitial.InterstitialAdLoadCallback
import com.google.android.gms.ads.rewarded.RewardedAd
import com.google.android.gms.ads.rewarded.RewardedAdLoadCallback
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Centralized AdMob manager following best practices
 * - Non-intrusive ad placement
 * - Proper lifecycle management
 * - User experience optimization
 */
@Singleton
class AdMobManager @Inject constructor(
    private val context: Context,
    private val userPreferences: UserPreferences
) {
    private val scope = CoroutineScope(Dispatchers.IO)
    companion object {
        // Production Ad Unit IDs
        // Banner Ad Units
        const val BANNER_AD_UNIT_ID = "ca-app-pub-7309731358576813/1454901524" // Home banner
        const val BANNER_HOME_AD_UNIT_ID = "ca-app-pub-7309731358576813/1454901524"
        const val BANNER_SETTINGS_AD_UNIT_ID = "ca-app-pub-7309731358576813/3954852028"
        const val BANNER_ADD_EDIT_CONTACT_AD_UNIT_ID = "ca-app-pub-7309731358576813/3472841574"
        const val BANNER_DETAIL_CONTACT_AD_UNIT_ID = "ca-app-pub-7309731358576813/1747808311"

        // Interstitial Ad Units
        const val INTERSTITIAL_AD_UNIT_ID = "ca-app-pub-7309731358576813/8664151598" // Homescreen
        const val INTERSTITIAL_HOMESCREEN_AD_UNIT_ID = "ca-app-pub-7309731358576813/8664151598"
        const val INTERSTITIAL_CONTACT_ADDED_AD_UNIT_ID = "ca-app-pub-7309731358576813/1135512604"

        // Rewarded Ad Unit (keeping test ID as no production ID provided)
        const val REWARDED_AD_UNIT_ID = "ca-app-pub-3940256099942544/5224354917" // Test ID

        // Ad frequency limits (to avoid overwhelming users)
        private const val INTERSTITIAL_MIN_INTERVAL_MS = 180_000L // 3 minutes
    }

    private var interstitialAd: InterstitialAd? = null
    private var rewardedAd: RewardedAd? = null
    private var lastInterstitialTime = 0L
    private var isInitialized = false

    /**
     * Initialize AdMob SDK (call from Application onCreate)
     */
    fun initialize() {
        if (!isInitialized) {
            MobileAds.initialize(context) {
                isInitialized = true
                // Preload ads after SDK is fully initialized
                preloadInterstitialAd()
            }
        }
    }

    /**
     * Create ad request
     */
    fun createAdRequest(): AdRequest {
        return AdRequest.Builder().build()
    }

    /**
     * Preload interstitial ad for seamless experience
     */
    fun preloadInterstitialAd() {
        if (interstitialAd == null) {
            InterstitialAd.load(
                context,
                INTERSTITIAL_AD_UNIT_ID,
                createAdRequest(),
                object : InterstitialAdLoadCallback() {
                    override fun onAdLoaded(ad: InterstitialAd) {
                        interstitialAd = ad
                    }

                    override fun onAdFailedToLoad(error: LoadAdError) {
                        interstitialAd = null
                    }
                }
            )
        }
    }

    /**
     * Show interstitial ad at safe navigation points
     * Respects minimum interval to avoid annoying users
     * Will not show ads if user has premium subscription
     */
    fun showInterstitialAd(
        activity: Activity,
        onAdDismissed: () -> Unit = {},
        onAdFailed: () -> Unit = {}
    ) {
        scope.launch(Dispatchers.IO) {
            // Check premium status
            val isPremium = userPreferences.isPremium.first()
            if (isPremium) {
                // User is premium, skip ad
                withContext(Dispatchers.Main) {
                    onAdDismissed()
                }
                return@launch
            }

            val currentTime = System.currentTimeMillis()

            // Check if enough time has passed since last ad
            if (currentTime - lastInterstitialTime < INTERSTITIAL_MIN_INTERVAL_MS) {
                withContext(Dispatchers.Main) {
                    onAdDismissed()
                }
                return@launch
            }

            // Show ad on Main thread (WebView requirement)
            withContext(Dispatchers.Main) {
                interstitialAd?.let { ad ->
                    ad.fullScreenContentCallback = object : FullScreenContentCallback() {
                        override fun onAdDismissedFullScreenContent() {
                            lastInterstitialTime = currentTime
                            interstitialAd = null
                            preloadInterstitialAd() // Preload next ad
                            onAdDismissed()
                        }

                        override fun onAdFailedToShowFullScreenContent(error: AdError) {
                            interstitialAd = null
                            preloadInterstitialAd()
                            onAdFailed()
                        }
                    }
                    ad.show(activity)
                } ?: run {
                    preloadInterstitialAd()
                    onAdDismissed()
                }
            }
        }
    }

    /**
     * Check if ads should be shown (returns false if user is premium)
     */
    suspend fun shouldShowAds(): Boolean {
        return !userPreferences.isPremium.first()
    }

    /**
     * Preload rewarded ad
     */
    fun preloadRewardedAd(onAdLoaded: () -> Unit = {}) {
        if (rewardedAd == null) {
            RewardedAd.load(
                context,
                REWARDED_AD_UNIT_ID,
                createAdRequest(),
                object : RewardedAdLoadCallback() {
                    override fun onAdLoaded(ad: RewardedAd) {
                        rewardedAd = ad
                        onAdLoaded()
                    }

                    override fun onAdFailedToLoad(error: LoadAdError) {
                        rewardedAd = null
                    }
                }
            )
        }
    }

    /**
     * Show rewarded ad (e.g., to unlock premium features or remove ads temporarily)
     */
    fun showRewardedAd(
        activity: Activity,
        onUserEarnedReward: (Int) -> Unit,
        onAdDismissed: () -> Unit = {},
        onAdFailed: () -> Unit = {}
    ) {
        rewardedAd?.let { ad ->
            ad.fullScreenContentCallback = object : FullScreenContentCallback() {
                override fun onAdDismissedFullScreenContent() {
                    rewardedAd = null
                    preloadRewardedAd()
                    onAdDismissed()
                }

                override fun onAdFailedToShowFullScreenContent(error: AdError) {
                    rewardedAd = null
                    preloadRewardedAd()
                    onAdFailed()
                }
            }

            ad.show(activity) { rewardItem ->
                onUserEarnedReward(rewardItem.amount)
            }
        } ?: run {
            preloadRewardedAd()
            onAdFailed()
        }
    }

    /**
     * Check if rewarded ad is ready
     */
    fun isRewardedAdReady(): Boolean = rewardedAd != null
}
