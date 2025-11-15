package com.apptolast.spaindecides.domain.model

import kotlinx.serialization.Serializable

/**
 * Domain model representing an authenticated user
 */
@Serializable
data class AuthUser(
    val id: String,
    val email: String,
    val displayName: String? = null,
    val photoUrl: String? = null,
    val emailVerified: Boolean = false
)
