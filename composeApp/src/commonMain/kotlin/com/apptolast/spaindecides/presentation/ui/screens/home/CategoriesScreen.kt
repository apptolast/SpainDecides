package com.apptolast.spaindecides.presentation.ui.screens.home

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
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
import androidx.compose.ui.unit.dp
import com.apptolast.spaindecides.presentation.ui.components.AppTopBar
import com.apptolast.spaindecides.presentation.ui.components.CategoryCard
import com.apptolast.spaindecides.presentation.viewmodel.AuthViewModel
import com.apptolast.spaindecides.presentation.viewmodel.CategoryViewModel
import kotlinx.coroutines.launch
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.viewmodel.koinViewModel
import spaindecides.composeapp.generated.resources.Res
import spaindecides.composeapp.generated.resources.categories_subtitle
import spaindecides.composeapp.generated.resources.categories_title
import spaindecides.composeapp.generated.resources.error_unknown
import spaindecides.composeapp.generated.resources.logout_cancel
import spaindecides.composeapp.generated.resources.logout_confirm
import spaindecides.composeapp.generated.resources.logout_confirmation_message
import spaindecides.composeapp.generated.resources.logout_confirmation_title
import spaindecides.composeapp.generated.resources.retry

/**
 * Categories screen - Main screen showing all participation categories.
 *
 * @param onCategoryClick Callback when a category is clicked
 * @param onLogout Callback when user logs out successfully
 * @param categoryViewModel Category ViewModel (injected via Koin)
 * @param authViewModel Auth ViewModel for logout functionality (injected via Koin)
 */
@Composable
fun CategoriesScreen(
    onCategoryClick: (categoryId: String, categoryName: String) -> Unit,
    onLogout: () -> Unit,
    categoryViewModel: CategoryViewModel = koinViewModel(),
    authViewModel: AuthViewModel = koinViewModel()
) {
    val categories by categoryViewModel.categories.collectAsState()
    val isLoading by categoryViewModel.isLoading.collectAsState()
    val error by categoryViewModel.error.collectAsState()

    // Coroutine scope for logout operation
    val scope = rememberCoroutineScope()

    // State for logout confirmation dialog
    var showLogoutDialog by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            AppTopBar(
                title = stringResource(Res.string.categories_title),
                subtitle = stringResource(Res.string.categories_subtitle),
                onSettingsClick = {
                    // TODO: Navigate to settings screen
                },
                onLogoutClick = {
                    showLogoutDialog = true
                }
            )
        }
    ) { paddingValues ->
        // Content
        when {
            isLoading -> {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(paddingValues),
                    contentAlignment = androidx.compose.ui.Alignment.Center
                ) {
                    CircularProgressIndicator()
                }
            }

            error != null -> {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(paddingValues)
                        .padding(16.dp),
                    horizontalAlignment = androidx.compose.ui.Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    Text(
                        text = error ?: stringResource(Res.string.error_unknown),
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.error
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Button(onClick = { categoryViewModel.loadCategories() }) {
                        Text(stringResource(Res.string.retry))
                    }
                }
            }

            else -> {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(paddingValues),
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(categories) { category ->
                        CategoryCard(
                            category = category,
                            onClick = {
                                onCategoryClick(category.id, category.key)
                            }
                        )
                    }
                }
            }
        }
    }

    // Logout confirmation dialog
    if (showLogoutDialog) {
        AlertDialog(
            onDismissRequest = { showLogoutDialog = false },
            title = {
                Text(text = stringResource(Res.string.logout_confirmation_title))
            },
            text = {
                Text(text = stringResource(Res.string.logout_confirmation_message))
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        showLogoutDialog = false
                        // Launch coroutine to wait for signOut to complete before navigating
                        // This prevents race condition on iOS where session might not be cleared yet
                        scope.launch {
                            authViewModel.signOut()  // Wait for signOut to complete
                            onLogout()               // Then navigate to login
                        }
                    }
                ) {
                    Text(stringResource(Res.string.logout_confirm))
                }
            },
            dismissButton = {
                TextButton(
                    onClick = { showLogoutDialog = false }
                ) {
                    Text(stringResource(Res.string.logout_cancel))
                }
            }
        )
    }
}
