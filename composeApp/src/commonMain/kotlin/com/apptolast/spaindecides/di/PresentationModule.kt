package com.apptolast.spaindecides.di

import com.apptolast.spaindecides.presentation.viewmodel.AuthViewModel
import com.apptolast.spaindecides.presentation.viewmodel.CategoryViewModel
import com.apptolast.spaindecides.presentation.viewmodel.ProposalViewModel
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
    viewModelOf(::ProposalViewModel)
}
