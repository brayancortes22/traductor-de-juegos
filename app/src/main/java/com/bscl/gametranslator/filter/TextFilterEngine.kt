package com.bscl.gametranslator.filter

import com.bscl.gametranslator.model.TextBlockData

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

    fun filterBlocks(blocks: List<TextBlockData>): List<TextBlockData> {
        return blocks.filter { block ->
            !isIrrelevant(block.originalText)
        }
    }

    fun extractCleanFullText(blocks: List<TextBlockData>): String {
        return filterBlocks(blocks)
            .map { it.originalText.trim() }
            .filter { it.isNotEmpty() }
            .joinToString("\n")
    }
}
