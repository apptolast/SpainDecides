package com.apptolast.spaindecides.data.remote

import com.apptolast.spaindecides.data.model.ProposalProcessingRequest
import com.apptolast.spaindecides.data.model.ProposalProcessingResponse
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.network.sockets.ConnectTimeoutException
import io.ktor.client.network.sockets.SocketTimeoutException
import io.ktor.client.plugins.HttpRequestTimeoutException
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.client.statement.bodyAsText
import io.ktor.http.ContentType
import io.ktor.http.contentType
import io.ktor.http.isSuccess
import io.ktor.serialization.JsonConvertException

private const val BASE_URL_WEBHOOK = "https://n8n.apptolast.com/webhook-test/"
private const val PATH_URL_WEBHOOK = "new-proposal"

/**
 * HTTP client for communicating with n8n webhooks.
 *
 * This client handles the communication with the n8n workflow that processes
 * new proposals, including duplicate detection via AI embeddings.
 *
 * ## n8n Webhook Configuration Requirements:
 * - **Respond**: Must be "When Last Node Finishes" (NOT "Immediately")
 * - **Method**: POST
 * - **Content-Type**: application/json
 *
 * @param httpClient Ktor HTTP client (injected via Koin)
 */
class N8nWebhookClient(
    private val httpClient: HttpClient,
) {

    private val webhookUrl = BASE_URL_WEBHOOK + PATH_URL_WEBHOOK

    /**
     * Sends a proposal to n8n for processing.
     *
     * The n8n workflow will:
     * 1. Generate embeddings for semantic search
     * 2. Check for duplicate/similar proposals
     * 3. Return status indicating if proposal was created or duplicates were found
     *
     * @param request The proposal data to process
     * @return ProposalProcessingResponse with status and any duplicates found
     */
    suspend fun processProposal(request: ProposalProcessingRequest): Result<ProposalProcessingResponse> {
        return try {
            val response = httpClient.post(webhookUrl) {
                contentType(ContentType.Application.Json)
                setBody(request)
            }

            if (response.status.isSuccess()) {
                val result = response.body<ProposalProcessingResponse>()
                Result.success(result)
            } else {
                val errorBody = response.bodyAsText()
                Result.failure(
                    N8nWebhookException.ServerError(
                        statusCode = response.status.value,
                        message = "Server returned ${response.status.value}: $errorBody"
                    )
                )
            }
        } catch (e: HttpRequestTimeoutException) {
            Result.failure(
                N8nWebhookException.Timeout("Request timed out while processing proposal")
            )
        } catch (e: ConnectTimeoutException) {
            Result.failure(
                N8nWebhookException.NetworkError("Could not connect to n8n server")
            )
        } catch (e: SocketTimeoutException) {
            Result.failure(
                N8nWebhookException.Timeout("Connection timed out")
            )
        } catch (e: JsonConvertException) {
            Result.failure(
                N8nWebhookException.ParseError("Invalid response format from n8n: ${e.message}")
            )
        } catch (e: Exception) {
            Result.failure(
                N8nWebhookException.Unknown("Unexpected error: ${e.message}")
            )
        }
    }
}

/**
 * Sealed class representing errors that can occur when communicating with n8n.
 */
sealed class N8nWebhookException(message: String) : Exception(message) {
    /** Network connectivity issues */
    class NetworkError(message: String) : N8nWebhookException(message)

    /** Request or connection timeout */
    class Timeout(message: String) : N8nWebhookException(message)

    /** Server returned an error status code */
    class ServerError(val statusCode: Int, message: String) : N8nWebhookException(message)

    /** Response could not be parsed */
    class ParseError(message: String) : N8nWebhookException(message)

    /** Any other unexpected error */
    class Unknown(message: String) : N8nWebhookException(message)
}
