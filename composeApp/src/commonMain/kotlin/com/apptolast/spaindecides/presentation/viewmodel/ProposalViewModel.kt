package com.apptolast.spaindecides.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.apptolast.spaindecides.data.model.ProposalWithUserVote
import com.apptolast.spaindecides.domain.repository.ProposalRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

/**
 * ViewModel for proposal-related screens (List and Create).
 * Manages proposals for a specific category, voting, and creating new proposals.
 * Uses ProposalWithUserVote to include the current user's vote status.
 */
class ProposalViewModel(
    private val proposalRepository: ProposalRepository
) : ViewModel() {

    val proposals: StateFlow<List<ProposalWithUserVote>>
        field: MutableStateFlow<List<ProposalWithUserVote>> = MutableStateFlow(emptyList())

    val isLoading: StateFlow<Boolean>
        field: MutableStateFlow<Boolean> = MutableStateFlow(false)

    val error: StateFlow<String?>
        field: MutableStateFlow<String?> = MutableStateFlow(null)

    private val _currentCategoryId = MutableStateFlow<String?>(null)

    // For create proposal screen
    val newProposalText: StateFlow<String>
        field: MutableStateFlow<String> = MutableStateFlow("")

    val isCreating: StateFlow<Boolean>
        field: MutableStateFlow<Boolean> = MutableStateFlow(false)

    /**
     * Loads proposals for a specific category
     */
    fun loadProposalsForCategory(categoryId: String) {
        _currentCategoryId.value = categoryId

        viewModelScope.launch {
            isLoading.value = true
            error.value = null

            try {
                proposalRepository.getProposalsByCategory(categoryId).collect { proposalsList ->
                    proposals.value = proposalsList
                    isLoading.value = false
                }
            } catch (e: Exception) {
                error.value = e.message ?: "Error al cargar propuestas"
                isLoading.value = false
            }
        }
    }

    /**
     * Votes on a proposal
     * @param proposalId ID of the proposal to vote on
     * @param voteType 1 for upvote, -1 for downvote, 0 to remove vote
     */
    fun vote(proposalId: String, voteType: Int) {
        viewModelScope.launch {
            try {
                proposalRepository.voteOnProposal(proposalId, voteType)
            } catch (e: Exception) {
                error.value = e.message ?: "Error al votar"
            }
        }
    }

    /**
     * Updates the new proposal text field
     */
    fun updateNewProposalText(text: String) {
        if (text.length <= 150) { // Enforce 150 character limit
            newProposalText.value = text
        }
    }

    /**
     * Creates a new proposal in the current category
     */
    suspend fun createProposal(categoryId: String): Boolean {
        if (newProposalText.value.isBlank()) {
            error.value = "La propuesta no puede estar vacía"
            return false
        }

        if (newProposalText.value.length > 150) {
            error.value = "La propuesta no puede tener más de 150 caracteres"
            return false
        }

        isCreating.value = true
        error.value = null

        return try {
            proposalRepository.createProposal(
                title = newProposalText.value.trim(),
                categoryId = categoryId
            )
            newProposalText.value = "" // Clear the text field
            isCreating.value = false
            true
        } catch (e: Exception) {
            error.value = e.message ?: "Error al crear propuesta"
            isCreating.value = false
            false
        }
    }

    /**
     * Clears the new proposal text
     */
    fun clearNewProposalText() {
        newProposalText.value = ""
    }

    /**
     * Clears any error message
     */
    fun clearError() {
        error.value = null
    }

    /**
     * Character count for the current proposal text
     */
    val characterCount: Int
        get() = newProposalText.value.length
}
