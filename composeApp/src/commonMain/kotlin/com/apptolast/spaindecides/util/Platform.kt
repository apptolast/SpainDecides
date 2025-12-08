package com.apptolast.spaindecides.util

/**
 * Platform detection for conditional behavior.
 */
enum class Platform {
    ANDROID,
    IOS
}

/**
 * Returns the current platform.
 */
expect fun getPlatform(): Platform

/**
 * Returns true if running on iOS.
 */
fun isIOS(): Boolean = getPlatform() == Platform.IOS

/**
 * Returns true if running on Android.
 */
fun isAndroid(): Boolean = getPlatform() == Platform.ANDROID
