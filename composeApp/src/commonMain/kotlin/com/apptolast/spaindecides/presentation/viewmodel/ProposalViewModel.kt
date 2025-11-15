package com.apptolast.spaindecides.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.apptolast.spaindecides.data.model.Proposal
import com.apptolast.spaindecides.domain.repository.ProposalRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

/**
 * ViewModel for proposal-related screens (List and Create).
 * Manages proposals for a specific category, voting, and creating new proposals.
 */
class ProposalViewModel(
    private val proposalRepository: ProposalRepository
) : ViewModel() {

    private val _proposals = MutableStateFlow<List<Proposal>>(emptyList())
    val proposals: StateFlow<List<Proposal>> = _proposals.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error.asStateFlow()

    private val _currentCategoryId = MutableStateFlow<String?>(null)

    // For create proposal screen
    private val _newProposalText = MutableStateFlow("")
    val newProposalText: StateFlow<String> = _newProposalText.asStateFlow()

    private val _isCreating = MutableStateFlow(false)
    val isCreating: StateFlow<Boolean> = _isCreating.asStateFlow()

    /**
     * Loads proposals for a specific category
     */
    fun loadProposalsForCategory(categoryId: String) {
        _currentCategoryId.value = categoryId

        viewModelScope.launch {
            _isLoading.value = true
            _error.value = null

            try {
                proposalRepository.getProposalsByCategory(categoryId).collect { proposalsList ->
                    _proposals.value = proposalsList
                    _isLoading.value = false
                }
            } catch (e: Exception) {
                _error.value = e.message ?: "Error al cargar propuestas"
                _isLoading.value = false
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
                _error.value = e.message ?: "Error al votar"
            }
        }
    }

    /**
     * Updates the new proposal text field
     */
    fun updateNewProposalText(text: String) {
        if (text.length <= 150) { // Enforce 150 character limit
            _newProposalText.value = text
        }
    }

    /**
     * Creates a new proposal in the current category
     */
    suspend fun createProposal(categoryId: String): Boolean {
        if (_newProposalText.value.isBlank()) {
            _error.value = "La propuesta no puede estar vacía"
            return false
        }

        if (_newProposalText.value.length > 150) {
            _error.value = "La propuesta no puede tener más de 150 caracteres"
            return false
        }

        _isCreating.value = true
        _error.value = null

        return try {
            proposalRepository.createProposal(
                title = _newProposalText.value.trim(),
                categoryId = categoryId
            )
            _newProposalText.value = "" // Clear the text field
            _isCreating.value = false
            true
        } catch (e: Exception) {
            _error.value = e.message ?: "Error al crear propuesta"
            _isCreating.value = false
            false
        }
    }

    /**
     * Clears the new proposal text
     */
    fun clearNewProposalText() {
        _newProposalText.value = ""
    }

    /**
     * Clears any error message
     */
    fun clearError() {
        _error.value = null
    }

    /**
     * Character count for the current proposal text
     */
    val characterCount: Int
        get() = _newProposalText.value.length
}
