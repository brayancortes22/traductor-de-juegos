package com.bscl.gametranslator.glossary

class GameGlossary {

    private val glossaryMap = listOf(
        Regex("""\b(amino\s*acid|amino\s*ácido|ácido\s*amino)\b""", RegexOption.IGNORE_CASE) to "Aminoácido",
        Regex("""\b(portable\s*formula)\b""", RegexOption.IGNORE_CASE) to "Fórmula Portátil",
        Regex("""\b(material\s*bench)\b""", RegexOption.IGNORE_CASE) to "Banco de Materiales",
        Regex("""\b(survival\s*manual)\b""", RegexOption.IGNORE_CASE) to "Manual de Supervivencia",
        Regex("""\b(page\s*fragments?)\b""", RegexOption.IGNORE_CASE) to "Fragmentos de Fórmula",
        Regex("""\b(gathering\s*list)\b""", RegexOption.IGNORE_CASE) to "Lista de Recolección",
        Regex("""\b(gear\s*workstation)\b""", RegexOption.IGNORE_CASE) to "Mesa de Armamento",
        Regex("""\b(repair\s*bench)\b""", RegexOption.IGNORE_CASE) to "Banco de Reparación",
        Regex("""\b(stronghold\s*battle)\b""", RegexOption.IGNORE_CASE) to "Batalla de Fortaleza",
        Regex("""\b(area\s*operation)\b""", RegexOption.IGNORE_CASE) to "Operación de Área",
        Regex("""\b(hope\s*101)\b""", RegexOption.IGNORE_CASE) to "Hope 101",
        Regex("""\b(fall\s*forest)\b""", RegexOption.IGNORE_CASE) to "Bosque de Otoño (Fall Forest)",
        Regex("""\b(sand\s*castle)\b""", RegexOption.IGNORE_CASE) to "Castillo de Arena (Sandcastle)",
        Regex("""\b(snow\s*highland)\b""", RegexOption.IGNORE_CASE) to "Tierras Nevadas (Snow Highland)",
        Regex("""\b(miska\s*university)\b""", RegexOption.IGNORE_CASE) to "Universidad Miska"
    )

    fun applyGlossary(text: String): String {
        var result = text
        for ((pattern, replacement) in glossaryMap) {
            result = pattern.replace(result, replacement)
        }
        return result
    }
}
