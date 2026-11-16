package com.example.audio

import android.media.AudioAttributes
import android.media.AudioFormat
import android.media.AudioTrack
import androidx.annotation.VisibleForTesting
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Semaphore
import kotlin.math.PI
import kotlin.math.exp
import kotlin.math.sin

/**
 * Sound-effects-only (SFX) procedural audio synthesizer using Android AudioTrack.
 * Background music is permanently disabled across all screens.
 */
class SoundEffectsManager {
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Default)
    private val trackSemaphore = Semaphore(permits = 4)

    @Volatile
    var soundEnabled: Boolean = true

    @Volatile
    @VisibleForTesting
    internal var soundObserverForTest: ((String) -> Unit)? = null

    fun playButtonClick() {
        if (!soundEnabled) return
        soundObserverForTest?.invoke("BUTTON_CLICK")
        playToneSequence(listOf(680.0 to 40))
    }

    fun playSwap() {
        if (!soundEnabled) return
        soundObserverForTest?.invoke("SWAP")
        playToneSequence(listOf(440.0 to 40, 587.33 to 50))
    }

    fun playInvalidSwap() {
        if (!soundEnabled) return
        soundObserverForTest?.invoke("INVALID_SWAP")
        playToneSequence(listOf(280.0 to 65, 210.0 to 85))
    }

    fun playFruitMatch(combo: Int = 1) {
        if (!soundEnabled) return
        soundObserverForTest?.invoke("FRUIT_MATCH")
        val base = 523.25 * (1.0 + ((combo - 1).coerceAtMost(5) * 0.1))
        playToneSequence(listOf(base to 55, base * 1.25 to 70, base * 1.5 to 85))
    }

    fun playFruitPop() {
        if (!soundEnabled) return
        soundObserverForTest?.invoke("FRUIT_POP")
        playToneSequence(listOf(880.0 to 35, 1174.66 to 45))
    }

    fun playPowerUp() {
        if (!soundEnabled) return
        soundObserverForTest?.invoke("POWER_UP")
        playToneSequence(listOf(523.25 to 50, 659.25 to 50, 783.99 to 60, 1046.5 to 105))
    }

    fun playCoinCollect() {
        if (!soundEnabled) return
        soundObserverForTest?.invoke("COIN_COLLECT")
        playToneSequence(listOf(987.77 to 55, 1318.51 to 130))
    }

    fun playRewardCollect() {
        if (!soundEnabled) return
        soundObserverForTest?.invoke("REWARD_COLLECT")
        playToneSequence(listOf(587.33 to 60, 783.99 to 60, 987.77 to 75, 1318.51 to 145))
    }

    /**
     * Rich, bright polyphonic celebratory chime played exclusively when a Store purchase
     * has been verified and confirmed.
     */
    fun playPurchaseSuccess() {
        if (!soundEnabled) return
        soundObserverForTest?.invoke("PURCHASE_SUCCESS")
        if (!trackSemaphore.tryAcquire()) return
        scope.launch {
            try {
                playPolyphonicPurchaseChime()
            } finally {
                trackSemaphore.release()
            }
        }
    }

    fun playLevelComplete() {
        if (!soundEnabled) return
        soundObserverForTest?.invoke("LEVEL_COMPLETE")
        playToneSequence(
            listOf(
                523.25 to 90,
                659.25 to 90,
                783.99 to 90,
                1046.50 to 210
            )
        )
    }

    fun playGameOver() {
        if (!soundEnabled) return
        soundObserverForTest?.invoke("GAME_OVER")
        playToneSequence(listOf(392.0 to 125, 349.23 to 125, 293.66 to 220))
    }

    fun release() {
        scope.cancel()
    }

    private fun playToneSequence(notes: List<Pair<Double, Int>>) {
        if (!trackSemaphore.tryAcquire()) return
        scope.launch {
            try {
                for ((freq, durationMs) in notes) {
                    if (!soundEnabled) break
                    playSoftChime(freq, durationMs, volume = 0.20f)
                }
            } finally {
                trackSemaphore.release()
            }
        }
    }

    private fun playPolyphonicPurchaseChime() {
        var track: AudioTrack? = null
        try {
            val sampleRate = 44100
            val totalDurationMs = 680
            val numSamples = (sampleRate * totalDurationMs) / 1000
            val pcm = ShortArray(numSamples)

            // Overlapping bright ascending arpeggio + celebratory major chime overtones
            data class ChimeTone(
                val startSec: Double,
                val freqHz: Double,
                val decayRate: Double,
                val weight: Double
            )
            val tones = listOf(
                ChimeTone(0.000, 523.25, 8.5, 0.28),   // C5
                ChimeTone(0.065, 659.25, 8.0, 0.28),   // E5
                ChimeTone(0.130, 783.99, 7.5, 0.30),   // G5
                ChimeTone(0.195, 1046.50, 5.2, 0.38),  // C6
                ChimeTone(0.245, 1318.51, 4.6, 0.34),  // E6 sparkle
                ChimeTone(0.245, 1567.98, 6.2, 0.16)   // G6 soft harmonic shimmer
            )

            val attackSec = 0.008
            val masterVolume = 0.34
            for (i in 0 until numSamples) {
                val t = i.toDouble() / sampleRate
                var sampleSum = 0.0
                for (tone in tones) {
                    val localT = t - tone.startSec
                    if (localT >= 0.0) {
                        val attack = (localT / attackSec).coerceIn(0.0, 1.0)
                        val env = attack * exp(-tone.decayRate * localT)
                        val fundamental = sin(2.0 * PI * tone.freqHz * localT)
                        val overtone = 0.22 * sin(2.0 * PI * (tone.freqHz * 2.0) * localT) * exp(-4.0 * localT)
                        sampleSum += (fundamental + overtone) * env * tone.weight
                    }
                }
                val scaled = (sampleSum * masterVolume * Short.MAX_VALUE)
                    .toInt()
                    .coerceIn(Short.MIN_VALUE.toInt(), Short.MAX_VALUE.toInt())
                pcm[i] = scaled.toShort()
            }

            val minBuf = AudioTrack.getMinBufferSize(
                sampleRate,
                AudioFormat.CHANNEL_OUT_MONO,
                AudioFormat.ENCODING_PCM_16BIT
            )
            if (minBuf <= 0) return
            track = AudioTrack.Builder()
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
                .setBufferSizeInBytes(maxOf(minBuf, pcm.size * 2))
                .setTransferMode(AudioTrack.MODE_STATIC)
                .build()
            track.write(pcm, 0, pcm.size)
            track.play()
            Thread.sleep(totalDurationMs.toLong() + 24L)
        } catch (_: Throwable) {
            // Safe fallback on environments without audio hardware (e.g., Robolectric JVM tests)
        } finally {
            runCatching { track?.stop() }
            runCatching { track?.release() }
        }
    }

    private fun playSoftChime(freqHz: Double, durationMs: Int, volume: Float) {
        var track: AudioTrack? = null
        try {
            val sampleRate = 22050
            val numSamples = (sampleRate * durationMs) / 1000
            if (numSamples <= 0) return
            val pcm = ShortArray(numSamples)
            for (i in 0 until numSamples) {
                val t = i.toDouble() / sampleRate
                val env = exp(-4.8 * i / numSamples)
                val wave = sin(2.0 * PI * freqHz * t)
                pcm[i] = (wave * env * volume * Short.MAX_VALUE).toInt()
                    .coerceIn(Short.MIN_VALUE.toInt(), Short.MAX_VALUE.toInt()).toShort()
            }
            val minBuf = AudioTrack.getMinBufferSize(
                sampleRate,
                AudioFormat.CHANNEL_OUT_MONO,
                AudioFormat.ENCODING_PCM_16BIT
            )
            if (minBuf <= 0) return
            track = AudioTrack.Builder()
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
                .setBufferSizeInBytes(maxOf(minBuf, pcm.size * 2))
                .setTransferMode(AudioTrack.MODE_STATIC)
                .build()
            track.write(pcm, 0, pcm.size)
            track.play()
            Thread.sleep(durationMs.toLong() + 18L)
        } catch (_: Throwable) {
            // Safe fallback on environments without audio hardware (e.g., Robolectric JVM tests)
        } finally {
            runCatching { track?.stop() }
            runCatching { track?.release() }
        }
    }
}
