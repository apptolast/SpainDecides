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
    val urlString = url.absoluteString ?: run {
        println("Kotlin DeepLinkHandler: ERROR - URL absoluteString is null")
        return
    }

    println("========================================")
    println("Kotlin DeepLinkHandler: OAuth callback received")
    println("Kotlin DeepLinkHandler: URL = $urlString")

    // Parse the fragment to log OAuth details
    val fragment = urlString.substringAfter("#", "")
    if (fragment.isNotEmpty()) {
        val params = fragment.split("&").associate {
            val parts = it.split("=", limit = 2)
            if (parts.size == 2) parts[0] to parts[1] else parts[0] to ""
        }

        println("Kotlin DeepLinkHandler: OAuth parameters:")
        println(
            "  - access_token: ${
                if (params.containsKey("access_token")) "present (${
                    params["access_token"]?.take(
                        20
                    )
                }...)" else "missing"
            }"
        )
        println("  - refresh_token: ${if (params.containsKey("refresh_token")) "present" else "missing"}")
        println("  - expires_in: ${params["expires_in"]} seconds")
        println("  - token_type: ${params["token_type"]}")
    }

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