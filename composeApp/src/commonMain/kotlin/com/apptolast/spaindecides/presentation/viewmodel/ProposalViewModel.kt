package com.apptolast.spaindecides.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.apptolast.spaindecides.data.model.ProposalWithUserVote
import com.apptolast.spaindecides.data.model.SimilarProposal
import com.apptolast.spaindecides.domain.repository.CreateProposalResult
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
 * ## Architecture:
 * - Only depends on ProposalRepository (follows clean architecture)
 * - Business logic (duplicate detection, n8n) is encapsulated in repository
 * - ViewModel handles UI state only
 *
 * ## KISS Principle:
 * Each category gets its own ViewModel instance (via Koin parametersOf).
 * This ensures clean Realtime channel lifecycle management.
 *
 * @param categoryId The category ID this ViewModel manages (injected via Koin)
 * @param proposalRepository Repository for proposal operations
 */
class ProposalViewModel(
    private val categoryId: String,
    private val proposalRepository: ProposalRepository
) : ViewModel() {

    // ==================== Proposals List State ====================

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

    // ==================== Create Proposal State ====================

    val newProposalTitle: StateFlow<String>
        field: MutableStateFlow<String> = MutableStateFlow("")

    val newProposalDescription: StateFlow<String>
        field: MutableStateFlow<String> = MutableStateFlow("")

    val isCreating: StateFlow<Boolean>
        field: MutableStateFlow<Boolean> = MutableStateFlow(false)

    // ==================== Duplicate Detection State ====================

    val duplicatesFound: StateFlow<List<SimilarProposal>>
        field: MutableStateFlow<List<SimilarProposal>> = MutableStateFlow(emptyList())

    val showDuplicatesDialog: StateFlow<Boolean>
        field: MutableStateFlow<Boolean> = MutableStateFlow(false)

    // ==================== Voting ====================

    /**
     * Votes on a proposal.
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

    // ==================== Create Proposal Form ====================

    /**
     * Updates the new proposal title field.
     */
    fun updateNewProposalTitle(text: String) {
        if (text.length <= MAX_TITLE_LENGTH) {
            newProposalTitle.value = text
        }
    }

    /**
     * Updates the new proposal description field.
     */
    fun updateNewProposalDescription(text: String) {
        if (text.length <= MAX_DESCRIPTION_LENGTH) {
            newProposalDescription.value = text
        }
    }

    /**
     * Character count for the current proposal title.
     */
    val titleCharacterCount: Int
        get() = newProposalTitle.value.length

    /**
     * Character count for the current proposal description.
     */
    val descriptionCharacterCount: Int
        get() = newProposalDescription.value.length

    // ==================== Create Proposal Logic ====================

    /**
     * Creates a new proposal with AI-powered duplicate detection.
     *
     * @return true if created successfully, false if error or duplicates found
     */
    suspend fun createProposal(forceCreation: Boolean = false): Boolean {
        if (!validateProposal()) return false

        isCreating.value = true
        error.value = null

        val result = proposalRepository.createProposal(
            title = newProposalTitle.value.trim(),
            description = newProposalDescription.value.trim(),
            categoryId = categoryId,
            forceCreation = forceCreation
        )

        return when (result) {
            is CreateProposalResult.Success -> {
                clearNewProposalFields()
                isCreating.value = false
                true
            }

            is CreateProposalResult.DuplicatesFound -> {
                duplicatesFound.value = result.duplicates
                showDuplicatesDialog.value = true
                isCreating.value = false
                false
            }

            is CreateProposalResult.Error -> {
                error.value = result.message
                isCreating.value = false
                false
            }
        }
    }

    /**
     * User selected an existing proposal instead of creating a new one.
     */
    fun selectExistingProposal(proposalId: String) {
        viewModelScope.launch {
            showDuplicatesDialog.value = false

            try {
                proposalRepository.voteOnProposal(proposalId, 1)
                clearNewProposalFields()
            } catch (e: Exception) {
                error.value = e.message ?: "Error al seleccionar propuesta"
            }
        }
    }

    /**
     * Dismisses the duplicates dialog.
     */
    fun dismissDuplicatesDialog() {
        showDuplicatesDialog.value = false
        duplicatesFound.value = emptyList()
    }

    // ==================== Helpers ====================

    /**
     * Clears the new proposal form fields.
     */
    fun clearNewProposalFields() {
        newProposalTitle.value = ""
        newProposalDescription.value = ""
    }

    /**
     * Clears any error message.
     */
    fun clearError() {
        error.value = null
    }

    /**
     * Validates proposal form fields.
     */
    private fun validateProposal(): Boolean {
        val title = newProposalTitle.value
        val description = newProposalDescription.value

        return when {
            title.isBlank() -> {
                error.value = "El título no puede estar vacío"
                false
            }

            title.length < MIN_TITLE_LENGTH -> {
                error.value = "El título debe tener al menos $MIN_TITLE_LENGTH caracteres"
                false
            }

            title.length > MAX_TITLE_LENGTH -> {
                error.value = "El título no puede tener más de $MAX_TITLE_LENGTH caracteres"
                false
            }

            description.isBlank() -> {
                error.value = "La descripción no puede estar vacía"
                false
            }

            description.length < MIN_DESCRIPTION_LENGTH -> {
                error.value = "La descripción debe tener al menos $MIN_DESCRIPTION_LENGTH caracteres"
                false
            }

            description.length > MAX_DESCRIPTION_LENGTH -> {
                error.value = "La descripción no puede tener más de $MAX_DESCRIPTION_LENGTH caracteres"
                false
            }

            else -> true
        }
    }

    companion object {
        const val MIN_TITLE_LENGTH = 10
        const val MAX_TITLE_LENGTH = 100
        const val MIN_DESCRIPTION_LENGTH = 10
        const val MAX_DESCRIPTION_LENGTH = 1000
    }
}
