package com.apptolast.spaindecides.presentation.ui.screens.proposals

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.apptolast.spaindecides.presentation.viewmodel.ProposalViewModel
import kotlinx.coroutines.launch
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.viewmodel.koinViewModel
import org.koin.core.parameter.parametersOf
import spaindecides.composeapp.generated.resources.Res
import spaindecides.composeapp.generated.resources.close
import spaindecides.composeapp.generated.resources.create_proposal_category
import spaindecides.composeapp.generated.resources.create_proposal_counter
import spaindecides.composeapp.generated.resources.create_proposal_placeholder
import spaindecides.composeapp.generated.resources.create_proposal_publish
import spaindecides.composeapp.generated.resources.create_proposal_title

/**
 * Create proposal screen - Form to create a new proposal.
 *
 * @param categoryId ID of the category to create the proposal in
 * @param categoryName Name of the category (for display)
 * @param onClose Callback to close the screen
 * @param onProposalCreated Callback when proposal is successfully created
 * @param viewModel Proposal ViewModel (injected via Koin with categoryId parameter)
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CreateProposalScreen(
    categoryId: String,
    categoryName: String,
    onClose: () -> Unit,
    onProposalCreated: () -> Unit,
    viewModel: ProposalViewModel = koinViewModel { parametersOf(categoryId) }
) {
    val proposalText by viewModel.newProposalText.collectAsState()
    val isCreating by viewModel.isCreating.collectAsState()
    val error by viewModel.error.collectAsState()

    val scope = rememberCoroutineScope()
    val snackbarHostState = remember { SnackbarHostState() }

    // Clear text when screen is opened
    LaunchedEffect(Unit) {
        viewModel.clearNewProposalText()
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = stringResource(Res.string.create_proposal_title),
                        style = MaterialTheme.typography.titleLarge
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onClose) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = stringResource(Res.string.close)
                        )
                    }
                },
                actions = {
                    TextButton(
                        onClick = {
                            scope.launch {
                                val success = viewModel.createProposal()
                                if (success) {
                                    onProposalCreated()
                                } else {
                                    error?.let {
                                        snackbarHostState.showSnackbar(it)
                                    }
                                }
                            }
                        },
                        enabled = !isCreating && proposalText.isNotBlank()
                    ) {
                        Text(stringResource(Res.string.create_proposal_publish))
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface,
                    titleContentColor = MaterialTheme.colorScheme.onSurface
                )
            )
        },
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp)
        ) {
            // Category chip
            Surface(
                shape = MaterialTheme.shapes.small,
                color = MaterialTheme.colorScheme.primaryContainer
            ) {
                Text(
                    text = stringResource(Res.string.create_proposal_category, categoryName),
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onPrimaryContainer,
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Text field
            OutlinedTextField(
                value = proposalText,
                onValueChange = viewModel::updateNewProposalText,
                placeholder = { Text(stringResource(Res.string.create_proposal_placeholder)) },
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
                enabled = !isCreating,
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = MaterialTheme.colorScheme.primary,
                    unfocusedBorderColor = MaterialTheme.colorScheme.outline
                )
            )

            Spacer(modifier = Modifier.height(8.dp))

            // Character counter
            Text(
                text = stringResource(Res.string.create_proposal_counter, viewModel.characterCount),
                style = MaterialTheme.typography.bodySmall,
                color = if (viewModel.characterCount > 150) {
                    MaterialTheme.colorScheme.error
                } else {
                    MaterialTheme.colorScheme.onSurfaceVariant
                },
                modifier = Modifier.align(Alignment.End)
            )

            if (isCreating) {
                Spacer(modifier = Modifier.height(16.dp))
                LinearProgressIndicator(
                    modifier = Modifier.fillMaxWidth()
                )
            }
        }
    }
}
