package com.apptolast.spaindecides.di

import com.apptolast.spaindecides.data.remote.N8nWebhookClient
import com.apptolast.spaindecides.data.remote.ReportApiService
import com.apptolast.spaindecides.data.repository.CategoryRepositoryImpl
import com.apptolast.spaindecides.data.repository.ProposalRepositoryImpl
import com.apptolast.spaindecides.data.repository.ReportRepositoryImpl
import com.apptolast.spaindecides.domain.repository.CategoryRepository
import com.apptolast.spaindecides.domain.repository.ProposalRepository
import com.apptolast.spaindecides.domain.repository.ReportRepository
import io.ktor.client.HttpClient
import io.ktor.client.plugins.HttpTimeout
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
    // HTTP Client for external APIs (EmailJS, Firebase Functions, n8n)
    single {
        HttpClient {
            install(ContentNegotiation) {
                json(Json {
                    prettyPrint = true
                    isLenient = true
                    ignoreUnknownKeys = true
                })
            }

            // Timeout configuration for n8n webhook calls
            // n8n workflow involves: embedding generation + vector search + AI model
            install(HttpTimeout) {
                requestTimeoutMillis = 90_000    // 90 seconds for full request
                connectTimeoutMillis = 15_000   // 15 seconds to establish connection
                socketTimeoutMillis = 90_000    // 90 seconds for socket operations
            }
        }
    }

    // API Services
    singleOf(::ReportApiService)

    // Webhook clients
    singleOf(::N8nWebhookClient)

    // Repository implementations
    singleOf(::CategoryRepositoryImpl) bind CategoryRepository::class
    singleOf(::ProposalRepositoryImpl) bind ProposalRepository::class
    singleOf(::ReportRepositoryImpl) bind ReportRepository::class
}
