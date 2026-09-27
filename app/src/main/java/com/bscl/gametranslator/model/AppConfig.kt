package com.bscl.gametranslator.model

import com.google.mlkit.nl.translate.TranslateLanguage

enum class SupportedLanguage(val code: String, val displayName: String, val mlKitCode: String) {
    ENGLISH("en", "Inglés", TranslateLanguage.ENGLISH),
    SPANISH("es", "Español", TranslateLanguage.SPANISH),
    CHINESE("zh", "Chino", TranslateLanguage.CHINESE),
    JAPANESE("ja", "Japonés", TranslateLanguage.JAPANESE),
    PORTUGUESE("pt", "Portugués", TranslateLanguage.PORTUGUESE),
    FRENCH("fr", "Francés", TranslateLanguage.FRENCH),
    GERMAN("de", "Alemán", TranslateLanguage.GERMAN);

    companion object {
        fun fromCode(code: String): SupportedLanguage {
            return entries.find { it.code.equals(code, ignoreCase = true) } ?: SPANISH
        }
    }
}

enum class CaptureMode {
    FULL_SCREEN,
    SELECTION_CROP
}

data class AppConfig(
    val sourceLanguage: SupportedLanguage = SupportedLanguage.ENGLISH,
    val targetLanguage: SupportedLanguage = SupportedLanguage.SPANISH,
    val bubbleOpacity: Float = 0.90f,
    val autoCopyToClipboard: Boolean = false,
    val captureMode: CaptureMode = CaptureMode.FULL_SCREEN
)
