package com.apptolast.spaindecides.presentation.ui.screens.auth

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.ime
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.apptolast.spaindecides.data.remote.SupabaseClientConfig
import com.apptolast.spaindecides.presentation.viewmodel.AuthState
import com.apptolast.spaindecides.presentation.viewmodel.AuthViewModel
import com.apptolast.spaindecides.util.isIOS
import io.github.jan.supabase.compose.auth.composable.NativeSignInResult
import io.github.jan.supabase.compose.auth.composable.rememberSignInWithGoogle
import io.github.jan.supabase.compose.auth.composeAuth
import kotlinx.coroutines.launch
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.viewmodel.koinViewModel
import spaindecides.composeapp.generated.resources.Res
import spaindecides.composeapp.generated.resources.auth_divider
import spaindecides.composeapp.generated.resources.error_google_signin
import spaindecides.composeapp.generated.resources.field_email
import spaindecides.composeapp.generated.resources.field_password
import spaindecides.composeapp.generated.resources.forgot_password
import spaindecides.composeapp.generated.resources.hide_password
import spaindecides.composeapp.generated.resources.ic_google_logo
import spaindecides.composeapp.generated.resources.login_button
import spaindecides.composeapp.generated.resources.login_no_account
import spaindecides.composeapp.generated.resources.login_register_link
import spaindecides.composeapp.generated.resources.login_subtitle
import spaindecides.composeapp.generated.resources.login_title
import spaindecides.composeapp.generated.resources.login_with_google
import spaindecides.composeapp.generated.resources.show_password

/**
 * Login screen composable.
 * Allows users to log in with email and password (UI only for now).
 *
 * @param onLoginSuccess Callback when login is successful
 * @param onNavigateToRegister Callback to navigate to registration screen
 * @param successMessage Optional success message to show (e.g., from registration)
 * @param viewModel Auth ViewModel (injected via Koin)
 */
@Composable
fun LoginScreen(
    onLoginSuccess: () -> Unit,
    onNavigateToRegister: () -> Unit,
    successMessage: String? = null,
    viewModel: AuthViewModel = koinViewModel()
) {
    val email by viewModel.email.collectAsStateWithLifecycle()
    val password by viewModel.password.collectAsStateWithLifecycle()
    val isPasswordVisible by viewModel.isPasswordVisible.collectAsStateWithLifecycle()
    val isLoading by viewModel.isLoading.collectAsStateWithLifecycle()
    val errorMessage by viewModel.errorMessage.collectAsStateWithLifecycle()
    val authState by viewModel.authState.collectAsStateWithLifecycle()

    val scope = rememberCoroutineScope()
    val snackbarHostState = remember { SnackbarHostState() }

    // Resolve string resources (must be done in @Composable context)
    val googleSignInErrorText = stringResource(Res.string.error_google_signin)

    // Google Sign-In for Android only (native Google One Tap)
    // On iOS, we use the OAuth web flow via AuthRepository.signInWithGoogle()
    val googleSignInState = if (!isIOS()) {
        SupabaseClientConfig.client.composeAuth.rememberSignInWithGoogle(
            onResult = { result ->
                scope.launch {
                    when (result) {
                        is NativeSignInResult.Success -> {
                            // User successfully signed in with Google
                            onLoginSuccess()
                        }

                        is NativeSignInResult.ClosedByUser -> {
                            // User closed the sign-in dialog
                            // No action needed
                        }

                        is NativeSignInResult.Error -> {
                            // Error occurred during sign-in
                            snackbarHostState.showSnackbar(googleSignInErrorText)
                        }

                        is NativeSignInResult.NetworkError -> {
                            // Network error occurred
                            snackbarHostState.showSnackbar(googleSignInErrorText)
                        }
                    }
                }
            }
        )
    } else {
        null
    }

    // Show success message if provided (from registration)
    LaunchedEffect(successMessage) {
        successMessage?.let {
            snackbarHostState.showSnackbar(it)
        }
    }

    // Show error message when it changes
    val currentErrorMessage = errorMessage?.let { stringResource(it) }
    LaunchedEffect(currentErrorMessage) {
        currentErrorMessage?.let {
            snackbarHostState.showSnackbar(it)
            viewModel.clearError()
        }
    }

    // Observe auth state and navigate when authenticated
    // This is crucial for iOS: when Safari redirects back after Google OAuth,
    // the Compose UI may be recreated, losing the rememberSignInWithGoogle state.
    // By observing authState directly, we can detect when Supabase processes
    // the OAuth callback and creates a session, triggering navigation automatically.
    LaunchedEffect(authState) {
        if (authState is AuthState.Authenticated) {
            println("LoginScreen: User authenticated via authState observer, navigating to next screen")
            onLoginSuccess()
        }
    }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        contentWindowInsets = WindowInsets(0, 0, 0, 0)
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .windowInsetsPadding(WindowInsets.navigationBars)
                .windowInsetsPadding(WindowInsets.ime)
                .verticalScroll(rememberScrollState())
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            // App icon placeholder
            Surface(
                shape = MaterialTheme.shapes.large,
                color = MaterialTheme.colorScheme.primaryContainer,
                modifier = Modifier.size(80.dp)
            ) {
                Box(
                    contentAlignment = Alignment.Center,
                    modifier = Modifier.fillMaxSize()
                ) {
                    Icon(
                        imageVector = Icons.Default.Lock,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(40.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(32.dp))

            // Title
            Text(
                text = stringResource(Res.string.login_title),
                style = MaterialTheme.typography.headlineMedium,
                color = MaterialTheme.colorScheme.onSurface
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = stringResource(Res.string.login_subtitle),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Spacer(modifier = Modifier.height(32.dp))

            // Email field
            OutlinedTextField(
                value = email,
                onValueChange = viewModel::updateEmail,
                label = { Text(stringResource(Res.string.field_email)) },
                leadingIcon = {
                    Icon(
                        imageVector = Icons.Default.Email,
                        contentDescription = stringResource(Res.string.field_email)
                    )
                },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
                singleLine = true,
                modifier = Modifier.fillMaxWidth(),
                enabled = !isLoading
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Password field
            OutlinedTextField(
                value = password,
                onValueChange = viewModel::updatePassword,
                label = { Text(stringResource(Res.string.field_password)) },
                leadingIcon = {
                    Icon(
                        imageVector = Icons.Default.Lock,
                        contentDescription = stringResource(Res.string.field_password)
                    )
                },
                trailingIcon = {
                    IconButton(onClick = viewModel::togglePasswordVisibility) {
                        Icon(
                            imageVector = if (isPasswordVisible) Icons.Default.Visibility else Icons.Default.VisibilityOff,
                            contentDescription = if (isPasswordVisible) stringResource(Res.string.hide_password) else stringResource(
                                Res.string.show_password
                            )
                        )
                    }
                },
                visualTransformation = if (isPasswordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                singleLine = true,
                modifier = Modifier.fillMaxWidth(),
                enabled = !isLoading
            )

            Spacer(modifier = Modifier.height(8.dp))

            // Forgot password link
            TextButton(
                onClick = { /* TODO: Implement forgot password */ },
                enabled = !isLoading
            ) {
                Text(
                    text = stringResource(Res.string.forgot_password),
                    style = MaterialTheme.typography.bodySmall
                )
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Login button
            Button(
                onClick = {
                    scope.launch {
                        val success = viewModel.login()
                        if (success) {
                            onLoginSuccess()
                        }
                    }
                },
                modifier = Modifier.fillMaxWidth(),
                enabled = !isLoading && email.isNotBlank() && password.isNotBlank()
            ) {
                if (isLoading) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(20.dp),
                        color = MaterialTheme.colorScheme.onPrimary
                    )
                } else {
                    Text(stringResource(Res.string.login_button))
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Divider with "or" text
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                HorizontalDivider(modifier = Modifier.weight(1f))
                Text(
                    text = stringResource(Res.string.auth_divider),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(horizontal = 16.dp)
                )
                HorizontalDivider(modifier = Modifier.weight(1f))
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Google Sign-In button (standard Google style)
            OutlinedButton(
                onClick = {
                    if (isIOS()) {
                        // iOS: Use OAuth web flow via repository
                        scope.launch {
                            viewModel.signInWithGoogle()
                            // Navigation happens via authState observer when OAuth completes
                        }
                    } else {
                        // Android: Use native Google One Tap
                        googleSignInState?.startFlow()
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(48.dp),
                enabled = !isLoading,
                colors = ButtonDefaults.outlinedButtonColors(
                    containerColor = MaterialTheme.colorScheme.surface,
                    contentColor = MaterialTheme.colorScheme.onSurface
                ),
                border = BorderStroke(
                    width = 1.dp,
                    color = MaterialTheme.colorScheme.outline.copy(alpha = 0.5f)
                )
            ) {
                // Google logo icon
                Icon(
                    painter = painterResource(Res.drawable.ic_google_logo),
                    contentDescription = "Google",
                    modifier = Modifier.size(20.dp),
                    tint = Color.Unspecified // Use original colors from the vector drawable
                )
                Spacer(modifier = Modifier.width(12.dp))
                Text(
                    text = stringResource(Res.string.login_with_google),
                    style = MaterialTheme.typography.labelLarge
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Register link
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = stringResource(Res.string.login_no_account) + " ",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                TextButton(onClick = onNavigateToRegister) {
                    Text(stringResource(Res.string.login_register_link))
                }
            }
        }
    }
}
