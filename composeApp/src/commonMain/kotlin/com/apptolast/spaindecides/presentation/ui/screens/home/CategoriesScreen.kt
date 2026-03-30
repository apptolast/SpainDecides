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
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.apptolast.spaindecides.data.model.Category
import com.apptolast.spaindecides.presentation.ui.components.AppTopBar
import com.apptolast.spaindecides.presentation.ui.components.CategoryCard
import com.apptolast.spaindecides.presentation.ui.preview.SampleData
import com.apptolast.spaindecides.presentation.ui.theme.SpainDecidesTheme
import com.apptolast.spaindecides.presentation.viewmodel.CategoryViewModel
import org.jetbrains.compose.resources.stringResource
import org.jetbrains.compose.ui.tooling.preview.Preview
import org.koin.compose.viewmodel.koinViewModel
import spaindecides.composeapp.generated.resources.Res
import spaindecides.composeapp.generated.resources.categories_subtitle
import spaindecides.composeapp.generated.resources.categories_title
import spaindecides.composeapp.generated.resources.retry

/**
 * Stateful Categories screen - Main screen showing all participation categories.
 *
 * This composable handles ViewModel injection and state collection,
 * delegating the actual UI rendering to [CategoriesContent].
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
    // Refresh proposal counts each time the screen enters composition
    LaunchedEffect(Unit) {
        categoryViewModel.loadProposalCounts()
    }

    val categories by categoryViewModel.categories.collectAsState()
    val proposalCounts by categoryViewModel.proposalCounts.collectAsState()
    val isLoading by categoryViewModel.isLoading.collectAsState()
    val error by categoryViewModel.error.collectAsState()

    CategoriesContent(
        categories = categories,
        proposalCounts = proposalCounts,
        isLoading = isLoading,
        error = error,
        onCategoryClick = onCategoryClick,
        onNavigateToSettings = onNavigateToSettings,
        onRetry = { categoryViewModel.loadCategories() }
    )
}

/**
 * Stateless Categories content composable.
 *
 * This composable is responsible for rendering the UI based on the provided state.
 * It has no dependencies on ViewModels or other stateful components, making it
 * easy to preview and test.
 *
 * @param categories List of categories to display
 * @param proposalCounts Map of category IDs to their proposal counts
 * @param isLoading Whether data is being loaded
 * @param error Error message to display, if any
 * @param onCategoryClick Callback when a category is clicked
 * @param onNavigateToSettings Callback when settings button is clicked
 * @param onRetry Callback when retry button is clicked
 * @param modifier Optional modifier
 */
@Composable
fun CategoriesContent(
    categories: List<Category>,
    proposalCounts: Map<String, Int>,
    isLoading: Boolean,
    error: String?,
    onCategoryClick: (Category) -> Unit,
    onNavigateToSettings: () -> Unit,
    onRetry: () -> Unit,
    modifier: Modifier = Modifier
) {
    Scaffold(
        topBar = {
            AppTopBar(
                title = stringResource(Res.string.categories_title),
                subtitle = stringResource(Res.string.categories_subtitle),
                onSettingsClick = onNavigateToSettings
            )
        },
        modifier = modifier
    ) { paddingValues ->
        when {
            isLoading -> {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(paddingValues),
                    contentAlignment = Alignment.Center
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
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    Text(
                        text = error,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.error
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Button(onClick = onRetry) {
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
                            onClick = { onCategoryClick(category) }
                        )
                    }
                }
            }
        }
    }
}

@Preview
@Composable
private fun CategoriesContentPreview() {
    SpainDecidesTheme {
        CategoriesContent(
            categories = SampleData.sampleCategories,
            proposalCounts = SampleData.sampleProposalCounts,
            isLoading = false,
            error = null,
            onCategoryClick = {},
            onNavigateToSettings = {},
            onRetry = {}
        )
    }
}

@Preview
@Composable
private fun CategoriesContentLoadingPreview() {
    SpainDecidesTheme {
        CategoriesContent(
            categories = emptyList(),
            proposalCounts = emptyMap(),
            isLoading = true,
            error = null,
            onCategoryClick = {},
            onNavigateToSettings = {},
            onRetry = {}
        )
    }
}

@Preview
@Composable
private fun CategoriesContentErrorPreview() {
    SpainDecidesTheme {
        CategoriesContent(
            categories = emptyList(),
            proposalCounts = emptyMap(),
            isLoading = false,
            error = "Error al cargar las categorías. Comprueba tu conexión a internet.",
            onCategoryClick = {},
            onNavigateToSettings = {},
            onRetry = {}
        )
    }
}

@Preview
@Composable
private fun CategoriesContentEmptyPreview() {
    SpainDecidesTheme {
        CategoriesContent(
            categories = emptyList(),
            proposalCounts = emptyMap(),
            isLoading = false,
            error = null,
            onCategoryClick = {},
            onNavigateToSettings = {},
            onRetry = {}
        )
    }
}
