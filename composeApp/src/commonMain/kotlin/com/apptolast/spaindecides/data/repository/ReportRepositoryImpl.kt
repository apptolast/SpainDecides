package com.apptolast.spaindecides.data.repository

import com.apptolast.spaindecides.data.model.ReportRequest
import com.apptolast.spaindecides.data.remote.ReportApiService
import com.apptolast.spaindecides.domain.repository.ReportRepository

/**
 * Implementation of [ReportRepository] that uses EmailJS to send reports.
 *
 * @param apiService The API service for sending reports
 */
class ReportRepositoryImpl(
    private val apiService: ReportApiService
) : ReportRepository {

    override suspend fun sendReport(report: ReportRequest): Result<Unit> {
        return apiService.sendReportViaEmailJS(report)
    }
}
