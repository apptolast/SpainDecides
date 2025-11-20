package com.apptolast.spaindecides.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.apptolast.spaindecides.data.model.ProposalWithUserVote
import com.apptolast.spaindecides.domain.repository.ProposalRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

/**
 * ViewModel for proposal-related screens (List and Create).
 * Manages proposals for a specific category, voting, and creating new proposals.
 *
 * ## KISS Architecture:
 * Each category gets its own ViewModel instance (via Koin parametersOf).
 * This ensures clean Realtime channel lifecycle management:
 * - ViewModel created → Supabase channel subscribes
 * - ViewModel destroyed → Supabase channel unsubscribes
 * No race conditions, no channel reuse issues.
 *
 * @param categoryId The category ID this ViewModel manages (injected via Koin)
 * @param proposalRepository Repository for proposal operations
 */
class ProposalViewModel(
    private val categoryId: String,
    private val proposalRepository: ProposalRepository
) : ViewModel() {

    // Direct subscription to proposals for this category
    // Simple and straightforward - no complex reactive operators needed
    val proposals: StateFlow<List<ProposalWithUserVote>> =
        proposalRepository.getProposalsByCategory(categoryId)
            .onStart {
                isLoading.value = true
                error.value = null
            }
            .catch { e ->
                error.value = e.message ?: "Error al cargar propuestas"
                isLoading.value = false
                emit(emptyList())
            }
            .onEach {
                isLoading.value = false
            }
            .stateIn(
                scope = viewModelScope,
                started = SharingStarted.Lazily,
                initialValue = emptyList()
            )

    val isLoading: StateFlow<Boolean>
        field: MutableStateFlow<Boolean> = MutableStateFlow(false)

    val error: StateFlow<String?>
        field: MutableStateFlow<String?> = MutableStateFlow(null)

    // For create proposal screen
    val newProposalTitle: StateFlow<String>
        field: MutableStateFlow<String> = MutableStateFlow("")

    val newProposalDescription: StateFlow<String>
        field: MutableStateFlow<String> = MutableStateFlow("")

    val isCreating: StateFlow<Boolean>
        field: MutableStateFlow<Boolean> = MutableStateFlow(false)

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
     * Updates the new proposal title field
     */
    fun updateNewProposalTitle(text: String) {
        if (text.length <= 100) { // Enforce 100 character limit
            newProposalTitle.value = text
        }
    }

    /**
     * Updates the new proposal description field
     */
    fun updateNewProposalDescription(text: String) {
        if (text.length <= 1000) { // Enforce 1000 character limit
            newProposalDescription.value = text
        }
    }

    /**
     * Creates a new proposal in the current category
     */
    suspend fun createProposal(): Boolean {
        // Validate title
        if (newProposalTitle.value.isBlank()) {
            error.value = "El título no puede estar vacío"
            return false
        }

        if (newProposalTitle.value.length < 10) {
            error.value = "El título debe tener al menos 10 caracteres"
            return false
        }

        if (newProposalTitle.value.length > 100) {
            error.value = "El título no puede tener más de 100 caracteres"
            return false
        }

        // Validate description
        if (newProposalDescription.value.isBlank()) {
            error.value = "La descripción no puede estar vacía"
            return false
        }

        if (newProposalDescription.value.length < 10) {
            error.value = "La descripción debe tener al menos 10 caracteres"
            return false
        }

        if (newProposalDescription.value.length > 1000) {
            error.value = "La descripción no puede tener más de 1000 caracteres"
            return false
        }

        isCreating.value = true
        error.value = null

        return try {
            proposalRepository.createProposal(
                title = newProposalTitle.value.trim(),
                description = newProposalDescription.value.trim(),
                categoryId = categoryId
            )
            newProposalTitle.value = "" // Clear the title field
            newProposalDescription.value = "" // Clear the description field
            isCreating.value = false
            true
        } catch (e: Exception) {
            error.value = e.message ?: "Error al crear propuesta"
            isCreating.value = false
            false
        }
    }

    /**
     * Clears the new proposal title and description
     */
    fun clearNewProposalFields() {
        newProposalTitle.value = ""
        newProposalDescription.value = ""
    }

    /**
     * Clears any error message
     */
    fun clearError() {
        error.value = null
    }

    /**
     * Character count for the current proposal title
     */
    val titleCharacterCount: Int
        get() = newProposalTitle.value.length

    /**
     * Character count for the current proposal description
     */
    val descriptionCharacterCount: Int
        get() = newProposalDescription.value.length
}
