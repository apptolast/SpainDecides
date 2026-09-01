package com.apptolast.spaindecides.di

import com.apptolast.baselogin.config.GoogleSignInConfig
import com.apptolast.baselogin.di.LoginLibraryConfig
import com.apptolast.baselogin.di.PasswordPolicyConfig
import com.apptolast.spaindecides.data.remote.Environment

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
fun spainDecidesLoginConfig(): LoginLibraryConfig = LoginLibraryConfig(
    googleSignInConfig = GoogleSignInConfig(
        webClientId = Environment.GOOGLE_WEB_CLIENT_ID,
        iosClientId = Environment.GOOGLE_IOS_CLIENT_ID.takeIf { it.isNotBlank() }
    ),
    phoneEnabled = false,
    passwordPolicy = PasswordPolicyConfig(minLength = PASSWORD_MIN_LENGTH)
)
