package com.apptolast.spaindecides.data.model

import kotlinx.serialization.Serializable

/**
 * Represents a category of proposals in the citizen participation program.
 *
 * @property id Unique identifier for the category
 * @property name Display name of the category (in Spanish)
 * @property description Brief description of what the category covers
 * @property iconName Material Icons name for the category icon
 */
@Serializable
data class Category(
    val id: String,
    val name: String,
    val description: String,
    val iconName: String
)
