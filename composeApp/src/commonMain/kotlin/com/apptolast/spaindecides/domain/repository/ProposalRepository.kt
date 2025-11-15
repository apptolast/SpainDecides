package com.apptolast.spaindecides.domain.repository

import com.apptolast.spaindecides.data.model.Proposal
import kotlinx.coroutines.flow.Flow

/**
 * Repository interface for managing proposals.
 * Provides methods to retrieve, create, and vote on proposals.
 */
interface ProposalRepository {

    /**
     * Gets all proposals for a specific category as a Flow.
     * Proposals are sorted by net votes (descending).
     *
     * @param categoryId The ID of the category
     * @return Flow emitting the list of proposals for the category
     */
    fun getProposalsByCategory(categoryId: String): Flow<List<Proposal>>

    /**
     * Creates a new proposal in a specific category.
     *
     * @param title The proposal text (max 150 characters)
     * @param categoryId The ID of the category
     * @return The created proposal with a generated ID
     */
    suspend fun createProposal(title: String, categoryId: String): Proposal

    /**
     * Votes on a proposal.
     *
     * @param proposalId The ID of the proposal to vote on
     * @param voteType 1 for upvote, -1 for downvote, 0 to remove vote
     * @return The updated proposal
     */
    suspend fun voteOnProposal(proposalId: String, voteType: Int): Proposal?

    /**
     * Gets a specific proposal by its ID.
     *
     * @param proposalId The unique identifier of the proposal
     * @return The proposal if found, null otherwise
     */
    suspend fun getProposalById(proposalId: String): Proposal?
}
