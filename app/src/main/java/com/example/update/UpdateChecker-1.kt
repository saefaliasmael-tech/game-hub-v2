package com.example.update

import android.content.Context
import com.example.BuildConfig
import com.example.update.model.UpdateManifest
import com.example.update.model.UpdateState
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request
import org.json.JSONArray
import org.json.JSONObject
import java.util.concurrent.TimeUnit

/**
 * Checks for Game Hub updates by querying a remote version.json manifest.
 * Gracefully handles offline mode, network timeouts, and JSON parsing failures.
 */
class UpdateChecker(
    private val context: Context,
    private val client: OkHttpClient = OkHttpClient.Builder()
        .connectTimeout(10, TimeUnit.SECONDS)
        .readTimeout(10, TimeUnit.SECONDS)
        .build()
) {

    // Configurable endpoint for the remote manifest. Can be overridden in settings or debug.
    var manifestUrl: String = DEFAULT_MANIFEST_URL

    suspend fun checkForUpdates(): UpdateState = withContext(Dispatchers.IO) {
        val currentVersionCode = BuildConfig.VERSION_CODE

        try {
            val request = Request.Builder()
                .url(manifestUrl)
                .header("User-Agent", "GameHub-Android/${BuildConfig.VERSION_NAME}")
                .build()

            val response = client.newCall(request).execute()
            if (!response.isSuccessful) {
                return@withContext UpdateState.Error(
                    message = "Server returned error: ${response.code}",
                    canContinueOffline = true
                )
            }

            val body = response.body?.string() ?: return@withContext UpdateState.Error(
                message = "Empty response received from update server",
                canContinueOffline = true
            )

            val manifest = parseManifestJson(body)
            evaluateManifest(manifest, currentVersionCode)
        } catch (e: Exception) {
            // Offline or unreachable network - never crash
            UpdateState.Error(
                message = e.localizedMessage ?: "Failed to reach update server",
                canContinueOffline = true
            )
        }
    }

    fun evaluateManifest(manifest: UpdateManifest, currentVersionCode: Int): UpdateState {
        val isNewer = manifest.versionCode > currentVersionCode
        if (!isNewer) {
            return UpdateState.UpToDate
        }

        val isMandatory = manifest.forceUpdate || (currentVersionCode < manifest.minSupportedVersionCode)
        return UpdateState.UpdateAvailable(
            manifest = manifest,
            isMandatory = isMandatory
        )
    }

    private fun parseManifestJson(jsonString: String): UpdateManifest {
        val json = JSONObject(jsonString)
        val changelogList = mutableListOf<String>()
        val changelogArray = json.optJSONArray("changelog")
        if (changelogArray != null) {
            for (i in 0 until changelogArray.length()) {
                changelogList.add(changelogArray.getString(i))
            }
        }

        return UpdateManifest(
            versionCode = json.getInt("versionCode"),
            versionName = json.getString("versionName"),
            apkUrl = json.getString("apkUrl"),
            minSupportedVersionCode = json.optInt("minSupportedVersionCode", 1),
            forceUpdate = json.optBoolean("forceUpdate", false),
            changelog = changelogList,
            sha256 = json.optString("sha256", null)
        )
    }

    companion object {
        // Standard production endpoint for Game Hub version manifest
        const val DEFAULT_MANIFEST_URL = "https://raw.githubusercontent.com/gamehub/manifest/main/version.json"
    }
}
