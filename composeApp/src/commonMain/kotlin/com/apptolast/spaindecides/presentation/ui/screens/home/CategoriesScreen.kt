package com.apptolast.spaindecides.presentation.ui.screens.home

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Divider
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.apptolast.spaindecides.presentation.ui.components.CategoryCard
import com.apptolast.spaindecides.presentation.viewmodel.CategoryViewModel
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.viewmodel.koinViewModel
import spaindecides.composeapp.generated.resources.Res
import spaindecides.composeapp.generated.resources.app_name
import spaindecides.composeapp.generated.resources.categories_subtitle
import spaindecides.composeapp.generated.resources.categories_title
import spaindecides.composeapp.generated.resources.error_unknown
import spaindecides.composeapp.generated.resources.menu
import spaindecides.composeapp.generated.resources.retry

/**
 * Categories screen - Main screen showing all participation categories.
 *
 * @param onCategoryClick Callback when a category is clicked
 * @param viewModel Category ViewModel (injected via Koin)
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CategoriesScreen(
    onCategoryClick: (categoryId: String, categoryName: String) -> Unit,
    viewModel: CategoryViewModel = koinViewModel()
) {
    val categories by viewModel.categories.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    val error by viewModel.error.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = stringResource(Res.string.app_name),
                        style = MaterialTheme.typography.titleLarge
                    )
                },
                navigationIcon = {
                    IconButton(onClick = { /* TODO: Open menu */ }) {
                        Icon(
                            imageVector = Icons.Default.Menu,
                            contentDescription = stringResource(Res.string.menu)
                        )
                    }
                },
                actions = {
//                    IconButton(onClick = { /* TODO: Show notifications */ }) {
//                        Icon(
//                            imageVector = Icons.Default.Notifications,
//                            contentDescription = stringResource(Res.string.notifications)
//                        )
//                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface,
                    titleContentColor = MaterialTheme.colorScheme.onSurface
                )
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            // Header section
            Surface(
                color = MaterialTheme.colorScheme.surface,
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 16.dp)
                ) {
                    Text(
                        text = stringResource(Res.string.categories_title),
                        style = MaterialTheme.typography.headlineSmall,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = stringResource(Res.string.categories_subtitle),
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            Divider()

            // Content
            when {
                isLoading -> {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = androidx.compose.ui.Alignment.Center
                    ) {
                        CircularProgressIndicator()
                    }
                }

                error != null -> {
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
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
                        Button(onClick = { viewModel.loadCategories() }) {
                            Text(stringResource(Res.string.retry))
                        }
                    }
                }

                else -> {
                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        contentPadding = PaddingValues(16.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        items(categories) { category ->
                            CategoryCard(
                                category = category,
                                onClick = {
                                    onCategoryClick(category.id, category.name)
                                }
                            )
                        }
                    }
                }
            }
        }
    }
}
