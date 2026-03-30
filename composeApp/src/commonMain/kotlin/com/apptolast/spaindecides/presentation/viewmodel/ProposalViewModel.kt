package com.apptolast.spaindecides.presentation.viewmodel

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.apptolast.spaindecides.data.model.ProposalWithUserVote
import com.apptolast.spaindecides.data.model.SimilarProposal
import com.apptolast.spaindecides.domain.repository.CreateProposalResult
import com.apptolast.spaindecides.domain.repository.ProposalRepository
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.onStart
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
    private val proposalRepository: ProposalRepository,
    private val savedStateHandle: SavedStateHandle
) : ViewModel() {

    // ==================== Proposals List State ====================

    private val _proposals = MutableStateFlow<List<ProposalWithUserVote>>(emptyList())
    val proposals: StateFlow<List<ProposalWithUserVote>> = _proposals.asStateFlow()

    val isLoading: StateFlow<Boolean>
        field: MutableStateFlow<Boolean> = MutableStateFlow(false)

    val error: StateFlow<String?>
        field: MutableStateFlow<String?> = MutableStateFlow(null)

    /** Tracks proposals with in-flight votes to prevent concurrent taps */
    private val votingInProgress = mutableSetOf<String>()

    init {
        viewModelScope.launch {
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
                .collect { serverProposals ->
                    isLoading.value = false
                    _proposals.value = serverProposals
                }
        }
    }

    // ==================== Create Proposal State ====================

    val newProposalTitle: StateFlow<String> = savedStateHandle.getStateFlow(
        key = KEY_PROPOSAL_TITLE,
        initialValue = ""
    )

    val newProposalDescription: StateFlow<String> = savedStateHandle.getStateFlow(
        key = KEY_PROPOSAL_DESCRIPTION,
        initialValue = ""
    )

    val isCreating: StateFlow<Boolean>
        field: MutableStateFlow<Boolean> = MutableStateFlow(false)

    // ==================== Duplicate Detection State ====================

    val duplicatesFound: StateFlow<List<SimilarProposal>>
        field: MutableStateFlow<List<SimilarProposal>> = MutableStateFlow(emptyList())

    val showDuplicatesDialog: StateFlow<Boolean>
        field: MutableStateFlow<Boolean> = MutableStateFlow(false)

    /**
     * Real-time data for duplicate proposals from Supabase.
     * Contains actual vote counts and user vote state.
     */
    val duplicateProposals: StateFlow<List<ProposalWithUserVote>>
        field: MutableStateFlow<List<ProposalWithUserVote>> = MutableStateFlow(emptyList())

    private var duplicatesJob: Job? = null

    /**
     * Starts real-time subscription for duplicate proposals.
     * Called when duplicates are found to get live vote counts.
     */
    private fun subscribeToDuplicates(duplicateIds: List<String>) {
        duplicatesJob?.cancel()
        duplicatesJob = viewModelScope.launch {
            proposalRepository.getProposalsByIds(duplicateIds)
                .catch { e ->
                    error.value = e.message ?: "Error al cargar propuestas"
                }
                .collect { proposals ->
                    duplicateProposals.value = proposals
                }
        }
    }

    // ==================== Voting ====================

    /**
     * Votes on a proposal with optimistic UI update.
     *
     * 1. Updates local state immediately for instant feedback
     * 2. Sends vote to Supabase in background
     * 3. Reverts if the API call fails
     *
     * Concurrent taps on the same proposal are ignored while a vote is in-flight.
     *
     * @param proposalId ID of the proposal to vote on
     * @param voteType 1 for upvote, -1 for downvote, 0 to remove vote
     */
    fun vote(proposalId: String, voteType: Int) {
        if (!votingInProgress.add(proposalId)) return

        val previousState = _proposals.value

        // Optimistic update: apply vote change locally
        _proposals.value = previousState.map { item ->
            if (item.id == proposalId) {
                val oldVote = item.userVote
                val upvoteDelta = when {
                    voteType == 1 && oldVote != 1 -> 1
                    voteType != 1 && oldVote == 1 -> -1
                    else -> 0
                }
                val downvoteDelta = when {
                    voteType == -1 && oldVote != -1 -> 1
                    voteType != -1 && oldVote == -1 -> -1
                    else -> 0
                }
                ProposalWithUserVote(
                    proposal = item.proposal.copy(
                        upvotes = (item.upvotes + upvoteDelta).coerceAtLeast(0),
                        downvotes = (item.downvotes + downvoteDelta).coerceAtLeast(0)
                    ),
                    userVote = voteType
                )
            } else {
                item
            }
        }.sortedWith(
            compareByDescending<ProposalWithUserVote> { it.netVotes }
                .thenBy { it.id }
        )

        // Send to server in background
        viewModelScope.launch {
            try {
                val result = proposalRepository.voteOnProposal(proposalId, voteType)
                if (result == null) {
                    _proposals.value = previousState
                    error.value = "Error al votar"
                }
            } catch (e: Exception) {
                _proposals.value = previousState
                error.value = e.message ?: "Error al votar"
            } finally {
                votingInProgress.remove(proposalId)
            }
        }
    }

    // ==================== Create Proposal Form ====================

    /**
     * Updates the new proposal title field.
     */
    fun updateNewProposalTitle(text: String) {
        if (text.length <= MAX_TITLE_LENGTH) {
            savedStateHandle[KEY_PROPOSAL_TITLE] = text
        }
    }

    /**
     * Updates the new proposal description field.
     */
    fun updateNewProposalDescription(text: String) {
        if (text.length <= MAX_DESCRIPTION_LENGTH) {
            savedStateHandle[KEY_PROPOSAL_DESCRIPTION] = text
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
                // Start real-time subscription for live vote counts
                val duplicateIds = result.duplicates.map { it.id }
                subscribeToDuplicates(duplicateIds)
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

    /**
     * Votes on a duplicate proposal.
     * Real-time subscription automatically updates the UI when the vote is persisted.
     *
     * @param proposalId ID of the duplicate proposal to vote on
     * @param voteType 1 for upvote, -1 for downvote (toggles if already voted)
     */
    fun voteOnDuplicate(proposalId: String, voteType: Int) {
        // Find current vote from real-time data
        val currentVote = duplicateProposals.value
            .find { it.id == proposalId }?.userVote ?: 0
        val newVote = if (currentVote == voteType) 0 else voteType

        viewModelScope.launch {
            try {
                // Real-time subscription handles UI update automatically
                proposalRepository.voteOnProposal(proposalId, newVote)
            } catch (e: Exception) {
                error.value = e.message ?: "Error al votar"
            }
        }
    }

    /**
     * Clears duplicate proposals state. Called when leaving the duplicates screen.
     */
    fun clearDuplicatesState() {
        duplicatesJob?.cancel()
        duplicatesJob = null
        showDuplicatesDialog.value = false
        duplicatesFound.value = emptyList()
        duplicateProposals.value = emptyList()
    }

    // ==================== Helpers ====================

    /**
     * Clears the new proposal form fields.
     */
    fun clearNewProposalFields() {
        savedStateHandle[KEY_PROPOSAL_TITLE] = ""
        savedStateHandle[KEY_PROPOSAL_DESCRIPTION] = ""
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

        // SavedStateHandle keys for state persistence across process death
        private const val KEY_PROPOSAL_TITLE = "proposal_title"
        private const val KEY_PROPOSAL_DESCRIPTION = "proposal_description"
    }
}
