package com.apptolast.spaindecides.presentation.ui.screens.proposals

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
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.apptolast.spaindecides.data.model.Category
import com.apptolast.spaindecides.presentation.ui.components.ProposalCard
import com.apptolast.spaindecides.presentation.util.getLocalizedName
import com.apptolast.spaindecides.presentation.viewmodel.ProposalViewModel
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.viewmodel.koinViewModel
import org.koin.core.parameter.parametersOf
import spaindecides.composeapp.generated.resources.Res
import spaindecides.composeapp.generated.resources.back
import spaindecides.composeapp.generated.resources.proposal_new
import spaindecides.composeapp.generated.resources.proposals_empty_subtitle
import spaindecides.composeapp.generated.resources.proposals_empty_title

/**
 * Proposal list screen - Shows proposals for a specific category.
 *
 * @param categoryId ID of the category (UUID from database)
 * @param categoryKey i18n key for resolving localized name (e.g., "economy", "health")
 * @param viewModel Proposal ViewModel (injected via Koin with categoryId parameter)
 * @param onBack Callback to navigate back
 * @param onCreateProposal Callback to navigate to create proposal screen
 * @param onProposalClick Callback when a proposal card is clicked (navigate to detail)
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProposalListScreen(
    categoryId: String,
    categoryKey: String,
    viewModel: ProposalViewModel = koinViewModel { parametersOf(categoryId) },
    onBack: () -> Unit,
    onCreateProposal: () -> Unit,
    onProposalClick: (String) -> Unit,
) {
    // Reconstruct minimal Category object for using extension functions
    val category = Category(
        id = categoryId,
        key = categoryKey,
        iconName = "", // Not needed for ProposalListScreen
        sortOrder = 0  // Not needed for ProposalListScreen
    )

    val proposals by viewModel.proposals.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = category.getLocalizedName(),
                        style = MaterialTheme.typography.titleLarge
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = stringResource(Res.string.back)
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface,
                    titleContentColor = MaterialTheme.colorScheme.onSurface
                )
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = onCreateProposal,
                containerColor = MaterialTheme.colorScheme.primary
            ) {
                Icon(
                    imageVector = Icons.Default.Add,
                    contentDescription = stringResource(Res.string.proposal_new)
                )
            }
        }
    ) { paddingValues ->
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

            proposals.isEmpty() -> {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(paddingValues)
                        .padding(16.dp),
                    horizontalAlignment = androidx.compose.ui.Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    Text(
                        text = stringResource(Res.string.proposals_empty_title),
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = stringResource(Res.string.proposals_empty_subtitle),
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
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
                    items(proposals) { proposal ->
                        ProposalCard(
                            proposal = proposal,
                            onUpvote = {
                                val newVote = if (proposal.userVote == 1) 0 else 1
                                viewModel.vote(proposal.id, newVote)
                            },
                            onDownvote = {
                                val newVote = if (proposal.userVote == -1) 0 else -1
                                viewModel.vote(proposal.id, newVote)
                            },
                            onCardClick = {
                                onProposalClick(proposal.id)
                            }
                        )
                    }
                }
            }
        }
    }
}
