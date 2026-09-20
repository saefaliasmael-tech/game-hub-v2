package com.example.update

import com.example.update.model.UpdateManifest
import com.example.update.model.UpdateState
import org.junit.Assert.*
import org.junit.Test

class UpdateCheckerLogicTest {

    private fun evaluate(manifest: UpdateManifest, currentVersionCode: Int): UpdateState {
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

    @Test
    fun testUpToDateWhenVersionIsEqualOrLower() {
        val manifest = UpdateManifest(
            versionCode = 23,
            versionName = "v23",
            apkUrl = "https://example.com/gamehub.apk"
        )

        val result = evaluate(manifest, currentVersionCode = 23)
        assertEquals(UpdateState.UpToDate, result)

        val resultOlder = evaluate(manifest, currentVersionCode = 24)
        assertEquals(UpdateState.UpToDate, resultOlder)
    }

    @Test
    fun testOptionalUpdateAvailable() {
        val manifest = UpdateManifest(
            versionCode = 24,
            versionName = "v24",
            apkUrl = "https://example.com/gamehub-v24.apk",
            minSupportedVersionCode = 20,
            forceUpdate = false
        )

        val result = evaluate(manifest, currentVersionCode = 23)
        assertTrue(result is UpdateState.UpdateAvailable)
        val updateAvailable = result as UpdateState.UpdateAvailable
        assertFalse(updateAvailable.isMandatory)
        assertEquals("v24", updateAvailable.manifest.versionName)
    }

    @Test
    fun testMandatoryUpdateWhenBelowMinSupported() {
        val manifest = UpdateManifest(
            versionCode = 25,
            versionName = "v25",
            apkUrl = "https://example.com/gamehub-v25.apk",
            minSupportedVersionCode = 24,
            forceUpdate = false
        )

        val result = evaluate(manifest, currentVersionCode = 23)
        assertTrue(result is UpdateState.UpdateAvailable)
        val updateAvailable = result as UpdateState.UpdateAvailable
        assertTrue(updateAvailable.isMandatory)
    }

    @Test
    fun testMandatoryUpdateWhenForceUpdateTrue() {
        val manifest = UpdateManifest(
            versionCode = 24,
            versionName = "v24",
            apkUrl = "https://example.com/gamehub-v24.apk",
            minSupportedVersionCode = 20,
            forceUpdate = true
        )

        val result = evaluate(manifest, currentVersionCode = 23)
        assertTrue(result is UpdateState.UpdateAvailable)
        val updateAvailable = result as UpdateState.UpdateAvailable
        assertTrue(updateAvailable.isMandatory)
    }
}
