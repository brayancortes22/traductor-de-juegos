package com.bscl.gametranslator.voice

import android.content.Context
import android.media.AudioAttributes
import android.speech.tts.TextToSpeech
import android.util.Log
import java.util.Locale

class VoiceNarratorEngine(private val context: Context) : TextToSpeech.OnInitListener {

    private var tts: TextToSpeech? = null
    private var isInitialized = false
    private var lastSpokenText: String = ""
    private var lastSpokenTime: Long = 0L

    init {
        tts = TextToSpeech(context, this)
    }

    override fun onInit(status: Int) {
        if (status == TextToSpeech.SUCCESS) {
            val result = tts?.setLanguage(Locale("es", "ES"))
            if (result == TextToSpeech.LANG_MISSING_DATA || result == TextToSpeech.LANG_NOT_SUPPORTED) {
                tts?.setLanguage(Locale.getDefault())
            }

            val audioAttributes = AudioAttributes.Builder()
                .setUsage(AudioAttributes.USAGE_ASSISTANCE_NAVIGATION_GUIDANCE)
                .setContentType(AudioAttributes.CONTENT_TYPE_SPEECH)
                .build()
            tts?.setAudioAttributes(audioAttributes)
            tts?.setSpeechRate(1.05f)
            tts?.setPitch(1.0f)
            isInitialized = true
        } else {
            Log.e(TAG, "Fallo al inicializar TextToSpeech de Android: $status")
        }
    }

    fun speak(text: String, isPriority: Boolean = false) {
        val trimmed = text.trim()
        if (trimmed.isEmpty() || !isInitialized) return

        val now = System.currentTimeMillis()
        if (!isPriority && trimmed.equals(lastSpokenText, ignoreCase = true) && (now - lastSpokenTime) < COOLDOWN_MS) {
            return
        }

        lastSpokenText = trimmed
        lastSpokenTime = now

        val queueMode = if (isPriority) TextToSpeech.QUEUE_FLUSH else TextToSpeech.QUEUE_ADD
        tts?.speak(trimmed, queueMode, null, "GameAssistantNarrator_${System.currentTimeMillis()}")
    }

    fun stop() {
        tts?.stop()
    }

    fun isSpeaking(): Boolean {
        return tts?.isSpeaking == true
    }

    fun shutdown() {
        tts?.stop()
        tts?.shutdown()
        tts = null
        isInitialized = false
    }

    companion object {
        private const val TAG = "VoiceNarrator"
        private const val COOLDOWN_MS = 25_000L
    }
}
