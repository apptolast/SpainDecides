package com.apptolast.spaindecides.data.remote

import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.auth.Auth
import io.github.jan.supabase.compose.auth.ComposeAuth
import io.github.jan.supabase.compose.auth.googleNativeLogin
import io.github.jan.supabase.createSupabaseClient
import io.github.jan.supabase.postgrest.Postgrest
import io.github.jan.supabase.realtime.Realtime

/**
 * Supabase client configuration
 *
 * Credentials are loaded from local.properties via Environment (BuildKonfig or BuildConfig)
 * See local.properties.template for the required configuration
 */
object SupabaseClientConfig {

    val client: SupabaseClient by lazy {
        createSupabaseClient(
            supabaseUrl = Environment.SUPABASE_URL,
            supabaseKey = Environment.SUPABASE_ANON_KEY
        ) {
            install(Auth) {
                // Configure deep linking for OAuth callbacks
                scheme = "com.apptolast.spaindecides"
                host = "auth-callback"
            }

            install(ComposeAuth) {
                // Configure native Google login for Android
                googleNativeLogin(serverClientId = Environment.GOOGLE_WEB_CLIENT_ID)
            }

            install(Postgrest)

            install(Realtime)
        }
    }
}
