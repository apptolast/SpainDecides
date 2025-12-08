package com.apptolast.spaindecides.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.apptolast.spaindecides.domain.model.AuthUser
import com.apptolast.spaindecides.domain.repository.AuthRepository
import com.apptolast.spaindecides.util.AuthErrorMapper
import com.apptolast.spaindecides.util.ValidationError
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import org.jetbrains.compose.resources.StringResource

/**
 * Represents the authentication state of the user
 */
sealed interface AuthState {
    data object Unauthenticated : AuthState
    data class Authenticated(val user: AuthUser) : AuthState
}

/**
 * ViewModel for authentication screens (Login and Register).
 * Handles authentication with Supabase including email/password and Google OAuth.
 */
class AuthViewModel(
    private val authRepository: AuthRepository
) : ViewModel() {

    val email: StateFlow<String>
        field: MutableStateFlow<String> = MutableStateFlow("")

    val password: StateFlow<String>
        field: MutableStateFlow<String> = MutableStateFlow("")

    val name: StateFlow<String> // For registration
        field: MutableStateFlow<String> = MutableStateFlow("")

    val isPasswordVisible: StateFlow<Boolean>
        field: MutableStateFlow<Boolean> = MutableStateFlow(false)

    val isLoading: StateFlow<Boolean>
        field: MutableStateFlow<Boolean> = MutableStateFlow(false)

    val errorMessage: StateFlow<StringResource?>
        field: MutableStateFlow<StringResource?> = MutableStateFlow(null)

    val successMessage: StateFlow<StringResource?>
        field: MutableStateFlow<StringResource?> = MutableStateFlow(null)

    val authState: StateFlow<AuthState>
        field: MutableStateFlow<AuthState> = MutableStateFlow(AuthState.Unauthenticated)

    init {
        // Observe authentication state changes
        viewModelScope.launch {
            authRepository.observeAuthState().collect { user ->
                authState.value = if (user != null) {
                    AuthState.Authenticated(user)
                } else {
                    AuthState.Unauthenticated
                }
            }
        }
    }

    /**
     * Updates the email field
     */
    fun updateEmail(newEmail: String) {
        email.value = newEmail
        clearMessages() // Clear messages when user types
    }

    /**
     * Updates the password field
     */
    fun updatePassword(newPassword: String) {
        password.value = newPassword
        clearMessages() // Clear messages when user types
    }

    /**
     * Updates the name field (for registration)
     */
    fun updateName(newName: String) {
        name.value = newName
        clearMessages() // Clear messages when user types
    }

    /**
     * Toggles password visibility
     */
    fun togglePasswordVisibility() {
        isPasswordVisible.value = !isPasswordVisible.value
    }

    /**
     * Performs login with email and password using Supabase
     * Returns true if successful, false otherwise
     */
    suspend fun login(): Boolean {
        if (!validateLoginInput()) return false

        isLoading.value = true
        clearMessages()

        val result = authRepository.signInWithEmail(
            email = email.value.trim(),
            password = password.value
        )

        isLoading.value = false

        return result.fold(
            onSuccess = {
                clearForm()
                true
            },
            onFailure = { exception ->
                errorMessage.value = AuthErrorMapper.mapError(exception)
                false
            }
        )
    }

    /**
     * Performs registration with email and password using Supabase
     * Returns true if successful, false otherwise
     */
    suspend fun register(): Boolean {
        if (!validateRegisterInput()) return false

        isLoading.value = true
        clearMessages()

        val result = authRepository.signUpWithEmail(
            email = email.value.trim(),
            password = password.value,
            fullName = name.value.trim()
        )

        isLoading.value = false

        return result.fold(
            onSuccess = {
                clearForm()
                true
            },
            onFailure = { exception ->
                errorMessage.value = AuthErrorMapper.mapError(exception)
                false
            }
        )
    }

    /**
     * Sign in with Google OAuth (for iOS - opens Safari)
     * On Android, use the native Google Sign-In via ComposeAuth.
     * Returns true if the OAuth flow was initiated successfully.
     */
    suspend fun signInWithGoogle(): Boolean {
        isLoading.value = true
        clearMessages()

        val result = authRepository.signInWithGoogle()

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
     * Deletes the current user account permanently
     * Returns true if successful, false otherwise
     */
    suspend fun deleteAccount(): Boolean {
        // Immediately set state to Unauthenticated to prevent observer race condition
        authState.value = AuthState.Unauthenticated
        isLoading.value = true
        clearMessages()

        val result = authRepository.deleteAccount()

        isLoading.value = false

        return result.fold(
            onSuccess = {
                clearForm()
                true
            },
            onFailure = { exception ->
                errorMessage.value = AuthErrorMapper.mapError(exception)
                false
            }
        )
    }

    /**
     * Signs out the current user
     * This is a suspend function to ensure signOut completes before navigation happens,
     * preventing race conditions on iOS where the session might not be cleared yet.
     */
    suspend fun signOut() {
        // Immediately set state to Unauthenticated to prevent observer race condition
        authState.value = AuthState.Unauthenticated
        isLoading.value = true
        authRepository.signOut()
        clearForm()
        isLoading.value = false
    }


    /**
     * Sends password reset email
     */
    suspend fun sendPasswordResetEmail(email: String): Boolean {
        if (email.isBlank()) {
            errorMessage.value = AuthErrorMapper.mapValidationError(ValidationError.EMAIL_REQUIRED)
            return false
        }

        isLoading.value = true
        clearMessages()

        val result = authRepository.sendPasswordResetEmail(email.trim())

        isLoading.value = false

        return result.fold(
            onSuccess = {
                true
            },
            onFailure = { exception ->
                errorMessage.value = AuthErrorMapper.mapError(exception)
                false
            }
        )
    }

    /**
     * Clears all form fields
     */
    fun clearForm() {
        email.value = ""
        password.value = ""
        name.value = ""
    }

    /**
     * Clears the current error message
     */
    fun clearError() {
        errorMessage.value = null
    }

    /**
     * Clears the current success message
     */
    fun clearSuccess() {
        successMessage.value = null
    }

    /**
     * Clears both error and success messages
     */
    private fun clearMessages() {
        errorMessage.value = null
        successMessage.value = null
    }

    /**
     * Validates login input fields
     */
    private fun validateLoginInput(): Boolean {
        return when {
            email.value.isBlank() -> {
                errorMessage.value =
                    AuthErrorMapper.mapValidationError(ValidationError.EMAIL_REQUIRED)
                false
            }

            !email.value.contains("@") -> {
                errorMessage.value =
                    AuthErrorMapper.mapValidationError(ValidationError.EMAIL_INVALID)
                false
            }

            password.value.isBlank() -> {
                errorMessage.value =
                    AuthErrorMapper.mapValidationError(ValidationError.PASSWORD_REQUIRED)
                false
            }

            else -> true
        }
    }

    /**
     * Validates registration input fields
     */
    private fun validateRegisterInput(): Boolean {
        return when {
            name.value.isBlank() -> {
                errorMessage.value =
                    AuthErrorMapper.mapValidationError(ValidationError.NAME_REQUIRED)
                false
            }

            email.value.isBlank() -> {
                errorMessage.value =
                    AuthErrorMapper.mapValidationError(ValidationError.EMAIL_REQUIRED)
                false
            }

            !email.value.contains("@") -> {
                errorMessage.value =
                    AuthErrorMapper.mapValidationError(ValidationError.EMAIL_INVALID)
                false
            }

            password.value.isBlank() -> {
                errorMessage.value =
                    AuthErrorMapper.mapValidationError(ValidationError.PASSWORD_REQUIRED)
                false
            }

            password.value.length < 6 -> {
                errorMessage.value =
                    AuthErrorMapper.mapValidationError(ValidationError.PASSWORD_SHORT)
                false
            }

            else -> true
        }
    }
}
