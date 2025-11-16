package com.apptolast.spaindecides.data.storage

import com.liftric.kvault.KVault

/**
 * iOS implementation of SecureStorage using KVault
 * KVault wraps iOS Keychain
 */
actual class SecureStorage {

    private val store = KVault(serviceName = "com.apptolast.spaindecides")

    actual fun saveToken(key: String, value: String) {
        store.set(key, value)
    }

    actual fun getToken(key: String): String? {
        return store.string(key)
    }

    actual fun removeToken(key: String) {
        store.deleteObject(key)
    }

    actual fun clear() {
        store.clear()
    }
}
