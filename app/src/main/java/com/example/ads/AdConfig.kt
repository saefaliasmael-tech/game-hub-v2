package com.example.ads

import com.example.BuildConfig

/**
 * Centralized AdMob Configuration for Zuba Luba Game Hub.
 *
 * All AdMob App IDs, Ad Unit IDs, Frequency Caps, and Test Mode flags
 * are configured in this SINGLE file.
 *
 * To switch to Live Ads in the future:
 * 1. Set [IS_TEST_MODE] = false (or set via BuildConfig).
 * 2. Put your real AdMob Unit IDs into the LIVE_* constants below.
 * 3. Update the APPLICATION_ID meta-data in AndroidManifest.xml with your live AdMob App ID.
 */
object AdConfig {

    const val TAG = "ZubaLubaAds"

    /**
     * Controls whether official Google Test Ads or Live Ads are loaded.
     * During APK distribution to friends / QA testing, keep this TRUE.
     */
    const val IS_TEST_MODE = true

    // =========================================================================
    // GOOGLE ADMOB OFFICIAL TEST AD UNIT IDs
    // (Guaranteed to safely return test ads without policy violations)
    // =========================================================================
    const val TEST_APP_ID = "ca-app-pub-3940256099942544~3347511713"
    const val TEST_BANNER_ID = "ca-app-pub-3940256099942544/6300978111"
    const val TEST_INTERSTITIAL_ID = "ca-app-pub-3940256099942544/1033173712"
    const val TEST_REWARDED_ID = "ca-app-pub-3940256099942544/5224354917"

    // =========================================================================
    // FUTURE PRODUCTION / LIVE ADMOB UNIT IDs
    // (Place your approved live AdMob unit IDs here when ready for store release)
    // =========================================================================
    const val LIVE_APP_ID = "ca-app-pub-xxxxxxxxxxxxxxxx~yyyyyyyyyy"
    const val LIVE_BANNER_ID = "ca-app-pub-xxxxxxxxxxxxxxxx/zzzzzzzzzz"
    const val LIVE_INTERSTITIAL_ID = "ca-app-pub-xxxxxxxxxxxxxxxx/aaaaaaaaaa"
    const val LIVE_REWARDED_ID = "ca-app-pub-xxxxxxxxxxxxxxxx/bbbbbbbbbb"

    // =========================================================================
    // FREQUENCY CAPPING & PACING RULES
    // =========================================================================

    /**
     * Minimum interval (in milliseconds) required between consecutive interstitial ads.
     * Prevents spamming players even if multiple 3-win or 3-loss triggers occur rapidly.
     * Default: 120,000 ms (2 minutes).
     */
    const val INTERSTITIAL_MIN_INTERVAL_MS = 120_000L

    /**
     * Number of losses in an individual game required before attempting an interstitial ad.
     */
    const val LOSSES_THRESHOLD_FOR_AD = 3

    /**
     * Number of wins in an individual game required before attempting an interstitial ad.
     */
    const val WINS_THRESHOLD_FOR_AD = 3

    // =========================================================================
    // ACTIVE AD UNIT RESOLVERS
    // =========================================================================

    fun getBannerAdUnitId(): String = if (IS_TEST_MODE) TEST_BANNER_ID else LIVE_BANNER_ID

    fun getInterstitialAdUnitId(): String = if (IS_TEST_MODE) TEST_INTERSTITIAL_ID else LIVE_INTERSTITIAL_ID

    fun getRewardedAdUnitId(): String = if (IS_TEST_MODE) TEST_REWARDED_ID else LIVE_REWARDED_ID
}
