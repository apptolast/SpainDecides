package com.apptolast.spaindecides.data.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * Represents a user profile in the public.profiles table.
 *
 * This model maps to the public.profiles table in Supabase, which is separate from
 * the auth.users table. User profiles are automatically created via database trigger
 * when a user signs up.
 *
 * @property id Unique identifier for the user (references auth.users.id)
 * @property email User's email address
 * @property fullName User's full display name (optional)
 * @property avatarUrl URL to the user's avatar image (optional)
 * @property createdAt Timestamp when the profile was created (ISO 8601 format)
 * @property updatedAt Timestamp when the profile was last updated (ISO 8601 format)
 */
@Serializable
data class Profile(
    val id: String,
    val email: String,
    @SerialName("full_name") val fullName: String? = null,
    @SerialName("avatar_url") val avatarUrl: String? = null,
    @SerialName("created_at") val createdAt: String? = null,
    @SerialName("updated_at") val updatedAt: String? = null
)
