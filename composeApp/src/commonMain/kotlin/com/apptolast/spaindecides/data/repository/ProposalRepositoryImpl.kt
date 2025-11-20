package com.apptolast.spaindecides.data.repository

import com.apptolast.spaindecides.data.model.Proposal
import com.apptolast.spaindecides.data.model.ProposalInsert
import com.apptolast.spaindecides.data.model.ProposalVote
import com.apptolast.spaindecides.data.model.ProposalVoteUpsert
import com.apptolast.spaindecides.data.model.ProposalWithUserVote
import com.apptolast.spaindecides.data.remote.SupabaseClientConfig
import com.apptolast.spaindecides.domain.repository.ProposalRepository
import io.github.jan.supabase.auth.auth
import io.github.jan.supabase.postgrest.from
import io.github.jan.supabase.postgrest.query.Order
import io.github.jan.supabase.realtime.PostgresAction
import io.github.jan.supabase.realtime.channel
import io.github.jan.supabase.realtime.postgresChangeFlow
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.merge
import kotlinx.coroutines.launch

/**
 * Implementation of ProposalRepository using Supabase as the backend.
 *
 * This repository handles proposals and voting through the Supabase database.
 * Vote counts are automatically updated by database triggers when votes are cast.
 */
class ProposalRepositoryImpl : ProposalRepository {

    private val supabase = SupabaseClientConfig.client

    override fun getProposalsByCategory(categoryId: String): Flow<List<ProposalWithUserVote>> =
        callbackFlow {
            // Store reference to the coroutine scope for cleanup
            val flowScope = this

        // Helper function to fetch proposals with user votes
        suspend fun fetchProposalsWithVotes(): List<ProposalWithUserVote> {
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
            return proposalsWithVotes.sortedByDescending { it.netVotes }
        }

        // Create a Realtime channel for this category
        val channelId = "proposals_category_$categoryId"
        val channel = supabase.channel(channelId)

            // IMPORTANT: Subscribe to the channel FIRST before calling postgresChangeFlow()
            channel.subscribe()

        // Subscribe to changes on proposals table
        val proposalChanges = channel.postgresChangeFlow<PostgresAction>(schema = "public") {
            table = "proposals"
        }

        // Subscribe to changes on proposal_votes table
        val voteChanges = channel.postgresChangeFlow<PostgresAction>(schema = "public") {
            table = "proposal_votes"
        }

            // Emit initial data immediately
            try {
                send(fetchProposalsWithVotes())
            } catch (e: Exception) {
                println("Error fetching initial proposals: ${e.message}")
                send(emptyList())
            }

            // Collect changes from both flows and reload data
            try {
                merge(proposalChanges, voteChanges).collect {
                    // Reload and send updated data on any change
                    send(fetchProposalsWithVotes())
            }
            } catch (e: Exception) {
                println("Error in Realtime flow: ${e.message}")
                send(emptyList())
            }

            // Cleanup: Unsubscribe from channel when Flow is cancelled
            awaitClose {
                flowScope.launch {
                    channel.unsubscribe()
                }
            }
    }

    override suspend fun createProposal(title: String, categoryId: String): Proposal {
        val userId = supabase.auth.currentUserOrNull()?.id
            ?: throw IllegalStateException("User must be authenticated to create proposals")

        return supabase
            .from("proposals")
            .insert(
                ProposalInsert(
                    title = title,
                    categoryId = categoryId,
                    userId = userId
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
                            ProposalVoteUpsert(
                                proposalId = proposalId,
                                userId = userId,
                                voteType = voteType
                            )
                        ) {
                            onConflict = "proposal_id, user_id"
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
