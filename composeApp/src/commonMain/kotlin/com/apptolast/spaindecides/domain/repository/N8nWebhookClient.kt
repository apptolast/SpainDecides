package com.apptolast.spaindecides.domain.repository

import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.client.statement.bodyAsText
import io.ktor.http.ContentType
import io.ktor.http.contentType
import io.ktor.http.isSuccess
import kotlinx.serialization.Serializable

/**
 * Client for communicating with n8n webhooks for proposal processing.
 *
 * ## n8n Webhook Configuration Requirements:
 * - **Respond**: Must be set to "When Last Node Finishes" (NOT "Immediately")
 *   This ensures the workflow completes duplicate detection before responding.
 * - **Path**: Should match [webhookPath] parameter
 * - **Method**: POST
 *
 * ## URLs:
 * - Test: https://n8n.apptolast.com/webhook-test/{path}
 * - Production: https://n8n.apptolast.com/webhook/{path}
 *
 * @param httpClient Ktor HTTP client for making requests
 * @param baseUrl Base URL for n8n webhooks (use /webhook/ for production)
 * @param webhookPath The webhook path configured in n8n
 */
class N8nWebhookClient(
    private val httpClient: HttpClient,
    private val baseUrl: String = "https://n8n.apptolast.com/webhook-test/",
    private val webhookPath: String = "new-proposal"
) {
    /**
     * Sends a proposal to n8n for processing (duplicate detection, creation, etc.)
     *
     * @param request The proposal data to process
     * @return ProposalProcessingResult with status and any duplicates found
     * @throws ProposalProcessingException if the request fails
     */
    suspend fun processProposal(request: ProposalProcessingRequest): ProposalProcessingResult {
        return try {
            println("$baseUrl$webhookPath")
            //val response = httpClient.post("$baseUrl$webhookPath") {
            val response = httpClient.post("https://n8n.apptolast.com/webhook/new-proposal") {
                contentType(ContentType.Application.Json)
                setBody(request)
            }

            if (response.status.isSuccess()) {
                response.body<ProposalProcessingResult>()
            } else {
                val errorBody = response.bodyAsText()
                throw ProposalProcessingException(
                    "n8n webhook error: ${response.status.value} - $errorBody"
                )
            }
        } catch (e: ProposalProcessingException) {
            throw e
        } catch (e: Exception) {
            throw ProposalProcessingException(
                "Failed to connect to n8n webhook: ${e.message}"
            )
        }
    }
}

/**
 * Request payload sent to n8n webhook
 */
@Serializable
data class ProposalProcessingRequest(
    val title: String,
    val description: String,
    val categoryId: String,
    val userId: String,
    val sendNotification: Boolean = true
)

/**
 * Response from n8n webhook.
 *
 * n8n workflow should return this JSON structure:
 * ```json
 * {
 *   "success": true,
 *   "status": "CREATED",
 *   "proposalId": "uuid-here",
 *   "duplicates": [],
 *   "message": null
 * }
 * ```
 */
@Serializable
data class ProposalProcessingResult(
    val success: Boolean,
    val status: ProposalStatus,
    val proposalId: String? = null,
    val duplicates: List<DuplicateProposal> = emptyList(),
    val message: String? = null
)

@Serializable
enum class ProposalStatus {
    CREATED,           // Proposal was created successfully
    DUPLICATE_FOUND,   // Similar proposals found, user should choose
    ERROR              // Something went wrong
}

@Serializable
data class DuplicateProposal(
    val id: String,
    val title: String,
    val description: String,
    val similarity: Float,  // 0.0 - 1.0
    val votesCount: Int
)

class ProposalProcessingException(message: String) : Exception(message)
