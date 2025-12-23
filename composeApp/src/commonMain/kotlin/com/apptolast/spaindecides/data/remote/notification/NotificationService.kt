package com.apptolast.spaindecides.data.remote.notification

import com.apptolast.spaindecides.BuildKonfig
import io.ktor.client.HttpClient
import io.ktor.client.request.header
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.http.ContentType
import io.ktor.http.contentType
import kotlinx.serialization.Serializable

/**
 * Service for sending push notifications via Firebase Cloud Function.
 *
 * This service communicates with our Firebase Cloud Function which handles
 * sending FCM notifications to all users subscribed to the "new_proposals" topic.
 */
class NotificationService(
    private val httpClient: HttpClient
) {
    /**
     * Sends a notification about a new proposal to all users.
     *
     * @param title The proposal title (used as notification title)
     * @param description The proposal description (first sentence used as notification body)
     * @return true if notification was sent successfully, false otherwise
     */
    suspend fun sendNewProposalNotification(title: String, description: String): Boolean {
        return try {
            val notificationBody = extractFirstSentence(description)

            val response = httpClient.post(BuildKonfig.FIREBASE_FUNCTION_URL) {
                contentType(ContentType.Application.Json)
                header("x-api-key", BuildKonfig.FIREBASE_FUNCTION_API_KEY)
                setBody(
                    NewProposalNotificationRequest(
                        title = title,
                        body = notificationBody
                    )
                )
            }

            response.status.value in 200..299
        } catch (e: Exception) {
            println("Error sending notification: ${e.message}")
            false
        }
    }

    /**
     * Extracts the first sentence from the description.
     * A sentence ends with '.', '!' or '?'
     */
    private fun extractFirstSentence(text: String): String {
        val trimmed = text.trim()
        val endIndex = trimmed.indexOfFirst { it == '.' || it == '!' || it == '?' }

        return if (endIndex != -1) {
            trimmed.substring(0, endIndex + 1)
        } else {
            // If no sentence ending found, take first 100 characters
            trimmed.take(100).let {
                if (trimmed.length > 100) "$it..." else it
            }
        }
    }
}

@Serializable
data class NewProposalNotificationRequest(
    val title: String,
    val body: String
)
