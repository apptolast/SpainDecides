package com.apptolast.spaindecides.util

import org.jetbrains.compose.resources.StringResource
import spaindecides.composeapp.generated.resources.Res
import spaindecides.composeapp.generated.resources.error_email_invalid
import spaindecides.composeapp.generated.resources.error_email_not_confirmed
import spaindecides.composeapp.generated.resources.error_email_required
import spaindecides.composeapp.generated.resources.error_eula_required
import spaindecides.composeapp.generated.resources.error_invalid_credentials
import spaindecides.composeapp.generated.resources.error_name_required
import spaindecides.composeapp.generated.resources.error_network
import spaindecides.composeapp.generated.resources.error_password_required
import spaindecides.composeapp.generated.resources.error_password_short
import spaindecides.composeapp.generated.resources.error_unknown
import spaindecides.composeapp.generated.resources.error_user_already_registered
import spaindecides.composeapp.generated.resources.error_weak_password

/**
 * Maps authentication exceptions to user-friendly localized error messages.
 *
 * This mapper converts raw Supabase error messages into appropriate string resources
 * that can be displayed to users in their preferred language.
 */
object AuthErrorMapper {

    /**
     * Maps a Throwable from authentication operations to a StringResource.
     *
     * @param error The exception thrown during authentication
     * @return A StringResource containing the appropriate error message
     */
    fun mapError(error: Throwable): StringResource {
        val errorMessage = error.message?.lowercase() ?: ""

        return when {
            // Invalid credentials - wrong email or password
            errorMessage.contains("invalid login credentials") ||
                    errorMessage.contains("invalid credentials") ||
                    errorMessage.contains("invalid password") ||
                    errorMessage.contains("email not found") ||
                    errorMessage.contains("user not found") -> {
                Res.string.error_invalid_credentials
            }

            // User already exists - duplicate registration
            errorMessage.contains("user already registered") ||
                    errorMessage.contains("already registered") ||
                    errorMessage.contains("email already exists") ||
                    errorMessage.contains("duplicate") -> {
                Res.string.error_user_already_registered
            }

            // Email not confirmed - needs verification
            errorMessage.contains("email not confirmed") ||
                    errorMessage.contains("not verified") ||
                    errorMessage.contains("verify your email") ||
                    errorMessage.contains("email verification") -> {
                Res.string.error_email_not_confirmed
            }

            // Weak password
            errorMessage.contains("weak password") ||
                    errorMessage.contains("password is too weak") ||
                    errorMessage.contains("password strength") -> {
                Res.string.error_weak_password
            }

            // Network errors
            errorMessage.contains("network") ||
                    errorMessage.contains("connection") ||
                    errorMessage.contains("timeout") ||
                    errorMessage.contains("unable to resolve host") ||
                    errorMessage.contains("no internet") -> {
                Res.string.error_network
            }

            // Default unknown error
            else -> Res.string.error_unknown
        }
    }

    /**
     * Maps a validation error to a StringResource.
     * Used for client-side validation before making API calls.
     *
     * @param validationType The type of validation that failed
     * @return A StringResource containing the validation error message
     */
    fun mapValidationError(validationType: ValidationError): StringResource {
        return when (validationType) {
            ValidationError.EMAIL_REQUIRED -> Res.string.error_email_required
            ValidationError.EMAIL_INVALID -> Res.string.error_email_invalid
            ValidationError.PASSWORD_REQUIRED -> Res.string.error_password_required
            ValidationError.PASSWORD_SHORT -> Res.string.error_password_short
            ValidationError.NAME_REQUIRED -> Res.string.error_name_required
            ValidationError.EULA_REQUIRED -> Res.string.error_eula_required
        }
    }
}

/**
 * Enum representing client-side validation errors.
 */
enum class ValidationError {
    EMAIL_REQUIRED,
    EMAIL_INVALID,
    PASSWORD_REQUIRED,
    PASSWORD_SHORT,
    NAME_REQUIRED,
    EULA_REQUIRED
}
