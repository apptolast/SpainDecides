package com.apptolast.spaindecides.data.remote

import com.apptolast.spaindecides.data.model.ProposalProcessingRequest
import com.apptolast.spaindecides.data.model.ProposalProcessingResponse
import com.apptolast.spaindecides.domain.repository.AuthRepository
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.network.sockets.ConnectTimeoutException
import io.ktor.client.network.sockets.SocketTimeoutException
import io.ktor.client.plugins.HttpRequestTimeoutException
import io.ktor.client.request.bearerAuth
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.client.statement.bodyAsText
import io.ktor.http.ContentType
import io.ktor.http.HttpStatusCode
import io.ktor.http.contentType
import io.ktor.http.isSuccess
import io.ktor.serialization.JsonConvertException

/**
 * HTTP client for communicating with n8n webhooks.
 *
 * This client handles the communication with the n8n workflow that processes
 * new proposals, including duplicate detection via AI embeddings.
 *
 * ## Security:
 * - Uses JWT authentication via Supabase access token
 * - Token is sent in the Authorization: Bearer header
 * - n8n webhook must be configured with JWT Auth using Supabase JWT secret
 *
 * ## n8n Webhook Configuration Requirements:
 * - **Authentication**: JWT Auth (PEM Key with ES256 algorithm and Supabase public key)
 * - **Respond**: Must be "When Last Node Finishes" (NOT "Immediately")
 * - **Method**: POST
 * - **Content-Type**: application/json
 *
 * @param httpClient Ktor HTTP client (injected via Koin)
 * @param authRepository Repository to get the current user's JWT token
 */
class N8nWebhookClient(
    private val httpClient: HttpClient,
    private val authRepository: AuthRepository
) {

    /**
     * Sends a proposal to n8n for processing.
     *
     * The n8n workflow will:
     * 1. Validate the JWT token
     * 2. Generate embeddings for semantic search
     * 3. Check for duplicate/similar proposals
     * 4. Return status indicating if proposal was created or duplicates were found
     *
     * @param request The proposal data to process
     * @return ProposalProcessingResponse with status and any duplicates found
     */
    suspend fun processProposal(request: ProposalProcessingRequest): Result<ProposalProcessingResponse> {
        val accessToken = authRepository.getAccessToken()
            ?: return Result.failure(
                N8nWebhookException.Unauthorized("No hay sesión activa. Por favor, inicia sesión.")
            )

        return try {
            val response = httpClient.post(WEBHOOK_URL) {
                contentType(ContentType.Application.Json)
                bearerAuth(accessToken)
                setBody(request)
            }

            val responseBody = response.bodyAsText()

            when {
                response.status.isSuccess() -> {
                    if (responseBody.isBlank()) {
                        Result.failure(
                            N8nWebhookException.ParseError("El servidor no devolvió datos. Inténtalo de nuevo.")
                        )
                    } else {
                        val result = response.body<ProposalProcessingResponse>()
                        Result.success(result)
                    }
                }

                response.status == HttpStatusCode.Unauthorized -> {
                    Result.failure(
                        N8nWebhookException.Unauthorized("Sesión expirada. Por favor, inicia sesión de nuevo.")
                    )
                }

                response.status == HttpStatusCode.Forbidden -> {
                    Result.failure(
                        N8nWebhookException.Unauthorized("No tienes permiso para realizar esta acción.")
                    )
                }

                else -> {
                    Result.failure(
                        N8nWebhookException.ServerError(
                            statusCode = response.status.value,
                            message = "Server returned ${response.status.value}: $responseBody"
                        )
                    )
                }
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

    companion object {
        private const val BASE_URL_WEBHOOK = "https://n8n.apptolast.com/webhook/"

        private val WEBHOOK_URL: String
            get() = BASE_URL_WEBHOOK + Environment.N8N_WEBHOOK_PATH
    }
}

/**
 * Sealed class representing errors that can occur when communicating with n8n.
 */
sealed class N8nWebhookException(message: String) : Exception(message) {
    /** User is not authenticated or token is invalid/expired */
    class Unauthorized(message: String) : N8nWebhookException(message)

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