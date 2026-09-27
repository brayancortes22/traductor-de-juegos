package com.bscl.gametranslator.ml

import com.bscl.gametranslator.model.SupportedLanguage
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request
import org.json.JSONArray
import java.net.URLEncoder
import java.util.concurrent.TimeUnit

class OnlineTranslatorEngine {

    private val httpClient = OkHttpClient.Builder()
        .connectTimeout(5, TimeUnit.SECONDS)
        .readTimeout(8, TimeUnit.SECONDS)
        .build()

    suspend fun translateOnline(
        text: String,
        source: SupportedLanguage,
        target: SupportedLanguage
    ): String = withContext(Dispatchers.IO) {
        val trimmed = text.trim()
        if (trimmed.isEmpty()) return@withContext ""

        val sl = if (source == SupportedLanguage.AUTO) "auto" else source.code
        val tl = target.code
        val encodedQuery = URLEncoder.encode(trimmed, "UTF-8")

        val url = "https://translate.googleapis.com/translate_a/single?client=gtx&sl=$sl&tl=$tl&dt=t&q=$encodedQuery"

        val request = Request.Builder()
            .url(url)
            .header("User-Agent", "Mozilla/5.0 (Android; Mobile; rv:109.0)")
            .build()

        val response = httpClient.newCall(request).execute()
        if (!response.isSuccessful) {
            throw IllegalStateException("Servidor de traducción online respondió con error: ${response.code}")
        }

        val body = response.body?.string() ?: throw IllegalStateException("Respuesta vacía del servidor online")
        val jsonArray = JSONArray(body)
        val sentencesArray = jsonArray.optJSONArray(0) ?: throw IllegalStateException("Formato inesperado en respuesta online")

        val resultBuilder = StringBuilder()
        for (i in 0 until sentencesArray.length()) {
            val sentence = sentencesArray.optJSONArray(i)
            if (sentence != null && sentence.length() > 0) {
                resultBuilder.append(sentence.optString(0))
            }
        }

        val translated = resultBuilder.toString()
        if (translated.isBlank()) trimmed else translated
    }
}
