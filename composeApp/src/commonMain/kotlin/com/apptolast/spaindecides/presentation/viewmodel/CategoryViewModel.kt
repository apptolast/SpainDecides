package com.apptolast.spaindecides.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.apptolast.spaindecides.data.model.Category
import com.apptolast.spaindecides.domain.repository.CategoryRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

/**
 * ViewModel for the Categories screen.
 * Manages the list of available categories for citizen participation.
 */
class CategoryViewModel(
    private val categoryRepository: CategoryRepository
) : ViewModel() {

    private val _categories = MutableStateFlow<List<Category>>(emptyList())
    val categories: StateFlow<List<Category>> = _categories.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error.asStateFlow()

    init {
        loadCategories()
    }

    /**
     * Loads all categories from the repository
     */
    fun loadCategories() {
        viewModelScope.launch {
            _isLoading.value = true
            _error.value = null

            try {
                categoryRepository.getCategories().collect { categoriesList ->
                    _categories.value = categoriesList
                    _isLoading.value = false
                }
            } catch (e: Exception) {
                _error.value = e.message ?: "Error desconocido"
                _isLoading.value = false
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
        _error.value = null
    }
}
