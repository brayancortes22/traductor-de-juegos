package com.bscl.gametranslator.assistant

import android.graphics.Rect
import com.bscl.gametranslator.model.DetectedTextBlock

enum class MissionActionType {
    FOLLOW_ESCORT,      // Follow, Escort, Accompany
    TALK_INTERACT,      // Talk to, Speak with, Report to, Meet
    HUNT_DEFEAT,        // Defeat, Kill, Eliminate, Clear, Hunt
    GATHER_COLLECT,     // Gather, Collect, Mine, Chop
    CRAFT_BUILD,        // Craft, Make, Build, Upgrade
    DELIVER_SUBMIT,     // Deliver, Submit, Send
    GO_TO_EXPLORE,      // Go to, Reach, Head to, Investigate
    GENERAL
}

data class ParsedMission(
    val title: String,
    val location: String,
    val currentInstruction: String,
    val targetName: String,
    val actionType: MissionActionType,
    val rawText: String
)

class GameMissionParser {

    private val knownLocations = listOf(
        "Hope 101", "Fall Forest", "Sand Castle", "Snow Highlands", "Mount Snow",
        "Mouth Swamp", "Redwood Town", "Levin City", "Camp", "Manor", "Downtown",
        "Clear Sky Station", "St. Rona", "Santopany", "Farstar City"
    )

    private val knownNpcs = listOf(
        "Helena Wayne", "Helena", "Rachel", "Justin", "Chris", "Edward",
        "Fernandez", "Arthur", "Ingrid", "Billy", "Alex", "Ivan", "Anna", "Mia"
    )

    fun parseMission(
        blocks: List<DetectedTextBlock>,
        screenWidth: Int,
        screenHeight: Int
    ): ParsedMission? {
        if (blocks.isEmpty()) return null

        // 1. Filtrar bloques ubicados en el HUD de misiones (cuadrante superior izquierdo)
        val questBlocks = blocks.filter { block ->
            val b = block.boundingBox ?: return@filter false
            val isLeft = b.left < (screenWidth * 0.45)
            val isTopMid = b.top >= (screenHeight * 0.08) && b.bottom <= (screenHeight * 0.65)
            isLeft && isTopMid
        }.sortedBy { it.boundingBox?.top ?: 0 }

        val candidateBlocks = if (questBlocks.isNotEmpty()) questBlocks else blocks
        val lines = candidateBlocks
            .flatMap { it.originalText.lines() }
            .map { it.trim() }
            .filter { isValidMissionLine(it) }

        if (lines.isEmpty()) return null

        // Detectar ubicación
        var detectedLocation = ""
        for (loc in knownLocations) {
            if (lines.any { it.contains(loc, ignoreCase = true) }) {
                detectedLocation = loc
                break
            }
        }

        // Detectar NPC o Personaje
        var detectedTarget = ""
        for (npc in knownNpcs) {
            if (lines.any { it.contains(npc, ignoreCase = true) }) {
                detectedTarget = npc
                break
            }
        }

        // Título e Instrucción
        val titleCandidate = lines.firstOrNull { line ->
            !line.equals(detectedLocation, ignoreCase = true) &&
                    !line.startsWith("Gathering List", ignoreCase = true)
        } ?: lines.first()

        val instructionCandidate = lines.firstOrNull { line ->
            line != titleCandidate &&
                    !line.equals(detectedLocation, ignoreCase = true) &&
                    !line.startsWith("Gathering List", ignoreCase = true)
        } ?: titleCandidate

        val actionType = determineActionType(instructionCandidate + " " + titleCandidate)

        return ParsedMission(
            title = titleCandidate,
            location = if (detectedLocation.isNotBlank()) detectedLocation else "Zona de Misión",
            currentInstruction = instructionCandidate,
            targetName = detectedTarget,
            actionType = actionType,
            rawText = lines.joinToString("\n")
        )
    }

    private fun isValidMissionLine(text: String): Boolean {
        val trimmed = text.trim()
        if (trimmed.length < 3) return false
        // Excluir ruidos de HUD (balas, ping, porcentajes)
        if (trimmed.matches(Regex("""^\d+.*""")) && !trimmed.contains("Hope 101")) return false
        if (trimmed.equals("Training", ignoreCase = true)) return false
        if (trimmed.equals("Mall", ignoreCase = true)) return false
        if (trimmed.equals("Benefits", ignoreCase = true)) return false
        if (trimmed.equals("Survival Manual", ignoreCase = true)) return false
        if (trimmed.equals("Daily", ignoreCase = true)) return false
        return true
    }

    private fun determineActionType(text: String): MissionActionType {
        val lower = text.lowercase()
        return when {
            lower.contains("follow") || lower.contains("escort") || lower.contains("accompany") ->
                MissionActionType.FOLLOW_ESCORT

            lower.contains("talk") || lower.contains("speak") || lower.contains("report to") || lower.contains("meet") || lower.contains("tell") || lower.contains("see what") || lower.contains("go see") ->
                MissionActionType.TALK_INTERACT

            lower.contains("defeat") || lower.contains("kill") || lower.contains("eliminate") || lower.contains("hunt") || lower.contains("clear") ->
                MissionActionType.HUNT_DEFEAT

            lower.contains("gather") || lower.contains("collect") || lower.contains("mine") || lower.contains("chop") || lower.contains("pick") ->
                MissionActionType.GATHER_COLLECT

            lower.contains("craft") || lower.contains("make") || lower.contains("build") || lower.contains("upgrade") ->
                MissionActionType.CRAFT_BUILD

            lower.contains("deliver") || lower.contains("submit") || lower.contains("send") || lower.contains("bring") ->
                MissionActionType.DELIVER_SUBMIT

            lower.contains("go to") || lower.contains("reach") || lower.contains("head to") || lower.contains("investigate") || lower.contains("enter") ->
                MissionActionType.GO_TO_EXPLORE

            else -> MissionActionType.GENERAL
        }
    }
}
