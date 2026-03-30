package com.apptolast.spaindecides.navigation

import com.apptolast.spaindecides.navigation.DeepLinkManager.consumeDeepLink
import com.apptolast.spaindecides.navigation.DeepLinkManager.pendingDeepLink
import com.apptolast.spaindecides.navigation.DeepLinkManager.setDeepLink
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

private const val TAG = "DeepLinkManager"

/**
 * Deep link types supported by the app.
 * Each type represents a specific navigation destination that can be
 * triggered by external sources like push notifications.
 */
sealed class DeepLink {
    /**
     * Deep link to navigate to a specific proposal detail screen.
     * The categoryKey is looked up client-side from the categoryId
     * to keep the notification payload simple.
     *
     * @param proposalId UUID of the proposal to display
     * @param categoryId UUID of the category the proposal belongs to
     */
    data class ProposalDetail(
        val proposalId: String,
        val categoryId: String
    ) : DeepLink()
}

/**
 * Global manager for handling deep links from notifications.
 *
 * Platform-specific code (Android MainActivity, iOS AppDelegate) emits deep links here,
 * and the App composable observes and handles navigation.
 *
 * ## Usage Flow:
 * 1. User taps a notification
 * 2. Platform code extracts notification data and calls [setDeepLink]
 * 3. App.kt observes [pendingDeepLink] via collectAsState
 * 4. App.kt looks up categoryKey from CategoryRepository
 * 5. LaunchedEffect triggers navigation to the appropriate screen
 * 6. After navigation, [consumeDeepLink] is called to clear the pending state
 *
 * ## Thread Safety:
 * StateFlow is thread-safe, so it's safe to call [setDeepLink] from any thread
 * (e.g., Android main thread, iOS main queue).
 */
object DeepLinkManager {
    private val _pendingDeepLink = MutableStateFlow<DeepLink?>(null)

    /**
     * Observable state for pending deep links.
     * Null when no deep link is pending.
     */
    val pendingDeepLink: StateFlow<DeepLink?> = _pendingDeepLink.asStateFlow()

    /**
     * Sets a deep link to be processed by the navigation layer.
     * Called by platform-specific code when a notification is tapped.
     *
     * @param deepLink The deep link destination to navigate to
     */
    fun setDeepLink(deepLink: DeepLink) {
        println("[$TAG] setDeepLink called with: $deepLink")
        _pendingDeepLink.value = deepLink
        println("[$TAG] pendingDeepLink is now: ${_pendingDeepLink.value}")
    }

    /**
     * Clears the pending deep link after it has been processed.
     * Must be called after successful navigation to prevent
     * re-navigation on recomposition.
     */
    fun consumeDeepLink() {
        println("[$TAG] consumeDeepLink called, clearing pendingDeepLink (was: ${_pendingDeepLink.value})")
        _pendingDeepLink.value = null
    }
}
