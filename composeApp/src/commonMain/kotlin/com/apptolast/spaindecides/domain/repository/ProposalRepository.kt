package com.apptolast.spaindecides.domain.repository

import com.apptolast.spaindecides.data.model.Proposal
import com.apptolast.spaindecides.data.model.ProposalWithUserVote
import com.apptolast.spaindecides.data.model.SimilarProposal
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
     * Creates a new proposal, checking for duplicates via AI.
     *
     * This method:
     * 1. Sends the proposal to n8n for duplicate detection
     * 2. If no duplicates found, creates the proposal and sends notification
     * 3. If duplicates found, returns them for user decision
     *
     * @param title Brief title of the proposal (10-100 characters)
     * @param description Detailed description of the proposal (10-1000 characters)
     * @param categoryId The ID of the category
     * @return CreateProposalResult indicating success, duplicates found, or error
     */
    suspend fun createProposal(
        title: String,
        description: String,
        categoryId: String,
        forceCreation: Boolean
    ): CreateProposalResult

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
}

/**
 * Sealed class representing the result of creating a proposal.
 * Provides type-safe handling of different outcomes.
 */
sealed class CreateProposalResult {
    /**
     * Proposal was created successfully.
     * @param proposal The created proposal
     */
    data class Success(val proposal: Proposal) : CreateProposalResult()

    /**
     * Similar proposals were found. User should decide whether to proceed.
     * @param duplicates List of similar proposals with similarity scores
     */
    data class DuplicatesFound(val duplicates: List<SimilarProposal>) : CreateProposalResult()

    /**
     * An error occurred during proposal creation.
     * @param message Human-readable error message
     * @param isNetworkError True if the error was due to network issues
     */
    data class Error(
        val message: String,
        val isNetworkError: Boolean = false
    ) : CreateProposalResult()
}
