package com.bscl.gametranslator.ml

import com.bscl.gametranslator.model.SupportedLanguage
import com.google.mlkit.nl.languageid.LanguageIdentification
import com.google.mlkit.nl.languageid.LanguageIdentifier
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlin.coroutines.resume

class LanguageIdentifierEngine {

    private val identifier: LanguageIdentifier = LanguageIdentification.getClient()

    suspend fun identifyLanguage(text: String): SupportedLanguage? = suspendCancellableCoroutine { continuation ->
        val sample = if (text.length > 200) text.substring(0, 200) else text
        identifier.identifyLanguage(sample)
            .addOnSuccessListener { languageCode ->
                if (languageCode == "und" || languageCode.isNullOrEmpty()) {
                    continuation.resume(null)
                } else {
                    val detected = SupportedLanguage.entries.find {
                        it.code.equals(languageCode, ignoreCase = true) ||
                                it.mlKitCode.equals(languageCode, ignoreCase = true)
                    }
                    continuation.resume(detected)
                }
            }
            .addOnFailureListener {
                continuation.resume(null)
            }
    }

    fun close() {
        identifier.close()
    }
}
