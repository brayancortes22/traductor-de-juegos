package com.bscl.gametranslator.assistant

data class GameAdvice(
    val title: String,
    val objective: String,
    val whereToFind: String,
    val stepByStep: String,
    val proTip: String,
    val spokenSummary: String
)

class LifeAfterKnowledgeBase {

    private val entries = listOf(
        // Amino Acid
        AdviceEntry(
            keywords = listOf("amino acid", "amino", "lv.1 amino"),
            advice = GameAdvice(
                title = "Solución de Aminoácidos Nivel 1",
                objective = "Fabricar Solución de Aminoácidos Nivel 1 para curación en el difusor.",
                whereToFind = "Madera (100) y Cáñamo (25). Se recolectan fácilmente en Bosque de Otoño (Fall Forest) o alrededor de tu Mansión.",
                stepByStep = "1. Pulsa el botón 'Formula' (esquina inferior izquierda).\n2. Ve a la pestaña 'Básicos / Curación'.\n3. Selecciona 'Lv.1 Amino Acid Solution' y pulsa 'Craft'.",
                proTip = "Equípalo en tu Difusor Táctico para curar escudo y vida en plena batalla.",
                spokenSummary = "Para fabricar aminoácido nivel 1, abre la fórmula portátil abajo a la izquierda, entra a Básicos y dale a fabricar con madera y cáñamo."
            )
        ),
        // Material Bench
        AdviceEntry(
            keywords = listOf("material bench", "bench", "craft bench"),
            advice = GameAdvice(
                title = "Banco de Materiales (Material Bench)",
                objective = "Procesar materiales brutos y craftear tablones, ladrillos y componentes.",
                whereToFind = "Ubicado dentro de tu propia Mansión (Manor). Acércate a la mesa de madera con sierra circular.",
                stepByStep = "1. Acércate a la mesa hasta ver el botón 'Craft'.\n2. Toca 'Craft' para abrir el inventario de fórmulas de construcción y materiales.",
                proTip = "Guarda tus materiales brutos en gabinetes dentro de tu casa para no perderlos si mueres en mapas infectados.",
                spokenSummary = "Acércate al banco de materiales en tu mansión y pulsa Fabricar para crear componentes de construcción."
            )
        ),
        // Page Fragments / Formula Shards
        AdviceEntry(
            keywords = listOf("page fragments", "formula shard", "questionnaire"),
            advice = GameAdvice(
                title = "Fragmentos de Fórmula / Página",
                objective = "Obtener fragmentos para tirar en la máquina de fórmulas y desbloquear mejores armas.",
                whereToFind = "Respondiendo la encuesta del juego, cofres dorados en mapas de exploración y recompensas diarias del Campamento.",
                stepByStep = "1. Completa la encuesta que tienes en pantalla para recibir 20 fragmentos.\n2. Ve a la Máquina de Fórmulas en el Campamento o Mansión para fusionar fragmentos.",
                proTip = "Ahorra tus fragmentos para las tiradas de armas Grado 2 y 3 (UZI, Thompson, 590M), no los gastes en muebles.",
                spokenSummary = "Completa la encuesta en pantalla para recibir 20 fragmentos de fórmula gratis. Guárdalos para conseguir armas mejores."
            )
        ),
        // Survival Manual / Battle Upgrade
        AdviceEntry(
            keywords = listOf("survival manual", "battle upgrade", "gathering list"),
            advice = GameAdvice(
                title = "Manual de Supervivencia: Mejora de Batalla",
                objective = "Completar los objetivos del capítulo actual del Manual de Supervivencia.",
                whereToFind = "Abre 'Survival Manual' arriba a la derecha en el menú principal para reclamar recompensas acumuladas.",
                stepByStep = "1. Revisa la lista de tareas en la esquina superior izquierda.\n2. Completa primero las misiones de fabricación rápida y luego las de recolección.",
                proTip = "El Manual de Supervivencia te da oro y experiencia gratis. Terminarlo es la forma más rápida de subir a nivel 15.",
                spokenSummary = "Revisa tu manual de supervivencia arriba a la derecha. Completar estas misiones es la forma más rápida de subir de nivel."
            )
        )
    )

    fun findAdvice(ocrText: String): GameAdvice? {
        val lower = ocrText.lowercase()
        return entries.firstOrNull { entry ->
            entry.keywords.any { keyword -> lower.contains(keyword) }
        }?.advice
    }

    private data class AdviceEntry(
        val keywords: List<String>,
        val advice: GameAdvice
    )
}
