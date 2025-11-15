package com.apptolast.spaindecides.data.model

import kotlinx.serialization.Serializable

/**
 * Represents a user in the system.
 * For now, this is a simple model for future authentication implementation.
 *
 * @property id Unique identifier for the user
 * @property email User's email address
 * @property name User's display name (optional)
 */
@Serializable
data class User(
    val id: String,
    val email: String,
    val name: String? = null
)
