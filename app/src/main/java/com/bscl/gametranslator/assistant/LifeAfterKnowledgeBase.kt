package com.bscl.gametranslator.assistant

enum class AdviceType {
    MISSION_TACTICAL,     // Misión y objetivos de mapa
    SKILL_TALENT,         // Habilidades (crear cosas, fuerza, recolección)
    ITEM_EQUIPMENT,       // Ítems, armas, accesorios, fórmulas
    GENERAL_INTERFACE     // Menús, eventos, interfaz general
}

data class GameAdvice(
    val type: AdviceType = AdviceType.MISSION_TACTICAL,
    val title: String,
    val objective: String,
    val whereToGo: String,
    val whatToSearchAndBring: String,
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
                type = AdviceType.ITEM_EQUIPMENT,
                title = "Solución de Aminoácidos Nivel 1",
                objective = "Frasco consumible para curación continua de vida y escudo en combate mediante el Difusor Táctico.",
                whereToGo = "Fórmula Portátil (esquina inferior izquierda) > Básicos > Curación.",
                whatToSearchAndBring = "Madera (100) y Cáñamo (25). Se recolectan talando y cosechando en Fall Forest o alrededor de tu Mansión.",
                stepByStep = "1. Pulsa el botón 'Formula' (esquina inferior izquierda).\n2. Ve a la pestaña 'Básicos / Curación'.\n3. Selecciona 'Lv.1 Amino Acid Solution' y pulsa 'Craft'.",
                proTip = "Lanza el difusor al suelo antes de que los enemigos te rompan el escudo para regenerarte mientras disparas.",
                spokenSummary = "Solución de Aminoácidos Nivel 1. Sirve para curar escudo y vida con el difusor táctico. Se fabrica en tu fórmula portátil con cien de madera y veinticinco de cáñamo."
            )
        ),
        // Material Bench
        AdviceEntry(
            keywords = listOf("material bench", "bench", "craft bench"),
            advice = GameAdvice(
                type = AdviceType.ITEM_EQUIPMENT,
                title = "Banco de Materiales (Material Bench)",
                objective = "Mesa de trabajo esencial para procesar materiales brutos y craftear tablones, ladrillos y componentes de construcción.",
                whereToGo = "Ubicado dentro de tu propia Mansión (Manor). Acércate a la mesa de madera con sierra circular.",
                whatToSearchAndBring = "Materiales brutos traídos de mapas de exploración (madera, piedra, resina, mineral de hierro).",
                stepByStep = "1. Acércate a la mesa hasta ver el botón interactivo 'Craft'.\n2. Toca 'Craft' para abrir el catálogo de fórmulas de construcción.\n3. Selecciona el componente y pulsa Fabricar.",
                proTip = "Guarda tus materiales brutos en gabinetes dentro de tu casa para no perderlos si mueres en mapas infectados.",
                spokenSummary = "Banco de Materiales en tu mansión. Sirve para transformar madera y piedra en tablones y ladrillos para ampliar tu casa."
            )
        ),
        // Page Fragments / Formula Shards
        AdviceEntry(
            keywords = listOf("page fragments", "formula shard", "questionnaire"),
            advice = GameAdvice(
                type = AdviceType.ITEM_EQUIPMENT,
                title = "Fragmentos de Fórmula / Página (Formula Shards)",
                objective = "Moneda especial para tirar en la máquina de fórmulas (Formula R&D) y desbloquear nuevas armas y armaduras avanzadas.",
                whereToGo = "Máquina de Fórmulas en el Campamento, en tu Mansión, o en el menú de Mall / Eventos.",
                whatToSearchAndBring = "Se obtienen respondiendo encuestas del juego, cofres dorados en mapas de exploración y recompensas diarias del Campamento.",
                stepByStep = "1. Completa la encuesta que tienes en pantalla para recibir fragmentos gratis.\n2. Ve a la Máquina de Fórmulas en el Campamento o Mansión.\n3. Selecciona tirada de armas Grado 2 o 3 y fusiona tus fragmentos.",
                proTip = "Ahorra tus fragmentos para tiradas de armas de fuego de combate (UZI, Thompson, 590M), nunca los gastes en decoraciones o muebles.",
                spokenSummary = "Fragmentos de Fórmula. Sirven para tirar en la máquina de fórmulas y sacar armas de fuego mejores. Ahorra todos los fragmentos para fusiles o escopetas."
            )
        ),
        // Helena Wayne (Hope 101)
        AdviceEntry(
            keywords = listOf("helena", "helena wayne", "follow helena"),
            advice = GameAdvice(
                type = AdviceType.MISSION_TACTICAL,
                title = "Misión: Acompañar a Helena Wayne",
                objective = "Seguir y escoltar a Helena Wayne hasta su punto de reunión en la zona segura de Hope 101.",
                whereToGo = "Hope 101 (Plaza del monumento conmemorativo / Zona de supervivientes).",
                whatToSearchAndBring = "Busca a Helena Wayne (NPC con abrigo rojo y mochila). Mantén tus armas guardadas para no gastar durabilidad.",
                stepByStep = "1. Permanece caminando a pocos metros de Helena Wayne sin adelantarte.\n2. Sigue el indicador dorado en el minimapa si la pierdes de vista.\n3. Al llegar a su destino, espera a que se detenga y habla con ella para completar la misión.",
                proTip = "Hope 101 es una zona segura libre de zombis. No corras con sprint rápido o Helena se quedará atrás y tendrás que regresar a buscarla.",
                spokenSummary = "Misión activa: Escolta a Helena Wayne en Hope 101. Camina a su lado hasta el punto marcado en el mapa y habla con ella al llegar."
            )
        ),
        // Rachel (Hope 101 / Commerce Guild)
        AdviceEntry(
            keywords = listOf("rachel", "talk to rachel", "find rachel"),
            advice = GameAdvice(
                type = AdviceType.MISSION_TACTICAL,
                title = "Misión: Hablar con Rachel",
                objective = "Reunirte con Rachel en el Ayuntamiento de Hope 101 para avanzar en la historia de la Commerce Guild.",
                whereToGo = "Hope 101 (Ayuntamiento / Commerce Guild, edificio principal con banderas).",
                whatToSearchAndBring = "Busca a Rachel parada junto a la mesa de conferencias o el mostrador de bienvenida.",
                stepByStep = "1. Abre el mapa pulsando arriba a la derecha y localiza el Ayuntamiento.\n2. Entra al edificio y sube las escaleras hacia Rachel.\n3. Presiona el botón interactivo de diálogo para continuar la historia.",
                proTip = "Rachel desbloquea permisos para viajar a nuevos mapas infectados y otorga recompensas de supervivencia valiosas.",
                spokenSummary = "Misión: Ve con Rachel al ayuntamiento de Hope 101, entra al edificio principal y pulsa hablar para desbloquear nuevos mapas."
            )
        ),
        // Anna (Medical Personnel / Fatal Trials)
        AdviceEntry(
            keywords = listOf("anna", "fatal trials", "medical personnel", "go see what"),
            advice = GameAdvice(
                type = AdviceType.MISSION_TACTICAL,
                title = "Misión: Fatal Trials - Hablar con Anna",
                objective = "Hablar con Anna (Personal Médico) frente a la camilla para recibir auxilio y avanzar la historia de evacuación.",
                whereToGo = "Frente a ti en la carretera (indicador dorado de 1 metro).",
                whatToSearchAndBring = "Busca a Anna (paramédica de uniforme blanco y chaqueta clara junto a la camilla). No necesitas armas desenfundadas.",
                stepByStep = "1. Acércate a 1 metro de Anna.\n2. Toca el botón naranja 'Talk' (Hablar) que aparece a la derecha de la pantalla.\n3. Avanza en el diálogo para recibir suministros médicos iniciales.",
                proTip = "Completar Fatal Trials te desbloqueará tu primera mochila de supervivencia y equipo de combate.",
                spokenSummary = "Misión activa: Habla con Anna. Acércate a la paramédica frente a la camilla y toca el botón naranja Talk a la derecha para continuar."
            )
        ),
        // Survival Manual específico
        AdviceEntry(
            keywords = listOf("survival manual chapter", "claim survival manual"),
            advice = GameAdvice(
                type = AdviceType.MISSION_TACTICAL,
                title = "Manual de Supervivencia: Misión de Capítulo",
                objective = "Completar la guía de tareas del capítulo activo del Manual de Supervivencia para subir rápido a nivel 15.",
                whereToGo = "Abre 'Survival Manual' tocando el icono del libro arriba a la derecha en la pantalla principal.",
                whatToSearchAndBring = "Revisa los materiales o acciones requeridas en la lista del capítulo actual.",
                stepByStep = "1. Toca el botón del Manual de Supervivencia.\n2. Revisa la lista de tareas del capítulo activo.\n3. Completa primero las misiones de fabricación y luego las de recolección en mapas.",
                proTip = "Reclamar cada capítulo otorga armas gratis, New Dollars y grandes cantidades de oro.",
                spokenSummary = "Revisa tu manual de supervivencia tocando el libro arriba a la derecha para completar las tareas de capítulo y ganar oro rápido."
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
