package com.example.audio

import android.app.NotificationManager
import android.content.Context
import android.media.AudioAttributes
import android.media.AudioManager
import android.os.Build
import android.os.VibrationAttributes
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager
import android.provider.Settings
import androidx.annotation.VisibleForTesting

/**
 * Handles short, pleasant haptic vibration when a Store purchase is verified and completed.
 * Respects both the in-app Vibration setting and Android device vibration/ringer restrictions.
 */
class PurchaseHapticsManager(context: Context) {
    private val appContext = context.applicationContext

    @Volatile
    var vibrationEnabled: Boolean = true

    @Volatile
    @VisibleForTesting
    internal var onVibratedForTest: (() -> Unit)? = null

    fun vibratePurchaseSuccess(): Boolean {
        if (!vibrationEnabled) return false
        if (!isDeviceVibrationAllowed()) return false

        val vibrator = resolveVibrator() ?: return false
        if (!vibrator.hasVibrator()) return false

        return try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                val effect = if (vibrator.hasAmplitudeControl()) {
                    VibrationEffect.createWaveform(
                        longArrayOf(0L, 40L, 45L, 70L),
                        intArrayOf(0, 145, 0, 215),
                        -1
                    )
                } else {
                    VibrationEffect.createOneShot(75L, VibrationEffect.DEFAULT_AMPLITUDE)
                }

                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                    val attrs = VibrationAttributes.Builder()
                        .setUsage(VibrationAttributes.USAGE_NOTIFICATION)
                        .build()
                    vibrator.vibrate(effect, attrs)
                } else {
                    @Suppress("DEPRECATION")
                    val audioAttrs = AudioAttributes.Builder()
                        .setUsage(AudioAttributes.USAGE_GAME)
                        .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                        .build()
                    @Suppress("DEPRECATION")
                    vibrator.vibrate(effect, audioAttrs)
                }
            } else {
                @Suppress("DEPRECATION")
                vibrator.vibrate(longArrayOf(0L, 40L, 45L, 70L), -1)
            }
            onVibratedForTest?.invoke()
            true
        } catch (_: Throwable) {
            false
        }
    }

    private fun isDeviceVibrationAllowed(): Boolean {
        return try {
            val audioManager = appContext.getSystemService(Context.AUDIO_SERVICE) as? AudioManager
            if (audioManager?.ringerMode == AudioManager.RINGER_MODE_SILENT) {
                return false
            }

            val notificationManager =
                appContext.getSystemService(Context.NOTIFICATION_SERVICE) as? NotificationManager
            val interruptionFilter = notificationManager?.currentInterruptionFilter
            if (interruptionFilter == NotificationManager.INTERRUPTION_FILTER_NONE ||
                interruptionFilter == NotificationManager.INTERRUPTION_FILTER_ALARMS
            ) {
                return false
            }

            // Respect master vibration toggle in system settings when present (Android 13+)
            val masterVibrateOn = Settings.System.getInt(
                appContext.contentResolver,
                "vibrate_on",
                1
            )
            masterVibrateOn != 0
        } catch (_: Throwable) {
            true
        }
    }

    private fun resolveVibrator(): Vibrator? {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            appContext.getSystemService(VibratorManager::class.java)?.defaultVibrator
        } else {
            @Suppress("DEPRECATION")
            appContext.getSystemService(Context.VIBRATOR_SERVICE) as? Vibrator
        }
    }
}
