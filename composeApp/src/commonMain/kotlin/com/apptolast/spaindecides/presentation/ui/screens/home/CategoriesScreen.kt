package com.apptolast.spaindecides.presentation.ui.screens.home

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.apptolast.spaindecides.data.model.Category
import com.apptolast.spaindecides.presentation.ui.components.AppTopBar
import com.apptolast.spaindecides.presentation.ui.components.CategoryCard
import com.apptolast.spaindecides.presentation.viewmodel.CategoryViewModel
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.viewmodel.koinViewModel
import spaindecides.composeapp.generated.resources.Res
import spaindecides.composeapp.generated.resources.categories_subtitle
import spaindecides.composeapp.generated.resources.categories_title
import spaindecides.composeapp.generated.resources.error_unknown
import spaindecides.composeapp.generated.resources.retry

/**
 * Categories screen - Main screen showing all participation categories.
 *
 * @param onCategoryClick Callback when a category is clicked
 * @param onNavigateToSettings Callback when settings button is clicked
 * @param categoryViewModel Category ViewModel (injected via Koin)
 */
@Composable
fun CategoriesScreen(
    onCategoryClick: (categoryId: Category) -> Unit,
    onNavigateToSettings: () -> Unit,
    categoryViewModel: CategoryViewModel = koinViewModel()
) {
    val categories by categoryViewModel.categories.collectAsState()
    val proposalCounts by categoryViewModel.proposalCounts.collectAsState()
    val isLoading by categoryViewModel.isLoading.collectAsState()
    val error by categoryViewModel.error.collectAsState()

    Scaffold(
        topBar = {
            AppTopBar(
                title = stringResource(Res.string.categories_title),
                subtitle = stringResource(Res.string.categories_subtitle),
                onSettingsClick = onNavigateToSettings
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
                val navigationBarPadding = WindowInsets.navigationBars.asPaddingValues()
                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(paddingValues),
                    contentPadding = PaddingValues(
                        start = 16.dp,
                        end = 16.dp,
                        top = 16.dp,
                        bottom = 16.dp + navigationBarPadding.calculateBottomPadding()
                    ),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(
                        items = categories,
                        key = { category -> category.id }
                    ) { category ->
                        CategoryCard(
                            category = category,
                            proposalCount = proposalCounts[category.id],
                            onClick = {
                                onCategoryClick(category)
                            }
                        )
                    }
                }
            }
        }
    }
}
