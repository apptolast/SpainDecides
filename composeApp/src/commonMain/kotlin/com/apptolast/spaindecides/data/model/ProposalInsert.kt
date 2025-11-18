package com.apptolast.spaindecides.data.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * Data class for creating a new proposal in Supabase.
 *
 * This model is used for insert operations on the proposals table.
 * Unlike [Proposal], this does not include auto-generated fields like `id`,
 * `upvotes`, `downvotes`, or `created_at`.
 *
 * @property title The proposal text (10-150 characters)
 * @property categoryId ID of the category this proposal belongs to
 * @property userId ID of the user creating the proposal
 */
@Serializable
data class ProposalInsert(
    val title: String,
    @SerialName("category_id") val categoryId: String,
    @SerialName("user_id") val userId: String
)