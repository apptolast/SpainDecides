package com.apptolast.spaindecides.data.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * Represents a category of proposals in the citizen participation program.
 *
 * Categories use a key-based system for internationalization (i18n). The actual
 * display name and description are resolved from string resources based on the key.
 *
 * @property id Unique identifier for the category (UUID from database)
 * @property key Reference key for i18n resolution (e.g., "economy", "health", "education")
 * @property iconName Material Icons name for the category icon
 * @property sortOrder Display order for the category (lower numbers appear first)
 * @property createdAt Timestamp when the category was created (ISO 8601 format)
 */
@Serializable
data class Category(
    val id: String,
    val key: String,
    @SerialName("icon_name") val iconName: String,
    @SerialName("sort_order") val sortOrder: Int,
    @SerialName("created_at") val createdAt: String? = null
)
