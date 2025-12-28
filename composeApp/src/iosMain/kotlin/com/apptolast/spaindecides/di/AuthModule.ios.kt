package com.apptolast.spaindecides.di

import org.koin.core.module.Module
import org.koin.dsl.module

/**
 * iOS-specific auth module.
 * Note: Supabase handles token persistence automatically.
 */
actual fun createAuthModule(): Module = module {
    includes(authModuleCommon())
}
