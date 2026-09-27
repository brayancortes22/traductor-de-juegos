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
    private val skillDatabase = LifeAfterSkillDatabase()
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
        // 1. Detectar si la pantalla muestra un árbol de habilidades o talentos (Combate/Fuerza, Crafteo, Recolección)
        val skillAdvice = skillDatabase.findSkillAdvice(ocrText)
        if (skillAdvice != null) {
            return@withContext skillAdvice
        }

        // 2. Intentar parsear quirúrgicamente la misión activa del HUD de LifeAfter
        val parsedMission = missionParser.parseMission(blocks, screenWidth, screenHeight)
        if (parsedMission != null) {
            val missionAdvice = buildMissionAdvice(parsedMission, translatorEngine)
            return@withContext missionAdvice
        }

        // 3. Verificar base de conocimiento offline de ítems, fórmulas y NPCs
        val offlineAdvice = knowledgeBase.findAdvice(ocrText)
        if (offlineAdvice != null) {
            return@withContext offlineAdvice
        }

        // 4. Si hay API key de Gemini, consultar online con contexto táctico completo
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
                type = AdviceType.MISSION_TACTICAL,
                title = "Misión: $translatedTitle",
                objective = "Seguir y escoltar a $targetName manteniéndote a su lado sin separarte.",
                whereToGo = "$location (Sigue la estela dorada y el marcador en el minimapa).",
                whatToSearchAndBring = "Localiza a $targetName caminando a paso constante. Mantén espacio libre en la mochila y armas guardadas en zona segura para no gastar durabilidad.",
                stepByStep = "1. Permanece a pocos metros de $targetName sin adelantarte corriendo.\n2. Si se detiene para hablar con alguien, quédate a su lado.\n3. Al llegar a su destino final, pulsa el botón de diálogo para completar la misión.",
                proTip = "No actives el sprint automático al máximo ni te adelantes, o el NPC detendrá su marcha y la misión se pausará hasta que vuelvas.",
                spokenSummary = "Misión activa: Escolta a $targetName en $location. Camina a su lado sin separarte hasta llegar al punto final."
            )
            MissionActionType.TALK_INTERACT -> GameAdvice(
                type = AdviceType.MISSION_TACTICAL,
                title = "Misión: $translatedTitle",
                objective = "Localizar y hablar con $targetName en $location para avanzar la historia.",
                whereToGo = "$location (Abre el minimapa arriba a la derecha y busca el ícono amarillo de diálogo).",
                whatToSearchAndBring = "Busca a $targetName (personaje con nombre sobre la cabeza). No necesitas llevar armas desenfundadas.",
                stepByStep = "1. Toca el minimapa arriba a la derecha y marca a $targetName.\n2. Sigue el rumbo hasta estar frente a ella o él.\n3. Acércate hasta que aparezca el botón de interacción 'Hablar' e interactúa.",
                proTip = "Lee las opciones de diálogo con atención; algunas otorgan suministros de supervivencia gratis o aumentan la amistad del NPC.",
                spokenSummary = "Misión de diálogo: Ve a $location para hablar con $targetName. Sigue el punto amarillo del mapa e interactúa al acercarte."
            )
            MissionActionType.HUNT_DEFEAT -> GameAdvice(
                type = AdviceType.MISSION_TACTICAL,
                title = "Misión: $translatedTitle",
                objective = "Eliminar a los infectados o criaturas indicadas ($translatedInstruction).",
                whereToGo = "$location (Zonas marcadas con calavera roja o círculo de peligro biológico en el mapa).",
                whatToSearchAndBring = "Arma principal con durabilidad, caja de munición portátil, vendas medicinales y espacio para recoger grasa o garras infectadas.",
                stepByStep = "1. Carga tu munición y equipa vendas en tu acceso rápido.\n2. Viaja a $location y localiza a los objetivos.\n3. Dispara a la cabeza para infligir daño crítico y mantén distancia de salto.",
                proTip = "Súbete a rocas altas o tejados si la horda es grande para disparar con total seguridad sin recibir golpes cuerpo a cuerpo.",
                spokenSummary = "Misión de combate: Elimina los objetivos en $location. Lleva munición suficiente, vendas y apunta a la cabeza."
            )
            MissionActionType.GATHER_COLLECT -> GameAdvice(
                type = AdviceType.MISSION_TACTICAL,
                title = "Misión: $translatedTitle",
                objective = "Recolectar los materiales requeridos para la misión ($translatedInstruction).",
                whereToGo = "$location (Zonas boscosas, canteras o planicies según el recurso).",
                whatToSearchAndBring = "Hacha de hierro o pico de piedra en mano, espacio libre en el inventario y comida con buff de velocidad de tala/minería.",
                stepByStep = "1. Equipa la herramienta adecuada en tu mano activa.\n2. Acércate a los recursos marcados en tu minimapa.\n3. Golpea continuamente hasta llenar el contador de recolección de la misión.",
                proTip = "Antes de salir del mapa infectado, envía tus materiales recolectados a través del helicóptero de evacuación para no perderlos si caes.",
                spokenSummary = "Misión de recolección: Consigue los materiales en $location. Equipa tu herramienta, vacía tu mochila y recolecta lo marcado en el mapa."
            )
            MissionActionType.CRAFT_BUILD -> GameAdvice(
                type = AdviceType.MISSION_TACTICAL,
                title = "Misión: $translatedTitle",
                objective = "Fabricar el objeto solicitado: $translatedTitle ($translatedInstruction).",
                whereToGo = "Fórmula Portátil (icono de llave abajo a la izquierda) o Banco de Materiales en tu Mansión.",
                whatToSearchAndBring = "Revisa los materiales faltantes en la receta. Consíguelos en los gabinetes de tu casa o en Fall Forest.",
                stepByStep = "1. Toca 'Formula' o acércate a tu mesa de crafteo en la mansión.\n2. Selecciona la receta indicada.\n3. Pulsa Fabricar (Craft) y espera a que termine para recoger el objeto.",
                proTip = "Si te faltan recursos, pulsa directamente sobre el material en la lista de crafteo para ver su método de obtención gratis.",
                spokenSummary = "Misión de fabricación: Abre tu fórmula portátil o ve a tu mesa de trabajo para crear $translatedTitle."
            )
            MissionActionType.DELIVER_SUBMIT -> GameAdvice(
                type = AdviceType.MISSION_TACTICAL,
                title = "Misión: $translatedTitle",
                objective = "Entregar suministros o reporte de misión en $location.",
                whereToGo = "$location (Puesto de avanzada, helicóptero o NPC receptor).",
                whatToSearchAndBring = "Los objetos solicitados deben estar en tu inventario activo (no en los cofres de tu casa).",
                stepByStep = "1. Revisa tu mochila para confirmar que tienes los ítems requeridos.\n2. Dirígete al receptor en $location siguiendo la flecha del minimapa.\n3. Pulsa el botón de entrega para cobrar tu recompensa en oro y experiencia.",
                proTip = "No guardes suministros de misión en cajas privadas del campamento o el juego marcará que no los posees.",
                spokenSummary = "Misión de entrega: Lleva los recursos solicitados a $location y entrégalos al receptor para cobrar la recompensa."
            )
            MissionActionType.GO_TO_EXPLORE, MissionActionType.GENERAL -> GameAdvice(
                type = AdviceType.MISSION_TACTICAL,
                title = "Misión: $translatedTitle",
                objective = "Completar el objetivo de mapa: $translatedInstruction ($location).",
                whereToGo = "$location (Sigue la flecha dorada en pantalla y la distancia en metros del radar).",
                whatToSearchAndBring = "Puntos de control, puertas de acceso o balizas de exploración. Lleva equipo básico de supervivencia y vendas.",
                stepByStep = "1. Toca el minimapa arriba a la derecha para ver la ruta general.\n2. Marca el punto de destino.\n3. Avanza por los caminos principales evitando las zonas de niebla tóxica.",
                proTip = "Activa el sprint automático arrastrando hacia arriba el joystick de movimiento para desplazarte a máxima velocidad.",
                spokenSummary = "Misión activa: $translatedInstruction en $location. Sigue la flecha del minimapa para llegar a tu destino."
            )
        }
    }

    private fun queryGeminiAssistant(ocrText: String, apiKey: String): GameAdvice? {
        val endpoint = "https://generativelanguage.googleapis.com/v1beta/models/gemini-1.5-flash:generateContent?key=$apiKey"
        val prompt = """
            Eres un copiloto y asesor táctico experto en videojuegos de supervivencia como LifeAfter.
            Analiza el texto extraído de la pantalla del juego (puede ser una misión, un menú de habilidades de combate/fuerza/crafteo, o un ítem/fórmula) y responde en formato JSON EXACTO con las siguientes claves:
            {
              "type": "MISSION_TACTICAL" o "SKILL_TALENT" o "ITEM_EQUIPMENT",
              "title": "Nombre de la misión, habilidad o ítem",
              "objective": "Si es misión: qué debe hacer. Si es habilidad o ítem: qué es y para qué sirve con impacto en el juego",
              "whereToGo": "Si es misión: a dónde ir exactamente. Si es habilidad o ítem: en qué menú o banco de trabajo se desbloquea/equipa",
              "whatToSearchAndBring": "Si es misión: qué buscar en la zona y qué llevar de equipo. Si es habilidad: costo en puntos/dólares. Si es ítem: materiales requeridos",
              "stepByStep": "Pasos simples numerados 1, 2, 3 para resolverlo o mejorarlo",
              "proTip": "Consejo táctico pro o si vale la pena subirlo de nivel",
              "spokenSummary": "Frase clara y directa en español (máximo 2 oraciones) para leerle al jugador por voz mientras juega"
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

        val rawType = parsed.optString("type", "MISSION_TACTICAL")
        val adviceType = try {
            AdviceType.valueOf(rawType)
        } catch (_: Exception) {
            AdviceType.MISSION_TACTICAL
        }

        return GameAdvice(
            type = adviceType,
            title = parsed.optString("title", "Guía Táctica de Juego"),
            objective = parsed.optString("objective", "Revisar objetivo o detalles en pantalla."),
            whereToGo = parsed.optString("whereToGo", "Revisa tu mapa y almacenes."),
            whatToSearchAndBring = parsed.optString("whatToSearchAndBring", "Verifica los requisitos e inventario."),
            stepByStep = parsed.optString("stepByStep", "Sigue las indicaciones de la pantalla."),
            proTip = parsed.optString("proTip", "Guarda recursos raros en tu mansión para no perderlos."),
            spokenSummary = parsed.optString("spokenSummary", "He analizado la pantalla. Revisa el objetivo y los pasos tácticos para avanzar.")
        )
    }

    private fun generateHeuristicAdvice(ocrText: String): GameAdvice {
        val lines = ocrText.lines().map { it.trim() }.filter { it.length > 3 }
        val firstMeaningful = lines.firstOrNull() ?: "Elemento de Juego Detectado"

        return GameAdvice(
            type = AdviceType.MISSION_TACTICAL,
            title = "Objetivo Detectado",
            objective = "Completar la acción requerida: $firstMeaningful",
            whereToGo = "Abre tu mapa tocando arriba a la derecha para localizar el rumbo exacto.",
            whatToSearchAndBring = "Verifica tu inventario, herramientas (hacha/pico), munición y espacio libre en la mochila.",
            stepByStep = "1. Abre el mapa de exploración.\n2. Localiza el punto de recolección o combate indicado.\n3. Regresa a tu mansión o banco de trabajo para fabricar o cobrar recompensas.",
            proTip = "Mantén siempre espacio libre en tu inventario para no perder botín valioso al explorar.",
            spokenSummary = "Nueva tarea detectada: $firstMeaningful. Abre tu mapa o inventario para ubicar los recursos."
        )
    }
}
