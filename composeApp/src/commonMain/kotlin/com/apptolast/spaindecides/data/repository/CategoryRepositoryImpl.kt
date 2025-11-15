package com.apptolast.spaindecides.data.repository

import com.apptolast.spaindecides.data.MockData
import com.apptolast.spaindecides.data.model.Category
import com.apptolast.spaindecides.domain.repository.CategoryRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf

/**
 * Implementation of CategoryRepository using mock data.
 * This will be replaced with a real API implementation in the future.
 */
class CategoryRepositoryImpl : CategoryRepository {

    override fun getCategories(): Flow<List<Category>> {
        return flowOf(MockData.categories)
    }

    override suspend fun getCategoryById(categoryId: String): Category? {
        return MockData.categories.firstOrNull { it.id == categoryId }
    }
}
