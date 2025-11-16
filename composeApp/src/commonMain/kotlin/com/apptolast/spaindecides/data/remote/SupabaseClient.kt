package com.apptolast.spaindecides.data.remote

import com.apptolast.spaindecides.BuildKonfig
import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.auth.Auth
import io.github.jan.supabase.compose.auth.ComposeAuth
import io.github.jan.supabase.compose.auth.googleNativeLogin
import io.github.jan.supabase.createSupabaseClient
import io.github.jan.supabase.postgrest.Postgrest

/**
 * Supabase client configuration
 *
 * Credentials are loaded from local.properties via BuildKonfig for security
 * See local.properties.template for the required configuration
 */
object SupabaseClientConfig {

    val client: SupabaseClient by lazy {
        createSupabaseClient(
            supabaseUrl = BuildKonfig.SUPABASE_URL,
            supabaseKey = BuildKonfig.SUPABASE_ANON_KEY
        ) {
            install(Auth) {
                // Configure deep linking for OAuth callbacks
                scheme = "com.apptolast.spaindecides"
                host = "auth-callback"
            }

            install(ComposeAuth) {
                // Configure native Google login for Android
                googleNativeLogin(serverClientId = BuildKonfig.GOOGLE_WEB_CLIENT_ID)
            }

            install(Postgrest)
        }
    }
}
