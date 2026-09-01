package com.apptolast.spaindecides.di

import com.apptolast.baselogin.di.loginModules
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

        // Order matters: BaseLogin's modules must be loaded before dataModule, because
        // N8nWebhookClient, ProposalRepositoryImpl and the Supabase client all resolve
        // com.apptolast.baselogin.domain.AuthRepository from them.
        modules(loginModules(spainDecidesLoginConfig()))
        modules(
            dataModule,
            presentationModule
        )
    }
}
