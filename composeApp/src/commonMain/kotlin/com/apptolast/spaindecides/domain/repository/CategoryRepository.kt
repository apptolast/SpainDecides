package com.apptolast.spaindecides.domain.repository

import com.apptolast.spaindecides.data.model.Category
import kotlinx.coroutines.flow.Flow

/**
 * Repository interface for managing categories.
 * Provides methods to retrieve available categories for citizen participation.
 */
interface CategoryRepository {

    /**
     * Gets all available categories as a Flow.
     * This allows reactive updates if categories change in the future.
     *
     * @return Flow emitting the list of all categories
     */
    fun getCategories(): Flow<List<Category>>

    /**
     * Gets a specific category by its ID.
     *
     * @param categoryId The unique identifier of the category
     * @return The category if found, null otherwise
     */
    suspend fun getCategoryById(categoryId: String): Category?
}
