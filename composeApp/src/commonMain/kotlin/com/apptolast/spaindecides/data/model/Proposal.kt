package com.apptolast.spaindecides.data.model

import kotlinx.serialization.Serializable

/**
 * Represents a citizen proposal within a category.
 *
 * @property id Unique identifier for the proposal
 * @property title The proposal text (max 150 characters)
 * @property categoryId ID of the category this proposal belongs to
 * @property upvotes Number of positive votes
 * @property downvotes Number of negative votes
 * @property userVote Current user's vote: 1 for upvote, -1 for downvote, 0 for no vote
 */
@Serializable
data class Proposal(
    val id: String,
    val title: String,
    val categoryId: String,
    val upvotes: Int = 0,
    val downvotes: Int = 0,
    val userVote: Int = 0 // 1 = upvoted, -1 = downvoted, 0 = not voted
) {
    /**
     * Calculates the net vote count (upvotes - downvotes)
     */
    val netVotes: Int
        get() = upvotes - downvotes

    /**
     * Returns a formatted string of the net votes with K suffix if over 1000
     */
    val formattedVotes: String
        get() = when {
            netVotes >= 1000 -> "${netVotes / 1000}.${(netVotes % 1000) / 100}K"
            else -> netVotes.toString()
        }
}
