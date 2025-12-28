package com.apptolast.spaindecides.data.repository

import com.apptolast.spaindecides.data.model.Proposal
import com.apptolast.spaindecides.data.model.ProposalProcessingRequest
import com.apptolast.spaindecides.data.model.ProposalProcessingStatus
import com.apptolast.spaindecides.data.model.ProposalVote
import com.apptolast.spaindecides.data.model.ProposalVoteUpsert
import com.apptolast.spaindecides.data.model.ProposalWithUserVote
import com.apptolast.spaindecides.data.remote.N8nWebhookClient
import com.apptolast.spaindecides.data.remote.N8nWebhookException
import com.apptolast.spaindecides.data.remote.SupabaseClientConfig
import com.apptolast.spaindecides.domain.repository.CreateProposalResult
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
import kotlin.random.Random

/**
 * Implementation of ProposalRepository using Supabase as the backend.
 *
 * ## Architecture:
 * - Uses n8n webhook for AI-powered duplicate detection before creating proposals
 * - Falls back to direct creation if n8n is unavailable
 * - Sends push notifications via Firebase Cloud Functions
 *
 * ## Realtime Synchronization:
 * - Uses Supabase Postgres Changes to receive database updates via WebSocket
 * - Each Flow subscription creates a unique channel (prevents reuse conflicts)
 * - Automatically re-fetches data when proposals or votes change
 *
 * @param notificationService Service for sending push notifications
 * @param n8nWebhookClient Client for n8n webhook communication
 */
class ProposalRepositoryImpl(
    private val n8nWebhookClient: N8nWebhookClient
) : ProposalRepository {

    private val supabase = SupabaseClientConfig.client

    override fun getProposalsByCategory(categoryId: String): Flow<List<ProposalWithUserVote>> =
        callbackFlow {
            val flowScope = this

            suspend fun fetchProposalsWithVotes(): List<ProposalWithUserVote> {
                val currentUserId = supabase.auth.currentUserOrNull()?.id

                val proposals = supabase
                    .from("proposals")
                    .select {
                        filter { eq("category_id", categoryId) }
                        order("created_at", Order.DESCENDING)
                    }
                    .decodeList<Proposal>()

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

                    val votesByProposalId = userVotes.associateBy { it.proposalId }

                    proposals.map { proposal ->
                        val userVote = votesByProposalId[proposal.id]?.voteType ?: 0
                        ProposalWithUserVote(proposal, userVote)
                    }
                } else {
                    proposals.map { ProposalWithUserVote(it, 0) }
                }

                return proposalsWithVotes.sortedByDescending { it.netVotes }
            }

            val channelId = "proposals_${Random.nextLong()}"
            val channel = supabase.channel(channelId)

            val proposalChanges = channel.postgresChangeFlow<PostgresAction>(schema = "public") {
                table = "proposals"
            }

            val voteChanges = channel.postgresChangeFlow<PostgresAction>(schema = "public") {
                table = "proposal_votes"
            }

            channel.subscribe()

            try {
                val initialData = fetchProposalsWithVotes()
                send(initialData)
            } catch (e: Exception) {
                println("Error fetching proposals: ${e.message}")
                send(emptyList())
            }

            try {
                merge(proposalChanges, voteChanges).collect {
                    val updatedData = fetchProposalsWithVotes()
                    send(updatedData)
                }
            } catch (e: Exception) {
                println("Error in Realtime flow: ${e.message}")
                send(emptyList())
            }

            awaitClose {
                flowScope.launch { channel.unsubscribe() }
            }
        }

    /**
     * Creates a new proposal with AI-powered duplicate detection.
     *
     * Flow:
     * 1. Get current user ID
     * 2. Send proposal to n8n for duplicate detection
     * 3. Based on n8n response:
     *    - CREATED: Return success (n8n already created it)
     *    - DUPLICATE_FOUND: Return duplicates for user decision
     *    - ERROR: Return error with message
     * 4. If n8n fails (network error), return error with isNetworkError=true
     */
    override suspend fun createProposal(
        title: String,
        description: String,
        categoryId: String,
        forceCreation: Boolean
    ): CreateProposalResult {
        val userId = supabase.auth.currentUserOrNull()?.id
            ?: return CreateProposalResult.Error("Usuario no autenticado")

        val request = ProposalProcessingRequest(
            title = title,
            description = description,
            categoryId = categoryId,
            userId = userId,
            forceCreation = forceCreation,
        )

        return n8nWebhookClient.processProposal(request).fold(
            onSuccess = { response ->
                when (response.status) {
                    ProposalProcessingStatus.CREATED -> {
                        // n8n created the proposal, fetch it from DB
                        val proposalId = response.proposalId
                        if (proposalId != null) {
                            val proposal = fetchProposalById(proposalId)
                            if (proposal != null) {
                                CreateProposalResult.Success(proposal)
                            } else {
                                // Proposal was created but we couldn't fetch it
                                // This is unusual but not a failure
                                CreateProposalResult.Success(
                                    Proposal(
                                        id = proposalId,
                                        title = title,
                                        description = description,
                                        categoryId = categoryId,
                                        userId = userId,
                                        upvotes = 0,
                                        downvotes = 0,
                                        createdAt = ""
                                    )
                                )
                            }
                        } else {
                            CreateProposalResult.Error("Propuesta creada pero sin ID")
                        }
                    }

                    ProposalProcessingStatus.DUPLICATE_FOUND -> {
                        CreateProposalResult.DuplicatesFound(response.duplicates)
                    }

                    ProposalProcessingStatus.ERROR -> {
                        CreateProposalResult.Error(
                            response.message ?: "Error al procesar la propuesta"
                        )
                    }
                }
            },
            onFailure = { exception ->
                val isNetworkError = exception is N8nWebhookException.NetworkError ||
                        exception is N8nWebhookException.Timeout

                CreateProposalResult.Error(
                    message = when (exception) {
                        is N8nWebhookException.Unauthorized -> exception.message ?: "Sesión no válida"
                        is N8nWebhookException.NetworkError -> "Sin conexión al servidor"
                        is N8nWebhookException.Timeout -> "El servidor tardó demasiado en responder"
                        is N8nWebhookException.ServerError -> "Error del servidor: ${exception.statusCode}"
                        is N8nWebhookException.ParseError -> "Respuesta inválida del servidor"
                        else -> exception.message ?: "Error desconocido"
                    },
                    isNetworkError = isNetworkError
                )
            }
        )
    }

    override suspend fun voteOnProposal(proposalId: String, voteType: Int): ProposalWithUserVote? {
        try {
            val userId = supabase.auth.currentUserOrNull()?.id
                ?: throw IllegalStateException("User must be authenticated to vote")

            when (voteType) {
                0 -> {
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

            return getProposalById(proposalId)
        } catch (e: Exception) {
            println("Error voting on proposal: ${e.message}")
            return null
        }
    }

    override suspend fun getProposalById(proposalId: String): ProposalWithUserVote? {
        return try {
            val currentUserId = supabase.auth.currentUserOrNull()?.id

            val proposal = supabase
                .from("proposals")
                .select {
                    filter { eq("id", proposalId) }
                }
                .decodeSingleOrNull<Proposal>() ?: return null

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

    /**
     * Internal helper to fetch a proposal by ID without user vote info.
     */
    private suspend fun fetchProposalById(proposalId: String): Proposal? {
        return try {
            supabase
                .from("proposals")
                .select {
                    filter { eq("id", proposalId) }
                }
                .decodeSingleOrNull<Proposal>()
        } catch (e: Exception) {
            println("Error fetching proposal: ${e.message}")
            null
        }
    }

    override fun getProposalsByIds(proposalIds: List<String>): Flow<List<ProposalWithUserVote>> =
        callbackFlow {
            if (proposalIds.isEmpty()) {
                send(emptyList())
                awaitClose { }
                return@callbackFlow
            }

            val flowScope = this

            suspend fun fetchProposalsWithVotes(): List<ProposalWithUserVote> {
                val currentUserId = supabase.auth.currentUserOrNull()?.id

                val proposals = supabase
                    .from("proposals")
                    .select {
                        filter { isIn("id", proposalIds) }
                    }
                    .decodeList<Proposal>()

                val proposalsWithVotes = if (currentUserId != null && proposals.isNotEmpty()) {
                    val userVotes = supabase
                        .from("proposal_votes")
                        .select {
                            filter {
                                eq("user_id", currentUserId)
                                isIn("proposal_id", proposalIds)
                            }
                        }
                        .decodeList<ProposalVote>()

                    val votesByProposalId = userVotes.associateBy { it.proposalId }

                    proposals.map { proposal ->
                        val userVote = votesByProposalId[proposal.id]?.voteType ?: 0
                        ProposalWithUserVote(proposal, userVote)
                    }
                } else {
                    proposals.map { ProposalWithUserVote(it, 0) }
                }

                return proposalsWithVotes
            }

            val channelId = "duplicates_${Random.nextLong()}"
            val channel = supabase.channel(channelId)

            val proposalChanges = channel.postgresChangeFlow<PostgresAction>(schema = "public") {
                table = "proposals"
            }

            val voteChanges = channel.postgresChangeFlow<PostgresAction>(schema = "public") {
                table = "proposal_votes"
            }

            channel.subscribe()

            try {
                val initialData = fetchProposalsWithVotes()
                send(initialData)
            } catch (e: Exception) {
                println("Error fetching duplicate proposals: ${e.message}")
                send(emptyList())
            }

            try {
                merge(proposalChanges, voteChanges).collect {
                    val updatedData = fetchProposalsWithVotes()
                    send(updatedData)
                }
            } catch (e: Exception) {
                println("Error in Realtime flow for duplicates: ${e.message}")
            }

            awaitClose {
                flowScope.launch { channel.unsubscribe() }
            }
        }
}
