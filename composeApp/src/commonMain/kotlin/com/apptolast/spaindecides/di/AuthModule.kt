package com.apptolast.spaindecides.di

import com.apptolast.spaindecides.data.repository.AuthRepositoryImpl
import com.apptolast.spaindecides.domain.repository.AuthRepository
import org.koin.dsl.module

/**
 * Koin module for authentication dependencies.
 *
 * Provides:
 * - AuthRepository implementation (uses Supabase for session/token management)
 */
expect fun createAuthModule(): org.koin.core.module.Module

/**
 * Common auth module that can be extended by platform-specific modules.
 * Note: Supabase handles token persistence automatically, no SecureStorage needed.
 */
internal fun authModuleCommon() = module {
    // AuthRepository implementation (no dependencies - Supabase manages sessions internally)
    single<AuthRepository> { AuthRepositoryImpl() }
}
