package com.apptolast.spaindecides.di

import com.apptolast.spaindecides.data.repository.AuthRepositoryImpl
import com.apptolast.spaindecides.domain.repository.AuthRepository
import org.koin.dsl.module

/**
 * Koin module for authentication dependencies.
 *
 * Provides:
 * - SecureStorage (platform-specific)
 * - AuthRepository implementation
 */
expect fun createAuthModule(): org.koin.core.module.Module

/**
 * Common auth module that can be extended by platform-specific modules
 */
internal fun authModuleCommon() = module {
    // AuthRepository implementation
    single<AuthRepository> { AuthRepositoryImpl(get()) }
}
