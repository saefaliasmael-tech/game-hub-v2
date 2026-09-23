package com.zubaluba.gamehub.ads

/**
 * AdMob configuration for Zuba Luba Game Hub.
 * Preconfigured with official Google Mobile Ads test IDs for debug and test releases.
 * Ready for future production AdMob IDs.
 */
object AdConfig {
    // Official Google Test IDs (Android)
    const val TEST_APP_ID = "ca-app-pub-3940256099942544~3347511713"
    const val TEST_BANNER_ID = "ca-app-pub-3940256099942544/6300978111"
    const val TEST_INTERSTITIAL_ID = "ca-app-pub-3940256099942544/1033173712"
    const val TEST_REWARDED_ID = "ca-app-pub-3940256099942544/5224354917"

    // Active Ad Unit IDs (defaults to test ads)
    val bannerAdUnitId: String = TEST_BANNER_ID
    val interstitialAdUnitId: String = TEST_INTERSTITIAL_ID
    val rewardedAdUnitId: String = TEST_REWARDED_ID

    // Cooldown rules
    const val INTERSTITIAL_COOLDOWN_MS: Long = 180_000L // 3 minutes cooldown
    const val FIRST_LAUNCH_GRACE_PERIOD_MS: Long = 120_000L // 2 minutes grace period after first app install/launch
    const val MIN_HUB_ACTIONS_FOR_INTERSTITIAL: Int = 4 // Require at least 4 user actions before considering an interstitial
}
