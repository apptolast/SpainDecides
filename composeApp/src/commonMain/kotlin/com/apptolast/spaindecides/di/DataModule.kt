package com.apptolast.spaindecides.di

import com.apptolast.spaindecides.data.remote.ReportApiService
import com.apptolast.spaindecides.data.remote.notification.NotificationService
import com.apptolast.spaindecides.data.repository.CategoryRepositoryImpl
import com.apptolast.spaindecides.data.repository.ProposalRepositoryImpl
import com.apptolast.spaindecides.data.repository.ReportRepositoryImpl
import com.apptolast.spaindecides.domain.repository.CategoryRepository
import com.apptolast.spaindecides.domain.repository.N8nWebhookClient
import com.apptolast.spaindecides.domain.repository.ProposalRepository
import com.apptolast.spaindecides.domain.repository.ReportRepository
import io.ktor.client.HttpClient
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.serialization.kotlinx.json.json
import kotlinx.serialization.json.Json
import org.koin.core.module.dsl.singleOf
import org.koin.dsl.bind
import org.koin.dsl.module

/**
 * Koin module for data layer dependencies.
 * Provides repository implementations.
 */
val dataModule = module {
    // HTTP Client for external APIs (EmailJS, Firebase Functions)
    single {
        HttpClient {
            install(ContentNegotiation) {
                json(Json {
                    prettyPrint = true
                    isLenient = true
                    ignoreUnknownKeys = true
                })
            }
        }
    }

    // API Services
    singleOf(::ReportApiService)
    singleOf(::NotificationService)

    // Webhook clients
    singleOf(::N8nWebhookClient)

    // Repository implementations
    singleOf(::CategoryRepositoryImpl) bind CategoryRepository::class
    singleOf(::ProposalRepositoryImpl) bind ProposalRepository::class
    singleOf(::ReportRepositoryImpl) bind ReportRepository::class
}
