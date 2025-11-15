package com.apptolast.spaindecides

import com.apptolast.spaindecides.data.remote.SupabaseClientConfig
import io.github.jan.supabase.auth.handleDeeplinks
import platform.Foundation.NSURL

/**
 * Handles deep link URLs on iOS for OAuth callbacks.
 *
 * On iOS, when Safari redirects back to the app after OAuth authentication,
 * this function processes the deep link by calling supabase.handleDeeplinks(url).
 * This is REQUIRED on iOS - unlike Android, the OAuth callback is not processed
 * automatically through the Activity lifecycle.
 *
 * Process:
 * 1. iOS receives OAuth callback URL with tokens in the fragment
 * 2. This function calls supabase.handleDeeplinks() to process it
 * 3. Supabase creates a session and updates the auth state
 * 4. AuthViewModel observes the state change
 * 5. LoginScreen detects Authenticated state and navigates
 *
 * @param url The deep link NSURL received from iOS
 */
fun handleDeepLinkUrl(url: NSURL) {
    // CRITICAL: Process the OAuth callback with Supabase
    // This creates the session and updates the auth state
    try {
        println("Kotlin DeepLinkHandler: Processing URL with Supabase...")
        SupabaseClientConfig.client.handleDeeplinks(url = url) {
            println("User Session: $it")
        }
        println("Kotlin DeepLinkHandler: ✅ URL processed successfully")
        println("Kotlin DeepLinkHandler: Auth state will update, navigation should trigger")
    } catch (e: Exception) {
        println("Kotlin DeepLinkHandler: ❌ ERROR processing deep link: ${e.message}")
        e.printStackTrace()
    }

    println("========================================")
}