package com.bscl.gametranslator.ml

import android.util.LruCache
import com.bscl.gametranslator.model.SupportedLanguage
import com.google.mlkit.common.model.DownloadConditions
import com.google.mlkit.common.model.RemoteModelManager
import com.google.mlkit.nl.translate.TranslateRemoteModel
import com.google.mlkit.nl.translate.Translation
import com.google.mlkit.nl.translate.Translator
import com.google.mlkit.nl.translate.TranslatorOptions
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException

class TranslatorEngine {

    private val translationCache = LruCache<String, String>(500)
    private var activeTranslator: Translator? = null
    private var currentSource: SupportedLanguage? = null
    private var currentTarget: SupportedLanguage? = null
    private val languageIdentifier = LanguageIdentifierEngine()
    private val onlineTranslator = OnlineTranslatorEngine()

    @Volatile
    var isOfflineReady: Boolean = false
        private set

    private fun getOrCreateTranslator(
        source: SupportedLanguage,
        target: SupportedLanguage
    ): Translator {
        val resolvedSource = if (source == SupportedLanguage.AUTO) SupportedLanguage.ENGLISH else source
        if (activeTranslator != null && currentSource == resolvedSource && currentTarget == target) {
            return activeTranslator!!
        }

        activeTranslator?.close()
        val options = TranslatorOptions.Builder()
            .setSourceLanguage(resolvedSource.mlKitCode)
            .setTargetLanguage(target.mlKitCode)
            .build()

        currentSource = resolvedSource
        currentTarget = target
        val newTranslator = Translation.getClient(options)
        activeTranslator = newTranslator
        return newTranslator
    }

    suspend fun isModelDownloaded(language: SupportedLanguage): Boolean = suspendCancellableCoroutine { continuation ->
        if (language == SupportedLanguage.AUTO) {
            isOfflineReady = true
            continuation.resume(true)
            return@suspendCancellableCoroutine
        }
        val modelManager = RemoteModelManager.getInstance()
        val model = TranslateRemoteModel.Builder(language.mlKitCode).build()
        modelManager.isModelDownloaded(model)
            .addOnSuccessListener { isDownloaded ->
                if (language == currentTarget || language == SupportedLanguage.SPANISH) {
                    isOfflineReady = isDownloaded
                }
                continuation.resume(isDownloaded)
            }
            .addOnFailureListener {
                continuation.resume(false)
            }
    }

    suspend fun ensureModelDownloaded(
        source: SupportedLanguage,
        target: SupportedLanguage,
        requireWifi: Boolean = false
    ): Boolean = suspendCancellableCoroutine { continuation ->
        val resolvedSource = if (source == SupportedLanguage.AUTO) SupportedLanguage.ENGLISH else source
        val translator = getOrCreateTranslator(resolvedSource, target)
        val conditionsBuilder = DownloadConditions.Builder()
        if (requireWifi) {
            conditionsBuilder.requireWifi()
        }

        translator.downloadModelIfNeeded(conditionsBuilder.build())
            .addOnSuccessListener {
                isOfflineReady = true
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

        val effectiveSource = if (source == SupportedLanguage.AUTO) {
            languageIdentifier.identifyLanguage(trimmed) ?: SupportedLanguage.ENGLISH
        } else {
            source
        }

        if (effectiveSource == target) {
            return trimmed
        }

        val cacheKey = "${effectiveSource.code}:${target.code}:$trimmed"
        val cached = translationCache.get(cacheKey)
        if (cached != null) {
            return cached
        }

        // Si el modelo offline está listo en memoria, usar ML Kit on-device (15-30ms)
        if (isOfflineReady) {
            try {
                return suspendCancellableCoroutine { continuation ->
                    val translator = getOrCreateTranslator(effectiveSource, target)
                    translator.translate(trimmed)
                        .addOnSuccessListener { translated ->
                            translationCache.put(cacheKey, translated)
                            continuation.resume(translated)
                        }
                        .addOnFailureListener { ex ->
                            continuation.resumeWithException(ex)
                        }
                }
            } catch (_: Exception) {
                // Fallback automático al traductor online si falla
            }
        }

        // Si aún no está listo el offline, verificar si ya se descargó previamente
        try {
            val downloaded = isModelDownloaded(target)
            if (downloaded) {
                isOfflineReady = true
                return suspendCancellableCoroutine { continuation ->
                    val translator = getOrCreateTranslator(effectiveSource, target)
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
        } catch (_: Exception) {
            // Continuar al fallback online
        }

        // Fallback rápido Online
        return try {
            val onlineResult = onlineTranslator.translateOnline(trimmed, effectiveSource, target)
            translationCache.put(cacheKey, onlineResult)
            onlineResult
        } catch (_: Exception) {
            trimmed
        }
    }

    fun close() {
        activeTranslator?.close()
        activeTranslator = null
        languageIdentifier.close()
        translationCache.evictAll()
    }
}
