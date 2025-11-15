package com.apptolast.spaindecides.presentation.ui.screens.proposals

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material.icons.outlined.Flag
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconButtonDefaults
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
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.apptolast.spaindecides.data.model.ProposalWithUserVote
import com.apptolast.spaindecides.presentation.ui.components.ReportDialog
import com.apptolast.spaindecides.presentation.ui.preview.SampleData
import com.apptolast.spaindecides.presentation.ui.theme.SpainDecidesTheme
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
import spaindecides.composeapp.generated.resources.proposal_detail_title
import spaindecides.composeapp.generated.resources.proposal_downvotes
import spaindecides.composeapp.generated.resources.proposal_net_votes
import spaindecides.composeapp.generated.resources.proposal_upvotes
import spaindecides.composeapp.generated.resources.proposal_vote_down
import spaindecides.composeapp.generated.resources.proposal_vote_up
import spaindecides.composeapp.generated.resources.report_content
import spaindecides.composeapp.generated.resources.report_error
import spaindecides.composeapp.generated.resources.report_success

/**
 * Stateful Proposal detail screen - displays full proposal with voting capability.
 *
 * This composable handles ViewModel injection and state collection,
 * delegating the actual UI rendering to [ProposalDetailContent].
 *
 * @param proposalId ID of the proposal to display
 * @param categoryId ID of the category (for ViewModel)
 * @param categoryKey i18n key for category name
 * @param onBack Callback to navigate back
 * @param viewModel Proposal ViewModel (injected via Koin with categoryId parameter)
 * @param authViewModel Auth ViewModel for getting current user info
 * @param reportViewModel Report ViewModel for handling content reports
 */
@Composable
fun ProposalDetailScreen(
    proposalId: String,
    categoryId: String,
    categoryKey: String,
    viewModel: ProposalViewModel = koinViewModel { parametersOf(categoryId) },
    authViewModel: AuthViewModel = koinViewModel(),
    reportViewModel: ReportViewModel = koinViewModel(),
    onBack: () -> Unit
) {
    val proposals by viewModel.proposals.collectAsStateWithLifecycle()

    // Find the specific proposal by ID
    val proposal = proposals.find { it.id == proposalId }

    // Report state
    val reportUiState by reportViewModel.uiState.collectAsState()
    val authState by authViewModel.authState.collectAsState()
    var showReportDialog by remember { mutableStateOf(false) }
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
                snackbarHostState.showSnackbar(successMessage)
                reportViewModel.resetState()
            }

            is ReportUiState.Error -> {
                showReportDialog = false
                snackbarHostState.showSnackbar(errorMessage)
                reportViewModel.resetState()
            }

            else -> {}
        }
    }

    ProposalDetailContent(
        proposal = proposal,
        snackbarHostState = snackbarHostState,
        onBack = onBack,
        onUpvote = {
            proposal?.let {
                val newVote = if (it.userVote == 1) 0 else 1
                viewModel.vote(it.id, newVote)
            }
        },
        onDownvote = {
            proposal?.let {
                val newVote = if (it.userVote == -1) 0 else -1
                viewModel.vote(it.id, newVote)
            }
        },
        onReportClick = { showReportDialog = true }
    )

    // Report dialog
    if (showReportDialog && proposal != null) {
        ReportDialog(
            proposalTitle = proposal.title,
            onDismiss = { showReportDialog = false },
            onConfirm = { reason ->
                reportViewModel.submitReport(
                    proposalId = proposal.id,
                    proposalTitle = proposal.title,
                    proposalDescription = proposal.description,
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
 * Stateless Proposal detail content composable.
 *
 * This composable is responsible for rendering the UI based on the provided state.
 * It has no dependencies on ViewModels or other stateful components, making it
 * easy to preview and test.
 *
 * Features:
 * - Voting buttons in AppBar (always visible)
 * - Compact statistics below title
 * - Full description with scroll
 * - Report button for inappropriate content
 *
 * @param proposal The proposal to display (null if not found)
 * @param snackbarHostState State for showing snackbar messages
 * @param onBack Callback when back button is clicked
 * @param onUpvote Callback when upvote button is clicked
 * @param onDownvote Callback when downvote button is clicked
 * @param onReportClick Callback when report button is clicked
 * @param modifier Optional modifier
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProposalDetailContent(
    proposal: ProposalWithUserVote?,
    snackbarHostState: SnackbarHostState,
    onBack: () -> Unit,
    onUpvote: () -> Unit,
    onDownvote: () -> Unit,
    onReportClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = stringResource(Res.string.proposal_detail_title),
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
                actions = {
                    if (proposal != null) {
                        // Upvote button
                        IconButton(
                            onClick = onUpvote,
                            colors = IconButtonDefaults.iconButtonColors(
                                contentColor = if (proposal.userVote == 1) {
                                    MaterialTheme.colorScheme.primary
                                } else {
                                    MaterialTheme.colorScheme.onSurfaceVariant
                                }
                            )
                        ) {
                            Icon(
                                imageVector = Icons.Default.KeyboardArrowUp,
                                contentDescription = stringResource(Res.string.proposal_vote_up),
                                modifier = Modifier.size(28.dp)
                            )
                        }

                        // Vote count display
                        Text(
                            text = proposal.formattedVotes,
                            style = MaterialTheme.typography.titleMedium,
                            color = when (proposal.userVote) {
                                1 -> MaterialTheme.colorScheme.primary
                                -1 -> MaterialTheme.colorScheme.error
                                else -> MaterialTheme.colorScheme.onSurface
                            }
                        )

                        // Downvote button
                        IconButton(
                            onClick = onDownvote,
                            colors = IconButtonDefaults.iconButtonColors(
                                contentColor = if (proposal.userVote == -1) {
                                    MaterialTheme.colorScheme.error
                                } else {
                                    MaterialTheme.colorScheme.onSurfaceVariant
                                }
                            )
                        ) {
                            Icon(
                                imageVector = Icons.Default.KeyboardArrowDown,
                                contentDescription = stringResource(Res.string.proposal_vote_down),
                                modifier = Modifier.size(28.dp)
                            )
                        }

                        // Report button
                        IconButton(onClick = onReportClick) {
                            Icon(
                                imageVector = Icons.Outlined.Flag,
                                contentDescription = stringResource(Res.string.report_content),
                                tint = MaterialTheme.colorScheme.error,
                                modifier = Modifier.size(24.dp)
                            )
                        }
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface,
                    titleContentColor = MaterialTheme.colorScheme.onSurface
                )
            )
        },
        modifier = modifier
    ) { paddingValues ->
        if (proposal != null) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
                    .windowInsetsPadding(WindowInsets.navigationBars)
                    .verticalScroll(rememberScrollState())
                    .padding(16.dp)
            ) {
                // Proposal title
                Text(
                    text = proposal.title,
                    style = MaterialTheme.typography.headlineMedium,
                    color = MaterialTheme.colorScheme.onSurface
                )

                Spacer(modifier = Modifier.height(8.dp))

                // Compact statistics row
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceEvenly
                ) {
                    // Upvotes
                    Text(
                        text = stringResource(Res.string.proposal_upvotes, proposal.upvotes),
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.primary
                    )

                    // Downvotes
                    Text(
                        text = stringResource(Res.string.proposal_downvotes, proposal.downvotes),
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.error
                    )

                    // Net votes
                    Text(
                        text = stringResource(Res.string.proposal_net_votes, proposal.netVotes),
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Proposal description (full text, no truncation)
                Text(
                    text = proposal.description,
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        } else {
            // Proposal not found (shouldn't happen normally)
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
                    .windowInsetsPadding(WindowInsets.navigationBars)
                    .padding(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Spacer(modifier = Modifier.height(32.dp))

                Text(
                    text = "Propuesta no encontrada",
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.error
                )
            }
        }
    }
}

@Preview
@Composable
private fun ProposalDetailContentPreview() {
    SpainDecidesTheme {
        ProposalDetailContent(
            proposal = SampleData.sampleProposalWithVote,
            snackbarHostState = remember { SnackbarHostState() },
            onBack = {},
            onUpvote = {},
            onDownvote = {},
            onReportClick = {}
        )
    }
}

@Preview
@Composable
private fun ProposalDetailContentDownvotedPreview() {
    SpainDecidesTheme {
        ProposalDetailContent(
            proposal = SampleData.sampleProposalsWithVotes[2], // Downvoted
            snackbarHostState = remember { SnackbarHostState() },
            onBack = {},
            onUpvote = {},
            onDownvote = {},
            onReportClick = {}
        )
    }
}

@Preview
@Composable
private fun ProposalDetailContentNotFoundPreview() {
    SpainDecidesTheme {
        ProposalDetailContent(
            proposal = null,
            snackbarHostState = remember { SnackbarHostState() },
            onBack = {},
            onUpvote = {},
            onDownvote = {},
            onReportClick = {}
        )
    }
}
