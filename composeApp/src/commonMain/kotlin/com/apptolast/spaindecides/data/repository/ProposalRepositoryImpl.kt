package com.apptolast.spaindecides.data.repository

import com.apptolast.spaindecides.data.model.Proposal
import com.apptolast.spaindecides.data.model.ProposalVote
import com.apptolast.spaindecides.data.model.ProposalWithUserVote
import com.apptolast.spaindecides.data.remote.SupabaseClientConfig
import com.apptolast.spaindecides.domain.repository.ProposalRepository
import io.github.jan.supabase.auth.auth
import io.github.jan.supabase.postgrest.from
import io.github.jan.supabase.postgrest.query.Order
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.selects.select

/**
 * Implementation of ProposalRepository using Supabase as the backend.
 *
 * This repository handles proposals and voting through the Supabase database.
 * Vote counts are automatically updated by database triggers when votes are cast.
 */
class ProposalRepositoryImpl : ProposalRepository {

    private val supabase = SupabaseClientConfig.client

    override fun getProposalsByCategory(categoryId: String): Flow<List<ProposalWithUserVote>> =
        flow {
            try {
                val currentUserId = supabase.auth.currentUserOrNull()?.id

                // Fetch all proposals for the category
                val proposals = supabase
                    .from("proposals")
                    .select {
                        filter {
                            eq("category_id", categoryId)
                        }
                        order("created_at", Order.DESCENDING)
                    }
                    .decodeList<Proposal>()

                // If user is authenticated, fetch their votes for these proposals
                val proposalsWithVotes = if (currentUserId != null && proposals.isNotEmpty()) {
                    val proposalIds = proposals.map { it.id }

                    val userVotes = supabase
                        .from("proposal_votes")
                        .select {
                            filter {
                                eq("user_id", currentUserId)
                                isIn("proposal_id", proposalIds)
                            }
                        }
                        .decodeList<ProposalVote>()

                    // Map votes by proposal ID for quick lookup
                    val votesByProposalId = userVotes.associateBy { it.proposalId }

                    // Combine proposals with user votes
                    proposals.map { proposal ->
                        val userVote = votesByProposalId[proposal.id]?.voteType ?: 0
                        ProposalWithUserVote(proposal, userVote)
                    }
                } else {
                    // No user or no proposals - return proposals with no votes
                    proposals.map { ProposalWithUserVote(it, 0) }
                }

                // Sort by net votes (descending)
                val sorted = proposalsWithVotes.sortedByDescending { it.netVotes }
                emit(sorted)
            } catch (e: Exception) {
                println("Error fetching proposals: ${e.message}")
                emit(emptyList())
            }
        }

    override suspend fun createProposal(title: String, categoryId: String): Proposal {
        val userId = supabase.auth.currentUserOrNull()?.id
            ?: throw IllegalStateException("User must be authenticated to create proposals")

        return supabase
            .from("proposals")
            .insert(
                mapOf(
                    "title" to title,
                    "category_id" to categoryId,
                    "user_id" to userId
                )
            ) {
                select()
            }
            .decodeSingle<Proposal>()
    }

    override suspend fun voteOnProposal(proposalId: String, voteType: Int): ProposalWithUserVote? {
        try {
            val userId = supabase.auth.currentUserOrNull()?.id
                ?: throw IllegalStateException("User must be authenticated to vote")

            when (voteType) {
                0 -> {
                    // Remove vote - delete from proposal_votes
                    supabase
                        .from("proposal_votes")
                        .delete {
                            filter {
                                eq("proposal_id", proposalId)
                                eq("user_id", userId)
                            }
                        }
                }

                1, -1 -> {
                    // Upsert vote (insert or update)
                    // Note: We use upsert to handle both new votes and vote changes
                    supabase
                        .from("proposal_votes")
                        .upsert(
                            mapOf(
                                "proposal_id" to proposalId,
                                "user_id" to userId,
                                "vote_type" to voteType
                            )
                        ) {
                            onConflict = "proposal_id,user_id"
                        }
                }

                else -> throw IllegalArgumentException("voteType must be -1, 0, or 1")
            }

            // Fetch the updated proposal with new vote counts
            // (counts are automatically updated by database trigger)
            return getProposalById(proposalId)
        } catch (e: Exception) {
            println("Error voting on proposal: ${e.message}")
            return null
        }
    }

    override suspend fun getProposalById(proposalId: String): ProposalWithUserVote? {
        return try {
            val currentUserId = supabase.auth.currentUserOrNull()?.id

            // Fetch the proposal
            val proposal = supabase
                .from("proposals")
                .select {
                    filter {
                        eq("id", proposalId)
                    }
                }
                .decodeSingleOrNull<Proposal>() ?: return null

            // Fetch user's vote if authenticated
            val userVote = if (currentUserId != null) {
                supabase
                    .from("proposal_votes")
                    .select {
                        filter {
                            eq("proposal_id", proposalId)
                            eq("user_id", currentUserId)
                        }
                    }
                    .decodeSingleOrNull<ProposalVote>()?.voteType ?: 0
            } else {
                0
            }

            ProposalWithUserVote(proposal, userVote)
        } catch (e: Exception) {
            println("Error fetching proposal by ID: ${e.message}")
            null
        }
    }
}
