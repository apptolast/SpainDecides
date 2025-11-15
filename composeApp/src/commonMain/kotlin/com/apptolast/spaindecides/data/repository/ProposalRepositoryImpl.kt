package com.apptolast.spaindecides.data.repository

import com.apptolast.spaindecides.data.MockData
import com.apptolast.spaindecides.data.model.Proposal
import com.apptolast.spaindecides.domain.repository.ProposalRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.map

/**
 * Implementation of ProposalRepository using mock data stored in memory.
 * Supports creating proposals and voting with in-memory persistence.
 * This will be replaced with a real API implementation in the future.
 */
class ProposalRepositoryImpl : ProposalRepository {

    // In-memory storage of proposals (mutable for voting and creating)
    private val proposalsFlow = MutableStateFlow(
        MockData.proposalsByCategory.values.flatten().toMutableList()
    )

    override fun getProposalsByCategory(categoryId: String): Flow<List<Proposal>> {
        return proposalsFlow.map { proposals ->
            proposals
                .filter { it.categoryId == categoryId }
                .sortedByDescending { it.netVotes }
        }
    }

    override suspend fun createProposal(title: String, categoryId: String): Proposal {
        val newProposal = Proposal(
            id = "p_${System.currentTimeMillis()}", // Simple ID generation
            title = title,
            categoryId = categoryId,
            upvotes = 0,
            downvotes = 0,
            userVote = 0
        )

        val currentProposals = proposalsFlow.value.toMutableList()
        currentProposals.add(newProposal)
        proposalsFlow.value = currentProposals

        return newProposal
    }

    override suspend fun voteOnProposal(proposalId: String, voteType: Int): Proposal? {
        val currentProposals = proposalsFlow.value.toMutableList()
        val proposalIndex = currentProposals.indexOfFirst { it.id == proposalId }

        if (proposalIndex == -1) return null

        val proposal = currentProposals[proposalIndex]
        val previousVote = proposal.userVote

        // Calculate new vote counts based on previous and new vote
        val newUpvotes = when {
            previousVote == 1 && voteType != 1 -> proposal.upvotes - 1 // Remove upvote
            previousVote != 1 && voteType == 1 -> proposal.upvotes + 1 // Add upvote
            else -> proposal.upvotes
        }

        val newDownvotes = when {
            previousVote == -1 && voteType != -1 -> proposal.downvotes - 1 // Remove downvote
            previousVote != -1 && voteType == -1 -> proposal.downvotes + 1 // Add downvote
            else -> proposal.downvotes
        }

        val updatedProposal = proposal.copy(
            upvotes = newUpvotes,
            downvotes = newDownvotes,
            userVote = voteType
        )

        currentProposals[proposalIndex] = updatedProposal
        proposalsFlow.value = currentProposals

        return updatedProposal
    }

    override suspend fun getProposalById(proposalId: String): Proposal? {
        return proposalsFlow.value.firstOrNull { it.id == proposalId }
    }
}
