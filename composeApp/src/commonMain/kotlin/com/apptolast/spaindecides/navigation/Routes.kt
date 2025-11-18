package com.apptolast.spaindecides.navigation

import kotlinx.serialization.Serializable

/**
 * Sealed interface for type-safe navigation routes.
 * All routes must be serializable for Navigation Compose.
 */
@Serializable
sealed interface Route

/**
 * Login screen route
 * @param successMessage Optional success message to display (e.g., from registration)
 */
@Serializable
data class LoginRoute(
    val successMessage: String? = null
) : Route

/**
 * Register screen route
 */
@Serializable
object RegisterRoute : Route

/**
 * Categories/Home screen route
 */
@Serializable
object CategoriesRoute : Route

/**
 * Proposal list screen route
 * @param categoryId ID of the category to show proposals for
 * @param categoryName Display name of the category (for UI)
 */
@Serializable
data class ProposalListRoute(
    val categoryId: String,
    val categoryName: String
) : Route

/**
 * Create new proposal screen route
 * @param categoryId ID of the category to create the proposal in
 * @param categoryName Display name of the category (for UI)
 */
@Serializable
data class CreateProposalRoute(
    val categoryId: String,
    val categoryName: String
) : Route
