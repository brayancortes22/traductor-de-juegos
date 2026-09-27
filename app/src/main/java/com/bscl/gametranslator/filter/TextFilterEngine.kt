package com.bscl.gametranslator.filter

import com.bscl.gametranslator.model.DetectedTextBlock

class TextFilterEngine {

    private val ammoPattern = Regex("""^\d+\s*/\s*\d+$""")
    private val pingPattern = Regex("""^\d+\s*ms$""", RegexOption.IGNORE_CASE)
    private val isolatedNumberPattern = Regex("""^[\d\s(),.+#/~:_-]+$""")
    private val timePattern = Regex("""^\d{1,2}:\d{2}(:\d{2})?$""")
    private val dateStampPattern = Regex("""^\d{2}-\d{2}\s+\d{2}:\d{2}.*$""")

    fun isIrrelevant(text: String): Boolean {
        val trimmed = text.trim()
        if (trimmed.length < 2) return true
        if (ammoPattern.matches(trimmed)) return true
        if (pingPattern.matches(trimmed)) return true
        if (isolatedNumberPattern.matches(trimmed)) return true
        if (timePattern.matches(trimmed)) return true
        if (dateStampPattern.matches(trimmed)) return true
        return false
    }

    /**
     * Filtra la marquesina de chat general/campamento/mundo ubicada en la franja inferior central.
     * Evita que spam de otros jugadores llene la pantalla de pastillas constantes e invasivas.
     */
    fun isBottomWorldChat(
        block: DetectedTextBlock,
        screenWidth: Int,
        screenHeight: Int
    ): Boolean {
        val b = block.boundingBox ?: return false
        val text = block.originalText.lowercase()

        // 1. Detección por coordenadas: franja inferior central de chat
        val isBottomRegion = b.top >= (screenHeight * 0.78) &&
                b.left >= (screenWidth * 0.15) &&
                b.right <= (screenWidth * 0.88)

        // 2. Detección por tags de canales de chat típicos en juegos
        val hasChatTag = text.startsWith("[camp") ||
                text.startsWith("[world") ||
                text.startsWith("[recruit") ||
                text.startsWith("[novato") ||
                text.startsWith("[trade") ||
                text.startsWith("[team") ||
                text.contains("[emoji") ||
                text.contains("camp chat") ||
                text.contains("world chat")

        return isBottomRegion && (hasChatTag || b.top >= (screenHeight * 0.83))
    }

    fun filterBlocks(
        blocks: List<DetectedTextBlock>,
        screenWidth: Int = 1340,
        screenHeight: Int = 800,
        suppressBottomChat: Boolean = true
    ): List<DetectedTextBlock> {
        return blocks.filter { block ->
            if (isIrrelevant(block.originalText)) return@filter false
            if (suppressBottomChat && isBottomWorldChat(block, screenWidth, screenHeight)) return@filter false
            true
        }
    }

    fun extractCleanFullText(
        blocks: List<DetectedTextBlock>,
        screenWidth: Int = 1340,
        screenHeight: Int = 800
    ): String {
        return filterBlocks(blocks, screenWidth, screenHeight, suppressBottomChat = true)
            .map { it.originalText.trim() }
            .filter { it.isNotEmpty() }
            .joinToString("\n")
    }
}
