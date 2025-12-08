package com.apptolast.spaindecides.data.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * Data class representing a report request to be sent via EmailJS.
 *
 * @property proposalId Unique identifier of the reported proposal
 * @property proposalTitle Title of the reported proposal
 * @property proposalDescription Description of the reported proposal
 * @property reason Human-readable reason for the report
 * @property reporterUserId ID of the user making the report (null if anonymous)
 * @property reporterEmail Email of the user making the report (null if not available)
 * @property timestamp ISO 8601 timestamp of when the report was made
 */
@Serializable
data class ReportRequest(
    @SerialName("proposal_id") val proposalId: String,
    @SerialName("proposal_title") val proposalTitle: String,
    @SerialName("proposal_description") val proposalDescription: String,
    val reason: String,
    @SerialName("reporter_user_id") val reporterUserId: String?,
    @SerialName("reporter_email") val reporterEmail: String?,
    val timestamp: String
)
