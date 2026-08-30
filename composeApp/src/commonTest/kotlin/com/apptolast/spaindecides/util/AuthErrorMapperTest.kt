package com.apptolast.spaindecides.util

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
import kotlin.test.Test
import kotlin.test.assertEquals

class AuthErrorMapperTest {

    @Test
    fun invalidCredentialsMessagesMapToInvalidCredentials() {
        listOf(
            "Invalid login credentials",
            "invalid credentials",
            "Invalid password",
            "Email not found",
            "User not found"
        ).forEach { message ->
            assertEquals(
                Res.string.error_invalid_credentials,
                AuthErrorMapper.mapError(Exception(message)),
                "Message '$message' should map to invalid credentials"
            )
        }
    }

    @Test
    fun duplicateRegistrationMessagesMapToUserAlreadyRegistered() {
        listOf(
            "User already registered",
            "This email already exists",
            "duplicate key value violates unique constraint"
        ).forEach { message ->
            assertEquals(
                Res.string.error_user_already_registered,
                AuthErrorMapper.mapError(Exception(message)),
                "Message '$message' should map to user already registered"
            )
        }
    }

    @Test
    fun unconfirmedEmailMessagesMapToEmailNotConfirmed() {
        listOf("Email not confirmed", "Account not verified", "Please verify your email")
            .forEach { message ->
                assertEquals(
                    Res.string.error_email_not_confirmed,
                    AuthErrorMapper.mapError(Exception(message)),
                    "Message '$message' should map to email not confirmed"
                )
            }
    }

    @Test
    fun weakPasswordMessagesMapToWeakPassword() {
        assertEquals(
            Res.string.error_weak_password,
            AuthErrorMapper.mapError(Exception("Password is too weak"))
        )
    }

    @Test
    fun networkMessagesMapToNetworkError() {
        listOf(
            "Network error occurred",
            "Connection refused",
            "Request timeout",
            "Unable to resolve host supabase.co"
        ).forEach { message ->
            assertEquals(
                Res.string.error_network,
                AuthErrorMapper.mapError(Exception(message)),
                "Message '$message' should map to network error"
            )
        }
    }

    @Test
    fun mappingIsCaseInsensitive() {
        assertEquals(
            Res.string.error_invalid_credentials,
            AuthErrorMapper.mapError(Exception("INVALID LOGIN CREDENTIALS"))
        )
    }

    @Test
    fun unrecognizedMessageMapsToUnknownError() {
        assertEquals(
            Res.string.error_unknown,
            AuthErrorMapper.mapError(Exception("Something completely unexpected"))
        )
    }

    @Test
    fun nullMessageMapsToUnknownError() {
        assertEquals(Res.string.error_unknown, AuthErrorMapper.mapError(Exception()))
    }

    @Test
    fun validationErrorsMapToTheirResources() {
        val expected = mapOf(
            ValidationError.EMAIL_REQUIRED to Res.string.error_email_required,
            ValidationError.EMAIL_INVALID to Res.string.error_email_invalid,
            ValidationError.PASSWORD_REQUIRED to Res.string.error_password_required,
            ValidationError.PASSWORD_SHORT to Res.string.error_password_short,
            ValidationError.NAME_REQUIRED to Res.string.error_name_required,
            ValidationError.EULA_REQUIRED to Res.string.error_eula_required
        )
        expected.forEach { (validation, resource) ->
            assertEquals(
                resource,
                AuthErrorMapper.mapValidationError(validation),
                "Validation $validation should map to its resource"
            )
        }
    }
}
