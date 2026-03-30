package com.apptolast.spaindecides.data.model

import kotlinx.serialization.Serializable

/**
 * Composite model that combines a proposal with the current user's vote status.
 *
 * This model is used in the UI layer to display proposals along with the user's
 * voting state. It is NOT stored in the database - it's constructed by joining
 * data from the proposals and proposal_votes tables.
 *
 * @property proposal The proposal data from the database
 * @property userVote The current user's vote: 1 for upvote, -1 for downvote, 0 for no vote
 */
@Serializable
data class ProposalWithUserVote(
    val proposal: Proposal,
    val userVote: Int = 0 // 1 = upvoted, -1 = downvoted, 0 = not voted
) {
    /**
     * Convenience getters that delegate to the proposal
     */
    val id: String get() = proposal.id
    val title: String get() = proposal.title
    val description: String get() = proposal.description
    val categoryId: String get() = proposal.categoryId
    val userId: String get() = proposal.userId
    val upvotes: Int get() = proposal.upvotes
    val downvotes: Int get() = proposal.downvotes
    val createdAt: String? get() = proposal.createdAt
    val netVotes: Int get() = proposal.netVotes
    val formattedVotes: String get() = proposal.formattedVotes

    /**
     * Returns true if the current user has upvoted this proposal
     */
    val isUpvoted: Boolean
        get() = userVote == 1

    /**
     * Returns true if the current user has downvoted this proposal
     */
    val isDownvoted: Boolean
        get() = userVote == -1

    /**
     * Returns true if the current user hasn't voted on this proposal
     */
    val hasNotVoted: Boolean
        get() = userVote == 0
}
