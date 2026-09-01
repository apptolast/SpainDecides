package com.apptolast.spaindecides.di

import com.apptolast.baselogin.config.GoogleSignInConfig
import com.apptolast.baselogin.di.LoginLibraryConfig
import com.apptolast.baselogin.di.PasswordPolicyConfig
import com.apptolast.spaindecides.data.remote.Environment
import com.apptolast.spaindecides.util.isIOS

/**
 * Minimum password length accepted by the register and reset password screens.
 * Kept at the value the previous hand-written register screen enforced.
 */
private const val PASSWORD_MIN_LENGTH = 6

/**
 * BaseLogin configuration for España Decide.
 *
 * Only the providers the Firebase project actually has enabled are turned on. Everything else stays
 * off, and BaseLogin hides the buttons for disabled providers.
 *
 * Phone OTP and Magic Link are deliberately disabled: both need extra Firebase console setup
 * (SMS quota / email link templates) and, on iOS, Swift handlers that this app does not install.
 */
fun spainDecidesLoginConfig(): LoginLibraryConfig {
    val iosClientId = Environment.GOOGLE_IOS_CLIENT_ID.trim()
    val googleSignInConfig = when {
        // BaseLogin falls back to the web client on iOS when iosClientId is null, but Google
        // rejects custom URL schemes for web OAuth clients.
        isIOS() && iosClientId.isBlank() -> null
        else -> GoogleSignInConfig(
            webClientId = Environment.GOOGLE_WEB_CLIENT_ID,
            iosClientId = iosClientId.takeIf { it.isNotBlank() }
        )
    }

    return LoginLibraryConfig(
        googleSignInConfig = googleSignInConfig,
        phoneEnabled = false,
        passwordPolicy = PasswordPolicyConfig(minLength = PASSWORD_MIN_LENGTH)
    )
}
