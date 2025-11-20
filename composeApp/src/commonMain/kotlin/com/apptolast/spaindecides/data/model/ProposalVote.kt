package com.apptolast.spaindecides.data.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * Represents a vote cast by a user on a proposal.
 *
 * This model maps to the public.proposal_votes table in Supabase. Each user can only
 * have one vote per proposal (enforced by unique constraint in database).
 *
 * @property id Unique identifier for the vote (UUID)
 * @property proposalId ID of the proposal being voted on
 * @property userId ID of the user who cast the vote
 * @property voteType Type of vote: 1 for upvote, -1 for downvote
 * @property createdAt Timestamp when the vote was first cast (ISO 8601 format)
 * @property updatedAt Timestamp when the vote was last changed (ISO 8601 format)
 */
@Serializable
data class ProposalVote(
    val id: String,
    @SerialName("proposal_id") val proposalId: String,
    @SerialName("user_id") val userId: String,
    @SerialName("vote_type") val voteType: Int, // 1 = upvote, -1 = downvote
    @SerialName("created_at") val createdAt: String? = null,
    @SerialName("updated_at") val updatedAt: String? = null
) {
    init {
        require(voteType == 1 || voteType == -1) {
            "voteType must be 1 (upvote) or -1 (downvote), got $voteType"
        }
    }

    /**
     * Returns true if this is an upvote
     */
    val isUpvote: Boolean
        get() = voteType == 1

    /**
     * Returns true if this is a downvote
     */
    val isDownvote: Boolean
        get() = voteType == -1
}
