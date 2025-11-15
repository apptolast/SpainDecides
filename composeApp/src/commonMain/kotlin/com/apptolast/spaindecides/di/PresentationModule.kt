package com.apptolast.spaindecides.di

import com.apptolast.spaindecides.presentation.viewmodel.AuthViewModel
import com.apptolast.spaindecides.presentation.viewmodel.CategoryViewModel
import com.apptolast.spaindecides.presentation.viewmodel.ProposalViewModel
import com.apptolast.spaindecides.presentation.viewmodel.ReportViewModel
import org.koin.core.module.dsl.viewModel
import org.koin.core.module.dsl.viewModelOf
import org.koin.dsl.module

/**
 * Koin module for presentation layer dependencies.
 * Provides ViewModels with lifecycle management.
 */
val presentationModule = module {
    // ViewModels
    viewModelOf(::AuthViewModel)
    viewModelOf(::CategoryViewModel)
    viewModelOf(::ReportViewModel)

    // ProposalViewModel with categoryId parameter and SavedStateHandle for state persistence
    // Each category gets its own ViewModel instance to ensure clean Realtime channel lifecycle
    viewModel { (categoryId: String) ->
        ProposalViewModel(
            categoryId = categoryId,
            proposalRepository = get(),
            savedStateHandle = get()
        )
    }
}
