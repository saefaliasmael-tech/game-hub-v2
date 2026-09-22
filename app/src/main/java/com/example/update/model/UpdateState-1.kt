package com.example.update.model

import java.io.File

sealed interface UpdateState {
    object Idle : UpdateState
    object Checking : UpdateState
    object UpToDate : UpdateState
    data class UpdateAvailable(
        val manifest: UpdateManifest,
        val isMandatory: Boolean
    ) : UpdateState
    data class Downloading(
        val progressPercent: Int,
        val downloadedBytes: Long,
        val totalBytes: Long
    ) : UpdateState
    data class ReadyToInstall(
        val apkFile: File,
        val manifest: UpdateManifest
    ) : UpdateState
    data class Error(
        val message: String,
        val canContinueOffline: Boolean = true
    ) : UpdateState
}
