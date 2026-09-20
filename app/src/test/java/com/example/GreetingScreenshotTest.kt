package com.example

import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onRoot
import com.example.ui.theme.MyApplicationTheme
import com.example.watersort.core.model.Bottle
import com.example.watersort.core.model.LiquidColor
import com.example.watersort.ui.components.BottleView
import com.github.takahirom.roborazzi.RobolectricDeviceQualifiers
import com.github.takahirom.roborazzi.captureRoboImage
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import org.robolectric.annotation.GraphicsMode

@RunWith(RobolectricTestRunner::class)
@GraphicsMode(GraphicsMode.Mode.NATIVE)
@Config(qualifiers = RobolectricDeviceQualifiers.Pixel8, sdk = [36])
class GreetingScreenshotTest {

  @get:Rule val composeTestRule = createComposeRule()

  @Test
  fun bottle_screenshot() {
    val sampleBottle = Bottle(
        id = 0,
        capacity = 4,
        layers = listOf(LiquidColor.BLUE, LiquidColor.RED, LiquidColor.RED, LiquidColor.YELLOW)
    )
    composeTestRule.setContent {
        MyApplicationTheme {
            BottleView(
                bottle = sampleBottle,
                isSelected = false,
                onBottleClick = {}
            )
        }
    }

    composeTestRule.onRoot().captureRoboImage(filePath = "src/test/screenshots/bottle.png")
  }
}
