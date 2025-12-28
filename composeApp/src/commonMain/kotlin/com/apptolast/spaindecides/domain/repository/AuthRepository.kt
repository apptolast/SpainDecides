package com.apptolast.spaindecides.domain.repository

import com.apptolast.spaindecides.domain.model.AuthUser
import kotlinx.coroutines.flow.Flow

/**
 * Repository interface for authentication operations
 */
interface AuthRepository {

    /**
     * Sign up a new user with email and password
     * Note: When email confirmation is enabled in Supabase, this will return success
     * after creating the account, but the user must verify their email before signing in.
     *
     * @param email User's email address
     * @param password User's password
     * @param fullName User's full name (stored in user metadata)
     */
    suspend fun signUpWithEmail(email: String, password: String, fullName: String): Result<Unit>

    /**
     * Sign in an existing user with email and password
     */
    suspend fun signInWithEmail(email: String, password: String): Result<AuthUser>

    /**
     * Sign in with Google OAuth (for iOS - opens Safari)
     * This initiates the OAuth flow and redirects to the callback URL.
     * The session will be created when the deep link is handled.
     */
    suspend fun signInWithGoogle(): Result<Unit>

    /**
     * Sign out the current user
     */
    suspend fun signOut(): Result<Unit>

    /**
     * Get the currently authenticated user
     */
    suspend fun getCurrentUser(): AuthUser?

    /**
     * Observe authentication state changes
     */
    fun observeAuthState(): Flow<AuthUser?>

    /**
     * Send password reset email
     */
    suspend fun sendPasswordResetEmail(email: String): Result<Unit>

    /**
     * Delete the current user account permanently
     * This will delete the user from Supabase Auth and all associated data
     */
    suspend fun deleteAccount(): Result<Unit>

    /**
     * Get the current session's access token (JWT).
     * Used for authenticating requests to external services (n8n webhooks, etc.)
     *
     * @return The JWT access token if user is authenticated, null otherwise
     */
    suspend fun getAccessToken(): String?
}
