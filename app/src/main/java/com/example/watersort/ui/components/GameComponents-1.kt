package com.example.watersort.ui.components

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

enum class GameButtonColor(
    val topGradient: Color,
    val bottomGradient: Color,
    val shadowColor: Color,
    val textColor: Color = Color.White
) {
    GREEN(
        topGradient = Color(0xFF10B981),
        bottomGradient = Color(0xFF047857),
        shadowColor = Color(0xFF064E3B)
    ),
    GOLD(
        topGradient = Color(0xFFFBBF24),
        bottomGradient = Color(0xFFD97706),
        shadowColor = Color(0xFF92400E),
        textColor = Color(0xFF451A03)
    ),
    BLUE(
        topGradient = Color(0xFF38BDF8),
        bottomGradient = Color(0xFF0284C7),
        shadowColor = Color(0xFF0369A1)
    ),
    PURPLE(
        topGradient = Color(0xFFA855F7),
        bottomGradient = Color(0xFF7E22CE),
        shadowColor = Color(0xFF581C87)
    ),
    RED(
        topGradient = Color(0xFFF43F5E),
        bottomGradient = Color(0xFFBE123C),
        shadowColor = Color(0xFF881337)
    ),
    DARK(
        topGradient = Color(0xFF334155),
        bottomGradient = Color(0xFF1E293B),
        shadowColor = Color(0xFF0F172A)
    )
}

@Composable
fun TactileGameButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    icon: ImageVector? = null,
    iconEmoji: String? = null,
    color: GameButtonColor = GameButtonColor.GREEN,
    height: Dp = 56.dp,
    enabled: Boolean = true,
    testTag: String = ""
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()
    val pressOffset by animateFloatAsState(
        targetValue = if (isPressed && enabled) 4f else 0f,
        animationSpec = spring(stiffness = 800f),
        label = "press_offset"
    )

    val shadowDepth = 5.dp
    val shape = RoundedCornerShape(18.dp)

    Box(
        modifier = modifier
            .testTag(testTag)
            .height(height + shadowDepth)
            .clickable(
                interactionSource = interactionSource,
                indication = null,
                enabled = enabled,
                onClick = onClick
            ),
        contentAlignment = Alignment.TopCenter
    ) {
        // Bottom 3D shadow block
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(height)
                .offset(y = shadowDepth)
                .clip(shape)
                .background(if (enabled) color.shadowColor else Color(0xFF1E293B))
        )

        // Top button face
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(height)
                .offset(y = pressOffset.dp)
                .clip(shape)
                .background(
                    if (enabled) {
                        Brush.verticalGradient(
                            listOf(color.topGradient, color.bottomGradient)
                        )
                    } else {
                        Brush.verticalGradient(
                            listOf(Color(0xFF475569), Color(0xFF334155))
                        )
                    }
                )
                .border(
                    width = 1.dp,
                    brush = Brush.verticalGradient(
                        listOf(Color.White.copy(alpha = 0.35f), Color.Transparent)
                    ),
                    shape = shape
                ),
            contentAlignment = Alignment.Center
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center,
                modifier = Modifier.padding(horizontal = 20.dp)
            ) {
                if (iconEmoji != null) {
                    Text(text = iconEmoji, fontSize = 20.sp)
                    Spacer(modifier = Modifier.width(8.dp))
                } else if (icon != null) {
                    Icon(
                        imageVector = icon,
                        contentDescription = null,
                        tint = color.textColor,
                        modifier = Modifier.size(24.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                }

                Text(
                    text = text,
                    color = if (enabled) color.textColor else Color(0xFF94A3B8),
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Black,
                    letterSpacing = 0.5.sp
                )
            }
        }
    }
}

@Composable
fun TactileIconButton(
    icon: ImageVector,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    label: String? = null,
    badgeText: String? = null,
    color: GameButtonColor = GameButtonColor.DARK,
    enabled: Boolean = true,
    size: Dp = 56.dp,
    testTag: String = ""
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()
    val pressOffset by animateFloatAsState(
        targetValue = if (isPressed && enabled) 3f else 0f,
        animationSpec = spring(stiffness = 800f),
        label = "press_offset"
    )

    val shadowDepth = 4.dp
    val shape = RoundedCornerShape(16.dp)

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = modifier
    ) {
        Box(
            modifier = Modifier
                .testTag(testTag)
                .size(size)
                .clickable(
                    interactionSource = interactionSource,
                    indication = null,
                    enabled = enabled,
                    onClick = onClick
                ),
            contentAlignment = Alignment.TopCenter
        ) {
            // Shadow lip
            Box(
                modifier = Modifier
                    .size(size)
                    .offset(y = shadowDepth)
                    .clip(shape)
                    .background(if (enabled) color.shadowColor else Color(0xFF0F172A))
            )

            // Button body
            Box(
                modifier = Modifier
                    .size(size)
                    .offset(y = pressOffset.dp)
                    .clip(shape)
                    .background(
                        if (enabled) {
                            Brush.verticalGradient(
                                listOf(color.topGradient, color.bottomGradient)
                            )
                        } else {
                            Brush.verticalGradient(
                                listOf(Color(0xFF334155), Color(0xFF1E293B))
                            )
                        }
                    )
                    .border(
                        width = 1.dp,
                        color = Color.White.copy(alpha = if (enabled) 0.25f else 0.1f),
                        shape = shape
                    ),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = label,
                    tint = if (enabled) color.textColor else Color(0xFF64748B),
                    modifier = Modifier.size(size * 0.48f)
                )

                if (badgeText != null) {
                    Box(
                        modifier = Modifier
                            .align(Alignment.TopEnd)
                            .offset(x = 4.dp, y = (-4).dp)
                            .background(Color(0xFFEF4444), CircleShape)
                            .border(1.5.dp, Color(0xFF0F172A), CircleShape)
                            .padding(horizontal = 5.dp, vertical = 1.dp)
                    ) {
                        Text(
                            text = badgeText,
                            color = Color.White,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }

        if (label != null) {
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = label,
                color = if (enabled) Color(0xFFCBD5E1) else Color(0xFF64748B),
                fontSize = 11.sp,
                fontWeight = FontWeight.SemiBold
            )
        }
    }
}

@Composable
fun GameCoinPill(
    coins: Int,
    modifier: Modifier = Modifier,
    onClick: (() -> Unit)? = null
) {
    val shape = RoundedCornerShape(24.dp)
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = modifier
            .clip(shape)
            .background(
                Brush.horizontalGradient(
                    listOf(Color(0x33FBBF24), Color(0x22D97706))
                )
            )
            .border(1.dp, Color(0x66FBBF24), shape)
            .then(if (onClick != null) Modifier.clickable { onClick() } else Modifier)
            .padding(horizontal = 12.dp, vertical = 6.dp)
    ) {
        Text(text = "🪙", fontSize = 17.sp)
        Spacer(modifier = Modifier.width(6.dp))
        Text(
            text = "$coins",
            color = Color(0xFFFDE047),
            fontSize = 15.sp,
            fontWeight = FontWeight.Black
        )
    }
}

@Composable
fun GameSurfaceCard(
    modifier: Modifier = Modifier,
    borderColor: Color = Color(0x33FFFFFF),
    backgroundColor: Color = Color(0xCC1E293B),
    content: @Composable () -> Unit
) {
    val shape = RoundedCornerShape(22.dp)
    Box(
        modifier = modifier
            .clip(shape)
            .background(backgroundColor)
            .border(1.dp, borderColor, shape)
            .padding(16.dp)
    ) {
        content()
    }
}

@Composable
fun TactileGameCard(
    modifier: Modifier = Modifier,
    surfaceColor: Color = Color(0xFF161F30),
    borderColor: Color = Color(0x33475569),
    shadowColor: Color = Color(0xFF0C121E),
    content: @Composable () -> Unit
) {
    val shape = RoundedCornerShape(22.dp)
    Box(
        modifier = modifier
    ) {
        // Shadow depth
        Box(
            modifier = Modifier
                .matchParentSize()
                .offset(y = 4.dp)
                .clip(shape)
                .background(shadowColor)
        )
        // Card Surface
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .clip(shape)
                .background(surfaceColor)
                .border(1.dp, borderColor, shape)
        ) {
            content()
        }
    }
}
