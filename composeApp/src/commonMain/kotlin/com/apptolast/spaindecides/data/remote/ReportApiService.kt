package com.apptolast.spaindecides.data.remote

import com.apptolast.spaindecides.BuildKonfig
import com.apptolast.spaindecides.data.model.ReportRequest
import io.ktor.client.HttpClient
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.client.statement.bodyAsText
import io.ktor.http.ContentType
import io.ktor.http.contentType
import io.ktor.http.isSuccess
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * API service for sending content reports via EmailJS.
 *
 * EmailJS configuration:
 * 1. Create account at https://www.emailjs.com/
 * 2. Create an email service (Gmail, etc.)
 * 3. Create an email template with variables:
 *    - {{to_email}}, {{proposal_id}}, {{proposal_title}},
 *    - {{proposal_description}}, {{reason}}, {{reporter_user_id}},
 *    - {{reporter_email}}, {{timestamp}}
 * 4. Add your credentials to local.properties:
 *    - EMAILJS_SERVICE_ID=your_service_id
 *    - EMAILJS_TEMPLATE_ID=your_template_id
 *    - EMAILJS_PUBLIC_KEY=your_public_key
 *
 * @param httpClient Ktor HTTP client for making requests
 */
class ReportApiService(private val httpClient: HttpClient) {

    companion object {
        private const val EMAILJS_API_URL = "https://api.emailjs.com/api/v1.0/email/send"
        private const val ADMIN_EMAIL = "admin@apptolast.com"
    }

    /**
     * Sends a report via EmailJS API.
     *
     * @param report The report request containing proposal and user information
     * @return Result indicating success or failure
     */
    suspend fun sendReportViaEmailJS(report: ReportRequest): Result<Unit> = runCatching {
        val emailRequest = EmailJSRequest(
            serviceId = BuildKonfig.EMAILJS_SERVICE_ID,
            templateId = BuildKonfig.EMAILJS_TEMPLATE_ID,
            userId = BuildKonfig.EMAILJS_PUBLIC_KEY,
            templateParams = EmailTemplateParams(
                toEmail = ADMIN_EMAIL,
                proposalId = report.proposalId,
                proposalTitle = report.proposalTitle,
                proposalDescription = report.proposalDescription,
                reason = report.reason,
                reporterUserId = report.reporterUserId ?: "Anónimo",
                reporterEmail = report.reporterEmail ?: "No proporcionado",
                timestamp = report.timestamp
            )
        )

        val response = httpClient.post(EMAILJS_API_URL) {
            contentType(ContentType.Application.Json)
            setBody(emailRequest)
        }

        if (!response.status.isSuccess()) {
            val errorBody = response.bodyAsText()
            throw Exception("EmailJS API failed: ${response.status} - $errorBody")
        }
    }
}

/**
 * EmailJS API request body structure.
 */
@Serializable
private data class EmailJSRequest(
    @kotlinx.serialization.SerialName("service_id") val serviceId: String,
    @kotlinx.serialization.SerialName("template_id") val templateId: String,
    @kotlinx.serialization.SerialName("user_id") val userId: String,
    @kotlinx.serialization.SerialName("template_params") val templateParams: EmailTemplateParams
)

/**
 * Template parameters for the EmailJS email template.
 */
@Serializable
private data class EmailTemplateParams(
    @SerialName("to_email") val toEmail: String,
    @SerialName("proposal_id") val proposalId: String,
    @SerialName("proposal_title") val proposalTitle: String,
    @SerialName("proposal_description") val proposalDescription: String,
    @SerialName("reason") val reason: String,
    @SerialName("reporter_user_id") val reporterUserId: String,
    @SerialName("reporter_email") val reporterEmail: String,
    @SerialName("timestamp") val timestamp: String
)
