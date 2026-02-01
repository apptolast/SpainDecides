package com.apptolast.spaindecides

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.toRoute
import com.apptolast.spaindecides.domain.repository.CategoryRepository
import com.apptolast.spaindecides.navigation.CategoriesRoute
import com.apptolast.spaindecides.navigation.CreateProposalRoute
import com.apptolast.spaindecides.navigation.DeepLink
import com.apptolast.spaindecides.navigation.DeepLinkManager
import com.apptolast.spaindecides.navigation.DuplicateProposalsRoute
import com.apptolast.spaindecides.navigation.LoginRoute
import com.apptolast.spaindecides.navigation.ProposalDetailRoute
import com.apptolast.spaindecides.navigation.ProposalListRoute
import com.apptolast.spaindecides.navigation.RegisterRoute
import com.apptolast.spaindecides.navigation.SettingsRoute
import com.apptolast.spaindecides.presentation.ui.screens.auth.LoginScreen
import com.apptolast.spaindecides.presentation.ui.screens.auth.RegisterScreen
import com.apptolast.spaindecides.presentation.ui.screens.home.CategoriesScreen
import com.apptolast.spaindecides.presentation.ui.screens.proposals.CreateProposalScreen
import com.apptolast.spaindecides.presentation.ui.screens.proposals.DuplicateProposalsScreen
import com.apptolast.spaindecides.presentation.ui.screens.proposals.ProposalDetailScreen
import com.apptolast.spaindecides.presentation.ui.screens.proposals.ProposalListScreen
import com.apptolast.spaindecides.presentation.ui.screens.settings.SettingsScreen
import com.apptolast.spaindecides.presentation.ui.theme.SpainDecidesTheme
import com.apptolast.spaindecides.presentation.viewmodel.ProposalViewModel
import org.koin.compose.koinInject
import org.koin.compose.viewmodel.koinViewModel
import org.koin.core.parameter.parametersOf

/**
 * Main App composable with navigation.
 * Entry point for the entire application.
 */
@Composable
fun App() {
    SpainDecidesTheme {
        val navController = rememberNavController()

        // Inject CategoryRepository for deep link category lookup
        val categoryRepository: CategoryRepository = koinInject()

        // Deep link handling for push notifications
        val pendingDeepLink by DeepLinkManager.pendingDeepLink.collectAsState()
        var hasReachedHome by remember { mutableStateOf(false) }

        // Handle deep link navigation when user is authenticated (has reached home)
        LaunchedEffect(pendingDeepLink, hasReachedHome) {
            println("[DeepLink-App] LaunchedEffect triggered")
            println("[DeepLink-App] pendingDeepLink = $pendingDeepLink")
            println("[DeepLink-App] hasReachedHome = $hasReachedHome")

            val deepLink = pendingDeepLink
            if (deepLink == null) {
                println("[DeepLink-App] No pending deep link, returning")
                return@LaunchedEffect
            }
            if (!hasReachedHome) {
                println("[DeepLink-App] hasReachedHome is false, waiting for user to reach CategoriesRoute")
                return@LaunchedEffect
            }

            println("[DeepLink-App] Processing deep link: $deepLink")

            when (deepLink) {
                is DeepLink.ProposalDetail -> {
                    println("[DeepLink-App] DeepLink.ProposalDetail detected")
                    println("[DeepLink-App] proposalId = ${deepLink.proposalId}")
                    println("[DeepLink-App] categoryId = ${deepLink.categoryId}")

                    // Lookup categoryKey from categoryId using the repository
                    println("[DeepLink-App] Looking up category by ID...")
                    val category = categoryRepository.getCategoryById(deepLink.categoryId)
                    println("[DeepLink-App] Category lookup result: $category")

                    if (category == null) {
                        println("[DeepLink-App] ERROR: Category not found for ID ${deepLink.categoryId}")
                        DeepLinkManager.consumeDeepLink()
                        return@LaunchedEffect
                    }

                    val categoryKey = category.key
                    println("[DeepLink-App] categoryKey = $categoryKey")

                    println("[DeepLink-App] Navigating to ProposalDetailRoute...")
                    println("[DeepLink-App] Route: ProposalDetailRoute(proposalId=${deepLink.proposalId}, categoryId=${deepLink.categoryId}, categoryKey=$categoryKey)")

                    navController.navigate(
                        ProposalDetailRoute(
                            proposalId = deepLink.proposalId,
                            categoryId = deepLink.categoryId,
                            categoryKey = categoryKey
                        )
                    ) {
                        // Ensure CategoriesRoute stays in backstack for proper back navigation
                        popUpTo<CategoriesRoute> { inclusive = false }
                        launchSingleTop = true
                    }
                    println("[DeepLink-App] Navigation triggered, consuming deep link")
                    DeepLinkManager.consumeDeepLink()
                }
            }
        }

        NavHost(
            navController = navController,
            startDestination = LoginRoute()
        ) {
            // Login screen
            composable<LoginRoute> { backStackEntry ->
                val route: LoginRoute = backStackEntry.toRoute()
                LoginScreen(
                    onLoginSuccess = {
                        navController.navigate(CategoriesRoute) {
                            // Clear backstack so user can't go back to login
                            popUpTo<LoginRoute> { inclusive = true }
                        }
                    },
                    onNavigateToRegister = {
                        navController.navigate(RegisterRoute)
                    },
                    successMessage = route.successMessage
                )
            }

            // Register screen
            composable<RegisterRoute> {
                RegisterScreen(
                    onRegisterSuccess = { successMessage ->
                        navController.navigate(LoginRoute(successMessage = successMessage)) {
                            // Clear register screen from backstack
                            popUpTo<RegisterRoute> { inclusive = true }
                        }
                    },
                    onNavigateBack = {
                        navController.popBackStack()
                    }
                )
            }

            // Categories/Home screen
            composable<CategoriesRoute> {
                // Mark that user has reached home (authenticated)
                // This enables deep link navigation for push notifications
                LaunchedEffect(Unit) {
                    println("[DeepLink-App] CategoriesRoute reached, setting hasReachedHome = true")
                    hasReachedHome = true
                }

                CategoriesScreen(
                    onCategoryClick = { category ->
                        navController.navigate(
                            ProposalListRoute(
                                categoryId = category.id,
                                categoryKey = category.key
                            )
                        )
                    },
                    onNavigateToSettings = {
                        navController.navigate(SettingsRoute)
                    }
                )
            }

            // Proposal list screen
            composable<ProposalListRoute> { backStackEntry ->
                val route: ProposalListRoute = backStackEntry.toRoute()
                ProposalListScreen(
                    categoryId = route.categoryId,
                    categoryKey = route.categoryKey,
                    onBack = {
                        navController.popBackStack()
                    },
                    onCreateProposal = {
                        navController.navigate(
                            CreateProposalRoute(
                                categoryId = route.categoryId,
                                categoryKey = route.categoryKey
                            )
                        )
                    },
                    onProposalClick = { proposalId ->
                        navController.navigate(
                            ProposalDetailRoute(
                                proposalId = proposalId,
                                categoryId = route.categoryId,
                                categoryKey = route.categoryKey
                            )
                        )
                    }
                )
            }

            // Create proposal screen
            composable<CreateProposalRoute> { backStackEntry ->
                val route: CreateProposalRoute = backStackEntry.toRoute()

                // Get ViewModel scoped to ProposalListRoute (shared with DuplicateProposalsScreen)
                val proposalListEntry = remember(backStackEntry) {
                    navController.getBackStackEntry(
                        ProposalListRoute(
                            categoryId = route.categoryId,
                            categoryKey = route.categoryKey
                        )
                    )
                }
                val viewModel: ProposalViewModel = koinViewModel(
                    viewModelStoreOwner = proposalListEntry
                ) { parametersOf(route.categoryId) }

                CreateProposalScreen(
                    categoryId = route.categoryId,
                    categoryKey = route.categoryKey,
                    viewModel = viewModel,
                    onClose = {
                        navController.popBackStack()
                    },
                    onProposalCreated = {
                        navController.popBackStack()
                    },
                    onDuplicatesFound = {
                        navController.navigate(
                            DuplicateProposalsRoute(
                                categoryId = route.categoryId,
                                categoryKey = route.categoryKey
                            )
                        )
                    }
                )
            }

            // Duplicate proposals screen
            composable<DuplicateProposalsRoute> { backStackEntry ->
                val route: DuplicateProposalsRoute = backStackEntry.toRoute()

                // Get ViewModel scoped to ProposalListRoute (shared with CreateProposalScreen)
                val proposalListEntry = remember(backStackEntry) {
                    navController.getBackStackEntry(
                        ProposalListRoute(
                            categoryId = route.categoryId,
                            categoryKey = route.categoryKey
                        )
                    )
                }
                val viewModel: ProposalViewModel = koinViewModel(
                    viewModelStoreOwner = proposalListEntry
                ) { parametersOf(route.categoryId) }

                DuplicateProposalsScreen(
                    categoryId = route.categoryId,
                    categoryKey = route.categoryKey,
                    viewModel = viewModel,
                    onCancel = {
                        // Pop back to ProposalListScreen (remove CreateProposal and DuplicateProposals)
                        navController.popBackStack<ProposalListRoute>(inclusive = false)
                    },
                    onProposalCreated = {
                        // Pop back to ProposalListScreen (remove CreateProposal and DuplicateProposals)
                        navController.popBackStack<ProposalListRoute>(inclusive = false)
                    }
                )
            }

            // Proposal detail screen
            composable<ProposalDetailRoute> { backStackEntry ->
                val route: ProposalDetailRoute = backStackEntry.toRoute()
                ProposalDetailScreen(
                    proposalId = route.proposalId,
                    categoryId = route.categoryId,
                    categoryKey = route.categoryKey,
                    onBack = {
                        navController.popBackStack()
                    }
                )
            }

            // Settings screen
            composable<SettingsRoute> {
                SettingsScreen(
                    onBack = {
                        navController.popBackStack()
                    },
                    onLogoutSuccess = {
                        navController.navigate(LoginRoute()) {
                            // Clear all backstack so user can't go back after logout
                            popUpTo(0) { inclusive = true }
                        }
                    }
                )
            }
        }
    }
}
