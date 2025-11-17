package com.apptolast.spaindecides.di

import com.apptolast.spaindecides.data.storage.SecureStorage
import org.koin.core.module.Module
import org.koin.dsl.module

/**
 * iOS-specific auth module
 * Provides SecureStorage for iOS (no Context needed)
 */
actual fun createAuthModule(): Module = module {
    includes(authModuleCommon())

    // SecureStorage for iOS (no parameters needed)
    single { SecureStorage() }
}
