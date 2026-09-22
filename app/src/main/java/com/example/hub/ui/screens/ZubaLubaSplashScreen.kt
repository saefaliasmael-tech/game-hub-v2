package com.example.hub.ui.screens

import android.provider.Settings
import androidx.compose.animation.core.*
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.R
import com.example.hub.ui.theme.HubColors
import kotlinx.coroutines.delay

@Composable
fun ZubaLubaSplashScreen(
    onSplashFinished: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current

    // Check system animator duration scale for Reduce Motion
    val isReduceMotion = remember {
        try {
            val scale = Settings.Global.getFloat(
                context.contentResolver,
                Settings.Global.ANIMATOR_DURATION_SCALE,
                1.0f
            )
            scale == 0f
        } catch (_: Exception) {
            false
        }
    }

    var startAnimation by remember { mutableStateOf(false) }

    val alphaAnim by animateFloatAsState(
        targetValue = if (startAnimation || isReduceMotion) 1f else 0f,
        animationSpec = tween(
            durationMillis = if (isReduceMotion) 0 else 600,
            easing = FastOutSlowInEasing
        ),
        label = "splashAlpha"
    )

    val scaleAnim by animateFloatAsState(
        targetValue = if (startAnimation || isReduceMotion) 1f else 0.85f,
        animationSpec = tween(
            durationMillis = if (isReduceMotion) 0 else 700,
            easing = OvershootInterpolator()
        ),
        label = "splashScale"
    )

    LaunchedEffect(Unit) {
        startAnimation = true
        // Keep splash pleasant and quick (approx 1200ms)
        delay(if (isReduceMotion) 500L else 1300L)
        onSplashFinished()
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(HubColors.Void)
            .testTag("splash_screen"),
        contentAlignment = Alignment.Center
    ) {
        // Ambient radial background glow
        Box(
            modifier = Modifier
                .size(340.dp)
                .scale(scaleAnim)
                .alpha(alphaAnim * 0.45f)
                .background(
                    Brush.radialGradient(
                        colors = listOf(
                            HubColors.PrimaryViolet.copy(alpha = 0.5f),
                            HubColors.Cyan.copy(alpha = 0.2f),
                            Color.Transparent
                        )
                    ),
                    shape = CircleShape
                )
        )

        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
            modifier = Modifier
                .alpha(alphaAnim)
                .scale(scaleAnim)
                .padding(24.dp)
        ) {
            // 3D Portal Symbol
            Box(
                modifier = Modifier
                    .size(130.dp)
                    .clip(RoundedCornerShape(32.dp))
                    .background(HubColors.SurfaceMid)
                    .testTag("splash_logo"),
                contentAlignment = Alignment.Center
            ) {
                Image(
                    painter = painterResource(id = R.drawable.img_zuba_luba_icon),
                    contentDescription = "Zuba Luba Portal",
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.Crop
                )
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Brand Title: Zuba Luba
            Text(
                text = stringResource(R.string.game_hub_title),
                style = MaterialTheme.typography.headlineLarge.copy(
                    fontWeight = FontWeight.Black,
                    color = HubColors.HighText,
                    letterSpacing = 1.5.sp,
                    fontSize = 34.sp
                ),
                textAlign = TextAlign.Center,
                modifier = Modifier.testTag("splash_title")
            )

            Spacer(modifier = Modifier.height(8.dp))

            // Subtitle: Portal to a Universe of Games
            Text(
                text = stringResource(R.string.game_hub_subtitle),
                style = MaterialTheme.typography.bodyMedium.copy(
                    color = HubColors.Cyan,
                    fontWeight = FontWeight.Medium,
                    letterSpacing = 0.5.sp,
                    fontSize = 14.sp
                ),
                textAlign = TextAlign.Center
            )
        }
    }
}

private fun OvershootInterpolator(): Easing {
    return Easing { fraction ->
        val tension = 1.2f
        val t = fraction - 1.0f
        t * t * ((tension + 1) * t + tension) + 1.0f
    }
}
