package com.apptolast.spaindecides.di

import com.apptolast.spaindecides.data.storage.SecureStorage
import org.koin.core.module.Module
import org.koin.dsl.module

/**
 * Android-specific auth module
 * Provides SecureStorage with Android Context
 */
actual fun createAuthModule(): Module = module {
    includes(authModuleCommon())

    // SecureStorage for Android (requires Context)
    single { SecureStorage(get()) }
}
