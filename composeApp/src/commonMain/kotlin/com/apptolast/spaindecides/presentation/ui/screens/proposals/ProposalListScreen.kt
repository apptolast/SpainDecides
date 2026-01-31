package com.apptolast.spaindecides.presentation.ui.screens.proposals

import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
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
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.apptolast.spaindecides.data.model.Category
import com.apptolast.spaindecides.data.model.ProposalWithUserVote
import com.apptolast.spaindecides.presentation.ui.components.ProposalCard
import com.apptolast.spaindecides.presentation.ui.components.ReportDialog
import com.apptolast.spaindecides.presentation.ui.preview.SampleData
import com.apptolast.spaindecides.presentation.ui.theme.SpainDecidesTheme
import com.apptolast.spaindecides.presentation.util.getLocalizedName
import com.apptolast.spaindecides.presentation.viewmodel.AuthState
import com.apptolast.spaindecides.presentation.viewmodel.AuthViewModel
import com.apptolast.spaindecides.presentation.viewmodel.ProposalViewModel
import com.apptolast.spaindecides.presentation.viewmodel.ReportUiState
import com.apptolast.spaindecides.presentation.viewmodel.ReportViewModel
import org.jetbrains.compose.resources.stringResource
import org.jetbrains.compose.ui.tooling.preview.Preview
import org.koin.compose.viewmodel.koinViewModel
import org.koin.core.parameter.parametersOf
import spaindecides.composeapp.generated.resources.Res
import spaindecides.composeapp.generated.resources.back
import spaindecides.composeapp.generated.resources.proposal_new
import spaindecides.composeapp.generated.resources.proposals_empty_subtitle
import spaindecides.composeapp.generated.resources.proposals_empty_title
import spaindecides.composeapp.generated.resources.report_error
import spaindecides.composeapp.generated.resources.report_success

/**
 * Stateful Proposal list screen - Shows proposals for a specific category.
 *
 * This composable handles ViewModel injection and state collection,
 * delegating the actual UI rendering to [ProposalListContent].
 *
 * @param categoryId ID of the category (UUID from database)
 * @param categoryKey i18n key for resolving localized name (e.g., "economy", "health")
 * @param viewModel Proposal ViewModel (injected via Koin with categoryId parameter)
 * @param authViewModel Auth ViewModel for getting current user info
 * @param reportViewModel Report ViewModel for handling content reports
 * @param onBack Callback to navigate back
 * @param onCreateProposal Callback to navigate to create proposal screen
 * @param onProposalClick Callback when a proposal card is clicked (navigate to detail)
 */
@Composable
fun ProposalListScreen(
    categoryId: String,
    categoryKey: String,
    viewModel: ProposalViewModel = koinViewModel { parametersOf(categoryId) },
    authViewModel: AuthViewModel = koinViewModel(),
    reportViewModel: ReportViewModel = koinViewModel(),
    onBack: () -> Unit,
    onCreateProposal: () -> Unit,
    onProposalClick: (String) -> Unit,
) {
    // Reconstruct minimal Category object for using extension functions
    val category = Category(
        id = categoryId,
        key = categoryKey,
        iconName = "",
        sortOrder = 0
    )

    val proposals by viewModel.proposals.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()

    // Report state
    val reportUiState by reportViewModel.uiState.collectAsState()
    val authState by authViewModel.authState.collectAsState()
    var showReportDialog by remember { mutableStateOf(false) }
    var selectedProposal by remember { mutableStateOf<ProposalWithUserVote?>(null) }
    val snackbarHostState = remember { SnackbarHostState() }

    // Get current user info
    val currentUserId = (authState as? AuthState.Authenticated)?.user?.id
    val currentUserEmail = (authState as? AuthState.Authenticated)?.user?.email

    // String resources for snackbar
    val successMessage = stringResource(Res.string.report_success)
    val errorMessage = stringResource(Res.string.report_error)

    // Handle report state changes
    LaunchedEffect(reportUiState) {
        when (reportUiState) {
            is ReportUiState.Success -> {
                showReportDialog = false
                selectedProposal = null
                snackbarHostState.showSnackbar(successMessage)
                reportViewModel.resetState()
            }

            is ReportUiState.Error -> {
                showReportDialog = false
                selectedProposal = null
                snackbarHostState.showSnackbar(errorMessage)
                reportViewModel.resetState()
            }

            else -> {}
        }
    }

    ProposalListContent(
        categoryName = category.getLocalizedName(),
        proposals = proposals,
        isLoading = isLoading,
        snackbarHostState = snackbarHostState,
        onBack = onBack,
        onCreateProposal = onCreateProposal,
        onProposalClick = onProposalClick,
        onUpvote = { proposalId, currentVote ->
            val newVote = if (currentVote == 1) 0 else 1
            viewModel.vote(proposalId, newVote)
        },
        onDownvote = { proposalId, currentVote ->
            val newVote = if (currentVote == -1) 0 else -1
            viewModel.vote(proposalId, newVote)
        },
        onReportClick = { proposal ->
            selectedProposal = proposal
            showReportDialog = true
        }
    )

    // Report dialog
    if (showReportDialog && selectedProposal != null) {
        ReportDialog(
            proposalTitle = selectedProposal!!.title,
            onDismiss = {
                showReportDialog = false
                selectedProposal = null
            },
            onConfirm = { reason ->
                reportViewModel.submitReport(
                    proposalId = selectedProposal!!.id,
                    proposalTitle = selectedProposal!!.title,
                    proposalDescription = selectedProposal!!.description,
                    reason = reason,
                    currentUserId = currentUserId,
                    currentUserEmail = currentUserEmail
                )
            },
            isLoading = reportUiState is ReportUiState.Loading
        )
    }
}

/**
 * Stateless Proposal list content composable.
 *
 * This composable is responsible for rendering the UI based on the provided state.
 * It has no dependencies on ViewModels or other stateful components, making it
 * easy to preview and test.
 *
 * @param categoryName Localized name of the category to display in the title
 * @param proposals List of proposals with user vote status
 * @param isLoading Whether data is being loaded
 * @param snackbarHostState State for showing snackbar messages
 * @param onBack Callback when back button is clicked
 * @param onCreateProposal Callback when FAB is clicked
 * @param onProposalClick Callback when a proposal card is clicked
 * @param onUpvote Callback when upvote is clicked (proposalId, currentVote)
 * @param onDownvote Callback when downvote is clicked (proposalId, currentVote)
 * @param onReportClick Callback when report button is clicked
 * @param modifier Optional modifier
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProposalListContent(
    categoryName: String,
    proposals: List<ProposalWithUserVote>,
    isLoading: Boolean,
    snackbarHostState: SnackbarHostState,
    onBack: () -> Unit,
    onCreateProposal: () -> Unit,
    onProposalClick: (String) -> Unit,
    onUpvote: (proposalId: String, currentVote: Int) -> Unit,
    onDownvote: (proposalId: String, currentVote: Int) -> Unit,
    onReportClick: (ProposalWithUserVote) -> Unit,
    modifier: Modifier = Modifier
) {
    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = categoryName,
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

            proposals.isEmpty() -> {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(paddingValues)
                        .padding(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
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
                val navigationBarPadding = WindowInsets.navigationBars.asPaddingValues()
                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(paddingValues),
                    contentPadding = PaddingValues(
                        start = 16.dp,
                        end = 16.dp,
                        top = 16.dp,
                        // Extra padding at bottom for FAB (56.dp) + spacing (16.dp) + navigation bar
                        bottom = 72.dp + navigationBarPadding.calculateBottomPadding()
                    ),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(
                        items = proposals,
                        key = { proposal -> proposal.id },
                        contentType = { "proposal" }
                    ) { proposal ->
                        ProposalCard(
                            proposal = proposal,
                            onUpvote = { onUpvote(proposal.id, proposal.userVote) },
                            onDownvote = { onDownvote(proposal.id, proposal.userVote) },
                            onCardClick = { onProposalClick(proposal.id) },
                            onReportClick = { onReportClick(proposal) },
                            modifier = Modifier.animateItem(
                                fadeInSpec = spring(stiffness = Spring.StiffnessMediumLow),
                                fadeOutSpec = spring(stiffness = Spring.StiffnessMediumLow),
                                placementSpec = spring(
                                    dampingRatio = Spring.DampingRatioNoBouncy,
                                    stiffness = Spring.StiffnessMediumLow
                                )
                            )
                        )
                    }
                }
            }
        }
    }
}

@Preview
@Composable
private fun ProposalListContentPreview() {
    SpainDecidesTheme {
        ProposalListContent(
            categoryName = "Economía",
            proposals = SampleData.sampleProposalsWithVotes,
            isLoading = false,
            snackbarHostState = remember { SnackbarHostState() },
            onBack = {},
            onCreateProposal = {},
            onProposalClick = {},
            onUpvote = { _, _ -> },
            onDownvote = { _, _ -> },
            onReportClick = {}
        )
    }
}

@Preview
@Composable
private fun ProposalListContentLoadingPreview() {
    SpainDecidesTheme {
        ProposalListContent(
            categoryName = "Economía",
            proposals = emptyList(),
            isLoading = true,
            snackbarHostState = remember { SnackbarHostState() },
            onBack = {},
            onCreateProposal = {},
            onProposalClick = {},
            onUpvote = { _, _ -> },
            onDownvote = { _, _ -> },
            onReportClick = {}
        )
    }
}

@Preview
@Composable
private fun ProposalListContentEmptyPreview() {
    SpainDecidesTheme {
        ProposalListContent(
            categoryName = "Economía",
            proposals = emptyList(),
            isLoading = false,
            snackbarHostState = remember { SnackbarHostState() },
            onBack = {},
            onCreateProposal = {},
            onProposalClick = {},
            onUpvote = { _, _ -> },
            onDownvote = { _, _ -> },
            onReportClick = {}
        )
    }
}
