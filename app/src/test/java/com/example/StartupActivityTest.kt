package com.example

import androidx.compose.ui.test.hasTestTag
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performScrollToNode
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34])
class StartupActivityTest {

    @get:Rule
    val composeTestRule = createAndroidComposeRule<MainActivity>()

    @Test
    fun testMainActivityStartupAndCompose() {
        composeTestRule.waitForIdle()
    }

    @Test
    fun testLaunchHappyGlassGameDoesNotCrash() {
        composeTestRule.waitForIdle()
        try {
            composeTestRule.onNodeWithTag("splash_screen").performClick()
            composeTestRule.waitForIdle()
        } catch (_: Throwable) {}

        // Scroll the games list to the Happy Glass play button
        composeTestRule.onNodeWithTag("games_list")
            .performScrollToNode(hasTestTag("play_btn_happy_glass"))
        composeTestRule.waitForIdle()
        composeTestRule.onNodeWithTag("play_btn_happy_glass").performClick()
        composeTestRule.waitForIdle()
    }
}
