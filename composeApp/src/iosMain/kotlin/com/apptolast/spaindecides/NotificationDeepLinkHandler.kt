package com.apptolast.spaindecides

import com.apptolast.spaindecides.navigation.DeepLink
import com.apptolast.spaindecides.navigation.DeepLinkManager

private const val TAG = "DeepLink-iOS"

/**
 * iOS-callable function to handle notification taps.
 *
 * This function is called from Swift (AppDelegate) when a user taps
 * a push notification. It extracts the navigation parameters and
 * triggers deep link navigation via the DeepLinkManager.
 *
 * Note: categoryKey is looked up client-side from CategoryRepository
 * to keep the notification payload simple.
 *
 * ## Swift Usage:
 * ```swift
 * NotificationDeepLinkHandlerKt.handleNotificationTap(
 *     proposalId: proposalId,
 *     categoryId: categoryId
 * )
 * ```
 *
 * @param proposalId UUID of the proposal to navigate to
 * @param categoryId UUID of the category the proposal belongs to
 */
fun handleNotificationTap(proposalId: String, categoryId: String) {
    println("[$TAG] handleNotificationTap called")
    println("[$TAG] proposalId = $proposalId")
    println("[$TAG] categoryId = $categoryId")

    DeepLinkManager.setDeepLink(
        DeepLink.ProposalDetail(
            proposalId = proposalId,
            categoryId = categoryId
        )
    )
    println("[$TAG] DeepLink set successfully")
}
