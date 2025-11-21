package com.apptolast.spaindecides.data.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * Represents a citizen proposal within a category.
 *
 * This model maps directly to the public.proposals table in Supabase.
 * Note: The user's vote status is NOT stored here - it's in a separate
 * proposal_votes table. Use [ProposalWithUserVote] for UI display.
 *
 * @property id Unique identifier for the proposal (UUID)
 * @property title Brief title of the proposal (10-100 characters)
 * @property description Detailed description of the proposal (10-1000 characters)
 * @property categoryId ID of the category this proposal belongs to
 * @property userId ID of the user who created this proposal
 * @property upvotes Number of positive votes (automatically updated by database trigger)
 * @property downvotes Number of negative votes (automatically updated by database trigger)
 * @property createdAt Timestamp when the proposal was created (ISO 8601 format)
 */
@Serializable
data class Proposal(
    val id: String,
    val title: String,
    val description: String,
    @SerialName("category_id") val categoryId: String,
    @SerialName("user_id") val userId: String,
    val upvotes: Int = 0,
    val downvotes: Int = 0,
    @SerialName("created_at") val createdAt: String? = null
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
