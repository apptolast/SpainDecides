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
 * @param categoryId ID of the category (UUID from database)
 * @param categoryKey i18n key for resolving localized name (e.g., "economy", "health")
 */
@Serializable
data class ProposalListRoute(
    val categoryId: String,
    val categoryKey: String
) : Route

/**
 * Create new proposal screen route
 * @param categoryId ID of the category (UUID from database)
 * @param categoryKey i18n key for resolving localized name (e.g., "economy", "health")
 */
@Serializable
data class CreateProposalRoute(
    val categoryId: String,
    val categoryKey: String
) : Route

/**
 * Proposal detail screen route - displays full proposal with voting
 * @param proposalId ID of the proposal to display (UUID from database)
 * @param categoryId ID of the category (UUID from database)
 * @param categoryKey i18n key for resolving localized name (e.g., "economy", "health")
 */
@Serializable
data class ProposalDetailRoute(
    val proposalId: String,
    val categoryId: String,
    val categoryKey: String
) : Route

/**
 * Settings screen route
 */
@Serializable
object SettingsRoute : Route
