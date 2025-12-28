package com.apptolast.spaindecides.data.model

import kotlinx.serialization.Serializable

/**
 * Request payload sent to n8n webhook for proposal processing.
 */
@Serializable
data class ProposalProcessingRequest(
    val title: String,
    val description: String,
    val categoryId: String,
    val userId: String,
    val forceCreation: Boolean,
)

/**
 * Response from n8n webhook after processing a proposal.
 */
@Serializable
data class ProposalProcessingResponse(
    val success: Boolean,
    val status: ProposalProcessingStatus,
    val proposalId: String? = null,
    val duplicates: List<SimilarProposal> = emptyList(),
    val message: String? = null,
)

/**
 * Status returned by the n8n webhook.
 */
@Serializable
enum class ProposalProcessingStatus {
    CREATED,           // Proposal was created successfully
    DUPLICATE_FOUND,   // Similar proposals found, user should choose
    ERROR              // Something went wrong
}

/**
 * A similar proposal found during duplicate detection.
 */
@Serializable
data class SimilarProposal(
    val id: String,
    val title: String,
    val description: String,
    val similarity: Float,  // 0.0 - 1.0
    val votesCount: Int
)
