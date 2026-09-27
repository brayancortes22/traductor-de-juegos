package com.bscl.gametranslator.model

import android.graphics.Rect
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

enum class TranslationMode(val displayName: String) {
    FULL_SCREEN("Pantalla Completa"),
    PARTIAL_CROP("Recorte Libre"),
    LIFEAFTER_QUESTS("Misiones"),
    LIFEAFTER_SHOP("Tienda / Fórmulas"),
    LIFEAFTER_CHAT("Diálogos / Chat");

    fun getBoundingRect(screenWidth: Int, screenHeight: Int): Rect? {
        return when (this) {
            FULL_SCREEN -> null
            PARTIAL_CROP -> null
            LIFEAFTER_QUESTS -> Rect(
                0,
                (screenHeight * 0.10).toInt(),
                (screenWidth * 0.42).toInt(),
                (screenHeight * 0.65).toInt()
            )
            LIFEAFTER_SHOP -> Rect(
                (screenWidth * 0.05).toInt(),
                (screenHeight * 0.08).toInt(),
                (screenWidth * 0.95).toInt(),
                (screenHeight * 0.92).toInt()
            )
            LIFEAFTER_CHAT -> Rect(
                (screenWidth * 0.25).toInt(),
                (screenHeight * 0.75).toInt(),
                (screenWidth * 0.75).toInt(),
                (screenHeight * 0.98).toInt()
            )
        }
    }
}

data class AppConfig(
    val sourceLanguage: SupportedLanguage = SupportedLanguage.ENGLISH,
    val targetLanguage: SupportedLanguage = SupportedLanguage.SPANISH,
    val bubbleOpacity: Float = 0.90f,
    val autoCopyToClipboard: Boolean = false,
    val activeMode: TranslationMode = TranslationMode.FULL_SCREEN,
    val isRealTimeAutoScan: Boolean = false
)
