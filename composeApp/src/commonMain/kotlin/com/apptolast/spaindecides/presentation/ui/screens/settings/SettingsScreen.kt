package com.apptolast.spaindecides.presentation.ui.screens.settings

import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import com.apptolast.spaindecides.presentation.ui.components.LoadingDialog
import com.apptolast.spaindecides.presentation.viewmodel.AuthState
import com.apptolast.spaindecides.presentation.viewmodel.AuthViewModel
import kotlinx.coroutines.launch
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.viewmodel.koinViewModel
import spaindecides.composeapp.generated.resources.Res
import spaindecides.composeapp.generated.resources.loading_deleting_account
import spaindecides.composeapp.generated.resources.loading_signing_out
import spaindecides.composeapp.generated.resources.settings_delete_account_cancel
import spaindecides.composeapp.generated.resources.settings_delete_account_confirm
import spaindecides.composeapp.generated.resources.settings_delete_account_dialog_message
import spaindecides.composeapp.generated.resources.settings_delete_account_dialog_title
import spaindecides.composeapp.generated.resources.settings_logout_cancel
import spaindecides.composeapp.generated.resources.settings_logout_confirm
import spaindecides.composeapp.generated.resources.settings_logout_dialog_message
import spaindecides.composeapp.generated.resources.settings_logout_dialog_title

/**
 * Enum to track which operation is currently in progress
 */
private enum class SettingsOperation {
    LOGOUT,
    DELETE_ACCOUNT
}

/**
 * Stateful Settings screen composable
 *
 * Displays user settings including:
 * - User name (if available)
 * - User email
 * - Logout button with confirmation
 * - Delete account button with confirmation
 *
 * @param onBack Callback when back button is clicked
 * @param onLogoutSuccess Callback when logout is successful (navigate to login)
 * @param modifier Optional modifier
 * @param viewModel AuthViewModel injected via Koin
 */
@Composable
fun SettingsScreen(
    onBack: () -> Unit,
    onLogoutSuccess: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: AuthViewModel = koinViewModel()
) {
    val authState by viewModel.authState.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    val scope = rememberCoroutineScope()

    // Dialog states
    var showLogoutDialog by remember { mutableStateOf(false) }
    var showDeleteAccountDialog by remember { mutableStateOf(false) }

    // Track which operation is in progress
    var currentOperation by remember { mutableStateOf<SettingsOperation?>(null) }

    // Extract user data from auth state
    val user = when (val state = authState) {
        is AuthState.Authenticated -> state.user
        else -> null
    }

    // Show logout confirmation dialog
    if (showLogoutDialog) {
        AlertDialog(
            onDismissRequest = { showLogoutDialog = false },
            title = {
                Text(stringResource(Res.string.settings_logout_dialog_title))
            },
            text = {
                Text(stringResource(Res.string.settings_logout_dialog_message))
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        showLogoutDialog = false
                        currentOperation = SettingsOperation.LOGOUT
                        scope.launch {
                            viewModel.signOut()
                            currentOperation = null
                            onLogoutSuccess()
                        }
                    },
                    enabled = !isLoading
                ) {
                    Text(stringResource(Res.string.settings_logout_confirm))
                }
            },
            dismissButton = {
                TextButton(
                    onClick = { showLogoutDialog = false },
                    enabled = !isLoading
                ) {
                    Text(stringResource(Res.string.settings_logout_cancel))
                }
            }
        )
    }

    // Show delete account confirmation dialog
    if (showDeleteAccountDialog) {
        AlertDialog(
            onDismissRequest = { showDeleteAccountDialog = false },
            title = {
                Text(stringResource(Res.string.settings_delete_account_dialog_title))
            },
            text = {
                Text(stringResource(Res.string.settings_delete_account_dialog_message))
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        showDeleteAccountDialog = false
                        currentOperation = SettingsOperation.DELETE_ACCOUNT
                        scope.launch {
                            val success = viewModel.deleteAccount()
                            currentOperation = null
                            if (success) {
                                onLogoutSuccess() // Navigate to login after account deletion
                            }
                            // If failed, error is shown via viewModel.errorMessage
                            // You could handle the error here if needed
                        }
                    },
                    enabled = !isLoading
                ) {
                    Text(stringResource(Res.string.settings_delete_account_confirm))
                }
            },
            dismissButton = {
                TextButton(
                    onClick = { showDeleteAccountDialog = false },
                    enabled = !isLoading
                ) {
                    Text(stringResource(Res.string.settings_delete_account_cancel))
                }
            }
        )
    }

    // Display settings content
    SettingsContent(
        userName = user?.displayName,
        userEmail = user?.email ?: "",
        photoUrl = user?.photoUrl,
        onBack = onBack,
        onLogout = { showLogoutDialog = true },
        onDeleteAccount = { showDeleteAccountDialog = true },
        modifier = modifier
    )

    // Show loading dialog when an operation is in progress
    if (isLoading && currentOperation != null) {
        val loadingMessage = when (currentOperation) {
            SettingsOperation.LOGOUT -> stringResource(Res.string.loading_signing_out)
            SettingsOperation.DELETE_ACCOUNT -> stringResource(Res.string.loading_deleting_account)
            null -> "" // This shouldn't happen, but keeping it for safety
        }
        LoadingDialog(message = loadingMessage)
    }
}
