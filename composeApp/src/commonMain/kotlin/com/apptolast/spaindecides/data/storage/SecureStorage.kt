package com.apptolast.spaindecides.data.storage

/**
 * Secure storage for authentication tokens
 * Uses EncryptedSharedPreferences on Android and Keychain on iOS
 */
expect class SecureStorage {

    /**
     * Save a token securely
     */
    fun saveToken(key: String, value: String)

    /**
     * Get a token from secure storage
     */
    fun getToken(key: String): String?

    /**
     * Remove a specific token
     */
    fun removeToken(key: String)

    /**
     * Clear all stored tokens
     */
    fun clear()
}
