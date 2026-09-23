package com.zubaluba.gamehub.ads

import android.app.Activity
import android.content.Context
import android.util.Log
import com.google.android.ump.ConsentInformation
import com.google.android.ump.ConsentRequestParameters
import com.google.android.ump.FormError
import com.google.android.ump.UserMessagingPlatform

/**
 * Manages Google User Messaging Platform (UMP) consent flows for GDPR and privacy compliance.
 * Adheres strictly to the Google Mobile Ads initialization sequence:
 * 1. Request Consent Information update.
 * 2. Load and show consent form if required.
 * 3. Complete consent callback.
 * 4. Initialize MobileAds.
 * 5. Load ads.
 * Gracefully handles offline mode and errors without blocking the user.
 */
class ConsentManager private constructor(private val context: Context) {

    private val consentInformation: ConsentInformation =
        UserMessagingPlatform.getConsentInformation(context)

    val canRequestAds: Boolean
        get() = consentInformation.canRequestAds()

    val isPrivacyOptionsRequired: Boolean
        get() = consentInformation.privacyOptionsRequirementStatus ==
                ConsentInformation.PrivacyOptionsRequirementStatus.REQUIRED

    /**
     * Helper to gather consent from the user if required.
     * Always invokes [onConsentCompleted] on completion or error.
     */
    fun gatherConsent(activity: Activity, onConsentCompleted: () -> Unit) {
        val params = ConsentRequestParameters.Builder()
            .setTagForUnderAgeOfConsent(false)
            .build()

        try {
            consentInformation.requestConsentInfoUpdate(
                activity,
                params,
                {
                    // Consent info updated successfully; check if a form needs to be displayed
                    UserMessagingPlatform.loadAndShowConsentFormIfRequired(activity) { formError ->
                        if (formError != null) {
                            Log.w(TAG, "Consent form error: ${formError.errorCode} - ${formError.message}")
                        }
                        onConsentCompleted()
                    }
                },
                { requestConsentError ->
                    // Network offline or error - log gracefully and allow app to continue
                    Log.w(TAG, "Consent info request failed: ${requestConsentError.errorCode} - ${requestConsentError.message}")
                    onConsentCompleted()
                }
            )
        } catch (e: Exception) {
            Log.e(TAG, "Error initiating consent request", e)
            onConsentCompleted()
        }
    }

    /**
     * Shows the privacy options form when requested by the user from Settings.
     */
    fun showPrivacyOptionsForm(activity: Activity, onDismissed: (FormError?) -> Unit) {
        UserMessagingPlatform.showPrivacyOptionsForm(activity) { formError ->
            onDismissed(formError)
        }
    }

    companion object {
        private const val TAG = "ConsentManager"

        @Volatile
        private var instance: ConsentManager? = null

        fun getInstance(context: Context): ConsentManager {
            return instance ?: synchronized(this) {
                instance ?: ConsentManager(context.applicationContext).also { instance = it }
            }
        }
    }
}
