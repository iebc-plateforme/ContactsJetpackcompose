package com.contacts.android.contacts.ads

import android.app.Activity
import android.content.Context
import com.google.android.gms.ads.AdError
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.FullScreenContentCallback
import com.google.android.gms.ads.LoadAdError
import com.google.android.gms.ads.MobileAds
import com.google.android.gms.ads.interstitial.InterstitialAd
import com.google.android.gms.ads.interstitial.InterstitialAdLoadCallback
import com.google.android.gms.ads.rewarded.RewardedAd
import com.google.android.gms.ads.rewarded.RewardedAdLoadCallback
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
    private val context: Context
) {
    companion object {
        // Test Ad Unit IDs (replace with your actual IDs for production)
        const val BANNER_AD_UNIT_ID = "ca-app-pub-3940256099942544/6300978111" // Test ID
        const val INTERSTITIAL_AD_UNIT_ID = "ca-app-pub-3940256099942544/1033173712" // Test ID
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
     */
    fun showInterstitialAd(
        activity: Activity,
        onAdDismissed: () -> Unit = {},
        onAdFailed: () -> Unit = {}
    ) {
        val currentTime = System.currentTimeMillis()

        // Check if enough time has passed since last ad
        if (currentTime - lastInterstitialTime < INTERSTITIAL_MIN_INTERVAL_MS) {
            onAdDismissed()
            return
        }

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
