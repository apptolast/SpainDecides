package com.apptolast.spaindecides.data.storage

import android.content.Context
import com.liftric.kvault.KVault

/**
 * Android implementation of SecureStorage using KVault
 * KVault wraps EncryptedSharedPreferences on Android
 */
actual class SecureStorage(context: Context) {

    private val store = KVault(context, "auth_storage")

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
