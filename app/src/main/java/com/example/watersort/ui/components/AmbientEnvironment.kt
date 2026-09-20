package com.example.watersort.ui.components

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.DrawScope
import com.example.watersort.core.model.WorldEnvType
import kotlin.math.sin
import kotlin.random.Random

private data class Particle(
    val initialXRatio: Float,
    val initialYRatio: Float,
    val speed: Float,
    val size: Float,
    val alpha: Float,
    val swayAmount: Float,
    val swayFreq: Float,
    val phase: Float
)

@Composable
fun AmbientEnvironment(
    envType: WorldEnvType,
    modifier: Modifier = Modifier,
    enabled: Boolean = true
) {
    if (!enabled) return

    val transition = rememberInfiniteTransition(label = "ambient_anim")
    val progress by transition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 8000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "ambient_progress"
    )

    // Precompute 18 particles deterministically
    val particles = remember(envType) {
        val rng = Random(envType.ordinal * 1337 + 42)
        List(18) {
            Particle(
                initialXRatio = rng.nextFloat(),
                initialYRatio = rng.nextFloat(),
                speed = 0.4f + rng.nextFloat() * 0.8f,
                size = 3f + rng.nextFloat() * 6f,
                alpha = 0.2f + rng.nextFloat() * 0.45f,
                swayAmount = 15f + rng.nextFloat() * 25f,
                swayFreq = 1f + rng.nextFloat() * 2f,
                phase = rng.nextFloat() * 6.28f
            )
        }
    }

    Canvas(modifier = modifier.fillMaxSize()) {
        val w = size.width
        val h = size.height
        if (w <= 0f || h <= 0f) return@Canvas

        when (envType) {
            WorldEnvType.GARDEN -> drawGardenParticles(particles, progress, w, h)
            WorldEnvType.OCEAN -> drawOceanBubbles(particles, progress, w, h)
            WorldEnvType.EMBER -> drawEmberSparks(particles, progress, w, h)
            WorldEnvType.COSMIC -> drawCosmicDust(particles, progress, w, h)
            WorldEnvType.FROZEN -> drawFrozenFlakes(particles, progress, w, h)
            WorldEnvType.CAVERNS -> drawCavernGleams(particles, progress, w, h)
            WorldEnvType.SHADOW -> drawShadowWisps(particles, progress, w, h)
            WorldEnvType.MYSTIC -> drawMysticStardust(particles, progress, w, h)
        }
    }
}

private fun DrawScope.drawGardenParticles(particles: List<Particle>, progress: Float, w: Float, h: Float) {
    particles.forEach { p ->
        // Drift downwards and sway
        val currentY = ((p.initialYRatio + progress * p.speed * 0.4f) % 1f) * h
        val sway = sin(progress * 6.28f * p.swayFreq + p.phase) * p.swayAmount
        val currentX = ((p.initialXRatio * w) + sway).mod(w)

        // Leaf / spore shape
        drawCircle(
            color = Color(0xFF6EE7B7).copy(alpha = p.alpha * 0.7f),
            radius = p.size,
            center = Offset(currentX, currentY)
        )
        drawCircle(
            color = Color(0xFFA7F3D0).copy(alpha = p.alpha * 0.4f),
            radius = p.size * 1.6f,
            center = Offset(currentX, currentY)
        )
    }
}

private fun DrawScope.drawOceanBubbles(particles: List<Particle>, progress: Float, w: Float, h: Float) {
    particles.forEach { p ->
        // Rise upwards and sway
        val currentY = ((p.initialYRatio - progress * p.speed * 0.5f).mod(1f)) * h
        val sway = sin(progress * 6.28f * p.swayFreq + p.phase) * (p.swayAmount * 0.8f)
        val currentX = ((p.initialXRatio * w) + sway).mod(w)

        // Bubble outline + highlight
        val r = p.size * 1.3f
        drawCircle(
            color = Color(0xFF38BDF8).copy(alpha = p.alpha * 0.4f),
            radius = r,
            center = Offset(currentX, currentY),
            style = androidx.compose.ui.graphics.drawscope.Stroke(width = 1.5f)
        )
        drawCircle(
            color = Color.White.copy(alpha = p.alpha * 0.6f),
            radius = r * 0.35f,
            center = Offset(currentX - r * 0.3f, currentY - r * 0.3f)
        )
    }
}

private fun DrawScope.drawEmberSparks(particles: List<Particle>, progress: Float, w: Float, h: Float) {
    particles.forEach { p ->
        // Rise upwards rapidly with flickering
        val currentY = ((p.initialYRatio - progress * p.speed * 0.9f).mod(1f)) * h
        val sway = sin(progress * 6.28f * p.swayFreq + p.phase) * (p.swayAmount * 0.6f)
        val currentX = ((p.initialXRatio * w) + sway).mod(w)
        val flicker = (sin(progress * 15f + p.phase) * 0.3f + 0.7f).coerceIn(0f, 1f)

        drawCircle(
            color = Color(0xFFF97316).copy(alpha = p.alpha * flicker),
            radius = p.size * 0.9f,
            center = Offset(currentX, currentY)
        )
        drawCircle(
            color = Color(0xFFFEF08A).copy(alpha = p.alpha * 0.9f * flicker),
            radius = p.size * 0.4f,
            center = Offset(currentX, currentY)
        )
    }
}

private fun DrawScope.drawCosmicDust(particles: List<Particle>, progress: Float, w: Float, h: Float) {
    particles.forEach { p ->
        val currentY = p.initialYRatio * h
        val currentX = p.initialXRatio * w
        val twinkle = (sin(progress * 6.28f * p.speed * 2f + p.phase) * 0.5f + 0.5f)

        drawCircle(
            color = Color(0xFFC084FC).copy(alpha = p.alpha * twinkle * 0.8f),
            radius = p.size * 1.5f,
            center = Offset(currentX, currentY)
        )
        drawCircle(
            color = Color.White.copy(alpha = twinkle * 0.9f),
            radius = p.size * 0.6f,
            center = Offset(currentX, currentY)
        )
    }
}

private fun DrawScope.drawFrozenFlakes(particles: List<Particle>, progress: Float, w: Float, h: Float) {
    particles.forEach { p ->
        // Fall slowly downwards
        val currentY = ((p.initialYRatio + progress * p.speed * 0.35f) % 1f) * h
        val sway = sin(progress * 6.28f * p.swayFreq + p.phase) * p.swayAmount
        val currentX = ((p.initialXRatio * w) + sway).mod(w)

        drawCircle(
            color = Color(0xFFBAE6FD).copy(alpha = p.alpha * 0.8f),
            radius = p.size * 0.8f,
            center = Offset(currentX, currentY)
        )
        drawCircle(
            color = Color.White.copy(alpha = p.alpha * 0.9f),
            radius = p.size * 0.4f,
            center = Offset(currentX, currentY)
        )
    }
}

private fun DrawScope.drawCavernGleams(particles: List<Particle>, progress: Float, w: Float, h: Float) {
    particles.forEach { p ->
        val currentY = p.initialYRatio * h
        val currentX = p.initialXRatio * w
        val shimmer = (sin(progress * 8f * p.speed + p.phase) * 0.5f + 0.5f)

        drawCircle(
            color = Color(0xFFD8B4FE).copy(alpha = p.alpha * shimmer * 0.7f),
            radius = p.size * 1.2f,
            center = Offset(currentX, currentY)
        )
    }
}

private fun DrawScope.drawShadowWisps(particles: List<Particle>, progress: Float, w: Float, h: Float) {
    particles.forEach { p ->
        val currentY = ((p.initialYRatio - progress * p.speed * 0.25f).mod(1f)) * h
        val sway = sin(progress * 4f * p.swayFreq + p.phase) * (p.swayAmount * 1.2f)
        val currentX = ((p.initialXRatio * w) + sway).mod(w)

        drawCircle(
            color = Color(0xFF94A3B8).copy(alpha = p.alpha * 0.35f),
            radius = p.size * 2f,
            center = Offset(currentX, currentY)
        )
    }
}

private fun DrawScope.drawMysticStardust(particles: List<Particle>, progress: Float, w: Float, h: Float) {
    particles.forEach { p ->
        val currentY = ((p.initialYRatio - progress * p.speed * 0.3f).mod(1f)) * h
        val currentX = ((p.initialXRatio * w) + sin(progress * 6.28f + p.phase) * p.swayAmount).mod(w)
        val glow = (sin(progress * 10f * p.speed + p.phase) * 0.4f + 0.6f)

        drawCircle(
            color = Color(0xFFFDE047).copy(alpha = p.alpha * glow * 0.8f),
            radius = p.size * 1.2f,
            center = Offset(currentX, currentY)
        )
        drawCircle(
            color = Color.White.copy(alpha = p.alpha * glow),
            radius = p.size * 0.5f,
            center = Offset(currentX, currentY)
        )
    }
}
