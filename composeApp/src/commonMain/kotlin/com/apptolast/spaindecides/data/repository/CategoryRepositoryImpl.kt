package com.apptolast.spaindecides.data.repository

import com.apptolast.spaindecides.data.model.Category
import com.apptolast.spaindecides.data.remote.SupabaseClientConfig
import com.apptolast.spaindecides.domain.repository.CategoryRepository
import io.github.jan.supabase.postgrest.from
import io.github.jan.supabase.postgrest.query.Order
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow

/**
 * Implementation of CategoryRepository using Supabase as the backend.
 *
 * Fetches categories from the public.categories table in Supabase.
 * Categories are stored with key-based identifiers for internationalization.
 */
class CategoryRepositoryImpl : CategoryRepository {

    private val supabase = SupabaseClientConfig.client

    override fun getCategories(): Flow<List<Category>> = flow {
        try {
            val categories = supabase
                .from("categories")
                .select() {
                    order("sort_order", Order.ASCENDING)
                }
                .decodeList<Category>()
            emit(categories)
        } catch (e: Exception) {
            // Log error and emit empty list
            println("Error fetching categories: ${e.message}")
            emit(emptyList())
        }
    }

    override suspend fun getCategoryById(categoryId: String): Category? {
        return try {
            supabase
                .from("categories")
                .select {
                    filter {
                        eq("id", categoryId)
                    }
                }
                .decodeSingleOrNull<Category>()
        } catch (e: Exception) {
            println("Error fetching category by ID: ${e.message}")
            null
        }
    }
}
