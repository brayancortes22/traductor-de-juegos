package com.bscl.gametranslator.assistant

import com.bscl.gametranslator.ml.TranslatorEngine
import com.bscl.gametranslator.model.DetectedTextBlock
import com.bscl.gametranslator.model.SupportedLanguage
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
    private val missionParser = GameMissionParser()
    private val httpClient = OkHttpClient.Builder()
        .connectTimeout(8, TimeUnit.SECONDS)
        .readTimeout(10, TimeUnit.SECONDS)
        .build()

    suspend fun analyzeGameContext(
        ocrText: String,
        apiKey: String? = null,
        blocks: List<DetectedTextBlock> = emptyList(),
        screenWidth: Int = 1340,
        screenHeight: Int = 800,
        translatorEngine: TranslatorEngine? = null
    ): GameAdvice = withContext(Dispatchers.IO) {
        // 1. Intentar parsear quirúrgicamente la misión activa del HUD de LifeAfter
        val parsedMission = missionParser.parseMission(blocks, screenWidth, screenHeight)
        if (parsedMission != null) {
            val missionAdvice = buildMissionAdvice(parsedMission, translatorEngine)
            return@withContext missionAdvice
        }

        // 2. Verificar base de conocimiento offline
        val offlineAdvice = knowledgeBase.findAdvice(ocrText)
        if (offlineAdvice != null) {
            return@withContext offlineAdvice
        }

        // 3. Si hay API key de Gemini, consultar online
        if (!apiKey.isNullOrBlank()) {
            try {
                val onlineResult = queryGeminiAssistant(ocrText, apiKey)
                if (onlineResult != null) {
                    return@withContext onlineResult
                }
            } catch (_: Exception) {
                // Fallback automático
            }
        }

        generateHeuristicAdvice(ocrText)
    }

    private suspend fun buildMissionAdvice(
        mission: ParsedMission,
        translatorEngine: TranslatorEngine?
    ): GameAdvice {
        val targetLang = SupportedLanguage.SPANISH
        val sourceLang = SupportedLanguage.ENGLISH

        val translatedTitle = translatorEngine?.translateText(mission.title, sourceLang, targetLang)
            ?.ifBlank { mission.title } ?: mission.title
        val translatedInstruction = translatorEngine?.translateText(mission.currentInstruction, sourceLang, targetLang)
            ?.ifBlank { mission.currentInstruction } ?: mission.currentInstruction
        val targetName = if (mission.targetName.isNotBlank()) mission.targetName else "tu objetivo"
        val location = mission.location

        return when (mission.actionType) {
            MissionActionType.FOLLOW_ESCORT -> GameAdvice(
                title = "Misión: $translatedTitle",
                objective = "Sigue y acompaña a $targetName en $location.",
                whereToFind = "$location (Sigue el indicador dorado o flecha en el minimapa).",
                stepByStep = "1. Permanece caminando cerca de $targetName sin separarte.\n2. No tomes atajos ni te alejes del camino.\n3. Al llegar a su destino, espera a que se detenga y habla con ella para completar la misión.",
                proTip = "Si estás en $location y es zona segura, puedes guardar tus armas para ahorrar durabilidad.",
                spokenSummary = "Misión activa: $translatedInstruction en $location. Sigue de cerca a $targetName hasta que se detenga."
            )
            MissionActionType.TALK_INTERACT -> GameAdvice(
                title = "Misión: $translatedTitle",
                objective = "Hablar con $targetName en $location.",
                whereToFind = "$location (Busca el ícono amarillo de diálogo en el minimapa).",
                stepByStep = "1. Abre el mapa pulsando arriba a la derecha y localiza a $targetName.\n2. Dirígete a su posición.\n3. Acércate hasta que aparezca el botón de diálogo 'Hablar' e interactúa.",
                proTip = "Hablar con NPCs principales desbloquea nuevas fórmulas, mapas y recompensas de historia.",
                spokenSummary = "Nueva tarea: Dirígete a $location para hablar con $targetName y continuar la misión."
            )
            MissionActionType.HUNT_DEFEAT -> GameAdvice(
                title = "Misión: $translatedTitle",
                objective = "Eliminar enemigos / infectados en $location.",
                whereToFind = "$location (Zonas marcadas con calavera o círculo rojo en el mapa).",
                stepByStep = "1. Equipa tu arma de fuego principal y munición.\n2. Viaja a $location y localiza a los objetivos.\n3. Dispara a la cabeza para causar daño crítico y mantén la distancia.",
                proTip = "Lleva vendas y comida antes de salir a combatir para recuperar vida rápidamente.",
                spokenSummary = "Misión de combate: $translatedInstruction en $location. Equipa tus armas y ve a la zona marcada."
            )
            MissionActionType.GATHER_COLLECT -> GameAdvice(
                title = "Misión: $translatedTitle",
                objective = "Recolectar materiales requeridos en $location.",
                whereToFind = "$location (Árboles, piedras y plantas de cáñamo).",
                stepByStep = "1. Equipa hacha o pico en tu mano activa.\n2. Golpea los recursos marcados en tu minimapa.\n3. Verifica el contador de recolección en tu lista de tareas.",
                proTip = "Come alimentos con buff de recolección para talar y minar el doble de rápido.",
                spokenSummary = "Misión de recolección: $translatedInstruction en $location. Abre tu mapa y extrae los recursos."
            )
            MissionActionType.CRAFT_BUILD -> GameAdvice(
                title = "Misión: $translatedTitle",
                objective = "Fabricar el objeto solicitado: $translatedTitle.",
                whereToFind = "Fórmula Portátil (esquina inferior izquierda) o Banco de Materiales en tu Mansión.",
                stepByStep = "1. Pulsa el botón 'Formula' o acércate a tu mesa de trabajo.\n2. Revisa que tengas los materiales requeridos.\n3. Pulsa Fabricar (Craft) y espera a que termine.",
                proTip = "Si te faltan recursos, pulsa sobre ellos para ver en qué mapa conseguirlos gratis.",
                spokenSummary = "Misión de fabricación: Abre tu fórmula portátil o banco de trabajo para fabricar $translatedTitle."
            )
            MissionActionType.DELIVER_SUBMIT -> GameAdvice(
                title = "Misión: $translatedTitle",
                objective = "Entregar suministros o reporte en $location.",
                whereToFind = "$location (Punto de entrega o buzón de envío).",
                stepByStep = "1. Ve a $location siguiendo el marcador del minimapa.\n2. Interactúa con el NPC o buzón de entrega.\n3. Confirma la entrega para recibir tu recompensa.",
                proTip = "No guardes objetos de misión en tus cajas de la casa o el sistema dirá que no los tienes.",
                spokenSummary = "Misión de entrega: Lleva los recursos solicitados a $location."
            )
            MissionActionType.GO_TO_EXPLORE, MissionActionType.GENERAL -> GameAdvice(
                title = "Misión: $translatedTitle",
                objective = "$translatedInstruction ($location).",
                whereToFind = "$location (Sigue la flecha dorada en pantalla o el radar).",
                stepByStep = "1. Abre el mapa tocando el minimapa arriba a la derecha.\n2. Marca el punto de la misión.\n3. Sigue el camino indicado hasta completar el objetivo.",
                proTip = "Activa el sprint automático para moverte más rápido por el mapa.",
                spokenSummary = "Misión activa: $translatedInstruction en $location. Sigue el marcador de ruta para completarla."
            )
        }
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
