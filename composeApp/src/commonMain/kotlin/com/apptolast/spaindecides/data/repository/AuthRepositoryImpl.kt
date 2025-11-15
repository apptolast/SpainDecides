package com.apptolast.spaindecides.data.repository

import com.apptolast.spaindecides.data.remote.SupabaseClientConfig
import com.apptolast.spaindecides.data.remote.SupabaseClientConfig.client
import com.apptolast.spaindecides.domain.model.AuthUser
import com.apptolast.spaindecides.domain.repository.AuthRepository
import io.github.jan.supabase.auth.SignOutScope
import io.github.jan.supabase.auth.auth
import io.github.jan.supabase.auth.providers.Google
import io.github.jan.supabase.auth.providers.builtin.Email
import io.github.jan.supabase.auth.status.SessionStatus
import io.github.jan.supabase.auth.user.UserInfo
import io.github.jan.supabase.postgrest.postgrest
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.contentOrNull
import kotlinx.serialization.json.jsonPrimitive
import kotlin.time.ExperimentalTime

/**
 * Implementation of AuthRepository using Supabase.
 * Session/token management is handled automatically by Supabase Auth.
 */
class AuthRepositoryImpl : AuthRepository {

    private val auth = SupabaseClientConfig.client.auth

    override suspend fun signUpWithEmail(
        email: String,
        password: String,
        fullName: String
    ): Result<Unit> {
        return try {
            // Create the user account in Supabase
            // When email confirmation is enabled (default), Supabase will:
            // 1. Create the user account in auth.users
            // 2. Send a confirmation email
            // 3. NOT create a session until the user confirms their email
            auth.signUpWith(Email) {
                this.email = email
                this.password = password
                // Store full name in user metadata
                // This will be accessible via user.userMetadata["full_name"]
                data = JsonObject(
                    buildMap {
                        put("full_name", JsonPrimitive(fullName))
                    }
                )
            }

            // Return success immediately - user will receive confirmation email
            // Note: currentUserOrNull() will be null until email is confirmed
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun signInWithEmail(email: String, password: String): Result<AuthUser> {
        return try {
            auth.signInWith(Email) {
                this.email = email
                this.password = password
            }

            val user = auth.currentUserOrNull()?.toAuthUser()
            user?.let { Result.success(it) } ?: Result.failure(Exception("Sign in failed"))
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun signInWithGoogle(): Result<Unit> {
        return try {
            // This opens Safari/browser for OAuth flow on iOS
            // The redirect URL is automatically constructed from Auth config:
            // scheme://host (e.g., com.apptolast.spaindecides://auth-callback)
            auth.signInWith(Google)
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun signOut(): Result<Unit> {
        return try {
            // Use GLOBAL scope to clear ALL sessions including Supabase's internal storage
            // This prevents automatic session restoration after logout
            auth.signOut(scope = SignOutScope.GLOBAL)
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun getCurrentUser(): AuthUser? {
        return auth.currentUserOrNull()?.toAuthUser()
    }

    override fun observeAuthState(): Flow<AuthUser?> {
        return auth.sessionStatus.map { status ->
            when (status) {
                is SessionStatus.Authenticated -> status.session.user?.toAuthUser()
                else -> null
            }
        }
    }

    override suspend fun sendPasswordResetEmail(email: String): Result<Unit> {
        return try {
            auth.resetPasswordForEmail(email)
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun deleteAccount(): Result<Unit> {
        return try {
            // Delete the current user from Supabase Auth
            // Calls the 'delete_own_account' SQL function created in Supabase.
            //
            // SQL function definition (executed in Supabase SQL Editor):
            //
            //   CREATE OR REPLACE FUNCTION delete_own_account()
            //   RETURNS void
            //   LANGUAGE sql
            //   SECURITY DEFINER
            //   SET search_path = public
            //   AS $$
            //     DELETE FROM auth.users WHERE id = auth.uid();
            //   $$;
            //
            // The function uses 'SECURITY DEFINER', which means it runs with
            // admin privileges even when called by a regular user.
            // auth.uid() returns the currently authenticated user's ID,
            // ensuring users can only delete their own account.
            //
            // IMPORTANT: This RPC call MUST happen BEFORE signOut()
            // because auth.uid() needs an active session to return the user ID.
            client.postgrest.rpc("delete_own_account")

            // Sign out AFTER deletion to clean up the session
            // Use GLOBAL scope to clear ALL sessions including Supabase's internal storage
            auth.signOut(scope = SignOutScope.GLOBAL)

            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun getAccessToken(): String? {
        return auth.currentSessionOrNull()?.accessToken
    }

    /**
     * Extension function to convert Supabase UserInfo to AuthUser domain model
     */
    @OptIn(ExperimentalTime::class)
    private fun UserInfo.toAuthUser(): AuthUser {
        return AuthUser(
            id = id,
            email = email ?: "",
            displayName = userMetadata?.get("full_name")?.jsonPrimitive?.contentOrNull,
            photoUrl = userMetadata?.get("avatar_url")?.jsonPrimitive?.contentOrNull,
            emailVerified = emailConfirmedAt != null
        )
    }
}
