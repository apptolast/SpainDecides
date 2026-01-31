package com.apptolast.spaindecides.presentation.ui.preview

import com.apptolast.spaindecides.data.model.Category
import com.apptolast.spaindecides.data.model.Proposal
import com.apptolast.spaindecides.data.model.ProposalWithUserVote
import com.apptolast.spaindecides.data.model.SimilarProposal

/**
 * Sample data for Compose previews.
 *
 * This object provides reusable sample data for UI previews, making it easy to
 * visualize different states without needing real data or ViewModel injection.
 */
object SampleData {

    // Sample Categories
    val sampleCategory = Category(
        id = "cat-001",
        key = "economy",
        iconName = "AccountBalance",
        sortOrder = 1,
        createdAt = "2024-01-15T10:30:00Z"
    )

    val sampleCategories = listOf(
        sampleCategory,
        Category(
            id = "cat-002",
            key = "health",
            iconName = "LocalHospital",
            sortOrder = 2
        ),
        Category(
            id = "cat-003",
            key = "education",
            iconName = "School",
            sortOrder = 3
        ),
        Category(
            id = "cat-004",
            key = "environment",
            iconName = "Park",
            sortOrder = 4
        ),
        Category(
            id = "cat-005",
            key = "security",
            iconName = "Security",
            sortOrder = 5
        )
    )

    // Sample Proposals
    val sampleProposal = Proposal(
        id = "prop-001",
        title = "Reducir impuestos a las PYMES",
        description = "Propuesta para reducir la carga fiscal de las pequeñas y medianas empresas en un 15% durante los primeros 3 años de actividad, fomentando así el emprendimiento y la creación de empleo.",
        categoryId = "cat-001",
        userId = "user-001",
        upvotes = 142,
        downvotes = 23,
        createdAt = "2024-01-15T10:30:00Z"
    )

    val sampleProposals = listOf(
        sampleProposal,
        Proposal(
            id = "prop-002",
            title = "Aumentar el salario mínimo",
            description = "Incrementar el salario mínimo interprofesional de forma gradual hasta alcanzar los 1.500€ mensuales en 2026.",
            categoryId = "cat-001",
            userId = "user-002",
            upvotes = 256,
            downvotes = 89,
            createdAt = "2024-01-14T15:20:00Z"
        ),
        Proposal(
            id = "prop-003",
            title = "Inversión en energías renovables",
            description = "Destinar el 5% del PIB a la investigación y desarrollo de energías renovables durante la próxima legislatura.",
            categoryId = "cat-001",
            userId = "user-003",
            upvotes = 89,
            downvotes = 12,
            createdAt = "2024-01-13T08:45:00Z"
        )
    )

    // Sample ProposalWithUserVote
    val sampleProposalWithVote = ProposalWithUserVote(
        proposal = sampleProposal,
        userVote = 1 // Upvoted
    )

    val sampleProposalsWithVotes = listOf(
        sampleProposalWithVote,
        ProposalWithUserVote(
            proposal = sampleProposals[1],
            userVote = 0 // Not voted
        ),
        ProposalWithUserVote(
            proposal = sampleProposals[2],
            userVote = -1 // Downvoted
        )
    )

    // Sample proposal counts per category
    val sampleProposalCounts = mapOf(
        "cat-001" to 42,
        "cat-002" to 28,
        "cat-003" to 35,
        "cat-004" to 19,
        "cat-005" to 15
    )

    // Sample SimilarProposals for duplicate detection
    val sampleSimilarProposals = listOf(
        SimilarProposal(
            id = "prop-similar-001",
            title = "Reducción de impuestos para empresas pequeñas. Reducción de impuestos para empresas pequeñas",
            description = "Bajar los impuestos a las empresas de menos de 50 empleados para fomentar el crecimiento económico.",
            similarity = 0.87f,
            votesCount = 156
        ),
        SimilarProposal(
            id = "prop-similar-002",
            title = "Incentivos fiscales para nuevos emprendedores",
            description = "Crear un programa de incentivos fiscales para emprendedores durante sus primeros años de actividad empresarial.",
            similarity = 0.72f,
            votesCount = 89
        )
    )

    // Sample user data
    const val sampleUserName = "Juan García"
    const val sampleUserEmail = "juan.garcia@example.com"
    const val sampleUserPhotoUrl = "https://example.com/photo.jpg"
}
