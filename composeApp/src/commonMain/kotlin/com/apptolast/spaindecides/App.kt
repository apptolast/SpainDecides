package com.apptolast.spaindecides

import androidx.compose.runtime.Composable
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.toRoute
import com.apptolast.spaindecides.navigation.CategoriesRoute
import com.apptolast.spaindecides.navigation.CreateProposalRoute
import com.apptolast.spaindecides.navigation.LoginRoute
import com.apptolast.spaindecides.navigation.ProposalListRoute
import com.apptolast.spaindecides.navigation.RegisterRoute
import com.apptolast.spaindecides.presentation.ui.screens.auth.LoginScreen
import com.apptolast.spaindecides.presentation.ui.screens.auth.RegisterScreen
import com.apptolast.spaindecides.presentation.ui.screens.home.CategoriesScreen
import com.apptolast.spaindecides.presentation.ui.screens.proposals.CreateProposalScreen
import com.apptolast.spaindecides.presentation.ui.screens.proposals.ProposalListScreen
import com.apptolast.spaindecides.presentation.ui.theme.SpainDecidesTheme

/**
 * Main App composable with navigation.
 * Entry point for the entire application.
 */
@Composable
fun App() {
    SpainDecidesTheme(darkTheme = false) {
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
                    onCategoryClick = { categoryId, categoryName ->
                        navController.navigate(
                            ProposalListRoute(
                                categoryId = categoryId,
                                categoryName = categoryName
                            )
                        )
                    }
                )
            }

            // Proposal list screen
            composable<ProposalListRoute> { backStackEntry ->
                val route: ProposalListRoute = backStackEntry.toRoute()
                ProposalListScreen(
                    categoryId = route.categoryId,
                    categoryName = route.categoryName,
                    onBack = {
                        navController.popBackStack()
                    },
                    onCreateProposal = {
                        navController.navigate(
                            CreateProposalRoute(
                                categoryId = route.categoryId,
                                categoryName = route.categoryName
                            )
                        )
                    }
                )
            }

            // Create proposal screen
            composable<CreateProposalRoute> { backStackEntry ->
                val route: CreateProposalRoute = backStackEntry.toRoute()
                CreateProposalScreen(
                    categoryId = route.categoryId,
                    categoryName = route.categoryName,
                    onClose = {
                        navController.popBackStack()
                    },
                    onProposalCreated = {
                        navController.popBackStack()
                    }
                )
            }
        }
    }
}
