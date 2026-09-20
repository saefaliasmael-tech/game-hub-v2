package com.example.update.model

/**
 * Metadata model returned by the remote version manifest (version.json).
 */
data class UpdateManifest(
    val versionCode: Int,
    val versionName: String,
    val apkUrl: String,
    val minSupportedVersionCode: Int = 1,
    val forceUpdate: Boolean = false,
    val changelog: List<String> = emptyList(),
    val sha256: String? = null
)
