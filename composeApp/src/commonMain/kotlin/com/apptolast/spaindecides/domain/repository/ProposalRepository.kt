package com.apptolast.spaindecides.domain.repository

import com.apptolast.spaindecides.data.model.Proposal
import com.apptolast.spaindecides.data.model.ProposalWithUserVote
import kotlinx.coroutines.flow.Flow

/**
 * Repository interface for managing proposals.
 * Provides methods to retrieve, create, and vote on proposals.
 */
interface ProposalRepository {

    /**
     * Gets all proposals for a specific category as a Flow.
     * Proposals are sorted by net votes (descending) and include the current user's vote status.
     *
     * @param categoryId The ID of the category
     * @return Flow emitting the list of proposals with user vote information
     */
    fun getProposalsByCategory(categoryId: String): Flow<List<ProposalWithUserVote>>

    /**
     * Creates a new proposal in a specific category.
     *
     * @param title Brief title of the proposal (10-100 characters)
     * @param description Detailed description of the proposal (10-1000 characters)
     * @param categoryId The ID of the category
     * @param sendNotification Whether to send a push notification to all users (default: true)
     * @return The created proposal
     */
    suspend fun createProposal(
        title: String,
        description: String,
        categoryId: String,
        sendNotification: Boolean = true
    ): Proposal

    /**
     * Votes on a proposal. This will insert or update a vote in the proposal_votes table.
     * If voteType is 0, it removes the user's vote.
     *
     * @param proposalId The ID of the proposal to vote on
     * @param voteType 1 for upvote, -1 for downvote, 0 to remove vote
     * @return The updated proposal with user vote information, or null if proposal not found
     */
    suspend fun voteOnProposal(proposalId: String, voteType: Int): ProposalWithUserVote?

    /**
     * Gets a specific proposal by its ID with the current user's vote status.
     *
     * @param proposalId The unique identifier of the proposal
     * @return The proposal with vote information if found, null otherwise
     */
    suspend fun getProposalById(proposalId: String): ProposalWithUserVote?


    /**
     * Processes a new proposal through the AI pipeline.
     * This will:
     * 1. Generate embeddings for semantic search
     * 2. Check for duplicate/similar proposals
     * 3. If no duplicates: create the proposal and send notification
     * 4. If duplicates found: return them for user to choose
     *
     * @return ProposalProcessingResult with status and any found duplicates
     */
    suspend fun processNewProposal(
        title: String,
        description: String,
        categoryId: String,
        sendNotification: Boolean = true
    ): ProposalProcessingResult
}
