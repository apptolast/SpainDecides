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
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SecondaryScrollableTabRow
import androidx.compose.material3.Surface
import androidx.compose.material3.Tab
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.apptolast.spaindecides.data.model.SimilarProposal
import com.apptolast.spaindecides.presentation.viewmodel.ProposalViewModel
import kotlinx.coroutines.launch
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.viewmodel.koinViewModel
import org.koin.core.parameter.parametersOf
import spaindecides.composeapp.generated.resources.Res
import spaindecides.composeapp.generated.resources.back
import spaindecides.composeapp.generated.resources.duplicates_cancel
import spaindecides.composeapp.generated.resources.duplicates_create_anyway
import spaindecides.composeapp.generated.resources.duplicates_similarity
import spaindecides.composeapp.generated.resources.duplicates_title
import spaindecides.composeapp.generated.resources.proposal_vote_down
import spaindecides.composeapp.generated.resources.proposal_vote_up

/**
 * Duplicate proposals screen - displays similar proposals for user review.
 *
 * Features:
 * - TabRow with similarity percentages (swipe + click navigation)
 * - HorizontalPager for full proposal content
 * - Voting capability on each proposal
 * - Fixed bottom bar with Cancel and Create anyway buttons
 *
 * @param categoryId ID of the category
 * @param categoryKey i18n key for category name
 * @param viewModel Proposal ViewModel (injected via Koin with categoryId parameter)
 * @param onCancel Callback when user cancels (navigates back to proposal list)
 * @param onProposalCreated Callback when proposal is created anyway
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DuplicateProposalsScreen(
    categoryId: String,
    categoryKey: String,
    viewModel: ProposalViewModel = koinViewModel { parametersOf(categoryId) },
    onCancel: () -> Unit,
    onProposalCreated: () -> Unit
) {
    val duplicates by viewModel.duplicatesFound.collectAsStateWithLifecycle()
    val duplicateProposals by viewModel.duplicateProposals.collectAsStateWithLifecycle()
    val isCreating by viewModel.isCreating.collectAsStateWithLifecycle()

    // Create map for quick access to real-time data
    val proposalDataMap = duplicateProposals.associateBy { it.id }

    val scope = rememberCoroutineScope()
    val pagerState = rememberPagerState(pageCount = { duplicates.size })

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = stringResource(Res.string.duplicates_title),
                        style = MaterialTheme.typography.titleLarge
                    )
                },
                navigationIcon = {
                    IconButton(onClick = {
                        viewModel.clearDuplicatesState()
                        onCancel()
                    }) {
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
        bottomBar = {
            Surface(
                modifier = Modifier.fillMaxWidth(),
                tonalElevation = 3.dp
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    horizontalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    OutlinedButton(
                        onClick = {
                            viewModel.clearDuplicatesState()
                            onCancel()
                        },
                        modifier = Modifier.weight(1f),
                        enabled = !isCreating
                    ) {
                        Text(stringResource(Res.string.duplicates_cancel))
                    }

                    Button(
                        onClick = {
                            scope.launch {
                                val success = viewModel.createProposal(forceCreation = true)
                                if (success) {
                                    viewModel.clearDuplicatesState()
                                    onProposalCreated()
                                }
                            }
                        },
                        modifier = Modifier.weight(1f),
                        enabled = !isCreating
                    ) {
                        Text(stringResource(Res.string.duplicates_create_anyway))
                    }
                }
            }
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .windowInsetsPadding(WindowInsets.navigationBars)
        ) {
            if (duplicates.isNotEmpty()) {
                // Tab row with similarity percentages
                SecondaryScrollableTabRow(
                    selectedTabIndex = pagerState.currentPage,
                    edgePadding = 16.dp,
                    containerColor = MaterialTheme.colorScheme.surface,
                    contentColor = MaterialTheme.colorScheme.primary
                ) {
                    duplicates.forEachIndexed { index, duplicate ->
                        Tab(
                            selected = pagerState.currentPage == index,
                            onClick = {
                                scope.launch {
                                    pagerState.animateScrollToPage(index)
                                }
                            },
                            text = {
                                Text(
                                    text = stringResource(
                                        Res.string.duplicates_similarity,
                                        (duplicate.similarity * 100).toInt()
                                    )
                                )
                            }
                        )
                    }
                }

                // Horizontal pager for proposal content
                HorizontalPager(
                    state = pagerState,
                    modifier = Modifier.fillMaxSize(),
                    key = { page -> duplicates[page].id }
                ) { page ->
                    val similarProposal = duplicates[page]
                    val realData = proposalDataMap[similarProposal.id]

                    DuplicateProposalContent(
                        proposal = similarProposal,
                        votesCount = realData?.netVotes ?: similarProposal.votesCount,
                        userVote = realData?.userVote ?: 0,
                        onUpvote = { viewModel.voteOnDuplicate(similarProposal.id, 1) },
                        onDownvote = { viewModel.voteOnDuplicate(similarProposal.id, -1) }
                    )
                }
            }
        }
    }
}

/**
 * Content for a single duplicate proposal page.
 *
 * @param proposal The similar proposal data (title, description)
 * @param votesCount Real-time vote count from Supabase
 * @param userVote Current user's vote state (1, -1, or 0)
 * @param onUpvote Callback for upvote action
 * @param onDownvote Callback for downvote action
 */
@Composable
private fun DuplicateProposalContent(
    proposal: SimilarProposal,
    votesCount: Int,
    userVote: Int,
    onUpvote: () -> Unit,
    onDownvote: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp)
    ) {
        // Proposal title
        Text(
            text = proposal.title,
            style = MaterialTheme.typography.headlineMedium,
            color = MaterialTheme.colorScheme.onSurface
        )

        Spacer(modifier = Modifier.height(16.dp))

        // Proposal description
        Text(
            text = proposal.description,
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        Spacer(modifier = Modifier.height(24.dp))

        // Voting section with real-time data
        VotingSection(
            votesCount = votesCount,
            userVote = userVote,
            onUpvote = onUpvote,
            onDownvote = onDownvote
        )

        // Add extra space at the bottom for better scrolling experience
        Spacer(modifier = Modifier.height(32.dp))
    }
}

/**
 * Voting section component with upvote/downvote buttons.
 */
@Composable
private fun VotingSection(
    votesCount: Int,
    userVote: Int,
    onUpvote: () -> Unit,
    onDownvote: () -> Unit
) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = MaterialTheme.shapes.medium,
        tonalElevation = 1.dp
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Upvote button
            IconButton(
                onClick = onUpvote,
                colors = IconButtonDefaults.iconButtonColors(
                    contentColor = if (userVote == 1) {
                        MaterialTheme.colorScheme.primary
                    } else {
                        MaterialTheme.colorScheme.onSurfaceVariant
                    }
                )
            ) {
                Icon(
                    imageVector = Icons.Default.KeyboardArrowUp,
                    contentDescription = stringResource(Res.string.proposal_vote_up),
                    modifier = Modifier.size(32.dp)
                )
            }

            // Vote count
            Text(
                text = votesCount.toString(),
                style = MaterialTheme.typography.headlineSmall,
                color = when (userVote) {
                    1 -> MaterialTheme.colorScheme.primary
                    -1 -> MaterialTheme.colorScheme.error
                    else -> MaterialTheme.colorScheme.onSurface
                },
                modifier = Modifier.padding(horizontal = 16.dp)
            )

            // Downvote button
            IconButton(
                onClick = onDownvote,
                colors = IconButtonDefaults.iconButtonColors(
                    contentColor = if (userVote == -1) {
                        MaterialTheme.colorScheme.error
                    } else {
                        MaterialTheme.colorScheme.onSurfaceVariant
                    }
                )
            ) {
                Icon(
                    imageVector = Icons.Default.KeyboardArrowDown,
                    contentDescription = stringResource(Res.string.proposal_vote_down),
                    modifier = Modifier.size(32.dp)
                )
            }
        }
    }
}
