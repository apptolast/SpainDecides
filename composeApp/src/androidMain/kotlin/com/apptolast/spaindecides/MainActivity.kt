package com.apptolast.spaindecides

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Preview
import androidx.core.content.ContextCompat
import com.apptolast.baselogin.BaseLoginAndroid
import com.apptolast.spaindecides.navigation.DeepLink
import com.apptolast.spaindecides.navigation.DeepLinkManager

private const val TAG = "DeepLink"

class MainActivity : ComponentActivity() {

    private val requestPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted: Boolean ->
        if (isGranted) {
            println("Notification permission granted")
        } else {
            println("Notification permission denied")
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        // BaseLogin needs a foreground Activity to launch Google sign-in via Credential Manager.
        BaseLoginAndroid.attachActivity(this)
        enableEdgeToEdge()
        super.onCreate(savedInstanceState)

        // Request notification permission for Android 13+
        askNotificationPermission()

        // Handle notification tap on cold start
        handleNotificationIntent(intent)

        setContent {
            App()
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        BaseLoginAndroid.detachActivity(this)
    }

    private fun askNotificationPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            when {
                ContextCompat.checkSelfPermission(
                    this,
                    Manifest.permission.POST_NOTIFICATIONS
                ) == PackageManager.PERMISSION_GRANTED -> {
                    // Permission already granted
                }

                else -> {
                    // Request permission
                    requestPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
                }
            }
        }
    }

    /**
     * Handles deep links when the app is already running.
     * This is required for OAuth callback flow and notification deep links.
     */
    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        setIntent(intent)

        // Handle notification tap on warm start
        handleNotificationIntent(intent)
    }

    /**
     * Extracts notification data from Intent and triggers deep link navigation.
     * Called on both cold start (onCreate) and warm start (onNewIntent).
     *
     * The notification payload must contain:
     * - type: "new_proposal"
     * - proposalId: UUID of the proposal
     * - categoryId: UUID of the category
     *
     * Note: categoryKey is looked up client-side from CategoryRepository
     * to keep the notification payload simple.
     */
    private fun handleNotificationIntent(intent: Intent?) {
        Log.d(TAG, "=== handleNotificationIntent START ===")
        Log.d(TAG, "Intent: $intent")
        Log.d(TAG, "Intent action: ${intent?.action}")
        Log.d(TAG, "Intent flags: ${intent?.flags}")

        val extras = intent?.extras
        if (extras == null) {
            Log.d(TAG, "No extras in Intent, skipping")
            return
        }

        // Log all extras for debugging
        Log.d(TAG, "Intent extras keys: ${extras.keySet().toList()}")
        extras.keySet().forEach { key ->
            Log.d(TAG, "  Extra[$key] = ${extras.get(key)}")
        }

        val type = extras.getString("type")
        Log.d(TAG, "Extracted type: $type")

        if (type == "new_proposal") {
            val proposalId = extras.getString("proposalId")
            val categoryId = extras.getString("categoryId")
            Log.d(TAG, "Extracted proposalId: $proposalId")
            Log.d(TAG, "Extracted categoryId: $categoryId")

            if (proposalId == null) {
                Log.e(TAG, "proposalId is NULL, cannot create deep link")
                return
            }
            if (categoryId == null) {
                Log.e(TAG, "categoryId is NULL, cannot create deep link")
                return
            }

            Log.d(TAG, "Setting DeepLink.ProposalDetail(proposalId=$proposalId, categoryId=$categoryId)")
            DeepLinkManager.setDeepLink(
                DeepLink.ProposalDetail(
                    proposalId = proposalId,
                    categoryId = categoryId
                )
            )
            Log.d(TAG, "DeepLink set successfully!")
        } else {
            Log.d(TAG, "Type is not 'new_proposal', ignoring (type=$type)")
        }
        Log.d(TAG, "=== handleNotificationIntent END ===")
    }
}

@Preview
@Composable
fun AppAndroidPreview() {
    App()
}