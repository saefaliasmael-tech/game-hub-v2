package com.example.ads

import android.app.Activity
import android.content.Context
import android.util.Log
import com.google.android.gms.ads.AdError
import com.google.android.gms.ads.AdInspectorError
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.FullScreenContentCallback
import com.google.android.gms.ads.LoadAdError
import com.google.android.gms.ads.MobileAds
import com.google.android.gms.ads.RequestConfiguration
import com.google.android.gms.ads.interstitial.InterstitialAd
import com.google.android.gms.ads.interstitial.InterstitialAdLoadCallback
import com.google.android.gms.ads.rewarded.RewardedAd
import com.google.android.gms.ads.rewarded.RewardedAdLoadCallback
import com.google.android.ump.ConsentInformation
import com.google.android.ump.ConsentRequestParameters
import com.google.android.ump.UserMessagingPlatform
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.atomic.AtomicBoolean

/**
 * Centralized, production-grade AdMob manager for Zuba Luba Game Hub.
 *
 * Key Architectural Safeguards:
 * - Robust offline & error tolerance: Never crashes if network is missing.
 * - Anti-Exploit Rewarded system: Executes reward callback exactly once per viewed ad.
 * - Rapid-tap debounce: Prevents multi-click spam from opening duplicate ads or duplicating rewards.
 * - Concurrency control: Never attempts to display two full-screen ads at the same time.
 * - Per-game session counters: Every game's win and loss counters are completely isolated.
 * - Safe lifecycle handling: Prevents Activity/Context memory leaks.
 * - Cooldown enforcement: Respects [AdConfig.INTERSTITIAL_MIN_INTERVAL_MS] to avoid player annoyance.
 * - Clean logging: Clear logs prefixed with [AdConfig.TAG].
 */
object AdManager {

    private val isInitialized = AtomicBoolean(false)
    private var appContext: Context? = null

    // Full-screen state guards
    @Volatile
    private var isAdShowing: Boolean = false

    @Volatile
    private var lastInterstitialShowTime: Long = 0L

    // Interstitial Ad Caching
    @Volatile
    private var interstitialAd: InterstitialAd? = null
    private val isInterstitialLoading = AtomicBoolean(false)

    // Rewarded Ad Caching
    @Volatile
    private var rewardedAd: RewardedAd? = null
    private val isRewardedLoading = AtomicBoolean(false)

    // Anti-exploit guard for current rewarded ad presentation
    @Volatile
    private var rewardGrantedForCurrentAd: Boolean = false

    @Volatile
    private var isRewardedRequestInProgress: Boolean = false

    // Isolated Per-Game Counters
    data class GameAdSession(
        var winCount: Int = 0,
        var lossCount: Int = 0
    )

    private val gameSessions = ConcurrentHashMap<String, GameAdSession>()

    /**
     * Initializes the Mobile Ads SDK, applies consent configuration,
     * and preloads initial ads.
     */
    fun initialize(context: Context, onInitialized: () -> Unit = {}) {
        if (isInitialized.getAndSet(true)) {
            Log.d(AdConfig.TAG, "AdManager already initialized.")
            onInitialized()
            return
        }

        val applicationContext = context.applicationContext
        appContext = applicationContext

        try {
            if (AdConfig.IS_TEST_MODE) {
                val testConfig = RequestConfiguration.Builder()
                    .setTestDeviceIds(listOf(AdRequest.DEVICE_ID_EMULATOR))
                    .build()
                MobileAds.setRequestConfiguration(testConfig)
                Log.d(AdConfig.TAG, "Configured MobileAds in TEST MODE (Official Google Test Units).")
            }

            // Optional User Messaging Platform (UMP) Consent Check
            gatherConsentIfRequired(context)

            // Asynchronously initialize Mobile Ads SDK
            MobileAds.initialize(applicationContext) { initStatus ->
                Log.d(AdConfig.TAG, "MobileAds SDK initialized. Status: ${initStatus.adapterStatusMap.keys}")
                // Preload first ads for smooth gameplay transitions
                loadInterstitial(applicationContext)
                loadRewarded(applicationContext)
                onInitialized()
            }
        } catch (e: Throwable) {
            Log.e(AdConfig.TAG, "Error during AdManager initialization: ${e.message}", e)
            onInitialized()
        }
    }

    /**
     * Gracefully gathers UMP privacy consent without blocking the user or crashing offline.
     */
    private fun gatherConsentIfRequired(context: Context) {
        try {
            val params = ConsentRequestParameters.Builder()
                .setTagForUnderAgeOfConsent(false)
                .build()

            val consentInfo = UserMessagingPlatform.getConsentInformation(context)
            if (context is Activity) {
                consentInfo.requestConsentInfoUpdate(
                    context,
                    params,
                    {
                        UserMessagingPlatform.loadAndShowConsentFormIfRequired(context) { loadAndShowError ->
                            if (loadAndShowError != null) {
                                Log.d(AdConfig.TAG, "Consent form notice: ${loadAndShowError.message}")
                            } else {
                                Log.d(AdConfig.TAG, "Consent form processed successfully.")
                            }
                        }
                    },
                    { requestConsentError ->
                        Log.d(AdConfig.TAG, "Consent info update notice: ${requestConsentError.message}")
                    }
                )
            }
        } catch (e: Throwable) {
            Log.d(AdConfig.TAG, "Consent check bypassed (normal for standalone / offline): ${e.message}")
        }
    }

    // =========================================================================
    // INTERSTITIAL ADS
    // =========================================================================

    val isInterstitialAvailable: Boolean
        get() = interstitialAd != null

    fun loadInterstitial(context: Context? = appContext) {
        val ctx = context?.applicationContext ?: appContext ?: return
        if (interstitialAd != null) {
            Log.d(AdConfig.TAG, "Interstitial already cached and ready.")
            return
        }
        if (isInterstitialLoading.getAndSet(true)) {
            Log.d(AdConfig.TAG, "Interstitial load already in progress.")
            return
        }

        Log.d(AdConfig.TAG, "Loading Interstitial Ad (${AdConfig.getInterstitialAdUnitId()})...")
        val adRequest = AdRequest.Builder().build()

        InterstitialAd.load(
            ctx,
            AdConfig.getInterstitialAdUnitId(),
            adRequest,
            object : InterstitialAdLoadCallback() {
                override fun onAdLoaded(ad: InterstitialAd) {
                    isInterstitialLoading.set(false)
                    interstitialAd = ad
                    Log.d(AdConfig.TAG, "Interstitial loaded successfully.")
                }

                override fun onAdFailedToLoad(loadAdError: LoadAdError) {
                    isInterstitialLoading.set(false)
                    interstitialAd = null
                    Log.w(AdConfig.TAG, "Interstitial failed to load: ${loadAdError.message} (code: ${loadAdError.code})")
                }
            }
        )
    }

    /**
     * Shows an interstitial ad if available and safe.
     * Always calls [onDismissed] so gameplay flow is never frozen.
     */
    fun showInterstitial(
        activity: Activity?,
        onDismissed: () -> Unit
    ) {
        if (activity == null || activity.isFinishing || activity.isDestroyed) {
            Log.w(AdConfig.TAG, "Cannot show Interstitial: Activity is null, finishing, or destroyed.")
            onDismissed()
            return
        }

        if (isAdShowing) {
            Log.w(AdConfig.TAG, "Cannot show Interstitial: Another full-screen ad is currently active.")
            onDismissed()
            return
        }

        val ad = interstitialAd
        if (ad == null) {
            Log.d(AdConfig.TAG, "Interstitial not ready. Continuing gameplay and preloading next ad.")
            onDismissed()
            loadInterstitial(activity)
            return
        }

        ad.fullScreenContentCallback = object : FullScreenContentCallback() {
            override fun onAdShowedFullScreenContent() {
                isAdShowing = true
                lastInterstitialShowTime = System.currentTimeMillis()
                Log.d(AdConfig.TAG, "Interstitial shown.")
            }

            override fun onAdDismissedFullScreenContent() {
                isAdShowing = false
                interstitialAd = null
                Log.d(AdConfig.TAG, "Interstitial dismissed.")
                onDismissed()
                loadInterstitial(activity)
            }

            override fun onAdFailedToShowFullScreenContent(adError: AdError) {
                isAdShowing = false
                interstitialAd = null
                Log.e(AdConfig.TAG, "Interstitial failed to show: ${adError.message}")
                onDismissed()
                loadInterstitial(activity)
            }
        }

        try {
            ad.show(activity)
        } catch (e: Throwable) {
            Log.e(AdConfig.TAG, "Exception showing Interstitial: ${e.message}", e)
            isAdShowing = false
            interstitialAd = null
            onDismissed()
            loadInterstitial(activity)
        }
    }

    // =========================================================================
    // ISOLATED PER-GAME SESSION TRACKING (3 WINS / 3 LOSSES)
    // =========================================================================

    fun getSession(gameId: String): GameAdSession {
        return gameSessions.getOrPut(gameId) { GameAdSession() }
    }

    /**
     * Called when a player wins a level in a specific game.
     * Increments the game's win counter.
     * When reaching 3 wins:
     * - If cooldown has passed AND ad is ready: resets counter and presents Interstitial.
     * - Otherwise: continues game smoothly without interruption and preloads in background.
     */
    fun recordGameWin(
        gameId: String,
        activity: Activity?,
        onContinue: () -> Unit
    ) {
        val session = getSession(gameId)
        session.winCount++
        Log.d(AdConfig.TAG, "Game [$gameId] Win count: ${session.winCount}/${AdConfig.WINS_THRESHOLD_FOR_AD}")

        if (session.winCount >= AdConfig.WINS_THRESHOLD_FOR_AD) {
            val now = System.currentTimeMillis()
            val timeSinceLastAd = now - lastInterstitialShowTime

            if (timeSinceLastAd < AdConfig.INTERSTITIAL_MIN_INTERVAL_MS) {
                Log.d(AdConfig.TAG, "Frequency cap: Cooldown active (${timeSinceLastAd / 1000}s / ${AdConfig.INTERSTITIAL_MIN_INTERVAL_MS / 1000}s). Skipping ad.")
                onContinue()
                loadInterstitial(activity)
                return
            }

            if (!isInterstitialAvailable) {
                Log.d(AdConfig.TAG, "Win threshold reached (3), but Interstitial not ready yet. Skipping without blocking.")
                onContinue()
                loadInterstitial(activity)
                return
            }

            // Conditions met: Reset win counter and present ad safely
            session.winCount = 0
            showInterstitial(activity, onDismissed = onContinue)
        } else {
            onContinue()
        }
    }

    /**
     * Called when a player loses in a specific game.
     * Increments the game's loss counter.
     * When reaching 3 losses:
     * - If cooldown has passed AND ad is ready: resets counter and presents Interstitial.
     * - Otherwise: continues game smoothly and preloads in background.
     */
    fun recordGameLoss(
        gameId: String,
        activity: Activity?,
        onContinue: () -> Unit
    ) {
        val session = getSession(gameId)
        session.lossCount++
        Log.d(AdConfig.TAG, "Game [$gameId] Loss count: ${session.lossCount}/${AdConfig.LOSSES_THRESHOLD_FOR_AD}")

        if (session.lossCount >= AdConfig.LOSSES_THRESHOLD_FOR_AD) {
            val now = System.currentTimeMillis()
            val timeSinceLastAd = now - lastInterstitialShowTime

            if (timeSinceLastAd < AdConfig.INTERSTITIAL_MIN_INTERVAL_MS) {
                Log.d(AdConfig.TAG, "Frequency cap: Cooldown active (${timeSinceLastAd / 1000}s / ${AdConfig.INTERSTITIAL_MIN_INTERVAL_MS / 1000}s). Skipping ad.")
                onContinue()
                loadInterstitial(activity)
                return
            }

            if (!isInterstitialAvailable) {
                Log.d(AdConfig.TAG, "Loss threshold reached (3), but Interstitial not ready yet. Skipping without blocking.")
                onContinue()
                loadInterstitial(activity)
                return
            }

            // Conditions met: Reset loss counter and present ad safely
            session.lossCount = 0
            showInterstitial(activity, onDismissed = onContinue)
        } else {
            onContinue()
        }
    }

    // =========================================================================
    // REWARDED ADS (ANTI-EXPLOIT & SAFE CALLBACKS)
    // =========================================================================

    val isRewardedAvailable: Boolean
        get() = rewardedAd != null

    val isRewardedInProgress: Boolean
        get() = isRewardedRequestInProgress

    fun loadRewarded(context: Context? = appContext) {
        val ctx = context?.applicationContext ?: appContext ?: return
        if (rewardedAd != null) {
            Log.d(AdConfig.TAG, "Rewarded ad already cached and ready.")
            return
        }
        if (isRewardedLoading.getAndSet(true)) {
            Log.d(AdConfig.TAG, "Rewarded ad load already in progress.")
            return
        }

        Log.d(AdConfig.TAG, "Loading Rewarded Ad (${AdConfig.getRewardedAdUnitId()})...")
        val adRequest = AdRequest.Builder().build()

        RewardedAd.load(
            ctx,
            AdConfig.getRewardedAdUnitId(),
            adRequest,
            object : RewardedAdLoadCallback() {
                override fun onAdLoaded(ad: RewardedAd) {
                    isRewardedLoading.set(false)
                    rewardedAd = ad
                    Log.d(AdConfig.TAG, "Rewarded ad loaded successfully.")
                }

                override fun onAdFailedToLoad(loadAdError: LoadAdError) {
                    isRewardedLoading.set(false)
                    rewardedAd = null
                    Log.w(AdConfig.TAG, "Rewarded ad failed to load: ${loadAdError.message} (code: ${loadAdError.code})")
                }
            }
        )
    }

    /**
     * Shows a rewarded ad requested by the user.
     *
     * Anti-Exploit Guarantee:
     * - [onUserEarnedReward] is called ONLY IF the user fully watches the ad and Google triggers OnUserEarnedRewardListener.
     * - The callback is guaranteed to execute at most ONCE per ad via [rewardGrantedForCurrentAd].
     * - Rapid-clicking the reward button is debounced by [isRewardedRequestInProgress].
     */
    fun showRewarded(
        activity: Activity?,
        onUserEarnedReward: (amount: Int, type: String) -> Unit,
        onDismissed: () -> Unit
    ) {
        if (activity == null || activity.isFinishing || activity.isDestroyed) {
            Log.w(AdConfig.TAG, "Cannot show Rewarded: Activity is null, finishing, or destroyed.")
            onDismissed()
            return
        }

        if (isAdShowing || isRewardedRequestInProgress) {
            Log.w(AdConfig.TAG, "Rewarded ad request ignored: Ad is already showing or in progress.")
            return
        }

        val ad = rewardedAd
        if (ad == null) {
            Log.d(AdConfig.TAG, "Rewarded ad not ready. Preloading and notifying caller.")
            onDismissed()
            loadRewarded(activity)
            return
        }

        isRewardedRequestInProgress = true
        rewardGrantedForCurrentAd = false

        ad.fullScreenContentCallback = object : FullScreenContentCallback() {
            override fun onAdShowedFullScreenContent() {
                isAdShowing = true
                Log.d(AdConfig.TAG, "Rewarded ad shown.")
            }

            override fun onAdDismissedFullScreenContent() {
                isAdShowing = false
                isRewardedRequestInProgress = false
                rewardedAd = null
                Log.d(AdConfig.TAG, "Rewarded ad dismissed.")
                onDismissed()
                loadRewarded(activity)
            }

            override fun onAdFailedToShowFullScreenContent(adError: AdError) {
                isAdShowing = false
                isRewardedRequestInProgress = false
                rewardedAd = null
                Log.e(AdConfig.TAG, "Rewarded ad failed to show: ${adError.message}")
                onDismissed()
                loadRewarded(activity)
            }
        }

        try {
            ad.show(activity) { rewardItem ->
                // Anti-exploit check: Single reward execution per ad impression
                if (!rewardGrantedForCurrentAd) {
                    rewardGrantedForCurrentAd = true
                    Log.d(AdConfig.TAG, "Reward earned: ${rewardItem.amount} ${rewardItem.type}")
                    onUserEarnedReward(rewardItem.amount, rewardItem.type)
                } else {
                    Log.w(AdConfig.TAG, "Duplicate reward callback detected and suppressed.")
                }
            }
        } catch (e: Throwable) {
            Log.e(AdConfig.TAG, "Exception showing Rewarded ad: ${e.message}", e)
            isAdShowing = false
            isRewardedRequestInProgress = false
            rewardedAd = null
            onDismissed()
            loadRewarded(activity)
        }
    }

    // =========================================================================
    // GOOGLE AD INSPECTOR
    // =========================================================================

    /**
     * Opens Google Ad Inspector for diagnosing ad adapters, mediation, and ad request statuses.
     */
    fun openAdInspector(context: Context, onClosed: ((AdInspectorError?) -> Unit)? = null) {
        try {
            MobileAds.openAdInspector(context) { error ->
                if (error != null) {
                    Log.e(AdConfig.TAG, "Ad Inspector error: ${error.message} (code: ${error.code})")
                } else {
                    Log.d(AdConfig.TAG, "Ad Inspector opened successfully.")
                }
                onClosed?.invoke(error)
            }
        } catch (e: Throwable) {
            Log.e(AdConfig.TAG, "Failed to open Ad Inspector: ${e.message}", e)
            onClosed?.invoke(null)
        }
    }
}
