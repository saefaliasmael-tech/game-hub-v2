package com.zubaluba.gamehub.ads

import android.app.Activity
import android.content.Context
import android.content.SharedPreferences
import android.os.Handler
import android.os.Looper
import android.util.Log
import com.google.android.gms.ads.AdError
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.FullScreenContentCallback
import com.google.android.gms.ads.LoadAdError
import com.google.android.gms.ads.MobileAds
import com.google.android.gms.ads.interstitial.InterstitialAd
import com.google.android.gms.ads.interstitial.InterstitialAdLoadCallback
import com.google.android.gms.ads.rewarded.RewardedAd
import com.google.android.gms.ads.rewarded.RewardedAdLoadCallback
import java.util.concurrent.atomic.AtomicBoolean

/**
 * Unified AdMob Manager for Zuba Luba Game Hub.
 * Features:
 * - Persistent timestamps & counters via SharedPreferences (survives app restart).
 * - Enforces minimum 3-minute cooldown between interstitials.
 * - Suppresses interstitials on first launch and during initial gameplay.
 * - Blocks interstitials directly following a rewarded ad.
 * - Preloads Interstitial and Rewarded ads.
 * - Atomic reward callback handling to prevent duplicate coin grants.
 * - Clean offline fallback without errors or crashes.
 */
class UnifiedAdManager private constructor(context: Context) {

    private val appContext = context.applicationContext
    private val prefs: SharedPreferences = appContext.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

    private val isInitialized = AtomicBoolean(false)
    private val isShowingAd = AtomicBoolean(false)

    private var interstitialAd: InterstitialAd? = null
    private var isInterstitialLoading = false

    private var rewardedAd: RewardedAd? = null
    private var isRewardedLoading = false

    init {
        // Record first launch timestamp if not already set
        if (!prefs.contains(KEY_FIRST_LAUNCH_TIME)) {
            prefs.edit().putLong(KEY_FIRST_LAUNCH_TIME, System.currentTimeMillis()).apply()
        }
    }

    fun initialize() {
        if (isInitialized.compareAndSet(false, true)) {
            try {
                MobileAds.initialize(appContext) { status ->
                    Log.d(TAG, "MobileAds initialized: $status")
                    // Preload ads in background
                    preloadInterstitial()
                    preloadRewarded()
                }
            } catch (e: Exception) {
                Log.e(TAG, "Failed to initialize MobileAds", e)
            }
        }
    }

    // =========================================================================
    // Interstitial Ads
    // =========================================================================

    fun preloadInterstitial() {
        if (!isInitialized.get() || interstitialAd != null || isInterstitialLoading) return
        if (!NetworkUtils.isOnline(appContext)) return

        isInterstitialLoading = true
        val adRequest = AdRequest.Builder().build()
        InterstitialAd.load(
            appContext,
            AdConfig.interstitialAdUnitId,
            adRequest,
            object : InterstitialAdLoadCallback() {
                override fun onAdLoaded(ad: InterstitialAd) {
                    interstitialAd = ad
                    isInterstitialLoading = false
                    Log.d(TAG, "Interstitial ad loaded successfully")
                }

                override fun onAdFailedToLoad(error: LoadAdError) {
                    interstitialAd = null
                    isInterstitialLoading = false
                    Log.w(TAG, "Interstitial ad failed to load: ${error.code} - ${error.message}")
                }
            }
        )
    }

    /**
     * Determines whether an interstitial ad is permitted right now.
     * Enforces:
     * 1. Minimum 3 minutes since last interstitial.
     * 2. No interstitial during initial launch grace period.
     * 3. No interstitial within 3 minutes of watching a rewarded ad.
     * 4. Interstitial ad must be loaded and ready.
     * 5. No other full-screen ad currently showing.
     */
    fun canShowInterstitial(): Boolean {
        if (!isInitialized.get()) return false
        if (isShowingAd.get()) return false
        if (interstitialAd == null) return false

        val now = System.currentTimeMillis()
        val firstLaunchTime = prefs.getLong(KEY_FIRST_LAUNCH_TIME, 0L)
        if (now - firstLaunchTime < AdConfig.FIRST_LAUNCH_GRACE_PERIOD_MS) {
            // First launch protection: never show interstitial right away
            return false
        }

        val lastInterstitialTime = prefs.getLong(KEY_LAST_INTERSTITIAL_TIME, 0L)
        if (now - lastInterstitialTime < AdConfig.INTERSTITIAL_COOLDOWN_MS) {
            // Respect 3-minute cooldown
            return false
        }

        val lastRewardedTime = prefs.getLong(KEY_LAST_REWARDED_TIME, 0L)
        if (now - lastRewardedTime < AdConfig.INTERSTITIAL_COOLDOWN_MS) {
            // Never show directly after rewarded ad
            return false
        }

        return true
    }

    /**
     * Shows an interstitial ad if conditions allow. Always calls [onAdDismissed]
     * whether the ad was shown or suppressed.
     */
    fun showInterstitial(activity: Activity, onAdDismissed: () -> Unit = {}) {
        if (!canShowInterstitial()) {
            onAdDismissed()
            preloadInterstitial()
            return
        }

        val ad = interstitialAd
        if (ad == null) {
            onAdDismissed()
            preloadInterstitial()
            return
        }

        if (!isShowingAd.compareAndSet(false, true)) {
            onAdDismissed()
            return
        }

        ad.fullScreenContentCallback = object : FullScreenContentCallback() {
            override fun onAdDismissedFullScreenContent() {
                interstitialAd = null
                isShowingAd.set(false)
                prefs.edit().putLong(KEY_LAST_INTERSTITIAL_TIME, System.currentTimeMillis()).apply()
                preloadInterstitial()
                onAdDismissed()
            }

            override fun onAdFailedToShowFullScreenContent(error: AdError) {
                interstitialAd = null
                isShowingAd.set(false)
                Log.w(TAG, "Interstitial failed to show: ${error.code} - ${error.message}")
                preloadInterstitial()
                onAdDismissed()
            }

            override fun onAdShowedFullScreenContent() {
                Log.d(TAG, "Interstitial showing on screen")
            }
        }

        ad.show(activity)
    }

    /**
     * Increments Hub user action count. Can trigger interstitial after multiple actions
     * if cooldown and launch criteria are met.
     */
    fun recordHubAction(activity: Activity, onComplete: () -> Unit = {}) {
        val currentActions = prefs.getInt(KEY_HUB_ACTIONS, 0) + 1
        prefs.edit().putInt(KEY_HUB_ACTIONS, currentActions).apply()

        if (currentActions >= AdConfig.MIN_HUB_ACTIONS_FOR_INTERSTITIAL && canShowInterstitial()) {
            prefs.edit().putInt(KEY_HUB_ACTIONS, 0).apply()
            showInterstitial(activity, onComplete)
        } else {
            onComplete()
        }
    }

    // =========================================================================
    // Rewarded Ads
    // =========================================================================

    fun preloadRewarded() {
        if (!isInitialized.get() || rewardedAd != null || isRewardedLoading) return
        if (!NetworkUtils.isOnline(appContext)) return

        isRewardedLoading = true
        val adRequest = AdRequest.Builder().build()
        RewardedAd.load(
            appContext,
            AdConfig.rewardedAdUnitId,
            adRequest,
            object : RewardedAdLoadCallback() {
                override fun onAdLoaded(ad: RewardedAd) {
                    rewardedAd = ad
                    isRewardedLoading = false
                    Log.d(TAG, "Rewarded ad loaded successfully")
                }

                override fun onAdFailedToLoad(error: LoadAdError) {
                    rewardedAd = null
                    isRewardedLoading = false
                    Log.w(TAG, "Rewarded ad failed to load: ${error.code} - ${error.message}")
                }
            }
        )
    }

    fun isRewardedAdReady(): Boolean {
        return isInitialized.get() && rewardedAd != null && !isShowingAd.get()
    }

    /**
     * Shows a rewarded ad.
     * Guarantees:
     * - [onUserEarnedReward] is called AT MOST once per ad presentation.
     * - Updates [KEY_LAST_REWARDED_TIME] to block subsequent interstitials.
     * - Safely triggers [onAdClosed] on completion or failure.
     */
    fun showRewarded(
        activity: Activity,
        onUserEarnedReward: (amount: Int) -> Unit,
        onAdClosed: () -> Unit
    ) {
        val ad = rewardedAd
        if (ad == null || !isShowingAd.compareAndSet(false, true)) {
            preloadRewarded()
            onAdClosed()
            return
        }

        val rewardGranted = AtomicBoolean(false)

        ad.fullScreenContentCallback = object : FullScreenContentCallback() {
            override fun onAdDismissedFullScreenContent() {
                rewardedAd = null
                isShowingAd.set(false)
                prefs.edit().putLong(KEY_LAST_REWARDED_TIME, System.currentTimeMillis()).apply()
                preloadRewarded()
                onAdClosed()
            }

            override fun onAdFailedToShowFullScreenContent(error: AdError) {
                rewardedAd = null
                isShowingAd.set(false)
                Log.w(TAG, "Rewarded ad failed to show: ${error.code} - ${error.message}")
                preloadRewarded()
                onAdClosed()
            }

            override fun onAdShowedFullScreenContent() {
                Log.d(TAG, "Rewarded ad showing on screen")
            }
        }

        ad.show(activity) { rewardItem ->
            if (rewardGranted.compareAndSet(false, true)) {
                val amount = rewardItem.amount.takeIf { it > 0 } ?: 50
                Handler(Looper.getMainLooper()).post {
                    onUserEarnedReward(amount)
                }
            }
        }
    }

    companion object {
        private const val TAG = "UnifiedAdManager"
        private const val PREFS_NAME = "zubaluba_ad_prefs"
        private const val KEY_FIRST_LAUNCH_TIME = "first_launch_time_ms"
        private const val KEY_LAST_INTERSTITIAL_TIME = "last_interstitial_time_ms"
        private const val KEY_LAST_REWARDED_TIME = "last_rewarded_time_ms"
        private const val KEY_HUB_ACTIONS = "hub_actions_count"

        @Volatile
        private var instance: UnifiedAdManager? = null

        fun getInstance(context: Context): UnifiedAdManager {
            return instance ?: synchronized(this) {
                instance ?: UnifiedAdManager(context.applicationContext).also { instance = it }
            }
        }

        fun initialize(context: Context) {
            getInstance(context).initialize()
        }
    }
}
