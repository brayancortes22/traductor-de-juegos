package com.bscl.gametranslator.assistant

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONArray
import org.json.JSONObject
import java.util.concurrent.TimeUnit

class AiGameAssistantEngine {

    private val knowledgeBase = LifeAfterKnowledgeBase()
    private val httpClient = OkHttpClient.Builder()
        .connectTimeout(8, TimeUnit.SECONDS)
        .readTimeout(10, TimeUnit.SECONDS)
        .build()

    suspend fun analyzeGameContext(ocrText: String, apiKey: String? = null): GameAdvice = withContext(Dispatchers.IO) {
        val offlineAdvice = knowledgeBase.findAdvice(ocrText)
        if (offlineAdvice != null) {
            return@withContext offlineAdvice
        }

        if (!apiKey.isNullOrBlank()) {
            try {
                val onlineResult = queryGeminiAssistant(ocrText, apiKey)
                if (onlineResult != null) {
                    return@withContext onlineResult
                }
            } catch (_: Exception) {
                // Silently fallback to heuristic
            }
        }

        generateHeuristicAdvice(ocrText)
    }

    private fun queryGeminiAssistant(ocrText: String, apiKey: String): GameAdvice? {
        val endpoint = "https://generativelanguage.googleapis.com/v1beta/models/gemini-1.5-flash:generateContent?key=$apiKey"
        val prompt = """
            Eres un copiloto y asesor experto en videojuegos de supervivencia como LifeAfter.
            Analiza el siguiente texto extraído de la pantalla del juego y responde en formato JSON EXACTO con las siguientes claves:
            {
              "title": "Título corto de la tarea o recurso",
              "objective": "Qué debe hacer el jugador",
              "whereToFind": "Dónde conseguir los recursos necesarios o qué menú abrir",
              "stepByStep": "Pasos simples 1, 2, 3 para resolverlo",
              "proTip": "Un truco pro de supervivencia o combate",
              "spokenSummary": "Frase corta y directa (máximo 2 oraciones) en español para hablarle al jugador por voz"
            }
            Texto de pantalla:
            $ocrText
        """.trimIndent()

        val jsonBody = JSONObject().apply {
            val contents = JSONArray().apply {
                val part = JSONObject().put("text", prompt)
                val parts = JSONArray().put(part)
                put(JSONObject().put("parts", parts))
            }
            put("contents", contents)
        }

        val request = Request.Builder()
            .url(endpoint)
            .post(jsonBody.toString().toRequestBody("application/json".toMediaType()))
            .build()

        val response = httpClient.newCall(request).execute()
        if (!response.isSuccessful) return null

        val responseBody = response.body?.string() ?: return null
        val responseJson = JSONObject(responseBody)
        val candidates = responseJson.optJSONArray("candidates") ?: return null
        if (candidates.length() == 0) return null

        val candidate = candidates.getJSONObject(0)
        val content = candidate.getJSONObject("content")
        val parts = content.getJSONArray("parts")
        val rawText = parts.getJSONObject(0).getString("text")

        val cleanJson = rawText.replace("```json", "").replace("```", "").trim()
        val parsed = JSONObject(cleanJson)

        return GameAdvice(
            title = parsed.optString("title", "Guía de Juego"),
            objective = parsed.optString("objective", "Revisar objetivo de misión."),
            whereToFind = parsed.optString("whereToFind", "Revisa tu mapa y almacén."),
            stepByStep = parsed.optString("stepByStep", "Sigue las indicaciones del mapa."),
            proTip = parsed.optString("proTip", "Recolecta recursos antes de salir a zonas peligrosas."),
            spokenSummary = parsed.optString("spokenSummary", "He detectado una misión en pantalla. Revisa el objetivo para continuar.")
        )
    }

    private fun generateHeuristicAdvice(ocrText: String): GameAdvice {
        val lines = ocrText.lines().map { it.trim() }.filter { it.length > 3 }
        val firstMeaningful = lines.firstOrNull() ?: "Misión o Recurso del Juego"

        return GameAdvice(
            title = "Objetivo Detectado",
            objective = "Completar la acción requerida: $firstMeaningful",
            whereToFind = "Abre tu mochila o menú de fórmulas en pantalla para verificar los materiales requeridos.",
            stepByStep = "1. Abre el mapa de exploración.\n2. Localiza el punto de recolección indicado.\n3. Regresa a tu mansión o banco de trabajo para fabricar.",
            proTip = "Mantén siempre espacio libre en tu inventario para no perder botín raro al explorar.",
            spokenSummary = "Nueva tarea detectada: $firstMeaningful. Abre tu mapa o inventario para ubicar los recursos."
        )
    }
}
