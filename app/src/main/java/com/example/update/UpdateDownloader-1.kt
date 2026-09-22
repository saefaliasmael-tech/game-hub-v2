package com.example.update

import android.content.Context
import com.example.update.model.UpdateManifest
import com.example.update.model.UpdateState
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import okhttp3.OkHttpClient
import okhttp3.Request
import java.io.File
import java.io.FileOutputStream
import java.security.MessageDigest
import java.util.concurrent.TimeUnit

/**
 * Downloads the full Game Hub APK update into the app's isolated update cache.
 * Emits progress percentages and performs optional SHA-256 integrity validation.
 */
class UpdateDownloader(
    private val context: Context,
    private val client: OkHttpClient = OkHttpClient.Builder()
        .connectTimeout(15, TimeUnit.SECONDS)
        .readTimeout(60, TimeUnit.SECONDS)
        .build()
) {

    fun downloadApk(manifest: UpdateManifest): Flow<UpdateState> = flow {
        emit(UpdateState.Downloading(progressPercent = 0, downloadedBytes = 0L, totalBytes = 0L))

        val updatesDir = File(context.cacheDir, "updates")
        if (!updatesDir.exists()) {
            updatesDir.mkdirs()
        }

        val targetApk = File(updatesDir, "gamehub_v${manifest.versionName}_${manifest.versionCode}.apk")
        if (targetApk.exists()) {
            targetApk.delete()
        }

        val request = Request.Builder()
            .url(manifest.apkUrl)
            .build()

        try {
            val response = client.newCall(request).execute()
            if (!response.isSuccessful) {
                emit(UpdateState.Error("Download failed with HTTP ${response.code}", canContinueOffline = !manifest.forceUpdate))
                return@flow
            }

            val body = response.body ?: run {
                emit(UpdateState.Error("Server returned empty body", canContinueOffline = !manifest.forceUpdate))
                return@flow
            }

            val totalBytes = body.contentLength()
            var downloadedBytes = 0L

            body.byteStream().use { input ->
                FileOutputStream(targetApk).use { output ->
                    val buffer = ByteArray(8 * 1024)
                    var read: Int
                    var lastPercent = 0

                    while (input.read(buffer).also { read = it } != -1) {
                        output.write(buffer, 0, read)
                        downloadedBytes += read

                        val percent = if (totalBytes > 0) {
                            ((downloadedBytes * 100) / totalBytes).toInt().coerceIn(0, 100)
                        } else {
                            0
                        }

                        if (percent != lastPercent || downloadedBytes == totalBytes) {
                            lastPercent = percent
                            emit(UpdateState.Downloading(
                                progressPercent = percent,
                                downloadedBytes = downloadedBytes,
                                totalBytes = totalBytes
                            ))
                        }
                    }
                    output.flush()
                }
            }

            // Verify SHA-256 checksum if provided in manifest
            if (!manifest.sha256.isNullOrBlank()) {
                val computedHash = calculateSha256(targetApk)
                if (!computedHash.equals(manifest.sha256.trim(), ignoreCase = true)) {
                    targetApk.delete()
                    emit(UpdateState.Error("Checksum mismatch! The downloaded APK may be corrupted.", canContinueOffline = !manifest.forceUpdate))
                    return@flow
                }
            }

            emit(UpdateState.ReadyToInstall(apkFile = targetApk, manifest = manifest))

        } catch (e: CancellationException) {
            targetApk.delete()
            throw e
        } catch (e: Exception) {
            targetApk.delete()
            emit(UpdateState.Error("Download failed: ${e.localizedMessage}", canContinueOffline = !manifest.forceUpdate))
        }
    }.flowOn(Dispatchers.IO)

    private fun calculateSha256(file: File): String {
        val digest = MessageDigest.getInstance("SHA-256")
        file.inputStream().use { input ->
            val buffer = ByteArray(8 * 1024)
            var read: Int
            while (input.read(buffer).also { read = it } != -1) {
                digest.update(buffer, 0, read)
            }
        }
        val hashBytes = digest.digest()
        val sb = StringBuilder()
        for (b in hashBytes) {
            sb.append(String.format("%02x", b))
        }
        return sb.toString()
    }
}
