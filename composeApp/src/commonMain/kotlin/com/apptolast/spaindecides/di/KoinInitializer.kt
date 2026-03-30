package com.apptolast.spaindecides.di

import org.koin.core.context.startKoin
import org.koin.dsl.KoinAppDeclaration

/**
 * Initializes Koin dependency injection framework.
 * This function should be called once at application startup.
 */
fun initKoin(appDeclaration: KoinAppDeclaration? = null) {
    startKoin {
        // Platform-specific configuration (optional)
        appDeclaration?.invoke(this)

        // Order matters: authModule must be loaded before dataModule
        // because N8nWebhookClient depends on AuthRepository
        modules(
            createAuthModule(),
            dataModule,
            presentationModule
        )
    }
}
