package com.apptolast.spaindecides.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.apptolast.baselogin.domain.AuthRepository
import com.apptolast.baselogin.domain.model.UserSession
import com.apptolast.spaindecides.domain.model.AuthUser
import com.apptolast.spaindecides.util.AuthErrorMapper
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import org.jetbrains.compose.resources.StringResource
import com.apptolast.baselogin.domain.model.AuthState as LibraryAuthState

/**
 * Represents the authentication state of the user as the app's screens consume it.
 */
sealed interface AuthState {
    data object Unauthenticated : AuthState
    data class Authenticated(val user: AuthUser) : AuthState
}

/**
 * Session-level ViewModel for screens outside the auth flow.
 *
 * Sign-in and registration belong to BaseLogin's own screens and ViewModels; what stays here is
 * what the rest of the app needs: who is signed in, signing out and deleting the account.
 */
class SessionViewModel(
    private val authRepository: AuthRepository
) : ViewModel() {

    val authState: StateFlow<AuthState>
        field: MutableStateFlow<AuthState> = MutableStateFlow(AuthState.Unauthenticated)

    val isLoading: StateFlow<Boolean>
        field: MutableStateFlow<Boolean> = MutableStateFlow(false)

    val errorMessage: StateFlow<StringResource?>
        field: MutableStateFlow<StringResource?> = MutableStateFlow(null)

    init {
        viewModelScope.launch {
            authRepository.observeAuthState().collect { state ->
                authState.value = when (state) {
                    is LibraryAuthState.Authenticated -> AuthState.Authenticated(state.session.toAuthUser())
                    // Loading and Error are not distinguished by the app's screens: neither one
                    // means "signed in", and the auth flow owns the error reporting.
                    else -> AuthState.Unauthenticated
                }
            }
        }
    }

    /**
     * Signs out the current user.
     *
     * Suspends so callers can navigate only once the session is actually gone, which is what keeps
     * the auth flow from briefly seeing a stale session on iOS.
     */
    suspend fun signOut() {
        // Flip the state first so the observer cannot race the navigation that follows.
        authState.value = AuthState.Unauthenticated
        isLoading.value = true
        authRepository.signOut()
        isLoading.value = false
    }

    /**
     * Permanently deletes the current user account.
     *
     * @return true when the account was deleted.
     */
    suspend fun deleteAccount(): Boolean {
        authState.value = AuthState.Unauthenticated
        isLoading.value = true
        errorMessage.value = null

        val result = authRepository.deleteAccount()

        isLoading.value = false

        return result.fold(
            onSuccess = { true },
            onFailure = { exception ->
                errorMessage.value = AuthErrorMapper.mapError(exception)
                false
            }
        )
    }

    /**
     * Clears the current error message.
     */
    fun clearError() {
        errorMessage.value = null
    }
}

/**
 * Maps BaseLogin's session to the app's domain user.
 *
 * `email` is nullable on [UserSession] because providers such as phone auth have no email; the app
 * only enables email/password and Google, both of which always carry one.
 */
private fun UserSession.toAuthUser(): AuthUser = AuthUser(
    id = userId,
    email = email.orEmpty(),
    displayName = displayName,
    photoUrl = photoUrl,
    emailVerified = isEmailVerified
)
