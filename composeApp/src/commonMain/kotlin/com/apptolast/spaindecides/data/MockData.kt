package com.apptolast.spaindecides.data

import com.apptolast.spaindecides.data.model.Category
import com.apptolast.spaindecides.data.model.Proposal

/**
 * Mock data for categories and proposals.
 * This will be replaced with real API calls in the future.
 */
object MockData {

    /**
     * List of all categories in the citizen participation program
     */
    val categories = listOf(
        Category(
            id = "economia",
            name = "Economía y Empleo",
            description = "Propuestas sobre finanzas y trabajo.",
            iconName = "AccountBalance" // Bank/government building icon
        ),
        Category(
            id = "sanidad",
            name = "Sanidad Pública",
            description = "Ideas para mejorar el sistema de salud.",
            iconName = "Shield" // Health protection icon
        ),
        Category(
            id = "educacion",
            name = "Educación y Cultura",
            description = "Iniciativas para la formación y el arte.",
            iconName = "School" // Education icon
        ),
        Category(
            id = "medio-ambiente",
            name = "Medio Ambiente",
            description = "Acciones para la sostenibilidad y ecología.",
            iconName = "Park" // Tree/nature icon
        ),
        Category(
            id = "politica-exterior",
            name = "Política Exterior",
            description = "Relaciones internacionales y diplomacia.",
            iconName = "Public" // Globe icon
        ),
        Category(
            id = "justicia",
            name = "Justicia y Seguridad",
            description = "Leyes, orden público y sistema judicial.",
            iconName = "Gavel" // Justice gavel icon
        ),
        Category(
            id = "vivienda",
            name = "Vivienda",
            description = "Acceso a la vivienda y urbanismo.",
            iconName = "Apartment" // Building icon
        ),
        Category(
            id = "ciencia",
            name = "Ciencia e Innovación",
            description = "Impulso a la investigación y desarrollo.",
            iconName = "Science" // Flask/science icon
        ),
        Category(
            id = "politicas-sociales",
            name = "Políticas Sociales",
            description = "Igualdad, inclusión y servicios sociales.",
            iconName = "Groups" // People icon
        ),
        Category(
            id = "impuestos",
            name = "Impuestos y Fiscalidad",
            description = "Propuestas sobre el sistema tributario.",
            iconName = "Receipt" // Document/receipt icon
        )
    )

    /**
     * Mock proposals for the Health category (Sanidad)
     */
    private val healthProposals = listOf(
        Proposal(
            id = "h1",
            title = "Mejorar los tiempos de espera en atención primaria",
            categoryId = "sanidad",
            upvotes = 1450,
            downvotes = 150
        ),
        Proposal(
            id = "h2",
            title = "Aumentar la inversión en investigación de enfermedades raras",
            categoryId = "sanidad",
            upvotes = 1050,
            downvotes = 63
        ),
        Proposal(
            id = "h3",
            title = "Digitalización completa de historiales médicos en toda España",
            categoryId = "sanidad",
            upvotes = 920,
            downvotes = 58
        ),
        Proposal(
            id = "h4",
            title = "Creación de un plan nacional de salud mental en las escuelas",
            categoryId = "sanidad",
            upvotes = 800,
            downvotes = 55
        ),
        Proposal(
            id = "h5",
            title = "Ampliar el horario de atención en centros de salud",
            categoryId = "sanidad",
            upvotes = 650,
            downvotes = 120
        )
    )

    /**
     * Mock proposals for Economy category
     */
    private val economyProposals = listOf(
        Proposal(
            id = "e1",
            title = "Reducir jornada laboral a 4 días sin reducir salario",
            categoryId = "economia",
            upvotes = 2100,
            downvotes = 890
        ),
        Proposal(
            id = "e2",
            title = "Bonificaciones fiscales para empresas que contraten jóvenes",
            categoryId = "economia",
            upvotes = 1200,
            downvotes = 340
        ),
        Proposal(
            id = "e3",
            title = "Crear un fondo de ayuda para autónomos en crisis",
            categoryId = "economia",
            upvotes = 980,
            downvotes = 120
        )
    )

    /**
     * Mock proposals for Education category
     */
    private val educationProposals = listOf(
        Proposal(
            id = "ed1",
            title = "Incluir programación en el currículo desde primaria",
            categoryId = "educacion",
            upvotes = 1560,
            downvotes = 240
        ),
        Proposal(
            id = "ed2",
            title = "Aumentar el presupuesto en becas universitarias",
            categoryId = "educacion",
            upvotes = 1890,
            downvotes = 150
        ),
        Proposal(
            id = "ed3",
            title = "Gratuidad de material escolar en educación pública",
            categoryId = "educacion",
            upvotes = 1450,
            downvotes = 320
        )
    )

    /**
     * Mock proposals for Environment category
     */
    private val environmentProposals = listOf(
        Proposal(
            id = "env1",
            title = "Prohibir vehículos diésel en centros urbanos para 2030",
            categoryId = "medio-ambiente",
            upvotes = 1780,
            downvotes = 1200
        ),
        Proposal(
            id = "env2",
            title = "Plantar 10 millones de árboles en los próximos 5 años",
            categoryId = "medio-ambiente",
            upvotes = 2450,
            downvotes = 89
        ),
        Proposal(
            id = "env3",
            title = "Bonificaciones para instalación de paneles solares en hogares",
            categoryId = "medio-ambiente",
            upvotes = 1920,
            downvotes = 310
        )
    )

    /**
     * Mock proposals for Housing category
     */
    private val housingProposals = listOf(
        Proposal(
            id = "v1",
            title = "Regular el precio del alquiler en zonas tensionadas",
            categoryId = "vivienda",
            upvotes = 3200,
            downvotes = 1100
        ),
        Proposal(
            id = "v2",
            title = "Construcción de 50,000 viviendas sociales anuales",
            categoryId = "vivienda",
            upvotes = 2800,
            downvotes = 560
        ),
        Proposal(
            id = "v3",
            title = "Ayudas al alquiler para menores de 35 años",
            categoryId = "vivienda",
            upvotes = 2340,
            downvotes = 450
        )
    )

    /**
     * Returns all proposals organized by category ID
     */
    val proposalsByCategory: Map<String, List<Proposal>> = mapOf(
        "sanidad" to healthProposals,
        "economia" to economyProposals,
        "educacion" to educationProposals,
        "medio-ambiente" to environmentProposals,
        "vivienda" to housingProposals,
        "politica-exterior" to listOf(
            Proposal(
                id = "pe1",
                title = "Aumentar la cooperación con la Unión Europea",
                categoryId = "politica-exterior",
                upvotes = 890,
                downvotes = 340
            )
        ),
        "justicia" to listOf(
            Proposal(
                id = "j1",
                title = "Acelerar los procesos judiciales en casos de violencia de género",
                categoryId = "justicia",
                upvotes = 1650,
                downvotes = 120
            )
        ),
        "ciencia" to listOf(
            Proposal(
                id = "c1",
                title = "Doblar el presupuesto en I+D+i en 10 años",
                categoryId = "ciencia",
                upvotes = 1340,
                downvotes = 290
            )
        ),
        "politicas-sociales" to listOf(
            Proposal(
                id = "ps1",
                title = "Ampliar las ayudas a familias numerosas",
                categoryId = "politicas-sociales",
                upvotes = 1120,
                downvotes = 230
            )
        ),
        "impuestos" to listOf(
            Proposal(
                id = "i1",
                title = "Simplificar la declaración de la renta",
                categoryId = "impuestos",
                upvotes = 2890,
                downvotes = 120
            )
        )
    )

    /**
     * Returns all proposals as a flat list
     */
    val allProposals: List<Proposal>
        get() = proposalsByCategory.values.flatten()
}
