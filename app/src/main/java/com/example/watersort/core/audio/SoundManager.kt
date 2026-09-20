package com.example.watersort.core.audio

import android.content.Context
import android.media.AudioAttributes
import android.media.AudioFormat
import android.media.AudioTrack
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlin.math.sin

enum class GameSound {
    SELECT,
    POUR,
    INVALID,
    WIN,
    COIN,
    BUTTON,
    ACHIEVEMENT,
    COUNTDOWN,
    STOP,
    PERFECT,
    RESULT,
    JUMP,
    COLOR_CHANGE,
    PASS,
    HIT,
    GAME_OVER,
    KNIFE_THROW,
    KNIFE_HIT,
    APPLE,
    BOSS,
    LEVEL_COMPLETE
}

class SoundManager(
    private val context: Context,
    private val scope: CoroutineScope = CoroutineScope(Dispatchers.Default)
) {
    var soundEnabled: Boolean = true
    var musicEnabled: Boolean = true

    fun play(sound: GameSound) {
        if (!soundEnabled) return
        scope.launch {
            try {
                synthesizeSound(sound)
            } catch (_: Exception) {
                // Audio synthesis fallback
            }
        }
    }

    private fun synthesizeSound(sound: GameSound) {
        val sampleRate = 22050
        val (frequencies, durationSec) = when (sound) {
            GameSound.SELECT -> listOf(523.25f, 659.25f) to 0.08f // C5 -> E5 quick tap
            GameSound.POUR -> listOf(392.0f, 440.0f, 523.25f, 587.33f) to 0.18f // G4 -> A4 -> C5 -> D5 liquid trickle
            GameSound.INVALID -> listOf(220.0f, 185.0f) to 0.12f // low bump
            GameSound.WIN -> listOf(523.25f, 659.25f, 783.99f, 1046.50f) to 0.45f // C5 E5 G5 C6 triumphant arpeggio
            GameSound.COIN -> listOf(987.77f, 1318.51f) to 0.14f // B5 -> E6 bright coin chime
            GameSound.BUTTON -> listOf(440.0f) to 0.05f // soft click
            GameSound.ACHIEVEMENT -> listOf(440.0f, 554.37f, 659.25f, 880.0f) to 0.35f
            GameSound.COUNTDOWN -> listOf(440.0f) to 0.08f // short beep
            GameSound.STOP -> listOf(659.25f, 523.25f) to 0.10f // crisp chime
            GameSound.PERFECT -> listOf(523.25f, 659.25f, 783.99f, 1046.50f, 1318.51f) to 0.35f // glorious chord
            GameSound.RESULT -> listOf(440.0f, 554.37f, 659.25f) to 0.20f
            GameSound.JUMP -> listOf(330.0f, 493.88f) to 0.08f // quick upward flutter
            GameSound.COLOR_CHANGE -> listOf(440.0f, 659.25f, 880.0f) to 0.15f // magical sparkle
            GameSound.PASS -> listOf(587.33f, 880.0f) to 0.10f // ding
            GameSound.HIT -> listOf(220.0f, 146.83f) to 0.14f // dull thump
            GameSound.GAME_OVER -> listOf(349.23f, 311.13f, 277.18f, 220.0f) to 0.40f // descending loss
            GameSound.KNIFE_THROW -> listOf(783.99f, 1046.50f) to 0.06f // swoosh
            GameSound.KNIFE_HIT -> listOf(392.0f, 587.33f) to 0.08f // thunk into wood
            GameSound.APPLE -> listOf(1046.50f, 1318.51f) to 0.12f // juicy slice chime
            GameSound.BOSS -> listOf(220.0f, 277.18f, 329.63f, 440.0f) to 0.30f // ominous fanfare
            GameSound.LEVEL_COMPLETE -> listOf(523.25f, 659.25f, 783.99f, 1046.50f) to 0.35f
        }

        val totalSamples = (sampleRate * durationSec).toInt()
        val buffer = ShortArray(totalSamples)

        val freqCount = frequencies.size
        val samplesPerFreq = totalSamples / freqCount

        for (i in 0 until totalSamples) {
            val freqIndex = (i / samplesPerFreq).coerceIn(0, freqCount - 1)
            val freq = frequencies[freqIndex]
            val time = i.toDouble() / sampleRate
            // Envelope: decay to eliminate popping
            val envelope = (1.0 - (i.toDouble() / totalSamples))
            val wave = sin(2.0 * Math.PI * freq * time) * envelope
            buffer[i] = (wave * Short.MAX_VALUE * 0.4).toInt().toShort()
        }

        val audioTrack = AudioTrack.Builder()
            .setAudioAttributes(
                AudioAttributes.Builder()
                    .setUsage(AudioAttributes.USAGE_GAME)
                    .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                    .build()
            )
            .setAudioFormat(
                AudioFormat.Builder()
                    .setEncoding(AudioFormat.ENCODING_PCM_16BIT)
                    .setSampleRate(sampleRate)
                    .setChannelMask(AudioFormat.CHANNEL_OUT_MONO)
                    .build()
            )
            .setBufferSizeInBytes(buffer.size * 2)
            .setTransferMode(AudioTrack.MODE_STATIC)
            .build()

        audioTrack.write(buffer, 0, buffer.size)
        audioTrack.play()
        audioTrack.setNotificationMarkerPosition(buffer.size)
        audioTrack.setPlaybackPositionUpdateListener(object : AudioTrack.OnPlaybackPositionUpdateListener {
            override fun onMarkerReached(track: AudioTrack?) {
                track?.release()
            }
            override fun onPeriodicNotification(track: AudioTrack?) {}
        })
    }
}
