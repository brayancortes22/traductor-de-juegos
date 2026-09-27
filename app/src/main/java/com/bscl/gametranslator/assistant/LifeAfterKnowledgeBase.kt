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
        // Helena Wayne (Hope 101)
        AdviceEntry(
            keywords = listOf("helena", "helena wayne", "follow helena"),
            advice = GameAdvice(
                title = "Misión: Acompañar a Helena Wayne",
                objective = "Seguir a Helena Wayne hasta su destino en Hope 101.",
                whereToFind = "Hope 101 (Plaza del monumento conmemorativo / Zona de supervivientes).",
                stepByStep = "1. Permanece cerca de Helena Wayne mientras camina hacia su destino.\n2. Sigue el indicador dorado en el minimapa si te separas.\n3. Al llegar, interactúa con ella para completar la misión y reclamar tu recompensa.",
                proTip = "Hope 101 es una zona segura libre de zombis. Guarda tus armas para no gastar durabilidad.",
                spokenSummary = "Misión activa: Sigue a Helena Wayne en Hope 101 de cerca hasta su destino para completar la misión."
            )
        ),
        // Rachel (Hope 101 / Commerce Guild)
        AdviceEntry(
            keywords = listOf("rachel", "talk to rachel", "find rachel"),
            advice = GameAdvice(
                title = "Misión: Hablar con Rachel",
                objective = "Reunirte con Rachel en el Ayuntamiento de Hope 101.",
                whereToFind = "Hope 101 (Ayuntamiento / Commerce Guild).",
                stepByStep = "1. Dirígete al Ayuntamiento marcado en el minimapa.\n2. Sube y acércate a Rachel.\n3. Presiona el botón de diálogo para avanzar la historia.",
                proTip = "Rachel es la guía de la Commerce Guild y te desbloqueará nuevas zonas de exploración.",
                spokenSummary = "Ve con Rachel al ayuntamiento de Hope 101 y pulsa el botón de diálogo al acercarte."
            )
        ),
        // Survival Manual específico
        AdviceEntry(
            keywords = listOf("survival manual chapter", "claim survival manual"),
            advice = GameAdvice(
                title = "Manual de Supervivencia: Misión de Capítulo",
                objective = "Completar los objetivos del capítulo actual del Manual de Supervivencia.",
                whereToFind = "Abre 'Survival Manual' arriba a la derecha en el menú principal.",
                stepByStep = "1. Revisa la lista de tareas del capítulo activo.\n2. Completa primero las misiones de fabricación y luego las de recolección.",
                proTip = "El Manual de Supervivencia otorga oro y experiencia rápida para subir a nivel 15.",
                spokenSummary = "Revisa tu manual de supervivencia arriba a la derecha para reclamar recompensas de capítulo."
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
