package com.apptolast.spaindecides.presentation.ui.screens.proposals

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.ime
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.AlertDialog
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
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.apptolast.spaindecides.presentation.ui.theme.SpainDecidesTheme
import com.apptolast.spaindecides.presentation.util.getLocalizedCategoryName
import com.apptolast.spaindecides.presentation.viewmodel.ProposalViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import org.jetbrains.compose.resources.stringResource
import androidx.compose.ui.tooling.preview.Preview
import org.koin.compose.viewmodel.koinViewModel
import org.koin.core.parameter.parametersOf
import spaindecides.composeapp.generated.resources.Res
import spaindecides.composeapp.generated.resources.back
import spaindecides.composeapp.generated.resources.create_proposal_category
import spaindecides.composeapp.generated.resources.create_proposal_description_counter
import spaindecides.composeapp.generated.resources.create_proposal_description_label
import spaindecides.composeapp.generated.resources.create_proposal_description_placeholder
import spaindecides.composeapp.generated.resources.create_proposal_discard_cancel
import spaindecides.composeapp.generated.resources.create_proposal_discard_confirm
import spaindecides.composeapp.generated.resources.create_proposal_discard_message
import spaindecides.composeapp.generated.resources.create_proposal_discard_title
import spaindecides.composeapp.generated.resources.create_proposal_publish
import spaindecides.composeapp.generated.resources.create_proposal_title
import spaindecides.composeapp.generated.resources.create_proposal_title_counter
import spaindecides.composeapp.generated.resources.create_proposal_title_label
import spaindecides.composeapp.generated.resources.create_proposal_title_placeholder

/**
 * Stateful Create proposal screen - Form to create a new proposal.
 *
 * This composable handles ViewModel injection and state collection,
 * delegating the actual UI rendering to [CreateProposalContent].
 *
 * @param categoryId ID of the category to create the proposal in (UUID from database)
 * @param categoryKey i18n key for resolving localized name (e.g., "economy", "health")
 * @param onClose Callback to close the screen
 * @param onProposalCreated Callback when proposal is successfully created
 * @param onDuplicatesFound Callback when duplicates are detected (navigates to DuplicateProposalsScreen)
 * @param viewModel Proposal ViewModel (injected via Koin with categoryId parameter)
 */
@Composable
fun CreateProposalScreen(
    categoryId: String,
    categoryKey: String,
    viewModel: ProposalViewModel = koinViewModel { parametersOf(categoryId) },
    onClose: () -> Unit,
    onProposalCreated: () -> Unit,
    onDuplicatesFound: () -> Unit,
) {
    val proposalTitle by viewModel.newProposalTitle.collectAsStateWithLifecycle()
    val proposalDescription by viewModel.newProposalDescription.collectAsStateWithLifecycle()
    val isCreating by viewModel.isCreating.collectAsStateWithLifecycle()
    val error by viewModel.error.collectAsStateWithLifecycle()
    val showDuplicatesDialog by viewModel.showDuplicatesDialog.collectAsStateWithLifecycle()

    val scope = rememberCoroutineScope()
    val snackbarHostState = remember { SnackbarHostState() }

    // Resolve localized category name
    val categoryName = getLocalizedCategoryName(categoryKey)

    // Navigate to DuplicateProposalsScreen when duplicates are found
    LaunchedEffect(showDuplicatesDialog) {
        if (showDuplicatesDialog) {
            onDuplicatesFound()
        }
    }

    // Observe errors reactively - fixes stale state bug
    LaunchedEffect(error) {
        error?.let { errorMessage ->
            snackbarHostState.showSnackbar(errorMessage)
            viewModel.clearError()
        }
    }

    CreateProposalContent(
        categoryName = categoryName,
        proposalTitle = proposalTitle,
        proposalDescription = proposalDescription,
        titleCharacterCount = viewModel.titleCharacterCount,
        descriptionCharacterCount = viewModel.descriptionCharacterCount,
        isCreating = isCreating,
        snackbarHostState = snackbarHostState,
        onTitleChange = viewModel::updateNewProposalTitle,
        onDescriptionChange = viewModel::updateNewProposalDescription,
        onClose = onClose,
        onPublish = {
            scope.launch {
                val success = viewModel.createProposal()
                if (success) {
                    onProposalCreated()
                }
                // Errors are shown via LaunchedEffect(error) above
                // Duplicates are handled via LaunchedEffect(showDuplicatesDialog) above
            }
        }
    )
}

/**
 * Stateless Create proposal content composable.
 *
 * This composable is responsible for rendering the UI based on the provided state.
 * It has no dependencies on ViewModels or other stateful components, making it
 * easy to preview and test.
 *
 * @param categoryName Localized name of the category
 * @param proposalTitle Current title text
 * @param proposalDescription Current description text
 * @param titleCharacterCount Current character count for title
 * @param descriptionCharacterCount Current character count for description
 * @param isCreating Whether proposal is being created
 * @param snackbarHostState State for showing snackbar messages
 * @param onTitleChange Callback when title text changes
 * @param onDescriptionChange Callback when description text changes
 * @param onClose Callback when close button is clicked
 * @param onPublish Callback when publish button is clicked
 * @param modifier Optional modifier
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CreateProposalContent(
    categoryName: String,
    proposalTitle: String,
    proposalDescription: String,
    titleCharacterCount: Int,
    descriptionCharacterCount: Int,
    isCreating: Boolean,
    snackbarHostState: SnackbarHostState,
    onTitleChange: (String) -> Unit,
    onDescriptionChange: (String) -> Unit,
    onClose: () -> Unit,
    onPublish: () -> Unit,
    modifier: Modifier = Modifier
) {
    val scrollState = rememberScrollState()
    var showDiscardDialog by remember { mutableStateOf(false) }

    // Focus management for keyboard navigation
    val descriptionFocusRequester = remember { FocusRequester() }
    val keyboardController = LocalSoftwareKeyboardController.current

    // Check if form is valid for publishing
    val canPublish = !isCreating && proposalTitle.isNotBlank() && proposalDescription.isNotBlank()

    // Check if there's any content to discard
    val hasContent = proposalTitle.isNotBlank() || proposalDescription.isNotBlank()

    // Handle back button press
    val handleBack: () -> Unit = {
        if (hasContent) {
            showDiscardDialog = true
        } else {
            onClose()
        }
    }

    // Auto-scroll to bottom when keyboard appears (keeps cursor visible)
    LaunchedEffect(scrollState.maxValue) {
        if (scrollState.maxValue > 0) {
            delay(100) // Small delay for smooth transition
            scrollState.animateScrollTo(scrollState.maxValue)
        }
    }

    // Discard confirmation dialog
    if (showDiscardDialog) {
        AlertDialog(
            onDismissRequest = { showDiscardDialog = false },
            title = { Text(stringResource(Res.string.create_proposal_discard_title)) },
            text = { Text(stringResource(Res.string.create_proposal_discard_message)) },
            confirmButton = {
                TextButton(
                    onClick = {
                        showDiscardDialog = false
                        onClose()
                    }
                ) {
                    Text(stringResource(Res.string.create_proposal_discard_confirm))
                }
            },
            dismissButton = {
                TextButton(onClick = { showDiscardDialog = false }) {
                    Text(stringResource(Res.string.create_proposal_discard_cancel))
                }
            }
        )
    }

    Scaffold(
        modifier = modifier,
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = stringResource(Res.string.create_proposal_title),
                        style = MaterialTheme.typography.titleLarge
                    )
                },
                navigationIcon = {
                    IconButton(onClick = handleBack) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = stringResource(Res.string.back)
                        )
                    }
                },
                actions = {
                    TextButton(
                        onClick = onPublish,
                        enabled = canPublish
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
        snackbarHost = { SnackbarHost(snackbarHostState) },
        // Don't let Scaffold consume bottom insets - we'll handle them manually
        contentWindowInsets = WindowInsets(0, 0, 0, 0)
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .windowInsetsPadding(WindowInsets.navigationBars)
                .windowInsetsPadding(WindowInsets.ime)
                .verticalScroll(scrollState)
                .padding(16.dp)
        ) {
            // Category name indicator
            Text(
                text = stringResource(Res.string.create_proposal_category, categoryName),
                style = MaterialTheme.typography.titleLarge,
                color = MaterialTheme.colorScheme.primary
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Title label
            Text(
                text = stringResource(Res.string.create_proposal_title_label),
                style = MaterialTheme.typography.labelLarge,
                color = MaterialTheme.colorScheme.onSurface
            )

            Spacer(modifier = Modifier.height(4.dp))

            // Title input field
            OutlinedTextField(
                value = proposalTitle,
                onValueChange = onTitleChange,
                placeholder = { Text(stringResource(Res.string.create_proposal_title_placeholder)) },
                modifier = Modifier.fillMaxWidth(),
                enabled = !isCreating,
                singleLine = true,
                maxLines = 1,
                keyboardOptions = KeyboardOptions(
                    capitalization = KeyboardCapitalization.Sentences,
                    imeAction = ImeAction.Next
                ),
                keyboardActions = KeyboardActions(
                    onNext = { descriptionFocusRequester.requestFocus() }
                ),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = MaterialTheme.colorScheme.primary,
                    unfocusedBorderColor = MaterialTheme.colorScheme.outline
                )
            )

            Spacer(modifier = Modifier.height(4.dp))

            // Title character counter
            Text(
                text = stringResource(
                    Res.string.create_proposal_title_counter,
                    titleCharacterCount
                ),
                style = MaterialTheme.typography.bodySmall,
                color = if (titleCharacterCount > 100) {
                    MaterialTheme.colorScheme.error
                } else {
                    MaterialTheme.colorScheme.onSurfaceVariant
                },
                modifier = Modifier.align(Alignment.End)
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Description label
            Text(
                text = stringResource(Res.string.create_proposal_description_label),
                style = MaterialTheme.typography.labelLarge,
                color = MaterialTheme.colorScheme.onSurface
            )

            Spacer(modifier = Modifier.height(4.dp))

            // Description input field
            OutlinedTextField(
                value = proposalDescription,
                onValueChange = onDescriptionChange,
                placeholder = { Text(stringResource(Res.string.create_proposal_description_placeholder)) },
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(min = 200.dp)
                    .focusRequester(descriptionFocusRequester),
                enabled = !isCreating,
                keyboardOptions = KeyboardOptions(
                    capitalization = KeyboardCapitalization.Sentences,
                    imeAction = ImeAction.Done
                ),
                keyboardActions = KeyboardActions(
                    onDone = {
                        keyboardController?.hide()
                        if (canPublish) {
                            onPublish()
                        }
                    }
                ),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = MaterialTheme.colorScheme.primary,
                    unfocusedBorderColor = MaterialTheme.colorScheme.outline
                )
            )

            Spacer(modifier = Modifier.height(4.dp))

            // Description character counter
            Text(
                text = stringResource(
                    Res.string.create_proposal_description_counter,
                    descriptionCharacterCount
                ),
                style = MaterialTheme.typography.bodySmall,
                color = if (descriptionCharacterCount > 1000) {
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

@Preview
@Composable
private fun CreateProposalContentPreview() {
    SpainDecidesTheme {
        CreateProposalContent(
            categoryName = "Economía y Empleo",
            proposalTitle = "Reducir impuestos a las PYMES",
            proposalDescription = "Propuesta para reducir la carga fiscal de las pequeñas y medianas empresas.",
            titleCharacterCount = 28,
            descriptionCharacterCount = 75,
            isCreating = false,
            snackbarHostState = remember { SnackbarHostState() },
            onTitleChange = {},
            onDescriptionChange = {},
            onClose = {},
            onPublish = {}
        )
    }
}

@Preview
@Composable
private fun CreateProposalContentEmptyPreview() {
    SpainDecidesTheme {
        CreateProposalContent(
            categoryName = "Sanidad Pública",
            proposalTitle = "",
            proposalDescription = "",
            titleCharacterCount = 0,
            descriptionCharacterCount = 0,
            isCreating = false,
            snackbarHostState = remember { SnackbarHostState() },
            onTitleChange = {},
            onDescriptionChange = {},
            onClose = {},
            onPublish = {}
        )
    }
}

@Preview
@Composable
private fun CreateProposalContentCreatingPreview() {
    SpainDecidesTheme {
        CreateProposalContent(
            categoryName = "Economía y Empleo",
            proposalTitle = "Reducir impuestos a las PYMES",
            proposalDescription = "Propuesta para reducir la carga fiscal de las pequeñas y medianas empresas.",
            titleCharacterCount = 28,
            descriptionCharacterCount = 75,
            isCreating = true,
            snackbarHostState = remember { SnackbarHostState() },
            onTitleChange = {},
            onDescriptionChange = {},
            onClose = {},
            onPublish = {}
        )
    }
}

@Preview
@Composable
private fun CreateProposalContentOverLimitPreview() {
    SpainDecidesTheme {
        CreateProposalContent(
            categoryName = "Medio Ambiente",
            proposalTitle = "Este es un título muy largo que supera el límite de caracteres permitido para una propuesta ciudadana",
            proposalDescription = "Descripción de ejemplo",
            titleCharacterCount = 105,
            descriptionCharacterCount = 22,
            isCreating = false,
            snackbarHostState = remember { SnackbarHostState() },
            onTitleChange = {},
            onDescriptionChange = {},
            onClose = {},
            onPublish = {}
        )
    }
}
