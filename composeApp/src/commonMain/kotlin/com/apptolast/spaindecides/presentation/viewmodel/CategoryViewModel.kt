package com.apptolast.spaindecides.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.apptolast.spaindecides.data.model.Category
import com.apptolast.spaindecides.domain.repository.CategoryRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

/**
 * ViewModel for the Categories screen.
 * Manages the list of available categories for citizen participation.
 */
class CategoryViewModel(
    private val categoryRepository: CategoryRepository
) : ViewModel() {

    val categories: StateFlow<List<Category>>
        field: MutableStateFlow<List<Category>> = MutableStateFlow(emptyList())

    val isLoading: StateFlow<Boolean>
        field: MutableStateFlow<Boolean> = MutableStateFlow(false)

    val error: StateFlow<String?>
        field: MutableStateFlow<String?> = MutableStateFlow(null)

    init {
        loadCategories()
    }

    /**
     * Loads all categories from the repository
     */
    fun loadCategories() {
        viewModelScope.launch {
            isLoading.value = true
            error.value = null

            try {
                categoryRepository.getCategories().collect { categoriesList ->
                    categories.value = categoriesList
                    isLoading.value = false
                }
            } catch (e: Exception) {
                error.value = e.message ?: "Error desconocido"
                isLoading.value = false
            }
        }
    }

    /**
     * Gets a category by ID (for navigation purposes)
     */
    suspend fun getCategoryById(categoryId: String): Category? {
        return categoryRepository.getCategoryById(categoryId)
    }

    /**
     * Clears any error message
     */
    fun clearError() {
        error.value = null
    }
}
