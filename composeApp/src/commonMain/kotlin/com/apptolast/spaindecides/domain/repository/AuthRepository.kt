package com.apptolast.spaindecides.domain.repository

import com.apptolast.spaindecides.domain.model.AuthUser
import kotlinx.coroutines.flow.Flow

/**
 * Repository interface for authentication operations
 */
interface AuthRepository {

    /**
     * Sign up a new user with email and password
     */
    suspend fun signUpWithEmail(email: String, password: String): Result<AuthUser>

    /**
     * Sign in an existing user with email and password
     */
    suspend fun signInWithEmail(email: String, password: String): Result<AuthUser>

    /**
     * Sign in with Google OAuth
     */
    suspend fun signInWithGoogle(): Result<AuthUser>

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
}
