package com.bscl.gametranslator.ml

import android.util.LruCache
import com.bscl.gametranslator.model.SupportedLanguage
import com.google.mlkit.common.model.DownloadConditions
import com.google.mlkit.nl.translate.Translation
import com.google.mlkit.nl.translate.Translator
import com.google.mlkit.nl.translate.TranslatorOptions
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException

class TranslatorEngine {

    private val translationCache = LruCache<String, String>(300)
    private var activeTranslator: Translator? = null
    private var currentSource: SupportedLanguage? = null
    private var currentTarget: SupportedLanguage? = null

    private fun getOrCreateTranslator(
        source: SupportedLanguage,
        target: SupportedLanguage
    ): Translator {
        if (activeTranslator != null && currentSource == source && currentTarget == target) {
            return activeTranslator!!
        }

        activeTranslator?.close()
        val options = TranslatorOptions.Builder()
            .setSourceLanguage(source.mlKitCode)
            .setTargetLanguage(target.mlKitCode)
            .build()

        currentSource = source
        currentTarget = target
        val newTranslator = Translation.getClient(options)
        activeTranslator = newTranslator
        return newTranslator
    }

    suspend fun ensureModelDownloaded(
        source: SupportedLanguage,
        target: SupportedLanguage,
        requireWifi: Boolean = false
    ): Boolean = suspendCancellableCoroutine { continuation ->
        val translator = getOrCreateTranslator(source, target)
        val conditionsBuilder = DownloadConditions.Builder()
        if (requireWifi) {
            conditionsBuilder.requireWifi()
        }

        translator.downloadModelIfNeeded(conditionsBuilder.build())
            .addOnSuccessListener {
                continuation.resume(true)
            }
            .addOnFailureListener {
                continuation.resume(false)
            }
    }

    suspend fun translateText(
        text: String,
        source: SupportedLanguage,
        target: SupportedLanguage
    ): String {
        val trimmed = text.trim()
        if (trimmed.isEmpty()) return ""

        val cacheKey = "${source.code}:${target.code}:$trimmed"
        val cached = translationCache.get(cacheKey)
        if (cached != null) {
            return cached
        }

        return suspendCancellableCoroutine { continuation ->
            val translator = getOrCreateTranslator(source, target)
            translator.translate(trimmed)
                .addOnSuccessListener { translated ->
                    translationCache.put(cacheKey, translated)
                    continuation.resume(translated)
                }
                .addOnFailureListener { ex ->
                    continuation.resumeWithException(ex)
                }
        }
    }

    fun close() {
        activeTranslator?.close()
        activeTranslator = null
        translationCache.evictAll()
    }
}
