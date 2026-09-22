package com.example.update

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import androidx.core.content.FileProvider
import java.io.File

/**
 * Triggers the standard Android Package Installer to install or upgrade the Game Hub APK.
 * Employs FileProvider to grant the OS secure, read-only access to the cached APK.
 */
class UpdateInstaller(private val context: Context) {

    fun installApk(apkFile: File): Result<Unit> {
        return try {
            if (!apkFile.exists()) {
                return Result.failure(IllegalStateException("APK file does not exist: ${apkFile.absolutePath}"))
            }

            val authority = "${context.packageName}.provider"
            val apkUri: Uri = FileProvider.getUriForFile(context, authority, apkFile)

            val intent = Intent(Intent.ACTION_VIEW).apply {
                setDataAndType(apkUri, "application/vnd.android.package-archive")
                flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_GRANT_READ_URI_PERMISSION
            }

            context.startActivity(intent)
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
