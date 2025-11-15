package com.apptolast.spaindecides.domain.repository

import com.apptolast.spaindecides.data.model.ReportRequest

/**
 * Repository interface for handling content report operations.
 */
interface ReportRepository {
    /**
     * Sends a report for inappropriate content.
     *
     * @param report The report request containing all relevant information
     * @return Result indicating success or failure
     */
    suspend fun sendReport(report: ReportRequest): Result<Unit>
}
