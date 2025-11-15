package com.apptolast.spaindecides.di

import org.koin.core.context.startKoin
import org.koin.dsl.KoinAppDeclaration

/**
 * Initializes Koin dependency injection framework.
 * This function should be called once at application startup.
 */
fun initKoin(appDeclaration: KoinAppDeclaration = {}) {
    startKoin {
        appDeclaration()
        modules(
            dataModule,
            presentationModule
        )
    }
}
