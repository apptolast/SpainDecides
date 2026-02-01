package com.apptolast.spaindecides.presentation.ui.screens.auth

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
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.PersonAdd
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.Button
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.LinkAnnotation
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.TextLinkStyles
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.withLink
import androidx.compose.ui.unit.dp
import com.apptolast.spaindecides.presentation.ui.theme.SpainDecidesTheme
import com.apptolast.spaindecides.presentation.viewmodel.AuthViewModel
import kotlinx.coroutines.launch
import org.jetbrains.compose.resources.stringResource
import org.jetbrains.compose.ui.tooling.preview.Preview
import org.koin.compose.viewmodel.koinViewModel
import spaindecides.composeapp.generated.resources.Res
import spaindecides.composeapp.generated.resources.field_email
import spaindecides.composeapp.generated.resources.field_name
import spaindecides.composeapp.generated.resources.field_password
import spaindecides.composeapp.generated.resources.hide_password
import spaindecides.composeapp.generated.resources.register_button
import spaindecides.composeapp.generated.resources.register_csae_link
import spaindecides.composeapp.generated.resources.register_csae_prefix
import spaindecides.composeapp.generated.resources.register_csae_url
import spaindecides.composeapp.generated.resources.register_has_account
import spaindecides.composeapp.generated.resources.register_login_link
import spaindecides.composeapp.generated.resources.register_privacy_policy_link
import spaindecides.composeapp.generated.resources.register_privacy_policy_prefix
import spaindecides.composeapp.generated.resources.register_privacy_policy_url
import spaindecides.composeapp.generated.resources.register_subtitle
import spaindecides.composeapp.generated.resources.register_title
import spaindecides.composeapp.generated.resources.show_password
import spaindecides.composeapp.generated.resources.success_registration

/**
 * Stateful Registration screen composable.
 *
 * This composable handles ViewModel injection and state collection,
 * delegating the actual UI rendering to [RegisterContent].
 *
 * @param onRegisterSuccess Callback when registration is successful (navigates to login with success message)
 * @param onNavigateBack Callback to navigate back to login
 * @param viewModel Auth ViewModel (injected via Koin)
 */
@Composable
fun RegisterScreen(
    onRegisterSuccess: (successMessage: String) -> Unit,
    viewModel: AuthViewModel = koinViewModel(),
    onNavigateBack: () -> Unit,
) {
    val name by viewModel.name.collectAsState()
    val email by viewModel.email.collectAsState()
    val password by viewModel.password.collectAsState()
    val isPasswordVisible by viewModel.isPasswordVisible.collectAsState()
    val eulaAccepted by viewModel.eulaAccepted.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    val errorMessage by viewModel.errorMessage.collectAsState()

    val scope = rememberCoroutineScope()
    val snackbarHostState = remember { SnackbarHostState() }

    // Resolve string resources (must be done in @Composable context)
    val successMessageText = stringResource(Res.string.success_registration)

    // Build the policy annotated text with links
    val privacyPrefix = stringResource(Res.string.register_privacy_policy_prefix)
    val privacyLinkText = stringResource(Res.string.register_privacy_policy_link)
    val privacyUrl = stringResource(Res.string.register_privacy_policy_url)
    val csaePrefix = stringResource(Res.string.register_csae_prefix)
    val csaeLinkText = stringResource(Res.string.register_csae_link)
    val csaeUrl = stringResource(Res.string.register_csae_url)

    val linkStyle = TextLinkStyles(
        style = SpanStyle(
            color = MaterialTheme.colorScheme.primary,
            textDecoration = TextDecoration.Underline
        )
    )

    val policyAnnotatedText = buildAnnotatedString {
        append(privacyPrefix)
        withLink(LinkAnnotation.Url(url = privacyUrl, styles = linkStyle)) {
            append(privacyLinkText)
        }
        append(csaePrefix)
        withLink(LinkAnnotation.Url(url = csaeUrl, styles = linkStyle)) {
            append(csaeLinkText)
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

    RegisterContent(
        name = name,
        email = email,
        password = password,
        isPasswordVisible = isPasswordVisible,
        eulaAccepted = eulaAccepted,
        isLoading = isLoading,
        policyAnnotatedText = policyAnnotatedText,
        snackbarHostState = snackbarHostState,
        onNameChange = viewModel::updateName,
        onEmailChange = viewModel::updateEmail,
        onPasswordChange = viewModel::updatePassword,
        onTogglePasswordVisibility = viewModel::togglePasswordVisibility,
        onEulaAcceptedChange = viewModel::updateEulaAccepted,
        onRegister = {
            scope.launch {
                val success = viewModel.register()
                if (success) {
                    onRegisterSuccess(successMessageText)
                }
            }
        },
        onNavigateBack = onNavigateBack
    )
}

/**
 * Stateless Registration content composable.
 *
 * This composable is responsible for rendering the UI based on the provided state.
 * It has no dependencies on ViewModels or other stateful components, making it
 * easy to preview and test.
 *
 * @param name Current name text
 * @param email Current email text
 * @param password Current password text
 * @param isPasswordVisible Whether password is visible
 * @param eulaAccepted Whether EULA is accepted
 * @param isLoading Whether registration is in progress
 * @param policyAnnotatedText The annotated text for policy links
 * @param snackbarHostState State for showing snackbar messages
 * @param onNameChange Callback when name text changes
 * @param onEmailChange Callback when email text changes
 * @param onPasswordChange Callback when password text changes
 * @param onTogglePasswordVisibility Callback to toggle password visibility
 * @param onEulaAcceptedChange Callback when EULA checkbox changes
 * @param onRegister Callback when register button is clicked
 * @param onNavigateBack Callback to navigate back to login
 * @param modifier Optional modifier
 */
@Composable
fun RegisterContent(
    name: String,
    email: String,
    password: String,
    isPasswordVisible: Boolean,
    eulaAccepted: Boolean,
    isLoading: Boolean,
    policyAnnotatedText: AnnotatedString,
    snackbarHostState: SnackbarHostState,
    onNameChange: (String) -> Unit,
    onEmailChange: (String) -> Unit,
    onPasswordChange: (String) -> Unit,
    onTogglePasswordVisibility: () -> Unit,
    onEulaAcceptedChange: (Boolean) -> Unit,
    onRegister: () -> Unit,
    onNavigateBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        contentWindowInsets = WindowInsets(0, 0, 0, 0),
        modifier = modifier
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
                        imageVector = Icons.Default.PersonAdd,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(40.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(32.dp))

            // Title
            Text(
                text = stringResource(Res.string.register_title),
                style = MaterialTheme.typography.headlineMedium,
                color = MaterialTheme.colorScheme.onSurface
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = stringResource(Res.string.register_subtitle),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Spacer(modifier = Modifier.height(32.dp))

            // Name field
            OutlinedTextField(
                value = name,
                onValueChange = onNameChange,
                label = { Text(stringResource(Res.string.field_name)) },
                leadingIcon = {
                    Icon(
                        imageVector = Icons.Default.Person,
                        contentDescription = stringResource(Res.string.field_name)
                    )
                },
                singleLine = true,
                modifier = Modifier.fillMaxWidth(),
                enabled = !isLoading
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Email field
            OutlinedTextField(
                value = email,
                onValueChange = onEmailChange,
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
                onValueChange = onPasswordChange,
                label = { Text(stringResource(Res.string.field_password)) },
                leadingIcon = {
                    Icon(
                        imageVector = Icons.Default.Lock,
                        contentDescription = stringResource(Res.string.field_password)
                    )
                },
                trailingIcon = {
                    IconButton(onClick = onTogglePasswordVisibility) {
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

            Spacer(modifier = Modifier.height(16.dp))

            // Policy acceptance checkbox
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Checkbox(
                    checked = eulaAccepted,
                    onCheckedChange = onEulaAcceptedChange,
                    enabled = !isLoading
                )
                Text(
                    text = policyAnnotatedText,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurface
                )
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Register button
            Button(
                onClick = onRegister,
                modifier = Modifier.fillMaxWidth(),
                enabled = !isLoading && name.isNotBlank() && email.isNotBlank() && password.isNotBlank() && eulaAccepted
            ) {
                if (isLoading) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(20.dp),
                        color = MaterialTheme.colorScheme.onPrimary
                    )
                } else {
                    Text(stringResource(Res.string.register_button))
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Back to login link
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = stringResource(Res.string.register_has_account) + " ",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                TextButton(onClick = onNavigateBack) {
                    Text(stringResource(Res.string.register_login_link))
                }
            }
        }
    }
}

@Preview
@Composable
private fun RegisterContentPreview() {
    SpainDecidesTheme {
        RegisterContent(
            name = "Juan García",
            email = "juan@example.com",
            password = "password123",
            isPasswordVisible = false,
            eulaAccepted = true,
            isLoading = false,
            policyAnnotatedText = AnnotatedString("Acepto la Política de Privacidad y el CSAE"),
            snackbarHostState = remember { SnackbarHostState() },
            onNameChange = {},
            onEmailChange = {},
            onPasswordChange = {},
            onTogglePasswordVisibility = {},
            onEulaAcceptedChange = {},
            onRegister = {},
            onNavigateBack = {}
        )
    }
}

@Preview
@Composable
private fun RegisterContentEmptyPreview() {
    SpainDecidesTheme {
        RegisterContent(
            name = "",
            email = "",
            password = "",
            isPasswordVisible = false,
            eulaAccepted = false,
            isLoading = false,
            policyAnnotatedText = AnnotatedString("Acepto la Política de Privacidad y el CSAE"),
            snackbarHostState = remember { SnackbarHostState() },
            onNameChange = {},
            onEmailChange = {},
            onPasswordChange = {},
            onTogglePasswordVisibility = {},
            onEulaAcceptedChange = {},
            onRegister = {},
            onNavigateBack = {}
        )
    }
}

@Preview
@Composable
private fun RegisterContentLoadingPreview() {
    SpainDecidesTheme {
        RegisterContent(
            name = "Juan García",
            email = "juan@example.com",
            password = "password123",
            isPasswordVisible = false,
            eulaAccepted = true,
            isLoading = true,
            policyAnnotatedText = AnnotatedString("Acepto la Política de Privacidad y el CSAE"),
            snackbarHostState = remember { SnackbarHostState() },
            onNameChange = {},
            onEmailChange = {},
            onPasswordChange = {},
            onTogglePasswordVisibility = {},
            onEulaAcceptedChange = {},
            onRegister = {},
            onNavigateBack = {}
        )
    }
}

@Preview
@Composable
private fun RegisterContentPasswordVisiblePreview() {
    SpainDecidesTheme {
        RegisterContent(
            name = "Juan García",
            email = "juan@example.com",
            password = "password123",
            isPasswordVisible = true,
            eulaAccepted = false,
            isLoading = false,
            policyAnnotatedText = AnnotatedString("Acepto la Política de Privacidad y el CSAE"),
            snackbarHostState = remember { SnackbarHostState() },
            onNameChange = {},
            onEmailChange = {},
            onPasswordChange = {},
            onTogglePasswordVisibility = {},
            onEulaAcceptedChange = {},
            onRegister = {},
            onNavigateBack = {}
        )
    }
}
