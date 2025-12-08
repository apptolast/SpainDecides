package com.apptolast.spaindecides.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.apptolast.spaindecides.data.model.ReportReason
import com.apptolast.spaindecides.data.model.ReportRequest
import com.apptolast.spaindecides.domain.repository.ReportRepository
import com.apptolast.spaindecides.util.getCurrentTimestamp
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

/**
 * Represents the UI state for report submission.
 */
sealed class ReportUiState {
    data object Initial : ReportUiState()
    data object Loading : ReportUiState()
    data object Success : ReportUiState()
    data class Error(val message: String) : ReportUiState()
}

/**
 * ViewModel for handling content report submissions.
 *
 * @param reportRepository Repository for sending reports
 */
class ReportViewModel(
    private val reportRepository: ReportRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow<ReportUiState>(ReportUiState.Initial)
    val uiState: StateFlow<ReportUiState> = _uiState.asStateFlow()

    /**
     * Submits a report for a proposal.
     *
     * @param proposalId ID of the proposal being reported
     * @param proposalTitle Title of the proposal
     * @param proposalDescription Description of the proposal
     * @param reason The reason for the report
     * @param currentUserId ID of the current user (null if anonymous)
     * @param currentUserEmail Email of the current user (null if not available)
     */
    fun submitReport(
        proposalId: String,
        proposalTitle: String,
        proposalDescription: String,
        reason: ReportReason,
        currentUserId: String?,
        currentUserEmail: String?
    ) {
        viewModelScope.launch {
            _uiState.value = ReportUiState.Loading

            val report = ReportRequest(
                proposalId = proposalId,
                proposalTitle = proposalTitle,
                proposalDescription = proposalDescription,
                reason = reason.displayName,
                reporterUserId = currentUserId,
                reporterEmail = currentUserEmail,
                timestamp = getCurrentTimestamp()
            )

            reportRepository.sendReport(report)
                .onSuccess {
                    _uiState.value = ReportUiState.Success
                }
                .onFailure { error ->
                    _uiState.value = ReportUiState.Error(
                        error.message ?: "Error al enviar el reporte"
                    )
                }
        }
    }

    /**
     * Resets the UI state to initial.
     */
    fun resetState() {
        _uiState.value = ReportUiState.Initial
    }
}
