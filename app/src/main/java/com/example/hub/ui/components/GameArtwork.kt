package com.example.hub.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Fill
import androidx.compose.ui.graphics.drawscope.Stroke
import com.example.hub.registry.GameRegistry
import kotlin.math.cos
import kotlin.math.sin

/**
 * High-fidelity, vector-based original game artwork for each of the 19 standalone games.
 * Pure Compose Canvas graphics: zero external dependencies, no mock logos, perfectly scalable.
 */
@Composable
fun GameArtwork(
    gameId: String,
    modifier: Modifier = Modifier
) {
    Box(modifier = modifier) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            when (gameId) {
                GameRegistry.WATER_SORT_ID -> drawWaterSortArt(this)
                GameRegistry.COLOR_SEQUENCE_ID -> drawColorSequenceArt(this)
                GameRegistry.PUZZLE_2048_ID -> drawGame2048Art(this)
                GameRegistry.MASTERMIND_ID -> drawMastermindArt(this)
                GameRegistry.UNBLOCK_ME_ID -> drawUnblockMeArt(this)
                GameRegistry.MEMORY_CARDS_ID -> drawMemoryCardsArt(this)
                GameRegistry.STOP_THE_TIME_ID -> drawStopTheTimeArt(this)
                GameRegistry.COLOR_SWITCH_ID -> drawColorSwitchArt(this)
                GameRegistry.KNIFE_HIT_ID -> drawKnifeHitArt(this)
                GameRegistry.AA_ID -> drawAAArt(this)
                GameRegistry.MINI_TD_ID -> drawMiniTDArt(this)
                GameRegistry.ROPE_AROUND_ID -> drawRopeAroundArt(this)
                GameRegistry.ROPE_RESCUE_ID -> drawRopeRescueArt(this)
                GameRegistry.PIC_PUZZLE_ID -> drawPicPuzzleArt(this)
                GameRegistry.PULL_THE_PIN_ID -> drawPullThePinArt(this)
                GameRegistry.HAPPY_GLASS_ID -> drawHappyGlassArt(this)
                GameRegistry.MR_BULLET_ID -> drawMrBulletArt(this)
                GameRegistry.PROTECT_SHEEP_ID -> drawProtectSheepArt(this)
                GameRegistry.FUN_FRENZY_ID -> drawFunFrenzyArt(this)
                else -> drawGenericPortalArt(this)
            }
        }
    }
}

// 1. Water Sort: Glass vials with stratified glowing liquids
private fun drawWaterSortArt(scope: DrawScope) {
    val w = scope.size.width
    val h = scope.size.height

    // Vial 1 (left)
    val vial1Left = w * 0.22f
    val vialWidth = w * 0.24f
    val vialHeight = h * 0.7f
    val vialTop = h * 0.18f

    // Layer 1 liquid (bottom: Cyan)
    scope.drawRoundRect(
        color = Color(0xFF00B4D8),
        topLeft = Offset(vial1Left, vialTop + vialHeight * 0.5f),
        size = Size(vialWidth, vialHeight * 0.5f),
        cornerRadius = CornerRadius(vialWidth * 0.4f, vialWidth * 0.4f)
    )
    // Layer 2 liquid (mid: Magenta)
    scope.drawRect(
        color = Color(0xFFFF4D8D),
        topLeft = Offset(vial1Left, vialTop + vialHeight * 0.25f),
        size = Size(vialWidth, vialHeight * 0.28f)
    )
    // Glass tube outline
    scope.drawRoundRect(
        color = Color.White.copy(alpha = 0.85f),
        topLeft = Offset(vial1Left, vialTop),
        size = Size(vialWidth, vialHeight),
        cornerRadius = CornerRadius(vialWidth * 0.45f, vialWidth * 0.45f),
        style = Stroke(width = w * 0.04f)
    )

    // Vial 2 (right, tilted/pouring stream)
    val vial2Left = w * 0.54f
    scope.drawRoundRect(
        color = Color(0xFF7C5CFF),
        topLeft = Offset(vial2Left, vialTop + vialHeight * 0.35f),
        size = Size(vialWidth, vialHeight * 0.65f),
        cornerRadius = CornerRadius(vialWidth * 0.4f, vialWidth * 0.4f)
    )
    scope.drawRoundRect(
        color = Color.White.copy(alpha = 0.85f),
        topLeft = Offset(vial2Left, vialTop),
        size = Size(vialWidth, vialHeight),
        cornerRadius = CornerRadius(vialWidth * 0.45f, vialWidth * 0.45f),
        style = Stroke(width = w * 0.04f)
    )

    // Liquid droplets
    scope.drawCircle(
        color = Color(0xFF34E3E0),
        radius = w * 0.045f,
        center = Offset(w * 0.5f, h * 0.32f)
    )
}

// 2. Color Sequence: 4 glowing quadrant memory pads
private fun drawColorSequenceArt(scope: DrawScope) {
    val cx = scope.size.width * 0.5f
    val cy = scope.size.height * 0.5f
    val r = scope.size.width * 0.38f
    val gap = scope.size.width * 0.04f

    // 4 Colored Sectors
    scope.drawArc(Color(0xFFFF4D8D), 185f, 80f, true, Offset(cx - r, cy - r), Size(r * 2, r * 2))
    scope.drawArc(Color(0xFF34E3E0), 275f, 80f, true, Offset(cx - r, cy - r), Size(r * 2, r * 2))
    scope.drawArc(Color(0xFFB6FF3C), 5f, 80f, true, Offset(cx - r, cy - r), Size(r * 2, r * 2))
    scope.drawArc(Color(0xFF7C5CFF), 95f, 80f, true, Offset(cx - r, cy - r), Size(r * 2, r * 2))

    // Center Console Hub
    scope.drawCircle(
        color = Color(0xFF14122A),
        radius = r * 0.42f,
        center = Offset(cx, cy)
    )
    scope.drawCircle(
        color = Color.White.copy(alpha = 0.9f),
        radius = r * 0.16f,
        center = Offset(cx, cy)
    )
}

// 3. 2048: Merging number grid blocks
private fun drawGame2048Art(scope: DrawScope) {
    val w = scope.size.width
    val h = scope.size.height
    val pad = w * 0.1f
    val tileSize = (w - pad * 3) / 2

    // Tile 1: Top-Left (2048 golden)
    scope.drawRoundRect(
        color = Color(0xFFEDC22E),
        topLeft = Offset(pad, pad),
        size = Size(tileSize, tileSize),
        cornerRadius = CornerRadius(w * 0.04f)
    )
    // Tile 2: Top-Right (512 coral)
    scope.drawRoundRect(
        color = Color(0xFFF67C5F),
        topLeft = Offset(pad * 2 + tileSize, pad),
        size = Size(tileSize, tileSize),
        cornerRadius = CornerRadius(w * 0.04f)
    )
    // Tile 3: Bottom-Left (128 orange)
    scope.drawRoundRect(
        color = Color(0xFFE9C46A),
        topLeft = Offset(pad, pad * 2 + tileSize),
        size = Size(tileSize, tileSize),
        cornerRadius = CornerRadius(w * 0.04f)
    )
    // Tile 4: Bottom-Right (64 tangerine)
    scope.drawRoundRect(
        color = Color(0xFFF4A261),
        topLeft = Offset(pad * 2 + tileSize, pad * 2 + tileSize),
        size = Size(tileSize, tileSize),
        cornerRadius = CornerRadius(w * 0.04f)
    )
}

// 4. Mastermind: Secret code pegs & deduction markers
private fun drawMastermindArt(scope: DrawScope) {
    val w = scope.size.width
    val h = scope.size.height

    // Peg Board Surface
    scope.drawRoundRect(
        color = Color(0xFF272253),
        topLeft = Offset(w * 0.12f, h * 0.15f),
        size = Size(w * 0.76f, h * 0.7f),
        cornerRadius = CornerRadius(w * 0.06f)
    )

    val colors = listOf(Color(0xFFFF4D8D), Color(0xFF34E3E0), Color(0xFFB6FF3C), Color(0xFF7C5CFF))
    val pegRadius = w * 0.07f
    val startX = w * 0.28f
    val stepX = w * 0.15f
    val rowY = h * 0.42f

    colors.forEachIndexed { i, c ->
        scope.drawCircle(
            color = c,
            radius = pegRadius,
            center = Offset(startX + i * stepX, rowY)
        )
    }

    // Feedback key pegs (black & white dots)
    scope.drawCircle(Color.White, pegRadius * 0.4f, Offset(w * 0.35f, h * 0.68f))
    scope.drawCircle(Color.White, pegRadius * 0.4f, Offset(w * 0.47f, h * 0.68f))
    scope.drawCircle(Color(0xFF0A0918), pegRadius * 0.4f, Offset(w * 0.59f, h * 0.68f))
}

// 5. Unblock Me: Sliding puzzle obstacle blocks with red escape block
private fun drawUnblockMeArt(scope: DrawScope) {
    val w = scope.size.width
    val h = scope.size.height

    // Grid Container
    scope.drawRoundRect(
        color = Color(0xFF14122A),
        topLeft = Offset(w * 0.1f, h * 0.1f),
        size = Size(w * 0.8f, h * 0.8f),
        cornerRadius = CornerRadius(w * 0.05f),
        style = Stroke(width = w * 0.03f)
    )

    // Vertical Obstacle Blocks (Wood tone)
    scope.drawRoundRect(
        color = Color(0xFFD4A373),
        topLeft = Offset(w * 0.16f, h * 0.18f),
        size = Size(w * 0.18f, h * 0.45f),
        cornerRadius = CornerRadius(w * 0.03f)
    )
    scope.drawRoundRect(
        color = Color(0xFFBC6C25),
        topLeft = Offset(w * 0.66f, h * 0.38f),
        size = Size(w * 0.18f, h * 0.45f),
        cornerRadius = CornerRadius(w * 0.03f)
    )

    // The Red Escape Block!
    scope.drawRoundRect(
        color = Color(0xFFFF3366),
        topLeft = Offset(w * 0.28f, h * 0.42f),
        size = Size(w * 0.42f, h * 0.18f),
        cornerRadius = CornerRadius(w * 0.03f)
    )
}

// 6. Memory Cards: Flipped cards showing glowing glyphs
private fun drawMemoryCardsArt(scope: DrawScope) {
    val w = scope.size.width
    val h = scope.size.height

    // Card 1
    scope.drawRoundRect(
        color = Color(0xFF7C5CFF),
        topLeft = Offset(w * 0.15f, h * 0.18f),
        size = Size(w * 0.32f, h * 0.64f),
        cornerRadius = CornerRadius(w * 0.04f)
    )
    // Star glyph on Card 1
    scope.drawCircle(
        color = Color(0xFF34E3E0),
        radius = w * 0.07f,
        center = Offset(w * 0.31f, h * 0.5f)
    )

    // Card 2
    scope.drawRoundRect(
        color = Color(0xFF272253),
        topLeft = Offset(w * 0.53f, h * 0.18f),
        size = Size(w * 0.32f, h * 0.64f),
        cornerRadius = CornerRadius(w * 0.04f)
    )
    scope.drawCircle(
        color = Color(0xFFFF4D8D),
        radius = w * 0.07f,
        center = Offset(w * 0.69f, h * 0.5f)
    )
}

// 7. Stop the Time: Precision chronometer dial
private fun drawStopTheTimeArt(scope: DrawScope) {
    val cx = scope.size.width * 0.5f
    val cy = scope.size.height * 0.5f
    val r = scope.size.width * 0.36f

    // Outer Chronometer Ring
    scope.drawCircle(
        color = Color(0xFF272253),
        radius = r,
        center = Offset(cx, cy),
        style = Stroke(width = scope.size.width * 0.05f)
    )

    // Green Sweet Spot Sector
    scope.drawArc(
        color = Color(0xFFB6FF3C),
        startAngle = 290f,
        sweepAngle = 45f,
        useCenter = false,
        topLeft = Offset(cx - r, cy - r),
        size = Size(r * 2, r * 2),
        style = Stroke(width = scope.size.width * 0.07f, cap = StrokeCap.Round)
    )

    // Needle Hand locked on target
    val angleRad = Math.toRadians(310.0)
    val handX = cx + (r * 0.8f * cos(angleRad)).toFloat()
    val handY = cy + (r * 0.8f * sin(angleRad)).toFloat()
    scope.drawLine(
        color = Color(0xFF34E3E0),
        start = Offset(cx, cy),
        end = Offset(handX, handY),
        strokeWidth = scope.size.width * 0.045f,
        cap = StrokeCap.Round
    )

    scope.drawCircle(Color.White, r * 0.16f, Offset(cx, cy))
}

// 8. Color Switch: Concentric segmented spinning obstacle ring
private fun drawColorSwitchArt(scope: DrawScope) {
    val cx = scope.size.width * 0.5f
    val cy = scope.size.height * 0.5f
    val r = scope.size.width * 0.35f
    val stroke = scope.size.width * 0.08f

    // 4 Arc Colors of the obstacle ring
    scope.drawArc(Color(0xFFFF4D8D), 0f, 85f, false, Offset(cx - r, cy - r), Size(r * 2, r * 2), style = Stroke(stroke, cap = StrokeCap.Round))
    scope.drawArc(Color(0xFF34E3E0), 90f, 85f, false, Offset(cx - r, cy - r), Size(r * 2, r * 2), style = Stroke(stroke, cap = StrokeCap.Round))
    scope.drawArc(Color(0xFFB6FF3C), 180f, 85f, false, Offset(cx - r, cy - r), Size(r * 2, r * 2), style = Stroke(stroke, cap = StrokeCap.Round))
    scope.drawArc(Color(0xFF7C5CFF), 270f, 85f, false, Offset(cx - r, cy - r), Size(r * 2, r * 2), style = Stroke(stroke, cap = StrokeCap.Round))

    // Matching jumping orb passing through center
    scope.drawCircle(
        color = Color(0xFF34E3E0),
        radius = scope.size.width * 0.08f,
        center = Offset(cx, cy)
    )
}

// 9. Knife Hit: Rotating target log with blades
private fun drawKnifeHitArt(scope: DrawScope) {
    val cx = scope.size.width * 0.5f
    val cy = scope.size.height * 0.42f
    val r = scope.size.width * 0.28f

    // Target Log
    scope.drawCircle(
        color = Color(0xFFD4A373),
        radius = r,
        center = Offset(cx, cy)
    )
    scope.drawCircle(
        color = Color(0xFF8B5E3C),
        radius = r * 0.6f,
        center = Offset(cx, cy)
    )

    // Knives embedded in log (left and top)
    scope.drawLine(
        color = Color(0xFFE2E8F0),
        start = Offset(cx - r - scope.size.width * 0.12f, cy),
        end = Offset(cx - r * 0.8f, cy),
        strokeWidth = scope.size.width * 0.04f,
        cap = StrokeCap.Round
    )

    // Approaching Knife (bottom)
    val knifeTopY = scope.size.height * 0.72f
    scope.drawLine(
        color = Color(0xFF34E3E0),
        start = Offset(cx, knifeTopY),
        end = Offset(cx, scope.size.height * 0.92f),
        strokeWidth = scope.size.width * 0.045f,
        cap = StrokeCap.Round
    )
}

// 10. AA: Central rotating core with radial pins
private fun drawAAArt(scope: DrawScope) {
    val cx = scope.size.width * 0.5f
    val cy = scope.size.height * 0.5f
    val r = scope.size.width * 0.22f

    // Radial Pins
    val angles = listOf(30.0, 90.0, 150.0, 210.0, 270.0, 330.0)
    angles.forEach { deg ->
        val rad = Math.toRadians(deg)
        val endX = cx + (scope.size.width * 0.4f * cos(rad)).toFloat()
        val endY = cy + (scope.size.width * 0.4f * sin(rad)).toFloat()
        scope.drawLine(
            color = Color.White.copy(alpha = 0.8f),
            start = Offset(cx, cy),
            end = Offset(endX, endY),
            strokeWidth = scope.size.width * 0.02f
        )
        scope.drawCircle(Color(0xFF34E3E0), scope.size.width * 0.035f, Offset(endX, endY))
    }

    // Main rotating core
    scope.drawCircle(
        color = Color(0xFF7C5CFF),
        radius = r,
        center = Offset(cx, cy)
    )
    scope.drawCircle(
        color = Color.White,
        radius = r * 0.4f,
        center = Offset(cx, cy)
    )
}

// 11. Mini Tower Defense: Futuristic defensive turret blasting laser
private fun drawMiniTDArt(scope: DrawScope) {
    val w = scope.size.width
    val h = scope.size.height

    // Grid platform
    scope.drawRoundRect(
        color = Color(0xFF272253),
        topLeft = Offset(w * 0.15f, h * 0.55f),
        size = Size(w * 0.7f, h * 0.3f),
        cornerRadius = CornerRadius(w * 0.04f)
    )

    // Turret Base
    scope.drawCircle(
        color = Color(0xFF34E3E0),
        radius = w * 0.18f,
        center = Offset(w * 0.35f, h * 0.52f)
    )

    // Cannon Barrel
    scope.drawLine(
        color = Color(0xFF34E3E0),
        start = Offset(w * 0.35f, h * 0.52f),
        end = Offset(w * 0.68f, h * 0.32f),
        strokeWidth = w * 0.07f,
        cap = StrokeCap.Round
    )

    // Plasma blast projectile
    scope.drawCircle(
        color = Color(0xFFFF4D8D),
        radius = w * 0.06f,
        center = Offset(w * 0.82f, h * 0.24f)
    )
}

// 12. Rope Around: Neon grid pegs wrapped by glowing cord
private fun drawRopeAroundArt(scope: DrawScope) {
    val w = scope.size.width
    val h = scope.size.height

    val p1 = Offset(w * 0.25f, h * 0.25f)
    val p2 = Offset(w * 0.75f, h * 0.3f)
    val p3 = Offset(w * 0.65f, h * 0.75f)
    val p4 = Offset(w * 0.25f, h * 0.7f)

    // Connecting Glowing Rope
    val ropePath = Path().apply {
        moveTo(p1.x, p1.y)
        lineTo(p2.x, p2.y)
        lineTo(p3.x, p3.y)
        lineTo(p4.x, p4.y)
        close()
    }
    scope.drawPath(
        path = ropePath,
        color = Color(0xFF34E3E0),
        style = Stroke(width = w * 0.05f, cap = StrokeCap.Round)
    )

    // Pegs
    listOf(p1, p2, p3, p4).forEach { pt ->
        scope.drawCircle(Color(0xFFFF4D8D), w * 0.07f, pt)
        scope.drawCircle(Color.White, w * 0.025f, pt)
    }
}

// 13. Rope Rescue: Zip-line cable with rescue capsule
private fun drawRopeRescueArt(scope: DrawScope) {
    val w = scope.size.width
    val h = scope.size.height

    val startPt = Offset(w * 0.15f, h * 0.25f)
    val endPt = Offset(w * 0.85f, h * 0.75f)

    // Zip Cable
    scope.drawLine(
        color = Color(0xFFB6FF3C),
        start = startPt,
        end = endPt,
        strokeWidth = w * 0.035f,
        cap = StrokeCap.Round
    )

    // Mid-air rescue trolley capsule
    val midX = w * 0.5f
    val midY = h * 0.5f
    scope.drawRoundRect(
        color = Color(0xFFFF4D8D),
        topLeft = Offset(midX - w * 0.09f, midY),
        size = Size(w * 0.18f, h * 0.2f),
        cornerRadius = CornerRadius(w * 0.03f)
    )
}

// 14. Pic Puzzle: Sliding tiles with empty slot
private fun drawPicPuzzleArt(scope: DrawScope) {
    val w = scope.size.width
    val h = scope.size.height
    val pad = w * 0.12f
    val size = (w - pad * 2 - w * 0.06f) / 2

    // 3 active tiles + 1 empty
    scope.drawRoundRect(Color(0xFF7C5CFF), Offset(pad, pad), Size(size, size), CornerRadius(w * 0.03f))
    scope.drawRoundRect(Color(0xFF34E3E0), Offset(pad + size + w * 0.06f, pad), Size(size, size), CornerRadius(w * 0.03f))
    scope.drawRoundRect(Color(0xFFFF4D8D), Offset(pad, pad + size + w * 0.06f), Size(size, size), CornerRadius(w * 0.03f))

    // Empty highlighted slot
    scope.drawRoundRect(
        Color.White.copy(alpha = 0.25f),
        Offset(pad + size + w * 0.06f, pad + size + w * 0.06f),
        Size(size, size),
        CornerRadius(w * 0.03f),
        style = Stroke(width = w * 0.02f)
    )
}

// 15. Pull the Pin: Golden pins holding candy spheres
private fun drawPullThePinArt(scope: DrawScope) {
    val w = scope.size.width
    val h = scope.size.height

    // Glass Funnel Chamber
    scope.drawRoundRect(
        color = Color(0xFF272253),
        topLeft = Offset(w * 0.2f, h * 0.15f),
        size = Size(w * 0.6f, h * 0.7f),
        cornerRadius = CornerRadius(w * 0.05f),
        style = Stroke(width = w * 0.03f)
    )

    // Golden Pin sliding across
    scope.drawLine(
        color = Color(0xFFFFD166),
        start = Offset(w * 0.1f, h * 0.48f),
        end = Offset(w * 0.75f, h * 0.48f),
        strokeWidth = w * 0.06f,
        cap = StrokeCap.Round
    )

    // Trapped colorful balls above the pin
    scope.drawCircle(Color(0xFFFF4D8D), w * 0.07f, Offset(w * 0.4f, h * 0.35f))
    scope.drawCircle(Color(0xFF34E3E0), w * 0.065f, Offset(w * 0.58f, h * 0.36f))
    scope.drawCircle(Color(0xFFB6FF3C), w * 0.06f, Offset(w * 0.5f, h * 0.24f))
}

// 16. Happy Glass: Smiling goblet receiving cascading water
private fun drawHappyGlassArt(scope: DrawScope) {
    val w = scope.size.width
    val h = scope.size.height

    val glassLeft = w * 0.28f
    val glassTop = h * 0.35f
    val glassW = w * 0.44f
    val glassH = h * 0.52f

    // Water level inside cup
    scope.drawRoundRect(
        color = Color(0xFF00B4D8),
        topLeft = Offset(glassLeft + w * 0.03f, glassTop + glassH * 0.4f),
        size = Size(glassW - w * 0.06f, glassH * 0.55f),
        cornerRadius = CornerRadius(w * 0.04f)
    )

    // Glass cup outline
    scope.drawRoundRect(
        color = Color.White.copy(alpha = 0.9f),
        topLeft = Offset(glassLeft, glassTop),
        size = Size(glassW, glassH),
        cornerRadius = CornerRadius(w * 0.05f),
        style = Stroke(width = w * 0.035f)
    )

    // Water stream pouring into cup
    scope.drawLine(
        color = Color(0xFF34E3E0),
        start = Offset(w * 0.5f, h * 0.1f),
        end = Offset(w * 0.5f, glassTop + glassH * 0.4f),
        strokeWidth = w * 0.04f,
        cap = StrokeCap.Round
    )
}

// 17. Mr Bullet: Tactical ricochet trajectory laser
private fun drawMrBulletArt(scope: DrawScope) {
    val w = scope.size.width
    val h = scope.size.height

    // Angled Ricochet Wall
    scope.drawLine(
        color = Color(0xFF7C5CFF),
        start = Offset(w * 0.7f, h * 0.15f),
        end = Offset(w * 0.85f, h * 0.45f),
        strokeWidth = w * 0.06f,
        cap = StrokeCap.Round
    )

    // Trajectory path (origin -> wall -> target)
    val trajectory = Path().apply {
        moveTo(w * 0.18f, h * 0.75f)
        lineTo(w * 0.77f, h * 0.3f)
        lineTo(w * 0.3f, h * 0.2f)
    }
    scope.drawPath(
        path = trajectory,
        color = Color(0xFFFF4D8D),
        style = Stroke(width = w * 0.035f, cap = StrokeCap.Round)
    )

    // Bullet sparks
    scope.drawCircle(Color(0xFFB6FF3C), w * 0.04f, Offset(w * 0.77f, h * 0.3f))
}

// 18. Protect Sheep: Wooden corral fence protecting sheep
private fun drawProtectSheepArt(scope: DrawScope) {
    val cx = scope.size.width * 0.5f
    val cy = scope.size.height * 0.5f
    val r = scope.size.width * 0.34f

    // Meadow circle
    scope.drawCircle(
        color = Color(0xFF2A9D8F),
        radius = r,
        center = Offset(cx, cy)
    )

    // Protective Fence Barrier
    scope.drawCircle(
        color = Color(0xFFE9C46A),
        radius = r * 0.85f,
        center = Offset(cx, cy),
        style = Stroke(width = scope.size.width * 0.04f)
    )

    // Cute fluffy sheep in center
    scope.drawCircle(Color.White, r * 0.38f, Offset(cx, cy))
    scope.drawCircle(Color.White, r * 0.22f, Offset(cx - r * 0.25f, cy - r * 0.08f))
    scope.drawCircle(Color(0xFF0A0918), r * 0.06f, Offset(cx - r * 0.3f, cy - r * 0.1f))
}

// 19. Fun Frenzy: Arcade party bubble bursts
private fun drawFunFrenzyArt(scope: DrawScope) {
    val cx = scope.size.width * 0.5f
    val cy = scope.size.height * 0.5f

    // Radiating arcade confetti bursts
    val colors = listOf(Color(0xFFFF4D8D), Color(0xFF34E3E0), Color(0xFFB6FF3C), Color(0xFF7C5CFF), Color(0xFFFFD166))
    colors.forEachIndexed { i, c ->
        val rad = Math.toRadians((i * 72.0))
        val x = cx + (scope.size.width * 0.32f * cos(rad)).toFloat()
        val y = cy + (scope.size.width * 0.32f * sin(rad)).toFloat()
        scope.drawCircle(c, scope.size.width * 0.07f, Offset(x, y))
    }

    // Central starburst
    scope.drawCircle(
        color = Color.White,
        radius = scope.size.width * 0.12f,
        center = Offset(cx, cy)
    )
}

private fun drawGenericPortalArt(scope: DrawScope) {
    val cx = scope.size.width * 0.5f
    val cy = scope.size.height * 0.5f
    scope.drawCircle(
        brush = Brush.radialGradient(
            colors = listOf(Color(0xFF34E3E0), Color(0xFF7C5CFF), Color(0xFF0A0918))
        ),
        radius = scope.size.width * 0.4f,
        center = Offset(cx, cy)
    )
}
