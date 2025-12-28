package com.apptolast.spaindecides

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.toRoute
import com.apptolast.spaindecides.navigation.CategoriesRoute
import com.apptolast.spaindecides.navigation.CreateProposalRoute
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
