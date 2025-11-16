package com.apptolast.spaindecides.data.repository

import com.apptolast.spaindecides.data.remote.SupabaseClientConfig
import com.apptolast.spaindecides.data.storage.SecureStorage
import com.apptolast.spaindecides.domain.model.AuthUser
import com.apptolast.spaindecides.domain.repository.AuthRepository
import io.github.jan.supabase.auth.auth
import io.github.jan.supabase.auth.providers.builtin.Email
import io.github.jan.supabase.auth.status.SessionStatus
import io.github.jan.supabase.auth.user.UserInfo
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlin.time.ExperimentalTime

/**
 * Implementation of AuthRepository using Supabase
 */
class AuthRepositoryImpl(
    private val secureStorage: SecureStorage
) : AuthRepository {

    private val auth = SupabaseClientConfig.client.auth

    override suspend fun signUpWithEmail(email: String, password: String): Result<AuthUser> {
        return try {
            auth.signUpWith(Email) {
                this.email = email
                this.password = password
            }

            val user = auth.currentUserOrNull()?.toAuthUser()
            user?.let { Result.success(it) } ?: Result.failure(Exception("User creation failed"))
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

            // Save session tokens
            val session = auth.currentSessionOrNull()
            session?.let {
                secureStorage.saveToken("access_token", it.accessToken)
                it.refreshToken?.let { refreshToken ->
                    secureStorage.saveToken("refresh_token", refreshToken)
                }
            }

            val user = auth.currentUserOrNull()?.toAuthUser()
            user?.let { Result.success(it) } ?: Result.failure(Exception("Sign in failed"))
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun signInWithGoogle(): Result<AuthUser> {
        return try {
            // OAuth flow is handled by ComposeAuth in the UI layer
            // This method is called after the OAuth flow completes
            val user = auth.currentUserOrNull()?.toAuthUser()

            // Save session tokens
            val session = auth.currentSessionOrNull()
            session?.let {
                secureStorage.saveToken("access_token", it.accessToken)
                it.refreshToken?.let { refreshToken ->
                    secureStorage.saveToken("refresh_token", refreshToken)
                }
            }

            user?.let { Result.success(it) } ?: Result.failure(Exception("Google sign in failed"))
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun signOut(): Result<Unit> {
        return try {
            auth.signOut()
            secureStorage.clear()
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

    /**
     * Extension function to convert Supabase UserInfo to AuthUser domain model
     */
    @OptIn(ExperimentalTime::class)
    private fun UserInfo.toAuthUser(): AuthUser {
        return AuthUser(
            id = id,
            email = email ?: "",
            displayName = userMetadata?.get("full_name")?.toString(),
            photoUrl = userMetadata?.get("avatar_url")?.toString(),
            emailVerified = emailConfirmedAt != null
        )
    }
}
