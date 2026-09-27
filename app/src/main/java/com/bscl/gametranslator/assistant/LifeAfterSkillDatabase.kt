package com.bscl.gametranslator.assistant

class LifeAfterSkillDatabase {

    private val skills = listOf(
        // === HABILIDADES DE FUERZA Y COMBATE ===
        SkillEntry(
            keywords = listOf("light firearm", "firearm mastery", "light weapons", "rifle mastery"),
            advice = GameAdvice(
                type = AdviceType.SKILL_TALENT,
                title = "Dominio de Armas Ligeras (Fuerza / Combate)",
                objective = "Aumenta el daño de ataque, cadencia y precisión al disparar con rifles de asalto y subfusiles (SMG).",
                whereToGo = "Menú de Personaje > Habilidades (Skills) > Pestaña de Combate.",
                whatToSearchAndBring = "Puntos de Habilidad de Combate y New Dollars. Requiere nivel de Maestría de Combate.",
                stepByStep = "1. Abre el menú principal arriba a la derecha y toca 'Skills'.\n2. Entra a la categoría 'Combat'.\n3. Selecciona 'Light Firearm Mastery' y pulsa 'Upgrade'.",
                proTip = "Prioridad ALTA. Es la habilidad más importante para subir daño en misiones diarias, hordas y combates contra jefes.",
                spokenSummary = "Habilidad de Dominio de Armas Ligeras. Aumenta el daño de tus rifles y subfusiles. Súbela en Habilidades de Combate usando puntos y New Dollars. Prioridad alta."
            )
        ),
        SkillEntry(
            keywords = listOf("heavy firearm", "shotgun mastery", "heavy weapon", "bazooka"),
            advice = GameAdvice(
                type = AdviceType.SKILL_TALENT,
                title = "Dominio de Armas Pesadas (Escopetas y Artillería)",
                objective = "Incrementa el daño masivo a corta distancia de escopetas (590M, etc.) y lanzagranadas.",
                whereToGo = "Menú de Personaje > Habilidades > Pestaña de Combate.",
                whatToSearchAndBring = "Puntos de Combate y New Dollars.",
                stepByStep = "1. Ve a Skills > Combat.\n2. Busca Heavy Firearm Mastery.\n3. Mejora los niveles disponibles según tu maestría.",
                proTip = "Esencial si usas escopeta en batallas de campamento o mazmorras cerradas donde los infectados atacan cuerpo a cuerpo.",
                spokenSummary = "Dominio de Armas Pesadas. Aumenta el impacto de escopetas y lanzagranadas a corta distancia. Muy recomendada para limpiar zombis rápido."
            )
        ),
        SkillEntry(
            keywords = listOf("physical strength", "stamina", "vitality", "vigor"),
            advice = GameAdvice(
                type = AdviceType.SKILL_TALENT,
                title = "Fuerza Física y Resistencia (Vigor / Stamina)",
                objective = "Aumenta la barra de energía para esprintar, saltar y rodar sin cansarte rápido, y reduce el consumo de vigor al talar o picar.",
                whereToGo = "Menú > Skills > Pestaña General / Supervivencia.",
                whatToSearchAndBring = "Puntos de habilidad generales y New Dollars.",
                stepByStep = "1. Entra a Skills.\n2. Localiza Physical Fitness o Vigor Enhancement.\n3. Mejora al máximo nivel permitido.",
                proTip = "Vital para moverte rápido por mapas grandes de exploración y escapar de zombis veloces sin quedarte sin aliento.",
                spokenSummary = "Habilidad de Fuerza Física y Resistencia. Te permite correr y esquivar más tiempo sin cansarte. Esencial para explorar mapas abiertos."
            )
        ),
        SkillEntry(
            keywords = listOf("combat recovery", "hp recovery", "regeneration", "damage reduction"),
            advice = GameAdvice(
                type = AdviceType.SKILL_TALENT,
                title = "Recuperación de Combate y Resistencia al Daño",
                objective = "Reduce el daño recibido por mordeduras o balas y acelera la recuperación de escudo táctico.",
                whereToGo = "Menú > Skills > Pestaña de Combate / Defensa.",
                whatToSearchAndBring = "Puntos de Combate y Maestría defensiva.",
                stepByStep = "1. Ve a Skills > Combat > Defense.\n2. Pulsa en Damage Reduction / Combat Recovery.\n3. Mejora para ganar porcentaje pasivo de armadura.",
                proTip = "Te salvará de morir en incursiones nocturnas con zombis mutantes.",
                spokenSummary = "Recuperación de combate. Disminuye el daño recibido y recarga tu escudo más rápido. Muy útil para sobrevivir hordas nocturnas."
            )
        ),

        // === HABILIDADES DE CREACIÓN Y FABRICACIÓN ===
        SkillEntry(
            keywords = listOf("field craft", "quick craft", "portable craft", "crafting speed"),
            advice = GameAdvice(
                type = AdviceType.SKILL_TALENT,
                title = "Fabricación de Campo / Crafteo Rápido",
                objective = "Reduce el tiempo necesario para fabricar munición, vendas y herramientas desde la fórmula portátil sin ir a la casa.",
                whereToGo = "Menú > Skills > Pestaña de Fabricación (Crafting).",
                whatToSearchAndBring = "Puntos de Fabricación y New Dollars.",
                stepByStep = "1. Abre Skills > Crafting.\n2. Busca Field Crafting o Quick Production.\n3. Mejora el nivel para recortar segundos de espera.",
                proTip = "Mejora al menos al nivel 2 para craftear munición y vendas casi al instante en medio de un tiroteo.",
                spokenSummary = "Habilidad de Fabricación de Campo. Te permite craftear munición y vendas desde tu mochila mucho más rápido durante el combate."
            )
        ),
        SkillEntry(
            keywords = listOf("building enhance", "manor enhance", "structure defense", "wall enhance"),
            advice = GameAdvice(
                type = AdviceType.SKILL_TALENT,
                title = "Refuerzo de Construcción y Mansión",
                objective = "Aumenta los puntos de vida y defensa de paredes, pisos y puertas de tu casa para protegerte de asedios y saqueos.",
                whereToGo = "Menú > Skills > Crafting > Arquitectura / Mansión.",
                whatToSearchAndBring = "Madera procesada, tablones y puntos de fabricación.",
                stepByStep = "1. Sube tu nivel de Mansión en el controlador de la entrada.\n2. Ve a Skills > Crafting > Building Enhancement.\n3. Desbloquea para aplicar refuerzos nivel 1, 2 y 3 a tus paredes.",
                proTip = "Mejora primero los almacenes donde guardas materiales raros para que otros jugadores no puedan romperlos.",
                spokenSummary = "Refuerzo de Construcción. Aumenta la resistencia de las paredes y puertas de tu casa para evitar que te roben suministros."
            )
        ),
        SkillEntry(
            keywords = listOf("armor craft", "weapon craft", "equipment repair", "durability"),
            advice = GameAdvice(
                type = AdviceType.SKILL_TALENT,
                title = "Maestría en Fabricación de Equipamiento",
                objective = "Desbloquea recetas avanzadas de armas y armaduras y reduce la pérdida de durabilidad al disparar o recibir golpes.",
                whereToGo = "Menú > Skills > Crafting > Equipamiento.",
                whatToSearchAndBring = "Puntos de maestría de crafteo y New Dollars.",
                stepByStep = "1. Ve a Skills > Crafting.\n2. Selecciona Gear Crafting Mastery.\n3. Sube de nivel para desbloquear mejores accesorios.",
                proTip = "Ahorra muchísima durabilidad en tus armas principales, reduciendo el gasto de camas de reparación.",
                spokenSummary = "Fabricación de Equipamiento. Desbloquea mejores armas y hace que duren más tiempo sin romperse."
            )
        ),

        // === HABILIDADES DE RECOLECCIÓN ===
        SkillEntry(
            keywords = listOf("logging", "lumberjack", "wood gather", "tree chop"),
            advice = GameAdvice(
                type = AdviceType.SKILL_TALENT,
                title = "Tala y Leñador (Logging Mastery)",
                objective = "Incrementa la velocidad de tala con hacha y la probabilidad de obtener recursos secundarios raros (corteza, ramas duras, resina).",
                whereToGo = "Menú > Skills > Gathering (Recolección).",
                whatToSearchAndBring = "Puntos de Recolección y New Dollars.",
                stepByStep = "1. Ve a Skills > Gathering > Logging.\n2. Mejora Basic Logging al máximo permitido.\n3. Desbloquea las habilidades secundarias de resina y madera dura.",
                proTip = "La resina y la madera dura son ingredientes indispensables para fabricar tablones y armas como el UZI.",
                spokenSummary = "Habilidad de Tala y Leñador. Te da recursos raros como resina y ramas duras al talar árboles. Fundamental para craftear armas."
            )
        ),
        SkillEntry(
            keywords = listOf("mining", "miner", "stone gather", "iron ore"),
            advice = GameAdvice(
                type = AdviceType.SKILL_TALENT,
                title = "Minería y Cantera (Mining Mastery)",
                objective = "Aumenta la velocidad de picado con pico y la tasa de caída de hierro (Iron Ore), silicio y azufre de las rocas.",
                whereToGo = "Menú > Skills > Gathering > Mining.",
                whatToSearchAndBring = "Puntos de Recolección y New Dollars.",
                stepByStep = "1. Ve a Skills > Gathering > Mining.\n2. Sube Basic Mining.\n3. Activa la probabilidad de extracción de mineral de hierro.",
                proTip = "El mineral de hierro es el recurso más demandado para fabricar lingotes de hierro y cañones de fusil.",
                spokenSummary = "Habilidad de Minería. Aumenta la obtención de mineral de hierro y azufre al picar piedras. Esencial para crear munición y armas."
            )
        ),
        SkillEntry(
            keywords = listOf("hemp", "flax", "hemp gathering", "plant gather"),
            advice = GameAdvice(
                type = AdviceType.SKILL_TALENT,
                title = "Recolección de Cáñamo y Plantas Medicinales",
                objective = "Aumenta la velocidad para cosechar plantas de cáñamo y la probabilidad de conseguir tallos de lino y hojas medicinales.",
                whereToGo = "Menú > Skills > Gathering > Hemp Gathering.",
                whatToSearchAndBring = "Puntos de Recolección y New Dollars.",
                stepByStep = "1. Ve a Skills > Gathering > Hemp.\n2. Sube la habilidad pasiva de cáñamo.\n3. Cosecha todas las plantas verdes marcadas en tu minimapa.",
                proTip = "El cáñamo es la base de las vendas medicinales y de las telas con las que se tejen las mochilas y armaduras.",
                spokenSummary = "Recolección de Cáñamo. Te permite conseguir más hojas y fibras para fabricar vendas de curación y armaduras protectoras."
            )
        )
    )

    fun findSkillAdvice(ocrText: String): GameAdvice? {
        val lower = ocrText.lowercase()
        return skills.firstOrNull { skill ->
            skill.keywords.any { keyword -> lower.contains(keyword) }
        }?.advice
    }

    private data class SkillEntry(
        val keywords: List<String>,
        val advice: GameAdvice
    )
}
