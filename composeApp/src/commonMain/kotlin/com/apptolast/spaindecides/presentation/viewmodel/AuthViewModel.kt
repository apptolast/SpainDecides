package com.apptolast.spaindecides.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.apptolast.spaindecides.domain.model.AuthUser
import com.apptolast.spaindecides.domain.repository.AuthRepository
import com.apptolast.spaindecides.util.AuthErrorMapper
import com.apptolast.spaindecides.util.ValidationError
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
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

    private val _email = MutableStateFlow("")
    val email: StateFlow<String> = _email.asStateFlow()

    private val _password = MutableStateFlow("")
    val password: StateFlow<String> = _password.asStateFlow()

    private val _name = MutableStateFlow("") // For registration
    val name: StateFlow<String> = _name.asStateFlow()

    private val _isPasswordVisible = MutableStateFlow(false)
    val isPasswordVisible: StateFlow<Boolean> = _isPasswordVisible.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _errorMessage = MutableStateFlow<StringResource?>(null)
    val errorMessage: StateFlow<StringResource?> = _errorMessage.asStateFlow()

    private val _successMessage = MutableStateFlow<StringResource?>(null)
    val successMessage: StateFlow<StringResource?> = _successMessage.asStateFlow()

    private val _authState = MutableStateFlow<AuthState>(AuthState.Unauthenticated)
    val authState: StateFlow<AuthState> = _authState.asStateFlow()

    init {
        // Observe authentication state changes
        viewModelScope.launch {
            authRepository.observeAuthState().collect { user ->
                _authState.value = if (user != null) {
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
        _email.value = newEmail
        clearMessages() // Clear messages when user types
    }

    /**
     * Updates the password field
     */
    fun updatePassword(newPassword: String) {
        _password.value = newPassword
        clearMessages() // Clear messages when user types
    }

    /**
     * Updates the name field (for registration)
     */
    fun updateName(newName: String) {
        _name.value = newName
        clearMessages() // Clear messages when user types
    }

    /**
     * Toggles password visibility
     */
    fun togglePasswordVisibility() {
        _isPasswordVisible.value = !_isPasswordVisible.value
    }

    /**
     * Performs login with email and password using Supabase
     * Returns true if successful, false otherwise
     */
    suspend fun login(): Boolean {
        if (!validateLoginInput()) return false

        _isLoading.value = true
        clearMessages()

        val result = authRepository.signInWithEmail(
            email = _email.value.trim(),
            password = _password.value
        )

        _isLoading.value = false

        return result.fold(
            onSuccess = {
                clearForm()
                true
            },
            onFailure = { exception ->
                _errorMessage.value = AuthErrorMapper.mapError(exception)
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

        _isLoading.value = true
        clearMessages()

        val result = authRepository.signUpWithEmail(
            email = _email.value.trim(),
            password = _password.value
        )

        _isLoading.value = false

        return result.fold(
            onSuccess = {
                clearForm()
                true
            },
            onFailure = { exception ->
                _errorMessage.value = AuthErrorMapper.mapError(exception)
                false
            }
        )
    }

    /**
     * Performs Google sign-in using Supabase OAuth
     * This is called after the OAuth flow completes
     * Returns true if successful, false otherwise
     */
    suspend fun signInWithGoogle(): Boolean {
        _isLoading.value = true
        clearMessages()

        val result = authRepository.signInWithGoogle()

        _isLoading.value = false

        return result.fold(
            onSuccess = {
                true
            },
            onFailure = { exception ->
                _errorMessage.value = AuthErrorMapper.mapError(exception)
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
        _authState.value = AuthState.Unauthenticated
        _isLoading.value = true
        authRepository.signOut()
        clearForm()
        _isLoading.value = false
    }

    /**
     * Sends password reset email
     */
    suspend fun sendPasswordResetEmail(email: String): Boolean {
        if (email.isBlank()) {
            _errorMessage.value = AuthErrorMapper.mapValidationError(ValidationError.EMAIL_REQUIRED)
            return false
        }

        _isLoading.value = true
        clearMessages()

        val result = authRepository.sendPasswordResetEmail(email.trim())

        _isLoading.value = false

        return result.fold(
            onSuccess = {
                true
            },
            onFailure = { exception ->
                _errorMessage.value = AuthErrorMapper.mapError(exception)
                false
            }
        )
    }

    /**
     * Clears all form fields
     */
    fun clearForm() {
        _email.value = ""
        _password.value = ""
        _name.value = ""
    }

    /**
     * Clears the current error message
     */
    fun clearError() {
        _errorMessage.value = null
    }

    /**
     * Clears the current success message
     */
    fun clearSuccess() {
        _successMessage.value = null
    }

    /**
     * Clears both error and success messages
     */
    private fun clearMessages() {
        _errorMessage.value = null
        _successMessage.value = null
    }

    /**
     * Validates login input fields
     */
    private fun validateLoginInput(): Boolean {
        return when {
            _email.value.isBlank() -> {
                _errorMessage.value =
                    AuthErrorMapper.mapValidationError(ValidationError.EMAIL_REQUIRED)
                false
            }

            !_email.value.contains("@") -> {
                _errorMessage.value =
                    AuthErrorMapper.mapValidationError(ValidationError.EMAIL_INVALID)
                false
            }

            _password.value.isBlank() -> {
                _errorMessage.value =
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
            _name.value.isBlank() -> {
                _errorMessage.value =
                    AuthErrorMapper.mapValidationError(ValidationError.NAME_REQUIRED)
                false
            }

            _email.value.isBlank() -> {
                _errorMessage.value =
                    AuthErrorMapper.mapValidationError(ValidationError.EMAIL_REQUIRED)
                false
            }

            !_email.value.contains("@") -> {
                _errorMessage.value =
                    AuthErrorMapper.mapValidationError(ValidationError.EMAIL_INVALID)
                false
            }

            _password.value.isBlank() -> {
                _errorMessage.value =
                    AuthErrorMapper.mapValidationError(ValidationError.PASSWORD_REQUIRED)
                false
            }

            _password.value.length < 6 -> {
                _errorMessage.value =
                    AuthErrorMapper.mapValidationError(ValidationError.PASSWORD_SHORT)
                false
            }

            else -> true
        }
    }
}
