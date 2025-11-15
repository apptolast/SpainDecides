package com.apptolast.spaindecides.presentation.viewmodel

import androidx.lifecycle.ViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

/**
 * ViewModel for authentication screens (Login and Register).
 * For now, this just manages UI state without real authentication.
 * Real authentication will be implemented with Firebase/Supabase in the future.
 */
class AuthViewModel : ViewModel() {

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

    /**
     * Updates the email field
     */
    fun updateEmail(newEmail: String) {
        _email.value = newEmail
    }

    /**
     * Updates the password field
     */
    fun updatePassword(newPassword: String) {
        _password.value = newPassword
    }

    /**
     * Updates the name field (for registration)
     */
    fun updateName(newName: String) {
        _name.value = newName
    }

    /**
     * Toggles password visibility
     */
    fun togglePasswordVisibility() {
        _isPasswordVisible.value = !_isPasswordVisible.value
    }

    /**
     * Performs login (currently just a UI simulation)
     * Returns true if "successful" (always true for now)
     */
    suspend fun login(): Boolean {
        _isLoading.value = true
        // Simulate network delay
        kotlinx.coroutines.delay(500)
        _isLoading.value = false
        return true // Always successful for mock
    }

    /**
     * Performs registration (currently just a UI simulation)
     * Returns true if "successful" (always true for now)
     */
    suspend fun register(): Boolean {
        _isLoading.value = true
        // Simulate network delay
        kotlinx.coroutines.delay(500)
        _isLoading.value = false
        return true // Always successful for mock
    }

    /**
     * Clears all form fields
     */
    fun clearForm() {
        _email.value = ""
        _password.value = ""
        _name.value = ""
    }
}
