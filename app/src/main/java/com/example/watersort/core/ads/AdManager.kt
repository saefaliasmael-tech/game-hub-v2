package com.example.watersort.core.ads

interface AdManager {
    val isBannerAvailable: Boolean
    val isInterstitialAvailable: Boolean
    val isRewardedAvailable: Boolean

    fun loadBanner()
    fun showInterstitial(onDismissed: () -> Unit)
    fun showRewarded(onRewardEarned: (amount: Int) -> Unit, onFailedOrDismissed: () -> Unit)
}

/**
 * Standard production-ready offline abstraction of AdManager.
 * Does not show fake ads; gracefully reports when ads are not wired to an external network SDK.
 */
class OfflineAdManager : AdManager {
    override val isBannerAvailable: Boolean = false
    override val isInterstitialAvailable: Boolean = false
    override val isRewardedAvailable: Boolean = false

    override fun loadBanner() {
        // No-op for offline
    }

    override fun showInterstitial(onDismissed: () -> Unit) {
        onDismissed()
    }

    override fun showRewarded(onRewardEarned: (amount: Int) -> Unit, onFailedOrDismissed: () -> Unit) {
        // External network not connected
        onFailedOrDismissed()
    }
}
