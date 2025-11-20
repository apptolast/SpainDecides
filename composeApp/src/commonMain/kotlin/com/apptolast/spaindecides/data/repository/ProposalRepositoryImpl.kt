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
 * This repository handles proposals and voting through the Supabase database with
 * **Realtime synchronization** using Supabase Postgres Changes.
 *
 * ## Realtime Features:
 * - Automatically receives database changes (INSERT, UPDATE, DELETE) via WebSocket
 * - Subscribes to both `proposals` and `proposal_votes` tables
 * - Updates UI in real-time when votes are cast or proposals are created
 *
 * ## Requirements for Realtime to Work:
 * 1. Tables must be added to `supabase_realtime` publication in Database → Replication
 * 2. RLS policies must allow SELECT access for authenticated users
 * 3. REPLICA IDENTITY should be set to FULL for complete row data
 *
 * ## How it works:
 * 1. Creates a Realtime channel per category
 * 2. Subscribes to Postgres Changes on both tables
 * 3. Emits initial data immediately
 * 4. Re-fetches and emits updated data whenever a change event is received
 * 5. Properly cleans up subscriptions when the Flow is cancelled
 *
 * Vote counts are automatically updated by database triggers when votes are cast.
 */
class ProposalRepositoryImpl : ProposalRepository {

    private val supabase = SupabaseClientConfig.client

    /**
     * Returns a Flow of proposals for a specific category with real-time updates.
     *
     * This Flow will emit:
     * 1. Initial data immediately when collected
     * 2. Updated data whenever a proposal or vote changes in the database
     *
     * The Flow stays active as long as it's being collected, maintaining a WebSocket
     * connection to Supabase Realtime server.
     *
     * @param categoryId The ID of the category to fetch proposals for
     * @return Flow emitting lists of proposals with user vote information
     */
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

            // IMPORTANT: Set up postgresChangeFlow() BEFORE calling subscribe()
            // This tells the channel what database changes to listen for
            println("🔧 Setting up Realtime listeners for channel: $channelId")

            // Subscribe to changes on proposals table (INSERT, UPDATE, DELETE)
            val proposalChanges = channel.postgresChangeFlow<PostgresAction>(schema = "public") {
                table = "proposals"
            }

            // Subscribe to changes on proposal_votes table (INSERT, UPDATE, DELETE)
            val voteChanges = channel.postgresChangeFlow<PostgresAction>(schema = "public") {
                table = "proposal_votes"
            }

            // Now subscribe to the channel to start receiving events
            // This must be called AFTER postgresChangeFlow() setup
            println("📡 Subscribing to Realtime channel: $channelId")
            channel.subscribe()
            println("✅ Realtime channel subscribed successfully: $channelId")

            // Emit initial data immediately
            try {
                println("📊 Fetching initial proposals for category: $categoryId")
                val initialData = fetchProposalsWithVotes()
                println("✅ Initial data loaded: ${initialData.size} proposals")
                send(initialData)
            } catch (e: Exception) {
                println("❌ Error fetching initial proposals: ${e.message}")
                e.printStackTrace()
                send(emptyList())
            }

            // Collect changes from both flows and reload data
            try {
                println("👂 Listening for Realtime events on proposals and votes...")
                merge(proposalChanges, voteChanges).collect { action ->
                    // Log the received event with details
                    when (action) {
                        is PostgresAction.Insert -> {
                            println("🆕 Realtime INSERT event received: ${action.record}")
                        }

                        is PostgresAction.Update -> {
                            println("🔄 Realtime UPDATE event received")
                            println("   Old: ${action.oldRecord}")
                            println("   New: ${action.record}")
                        }

                        is PostgresAction.Delete -> {
                            println("🗑️ Realtime DELETE event received: ${action.oldRecord}")
                        }

                        is PostgresAction.Select -> {
                            println("📋 Realtime SELECT event received")
                        }
                    }

                    // Reload and send updated data on any change
                    println("🔄 Reloading proposals after Realtime event...")
                    val updatedData = fetchProposalsWithVotes()
                    println("✅ Reloaded ${updatedData.size} proposals, emitting to UI")
                    send(updatedData)
                }
            } catch (e: Exception) {
                println("❌ Error in Realtime flow: ${e.message}")
                e.printStackTrace()
                send(emptyList())
            }

            // Cleanup: Unsubscribe from channel when Flow is cancelled
            awaitClose {
                flowScope.launch {
                    println("🔌 Unsubscribing from Realtime channel: $channelId")
                    channel.unsubscribe()
                    println("✅ Realtime channel unsubscribed: $channelId")
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
