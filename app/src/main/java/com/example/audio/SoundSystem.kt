package com.example.audio

import android.content.Context
import android.media.AudioAttributes
import android.media.AudioFormat
import android.media.AudioTrack
import android.os.Build
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlin.math.PI
import kotlin.math.sin

class SoundSystem(private val context: Context) {
    var soundEnabled: Boolean = true
    var engineSoundEnabled: Boolean = true
    var hapticsEnabled: Boolean = true

    private val vibrator: Vibrator? = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
        val vibratorManager = context.getSystemService(Context.VIBRATOR_MANAGER_SERVICE) as? VibratorManager
        vibratorManager?.defaultVibrator
    } else {
        @Suppress("DEPRECATION")
        context.getSystemService(Context.VIBRATOR_SERVICE) as? Vibrator
    }

    private val sampleRate = 22050
    private var engineTrack: AudioTrack? = null
    private var engineJob: Job? = null
    private val scope = CoroutineScope(Dispatchers.Default)

    @Volatile
    var targetEnginePitch: Float = 0.2f // 0.0 (idle) to 1.0 (max rev)

    init {
        initEngineTrack()
    }

    private fun initEngineTrack() {
        try {
            val minBuf = AudioTrack.getMinBufferSize(
                sampleRate,
                AudioFormat.CHANNEL_OUT_MONO,
                AudioFormat.ENCODING_PCM_16BIT
            )
            val bufferSize = minBuf.coerceAtLeast(2048)

            engineTrack = AudioTrack.Builder()
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
                .setBufferSizeInBytes(bufferSize)
                .setTransferMode(AudioTrack.MODE_STREAM)
                .build()

            engineTrack?.play()
            startEngineLoop()
        } catch (_: Exception) {
            // AudioTrack fallback gracefully
        }
    }

    private fun startEngineLoop() {
        engineJob?.cancel()
        engineJob = scope.launch {
            val buffer = ShortArray(1024)
            var phase1 = 0.0
            var phase2 = 0.0
            var currentPitch = 0.2f

            while (isActive) {
                if (!soundEnabled || !engineSoundEnabled || engineTrack == null) {
                    kotlinx.coroutines.delay(100)
                    continue
                }

                currentPitch += (targetEnginePitch - currentPitch) * 0.15f
                val baseFreq = 75.0 + currentPitch * 180.0
                val harmonicFreq = baseFreq * 2.05

                for (i in buffer.indices) {
                    val s1 = sin(phase1)
                    val s2 = sin(phase2) * 0.35
                    val sample = ((s1 + s2) * 4500 * (0.4f + currentPitch * 0.6f)).toInt()
                    buffer[i] = sample.coerceIn(-32767, 32767).toShort()

                    phase1 += 2 * PI * baseFreq / sampleRate
                    if (phase1 > 2 * PI) phase1 -= 2 * PI

                    phase2 += 2 * PI * harmonicFreq / sampleRate
                    if (phase2 > 2 * PI) phase2 -= 2 * PI
                }

                engineTrack?.write(buffer, 0, buffer.size)
            }
        }
    }

    fun playCoinSound() {
        if (!soundEnabled) return
        scope.launch {
            generateTone(frequencies = listOf(987f, 1318f, 1760f), durationMs = 120, volume = 0.5f)
        }
        triggerHaptic(HapticType.LIGHT)
    }

    fun playFuelSound() {
        if (!soundEnabled) return
        scope.launch {
            generateTone(frequencies = listOf(523f, 659f, 784f, 1046f), durationMs = 220, volume = 0.6f)
        }
        triggerHaptic(HapticType.MEDIUM)
    }

    fun playCrashSound() {
        if (!soundEnabled) return
        scope.launch {
            generateNoiseBurst(durationMs = 350, initialVolume = 0.8f)
        }
        triggerHaptic(HapticType.HEAVY)
    }

    fun playClickSound() {
        if (!soundEnabled) return
        scope.launch {
            generateTone(frequencies = listOf(800f), durationMs = 30, volume = 0.3f)
        }
        triggerHaptic(HapticType.LIGHT)
    }

    private fun generateTone(frequencies: List<Float>, durationMs: Int, volume: Float) {
        try {
            val totalSamples = (sampleRate * durationMs / 1000)
            val buffer = ShortArray(totalSamples)
            val subDuration = totalSamples / frequencies.size

            for (i in 0 until totalSamples) {
                val freqIndex = (i / subDuration).coerceIn(0, frequencies.size - 1)
                val freq = frequencies[freqIndex]
                val envelope = 1.0f - (i.toFloat() / totalSamples.toFloat())
                val value = sin(2 * PI * freq * i / sampleRate) * 32767 * volume * envelope
                buffer[i] = value.toInt().toShort()
            }

            val track = AudioTrack.Builder()
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

            track.write(buffer, 0, buffer.size)
            track.play()
            scope.launch {
                kotlinx.coroutines.delay(durationMs.toLong() + 100)
                track.release()
            }
        } catch (_: Exception) {
            // Ignore if audio buffer cannot be allocated
        }
    }

    private fun generateNoiseBurst(durationMs: Int, initialVolume: Float) {
        try {
            val totalSamples = (sampleRate * durationMs / 1000)
            val buffer = ShortArray(totalSamples)

            for (i in 0 until totalSamples) {
                val progress = i.toFloat() / totalSamples.toFloat()
                val envelope = (1.0f - progress) * (1.0f - progress)
                val noise = (Math.random() * 2.0 - 1.0)
                val bass = sin(2 * PI * 65.0 * i / sampleRate) * 0.6
                val sample = ((noise * 0.5 + bass) * 32767 * initialVolume * envelope).toInt()
                buffer[i] = sample.coerceIn(-32767, 32767).toShort()
            }

            val track = AudioTrack.Builder()
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

            track.write(buffer, 0, buffer.size)
            track.play()
            scope.launch {
                kotlinx.coroutines.delay(durationMs.toLong() + 100)
                track.release()
            }
        } catch (_: Exception) {
            // Ignore
        }
    }

    enum class HapticType { LIGHT, MEDIUM, HEAVY }

    private fun triggerHaptic(type: HapticType) {
        if (!hapticsEnabled || vibrator == null || !vibrator.hasVibrator()) return
        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                val effect = when (type) {
                    HapticType.LIGHT -> VibrationEffect.createOneShot(20, VibrationEffect.DEFAULT_AMPLITUDE)
                    HapticType.MEDIUM -> VibrationEffect.createOneShot(45, 180)
                    HapticType.HEAVY -> VibrationEffect.createWaveform(longArrayOf(0, 70, 40, 90), -1)
                }
                vibrator.vibrate(effect)
            } else {
                @Suppress("DEPRECATION")
                when (type) {
                    HapticType.LIGHT -> vibrator.vibrate(20)
                    HapticType.MEDIUM -> vibrator.vibrate(45)
                    HapticType.HEAVY -> vibrator.vibrate(150)
                }
            }
        } catch (_: Exception) {
            // Graceful fallback
        }
    }

    fun release() {
        engineJob?.cancel()
        try {
            engineTrack?.stop()
            engineTrack?.release()
            engineTrack = null
        } catch (_: Exception) {}
    }
}
