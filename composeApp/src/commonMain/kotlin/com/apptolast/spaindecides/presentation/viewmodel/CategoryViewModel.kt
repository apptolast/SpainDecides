package com.apptolast.spaindecides.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.apptolast.spaindecides.data.model.Category
import com.apptolast.spaindecides.domain.repository.CategoryRepository
import com.apptolast.spaindecides.domain.repository.ProposalRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

/**
 * ViewModel for the Categories screen.
 * Manages the list of available categories for citizen participation.
 */
class CategoryViewModel(
    private val categoryRepository: CategoryRepository,
    private val proposalRepository: ProposalRepository
) : ViewModel() {

    val categories: StateFlow<List<Category>>
        field: MutableStateFlow<List<Category>> = MutableStateFlow(emptyList())

    val proposalCounts: StateFlow<Map<String, Int>>
        field: MutableStateFlow<Map<String, Int>> = MutableStateFlow(emptyMap())

    val isLoading: StateFlow<Boolean>
        field: MutableStateFlow<Boolean> = MutableStateFlow(false)

    val error: StateFlow<String?>
        field: MutableStateFlow<String?> = MutableStateFlow(null)

    init {
        loadCategories()
        loadProposalCounts()
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

    /**
     * Loads proposal counts for all categories
     */
    fun loadProposalCounts() {
        viewModelScope.launch {
            try {
                proposalCounts.value = proposalRepository.getProposalCountsByCategory()
            } catch (e: Exception) {
                // Silently fail - counts are not critical
                println("Error loading proposal counts: ${e.message}")
            }
        }
    }
}
