package com.example.ads

import android.app.Activity
import android.widget.Toast
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CardGiftcard
import androidx.compose.material.icons.filled.PlayCircle
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.R

/**
 * Tactical Rewarded Ad Button for in-game rewards.
 *
 * Guarantees:
 * - Anti-Exploit: Reward is applied only upon full completion of video.
 * - Single callback firing: Multiple taps or rapid-clicks are safely ignored.
 * - Non-blocking: If ad is not ready, gracefully notifies without interrupting gameplay.
 */
@Composable
fun ZubaLubaRewardedButton(
    rewardDescription: String,
    onRewardEarned: (amount: Int, type: String) -> Unit,
    modifier: Modifier = Modifier,
    containerColor: Color = Color(0xFFD97706),
    contentColor: Color = Color.White,
    onAdUnavailable: () -> Unit = {}
) {
    val context = LocalContext.current
    val activity = context as? Activity
    var isProcessing by remember { mutableStateOf(false) }

    Button(
        onClick = {
            if (isProcessing || AdManager.isRewardedInProgress) return@Button
            isProcessing = true

            if (!AdManager.isRewardedAvailable) {
                isProcessing = false
                Toast.makeText(
                    context,
                    context.getString(R.string.ad_not_available),
                    Toast.LENGTH_SHORT
                ).show()
                onAdUnavailable()
                AdManager.loadRewarded(context)
                return@Button
            }

            AdManager.showRewarded(
                activity = activity,
                onUserEarnedReward = { amount, type ->
                    onRewardEarned(amount, type)
                    Toast.makeText(
                        context,
                        context.getString(R.string.reward_earned),
                        Toast.LENGTH_SHORT
                    ).show()
                },
                onDismissed = {
                    isProcessing = false
                }
            )
        },
        enabled = !isProcessing,
        shape = RoundedCornerShape(16.dp),
        colors = ButtonDefaults.buttonColors(
            containerColor = containerColor,
            contentColor = contentColor,
            disabledContainerColor = containerColor.copy(alpha = 0.5f),
            disabledContentColor = contentColor.copy(alpha = 0.7f)
        ),
        modifier = modifier
            .height(48.dp)
            .testTag("rewarded_ad_button")
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.padding(horizontal = 4.dp)
        ) {
            if (isProcessing) {
                CircularProgressIndicator(
                    modifier = Modifier.size(20.dp),
                    color = contentColor,
                    strokeWidth = 2.dp
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = stringResource(R.string.ad_loading),
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold
                )
            } else {
                Icon(
                    imageVector = Icons.Default.CardGiftcard,
                    contentDescription = null,
                    modifier = Modifier.size(20.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = stringResource(R.string.watch_ad_reward, rewardDescription),
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}
