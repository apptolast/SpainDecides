package com.apptolast.spaindecides.di

import com.apptolast.spaindecides.data.repository.CategoryRepositoryImpl
import com.apptolast.spaindecides.data.repository.ProposalRepositoryImpl
import com.apptolast.spaindecides.domain.repository.CategoryRepository
import com.apptolast.spaindecides.domain.repository.ProposalRepository
import org.koin.core.module.dsl.singleOf
import org.koin.dsl.bind
import org.koin.dsl.module

/**
 * Koin module for data layer dependencies.
 * Provides repository implementations.
 */
val dataModule = module {
    // Repository implementations
    singleOf(::CategoryRepositoryImpl) bind CategoryRepository::class
    singleOf(::ProposalRepositoryImpl) bind ProposalRepository::class
}
