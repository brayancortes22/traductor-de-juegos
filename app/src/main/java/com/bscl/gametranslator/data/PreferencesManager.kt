package com.bscl.gametranslator.data

import android.content.Context
import android.content.SharedPreferences
import com.bscl.gametranslator.model.AppConfig
import com.bscl.gametranslator.model.SupportedLanguage

class PreferencesManager(context: Context) {

    private val prefs: SharedPreferences =
        context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

    fun loadConfig(): AppConfig {
        val srcCode = prefs.getString(KEY_SOURCE_LANG, SupportedLanguage.ENGLISH.code)
            ?: SupportedLanguage.ENGLISH.code
        val targetCode = prefs.getString(KEY_TARGET_LANG, SupportedLanguage.SPANISH.code)
            ?: SupportedLanguage.SPANISH.code
        val opacity = prefs.getFloat(KEY_BUBBLE_OPACITY, 0.90f)
        val autoCopy = prefs.getBoolean(KEY_AUTO_COPY, false)
        val filterElements = prefs.getBoolean(KEY_FILTER_ELEMENTS, true)
        val aiMax = prefs.getBoolean(KEY_AI_MAX, true)
        val voiceAssistant = prefs.getBoolean(KEY_VOICE_ASSISTANT, true)
        val apiKey = prefs.getString(KEY_API_KEY, "") ?: ""

        return AppConfig(
            sourceLanguage = SupportedLanguage.fromCode(srcCode),
            targetLanguage = SupportedLanguage.fromCode(targetCode),
            bubbleOpacity = opacity,
            autoCopyToClipboard = autoCopy,
            filterIrrelevantElements = filterElements,
            enableAiMaxGlossary = aiMax,
            enableVoiceAssistant = voiceAssistant,
            geminiApiKey = apiKey
        )
    }

    fun saveConfig(config: AppConfig) {
        prefs.edit()
            .putString(KEY_SOURCE_LANG, config.sourceLanguage.code)
            .putString(KEY_TARGET_LANG, config.targetLanguage.code)
            .putFloat(KEY_BUBBLE_OPACITY, config.bubbleOpacity)
            .putBoolean(KEY_AUTO_COPY, config.autoCopyToClipboard)
            .putBoolean(KEY_FILTER_ELEMENTS, config.filterIrrelevantElements)
            .putBoolean(KEY_AI_MAX, config.enableAiMaxGlossary)
            .putBoolean(KEY_VOICE_ASSISTANT, config.enableVoiceAssistant)
            .putString(KEY_API_KEY, config.geminiApiKey)
            .apply()
    }

    fun saveBubblePosition(x: Int, y: Int) {
        prefs.edit()
            .putInt(KEY_BUBBLE_X, x)
            .putInt(KEY_BUBBLE_Y, y)
            .apply()
    }

    fun getBubblePosition(): Pair<Int, Int> {
        val x = prefs.getInt(KEY_BUBBLE_X, 0)
        val y = prefs.getInt(KEY_BUBBLE_Y, 200)
        return Pair(x, y)
    }

    companion object {
        private const val PREFS_NAME = "game_translator_prefs"
        private const val KEY_SOURCE_LANG = "key_source_lang"
        private const val KEY_TARGET_LANG = "key_target_lang"
        private const val KEY_BUBBLE_OPACITY = "key_bubble_opacity"
        private const val KEY_AUTO_COPY = "key_auto_copy"
        private const val KEY_FILTER_ELEMENTS = "key_filter_elements"
        private const val KEY_AI_MAX = "key_ai_max"
        private const val KEY_VOICE_ASSISTANT = "key_voice_assistant"
        private const val KEY_API_KEY = "key_api_key"
        private const val KEY_BUBBLE_X = "key_bubble_x"
        private const val KEY_BUBBLE_Y = "key_bubble_y"
    }
}
