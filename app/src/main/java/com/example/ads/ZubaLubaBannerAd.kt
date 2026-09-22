package com.example.ads

import android.util.Log
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.viewinterop.AndroidView
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import com.google.android.gms.ads.AdListener
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.AdSize
import com.google.android.gms.ads.AdView
import com.google.android.gms.ads.LoadAdError

/**
 * Production-ready Compose wrapper for Google Mobile Ads Banner.
 *
 * Safeguards:
 * - Collapses completely (0dp height) when ad is loading or failed to load.
 * - Leaves no blank space or visual glitch if offline.
 * - Handles Android Lifecycle events (PAUSE, RESUME, DESTROY) safely to prevent leaks.
 * - Never throws or crashes if ad unit fails.
 */
@Composable
fun ZubaLubaBannerAd(
    modifier: Modifier = Modifier,
    adSize: AdSize = AdSize.BANNER,
    adUnitId: String = AdConfig.getBannerAdUnitId(),
    backgroundColor: Color = Color.Transparent,
    onAdLoadedCallback: () -> Unit = {},
    onAdFailedCallback: (LoadAdError) -> Unit = {}
) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current
    var isAdLoaded by remember { mutableStateOf(false) }

    // Preserve AdView across recompositions
    val adView = remember {
        AdView(context).apply {
            setAdSize(adSize)
            setAdUnitId(adUnitId)
        }
    }

    // Lifecycle observer to pause/resume/destroy adView properly
    DisposableEffect(lifecycleOwner, adView) {
        val observer = LifecycleEventObserver { _, event ->
            when (event) {
                Lifecycle.Event.ON_PAUSE -> adView.pause()
                Lifecycle.Event.ON_RESUME -> adView.resume()
                Lifecycle.Event.ON_DESTROY -> adView.destroy()
                else -> Unit
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)

        onDispose {
            lifecycleOwner.lifecycle.removeObserver(observer)
            adView.destroy()
        }
    }

    // Only occupy visual space if an ad is actually loaded and visible
    AnimatedVisibility(
        visible = isAdLoaded,
        enter = fadeIn() + expandVertically(),
        exit = fadeOut() + shrinkVertically(),
        modifier = modifier
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .wrapContentHeight()
                .background(backgroundColor)
                .testTag("admob_banner_container"),
            contentAlignment = Alignment.Center
        ) {
            AndroidView(
                factory = {
                    adView.apply {
                        adListener = object : AdListener() {
                            override fun onAdLoaded() {
                                super.onAdLoaded()
                                isAdLoaded = true
                                Log.d(AdConfig.TAG, "Banner loaded successfully.")
                                onAdLoadedCallback()
                            }

                            override fun onAdFailedToLoad(loadAdError: LoadAdError) {
                                super.onAdFailedToLoad(loadAdError)
                                isAdLoaded = false
                                Log.w(AdConfig.TAG, "Banner failed to load: ${loadAdError.message} (code: ${loadAdError.code})")
                                onAdFailedCallback(loadAdError)
                            }
                        }
                        val request = AdRequest.Builder().build()
                        loadAd(request)
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .wrapContentHeight()
                    .testTag("admob_banner_view")
            )
        }
    }
}
