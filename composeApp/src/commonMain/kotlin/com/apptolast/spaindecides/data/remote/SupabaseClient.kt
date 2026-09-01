package com.apptolast.spaindecides.data.remote

import com.apptolast.baselogin.domain.AuthRepository
import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.createSupabaseClient
import io.github.jan.supabase.postgrest.Postgrest
import io.github.jan.supabase.realtime.Realtime
import org.koin.mp.KoinPlatform

/**
 * Supabase client configuration.
 *
 * Credentials are loaded from local.properties via Environment (BuildKonfig).
 * See local.properties.template for the required configuration.
 *
 * ## Identity
 *
 * Authentication is owned by Firebase through BaseLogin, so the `Auth` plugin from `auth-kt` is not
 * installed — supabase-kt forbids combining it with a custom [accessToken]. Instead every request
 * carries the Firebase ID token, which the Supabase project accepts through its **Third-Party Auth**
 * Firebase integration. Inside a policy the token surfaces as `auth.jwt()`, and its `sub` claim is
 * the Firebase UID rather than a Supabase `auth.users` UUID.
 *
 * See docs/FIREBASE_SUPABASE_AUTH.md for the project-side setup this depends on.
 */
object SupabaseClientConfig {

    val client: SupabaseClient by lazy {
        createSupabaseClient(
            supabaseUrl = Environment.SUPABASE_URL,
            supabaseKey = Environment.SUPABASE_ANON_KEY
        ) {
            // Resolved from Koin on each call rather than injected: the client is an object, and
            // this lambda only ever runs after startKoin has loaded BaseLogin's modules.
            // BaseLogin returns the cached Firebase token and refreshes it when it is about to
            // expire, which is what this callback is documented to require.
            accessToken = {
                KoinPlatform.getKoin().get<AuthRepository>().getIdToken()
            }

            install(Postgrest)

            install(Realtime)
        }
    }
}
